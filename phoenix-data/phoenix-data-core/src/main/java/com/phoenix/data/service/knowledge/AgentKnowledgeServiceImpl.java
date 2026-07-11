package com.phoenix.data.service.knowledge;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.phoenix.data.converter.AgentKnowledgeConverter;
import com.phoenix.data.dto.knowledge.agentknowledge.AgentKnowledgeQueryDTO;
import com.phoenix.data.dto.knowledge.agentknowledge.CreateKnowledgeDTO;
import com.phoenix.data.dto.knowledge.agentknowledge.UpdateKnowledgeDTO;
import com.phoenix.data.entity.AgentKnowledge;
import com.phoenix.data.enums.EmbeddingStatus;
import com.phoenix.data.enums.KnowledgeType;
import com.phoenix.data.event.AgentKnowledgeDeletionEvent;
import com.phoenix.data.event.AgentKnowledgeEmbeddingEvent;
import com.phoenix.data.mapper.AgentKnowledgeMapper;
import com.phoenix.data.service.file.FileStorageService;
import com.phoenix.data.vo.AgentKnowledgeVO;
import com.phoenix.data.vo.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 智能体知识服务实现类
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AgentKnowledgeServiceImpl extends ServiceImpl<AgentKnowledgeMapper, AgentKnowledge> implements AgentKnowledgeService {

    /**
     * 知识文件存储子路径
     */
    private static final String AGENT_KNOWLEDGE_FILE_PATH = "agent-knowledge";

    /**
     * 文件存储服务
     */
    private final FileStorageService fileStorageService;

    /**
     * 知识转换器
     */
    private final AgentKnowledgeConverter agentKnowledgeConverter;

    /**
     * 事件发布器
     */
    private final ApplicationEventPublisher eventPublisher;

    public AgentKnowledgeServiceImpl(FileStorageService fileStorageService, AgentKnowledgeConverter agentKnowledgeConverter, ApplicationEventPublisher eventPublisher) {
        this.fileStorageService = fileStorageService;
        this.agentKnowledgeConverter = agentKnowledgeConverter;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 根据ID获取知识
     *
     * @param id 知识ID
     * @return 知识视图对象
     */
    @Override
    @Transactional(readOnly = true)
    public AgentKnowledgeVO getKnowledgeById(Integer id) {
        AgentKnowledge agentKnowledge = getMapper().selectOneById(id);
        return agentKnowledge == null ? null : agentKnowledgeConverter.toVo(agentKnowledge);
    }

    /**
     * 创建知识，如果是文档类型则先存储文件
     *
     * @param createKnowledgeDto 创建知识DTO
     * @return 知识视图对象
     */
    @Override
    public AgentKnowledgeVO createKnowledge(CreateKnowledgeDTO createKnowledgeDto) {
        String storagePath = null;
        checkCreateKnowledgeDto(createKnowledgeDto);

        if (createKnowledgeDto.getType().equals(KnowledgeType.DOCUMENT.getCode())) {
            // 将文件保存到磁盘
            try {
                storagePath = fileStorageService.storeFile(createKnowledgeDto.getFile(), AGENT_KNOWLEDGE_FILE_PATH);
            } catch (Exception e) {
                log.error("Failed to store file, agentId:{} title:{} type:{} ", createKnowledgeDto.getAgentId(),
                        createKnowledgeDto.getTitle(), createKnowledgeDto.getType());
                throw new RuntimeException("Failed to store file.");
            }
        }

        AgentKnowledge knowledge = agentKnowledgeConverter.toEntityForCreate(createKnowledgeDto, storagePath);

        if (getMapper().insert(knowledge) <= 0) {
            log.error("Failed to create knowledge, agentId:{} title:{} type:{} ", knowledge.getAgentId(),
                    knowledge.getTitle(), knowledge.getType());
            throw new RuntimeException("Failed to create knowledge in database.");
        }

        eventPublisher.publishEvent(new AgentKnowledgeEmbeddingEvent(this, knowledge.getId(), knowledge.getSplitterType()));
        log.info("Knowledge created and event published. Id: {}, splitterType: {}", knowledge.getId(),
                knowledge.getSplitterType());

        return agentKnowledgeConverter.toVo(knowledge);
    }

    /**
     * 校验创建知识的请求参数
     *
     * @param createKnowledgeDto 创建知识DTO
     */
    private static void checkCreateKnowledgeDto(CreateKnowledgeDTO createKnowledgeDto) {
        if (createKnowledgeDto.getType().equals(KnowledgeType.DOCUMENT.getCode())
                && createKnowledgeDto.getFile() == null) {
            throw new RuntimeException("File is required for document type.");
        }
        if (createKnowledgeDto.getType().equals(KnowledgeType.QA.getCode())
                || createKnowledgeDto.getType().equals(KnowledgeType.FAQ.getCode())) {

            if (!StringUtils.hasText(createKnowledgeDto.getQuestion())) {
                throw new RuntimeException("Question is required for QA or FAQ type.");
            }
            if (!StringUtils.hasText(createKnowledgeDto.getContent())) {
                throw new RuntimeException("Content is required for QA or FAQ type.");
            }

        }
    }

    /**
     * 更新知识
     *
     * @param id                 知识ID
     * @param updateKnowledgeDto 更新知识DTO
     * @return 知识视图对象
     */
    @Override
    public AgentKnowledgeVO updateKnowledge(Integer id, UpdateKnowledgeDTO updateKnowledgeDto) {
        // 基础校验：根据 id 查询数据库
        AgentKnowledge existingKnowledge = getMapper().selectOneById(id);
        if (existingKnowledge == null) {
            log.warn("Knowledge not found with id: {}", id);
            throw new RuntimeException("Knowledge not found.");
        }

        if (StringUtils.hasText(updateKnowledgeDto.getTitle()))
            existingKnowledge.setTitle(updateKnowledgeDto.getTitle());

        // content
        if (StringUtils.hasText(updateKnowledgeDto.getContent()))
            existingKnowledge.setContent(updateKnowledgeDto.getContent());

        // 更新数据库
        int updateResult = getMapper().updateWithNow(existingKnowledge);
        if (updateResult <= 0) {
            log.error("Failed to update knowledge with id: {}", existingKnowledge.getId());
            throw new RuntimeException("Failed to update knowledge in database.");
        }
        return agentKnowledgeConverter.toVo(existingKnowledge);
    }

    /**
     * 软删除知识，并发布删除事件
     *
     * @param id 知识ID
     * @return 是否删除成功
     */
    @Override
    public boolean deleteKnowledge(Integer id) {
        // 先获取知识信息，用于后续删除文件和向量数据
        AgentKnowledge knowledge = getMapper().selectOneById(id);
        if (knowledge == null) {
            log.warn("Knowledge not found with id: {}, treating as already deleted", id);
            return true;
        }

        // 同步执行软删除
        knowledge.setIsDeleted(1);
        knowledge.setIsResourceCleaned(0);
        knowledge.setUpdatedTime(LocalDateTime.now());

        if (getMapper().updateWithNow(knowledge) > 0) {
            eventPublisher.publishEvent(new AgentKnowledgeDeletionEvent(this, id));
            return true;
        }
        return false;
    }

    /**
     * 分页条件查询知识列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @Override
    @Transactional(readOnly = true)
    public PageResult<AgentKnowledgeVO> queryByConditionsWithPage(AgentKnowledgeQueryDTO queryDTO) {

        int offset = (queryDTO.getPageNum() - 1) * queryDTO.getPageSize();

        Long total = getMapper().countByConditions(queryDTO);

        List<AgentKnowledge> dataList = getMapper().selectByConditionsWithPage(queryDTO, offset);
        List<AgentKnowledgeVO> dataListVO = dataList.stream().map(agentKnowledgeConverter::toVo).toList();
        PageResult<AgentKnowledgeVO> pageResult = new PageResult<>();
        pageResult.setData(dataListVO);
        pageResult.setTotal(total);
        pageResult.setPageNum(queryDTO.getPageNum());
        pageResult.setPageSize(queryDTO.getPageSize());
        pageResult.calculateTotalPages();

        return pageResult;
    }

    /**
     * 更新知识的召回状态
     *
     * @param id       知识ID
     * @param recalled 是否召回
     * @return 知识视图对象
     */
    @Override
    public AgentKnowledgeVO updateKnowledgeRecallStatus(Integer id, Boolean recalled) {
        // 查询知识
        AgentKnowledge knowledge = getMapper().selectOneById(id);
        if (knowledge == null) {
            throw new RuntimeException("Knowledge not found.");
        }

        // 更新召回状态
        knowledge.setIsRecall(recalled ? 1 : 0);

        // 更新数据库
        boolean res = getMapper().updateWithNow(knowledge) > 0;
        if (!res) {
            log.error("Failed to update knowledge with id: {}", knowledge.getId());
            throw new RuntimeException("Failed to update knowledge in database.");
        }
        return agentKnowledgeConverter.toVo(knowledge);
    }

    /**
     * 重试知识向量化
     *
     * @param id 知识ID
     */
    @Override
    public void retryEmbedding(Integer id) {
        AgentKnowledge knowledge = getMapper().selectOneById(id);
        if (knowledge.getEmbeddingStatus().equals(EmbeddingStatus.PROCESSING)) {
            throw new RuntimeException("BusinessKnowledge is processing, please wait.");
        }

        // 非召回的不处理
        if (knowledge.getIsRecall() == null || knowledge.getIsRecall() == 0) {
            throw new RuntimeException("BusinessKnowledge is not recalled, please recall it first.");
        }

        // 重置状态
        // 立刻给用户反馈"已变成处理中"
        knowledge.setEmbeddingStatus(EmbeddingStatus.PENDING);
        knowledge.setErrorMsg("");
        getMapper().updateWithNow(knowledge);
        eventPublisher
                .publishEvent(new AgentKnowledgeEmbeddingEvent(this, knowledge.getId(), knowledge.getSplitterType()));
        log.info("Retry embedding for knowledgeId: {}, splitterType: {}", id, knowledge.getSplitterType());
    }

}

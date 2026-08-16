package com.phoenix.platform.service.front.impl;

import com.mybatisflex.core.paginate.Page;
import com.phoenix.platform.dto.request.ChatSessionRequestQuery;
import com.phoenix.platform.mapper.front.ChatSessionRequestMapper;
import com.phoenix.platform.service.front.ChatSessionRequestService;
import com.phoenix.platform.vo.ChatSessionRequestVO;
import com.phoenix.platform.vo.ChatSessionAgentStatVO;
import com.phoenix.platform.vo.ChatSessionUserStatVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户对智能体的请求管理服务实现
 *
 * <p>只读查询，直接使用自定义 XML Mapper 完成跨表关联和分页。</p>
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatSessionRequestServiceImpl implements ChatSessionRequestService {

	private final ChatSessionRequestMapper chatSessionRequestMapper;

	@Override
	public Page<ChatSessionRequestVO> page(ChatSessionRequestQuery query) {
		if (query.getPageNum() == null || query.getPageNum() < 1) {
			query.setPageNum(1);
		}
		if (query.getPageSize() == null || query.getPageSize() < 1) {
			query.setPageSize(10);
		}
		int limit = query.getPageSize();
		long offset = (long) (query.getPageNum() - 1) * query.getPageSize();

		List<ChatSessionRequestVO> records = chatSessionRequestMapper.selectPageByQuery(query, limit, offset);
		long total = chatSessionRequestMapper.countByQuery(query);

		Page<ChatSessionRequestVO> page = new Page<>(query.getPageNum(), query.getPageSize(), total);
		page.setRecords(records);
		return page;
	}

	@Override
	public long count(ChatSessionRequestQuery query) {
		return chatSessionRequestMapper.countByQuery(query);
	}

	@Override
	public List<ChatSessionAgentStatVO> agentStats(ChatSessionRequestQuery query, int limit) {
		return chatSessionRequestMapper.selectAgentStatsByQuery(query, limit);
	}

	@Override
	public List<ChatSessionUserStatVO> userStats(ChatSessionRequestQuery query, int limit) {
		return chatSessionRequestMapper.selectUserStatsByQuery(query, limit);
	}
}
package com.phoenix.data.service.category;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.phoenix.data.entity.AgentCategory;
import com.phoenix.data.mapper.AgentCategoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AgentCategoryServiceImpl extends ServiceImpl<AgentCategoryMapper, AgentCategory> implements AgentCategoryService {

    @Override
    @Transactional(readOnly = true)
    public List<AgentCategory> findAll() {
        return QueryChain.of(getMapper())
                .eq(AgentCategory::getDelFlag, 1)
                .orderBy(AgentCategory::getSn, true)
                .list();
    }

    @Override
    @Transactional(readOnly = true)
    public AgentCategory findById(String id) {
        return QueryChain.of(getMapper())
                .eq(AgentCategory::getId, id)
                .eq(AgentCategory::getDelFlag, 1)
                .one();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgentCategory> findByPid(String pid) {
        return QueryChain.of(getMapper())
                .eq(AgentCategory::getPid, pid)
                .eq(AgentCategory::getDelFlag, 1)
                .orderBy(AgentCategory::getSn, true)
                .list();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgentCategory> search(String name) {
        return QueryChain.of(getMapper())
                .eq(AgentCategory::getDelFlag, 1)
                .like(AgentCategory::getName, name, StrUtil.isNotBlank(name))
                .orderBy(AgentCategory::getSn, true)
                .list();
    }
}

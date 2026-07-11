package com.phoenix.rag.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.phoenix.rag.mapper.RagCategoryMapper;
import com.phoenix.rag.model.RagCategory;
import com.phoenix.rag.service.RagCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class RagCategoryServiceImpl extends ServiceImpl<RagCategoryMapper, RagCategory> implements RagCategoryService {
}

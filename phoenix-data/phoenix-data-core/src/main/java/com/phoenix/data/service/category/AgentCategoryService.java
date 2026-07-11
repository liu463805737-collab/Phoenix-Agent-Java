package com.phoenix.data.service.category;

import com.mybatisflex.core.service.IService;
import com.phoenix.data.entity.AgentCategory;

import java.util.List;

public interface AgentCategoryService extends IService<AgentCategory> {

    List<AgentCategory> findAll();

    AgentCategory findById(String id);

    List<AgentCategory> findByPid(String pid);

    List<AgentCategory> search(String name);
}

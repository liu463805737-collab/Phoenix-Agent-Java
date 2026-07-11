package com.phoenix.data.controller;

import com.phoenix.data.entity.AgentCategory;
import com.phoenix.data.service.category.AgentCategoryService;
import com.phoenix.tools.vo.ReturnVo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/agent-category")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class AgentCategoryController {

    private final AgentCategoryService agentCategoryService;

    @GetMapping("/list")
    public ReturnVo<List<AgentCategory>> list(@RequestParam(value = "name", required = false) String name) {
        List<AgentCategory> result;
        if (StringUtils.isNotBlank(name)) {
            result = agentCategoryService.search(name);
        } else {
            result = agentCategoryService.findAll();
        }
        return ReturnVo.ok(result);
    }

    @GetMapping("/{id}")
    public AgentCategory get(@PathVariable String id) {
        return checkExists(id);
    }

    @GetMapping("/pid/{pid}")
    public ReturnVo<List<AgentCategory>> getByPid(@PathVariable String pid) {
        List<AgentCategory> result = agentCategoryService.findByPid(pid);
        return ReturnVo.ok(result);
    }

    @PostMapping
    public AgentCategory create(@RequestBody AgentCategory agentCategory) {
        agentCategoryService.save(agentCategory);
        return agentCategory;
    }

    @PutMapping("/{id}")
    public AgentCategory update(@PathVariable String id, @RequestBody AgentCategory agentCategory) {
        checkExists(id);
        agentCategory.setId(id);
        agentCategoryService.updateById(agentCategory);
        return agentCategory;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        checkExists(id);
        agentCategoryService.removeById(id);
    }

    private AgentCategory checkExists(String id) {
        AgentCategory category = agentCategoryService.findById(id);
        if (category == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "agent category with id: %s not found".formatted(id));
        }
        return category;
    }
}

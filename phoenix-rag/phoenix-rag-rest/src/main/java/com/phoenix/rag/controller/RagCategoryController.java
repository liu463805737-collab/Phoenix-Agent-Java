package com.phoenix.rag.controller;

import com.phoenix.rag.model.RagCategory;
import com.phoenix.rag.service.RagCategoryService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/api/rag/category")
public class RagCategoryController {

    private final RagCategoryService ragCategoryService;

    @PostMapping
    public RagCategory create(@RequestBody RagCategory category) {
        ragCategoryService.save(category);
        return category;
    }

    @PutMapping
    public RagCategory update(@RequestBody RagCategory category) {
        ragCategoryService.updateById(category);
        return category;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        ragCategoryService.removeById(id);
    }

    @GetMapping("/{id}")
    public RagCategory get(@PathVariable String id) {
        return ragCategoryService.getById(id);
    }

    @GetMapping
    public List<RagCategory> list() {
        return ragCategoryService.list();
    }
}

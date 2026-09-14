package com.phoenix.rag.controller;

import com.mybatisflex.core.paginate.Page;
import com.phoenix.rag.model.RagCategory;
import com.phoenix.rag.service.RagCategoryService;
import com.phoenix.tools.vo.ReturnVo;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/api/rag/category")
public class RagCategoryController {

    private final RagCategoryService ragCategoryService;

    @PostMapping
    public ReturnVo<Boolean> create(@RequestBody RagCategory category) {
        return ReturnVo.ok(ragCategoryService.save(category));
    }

    @PutMapping
    public ReturnVo<Boolean> update(@RequestBody RagCategory category) {
        return ReturnVo.ok(ragCategoryService.updateById(category));
    }

    @DeleteMapping("/{id}")
    public ReturnVo<Boolean> delete(@PathVariable String id) {
        return ReturnVo.ok(ragCategoryService.removeById(id));
    }

    @GetMapping("/{id}")
    public ReturnVo<RagCategory> get(@PathVariable String id) {
        return ReturnVo.ok(ragCategoryService.getById(id));
    }

    @GetMapping("/page")
    public ReturnVo<Page<RagCategory>> page(@RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "10") long size,
                                             RagCategory query) {
//        return ReturnVo.ok(ragCategoryService.page(new Page<>(page, size), query));
        return ReturnVo.ok();
    }
}
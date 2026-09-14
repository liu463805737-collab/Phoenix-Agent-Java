package com.phoenix.rag.controller;

import com.mybatisflex.core.paginate.Page;
import com.phoenix.rag.model.RagFileInfo;
import com.phoenix.rag.service.RagFileInfoService;
import com.phoenix.tools.vo.ReturnVo;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/api/rag/item")
public class RagFileInfoController {

    private final RagFileInfoService ragFileInfoService;

    @PostMapping
    public ReturnVo<Boolean> create(@RequestBody RagFileInfo ragFileInfo) {
        return ReturnVo.ok(ragFileInfoService.save(ragFileInfo));
    }

    @PutMapping
    public ReturnVo<Boolean> update(@RequestBody RagFileInfo ragFileInfo) {
        return ReturnVo.ok(ragFileInfoService.updateById(ragFileInfo));
    }

    @DeleteMapping("/{id}")
    public ReturnVo<Boolean> delete(@PathVariable String id) {
        return ReturnVo.ok(ragFileInfoService.removeById(id));
    }

    @GetMapping("/{id}")
    public ReturnVo<RagFileInfo> get(@PathVariable String id) {
        return ReturnVo.ok(ragFileInfoService.getById(id));
    }

    @GetMapping("/page")
    public ReturnVo<Page<RagFileInfo>> page(@RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "10") long size,
                                             RagFileInfo query) {
//        return ReturnVo.ok(ragFileInfoService.page(new Page<>(page, size), query));
        return ReturnVo.ok();
    }
}
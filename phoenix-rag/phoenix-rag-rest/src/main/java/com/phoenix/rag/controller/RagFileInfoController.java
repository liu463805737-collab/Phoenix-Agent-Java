package com.phoenix.rag.controller;

import com.phoenix.rag.model.RagFileInfo;
import com.phoenix.rag.service.RagFileInfoService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/api/rag/file")
public class RagFileInfoController {

    private final RagFileInfoService ragFileInfoService;

    @PostMapping
    public RagFileInfo create(@RequestBody RagFileInfo ragFileInfo) {
        ragFileInfoService.save(ragFileInfo);
        return ragFileInfo;
    }

    @PutMapping
    public RagFileInfo update(@RequestBody RagFileInfo ragFileInfo) {
        ragFileInfoService.updateById(ragFileInfo);
        return ragFileInfo;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        ragFileInfoService.removeById(id);
    }

    @GetMapping("/{id}")
    public RagFileInfo get(@PathVariable String id) {
        return ragFileInfoService.getById(id);
    }

    @GetMapping
    public List<RagFileInfo> list() {
        return ragFileInfoService.list();
    }
}

package com.resumepilot.ai.rag;

import com.resumepilot.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 知识库管理接口
 *
 * POST /knowledge/import  → 动态导入新知识文档
 */
@RestController
@RequestMapping("/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    /**
     * 动态导入知识文档
     * POST /knowledge/import
     * Body: form-data, key=file
     */
    @PostMapping("/import")
    public Result<String> importDocument(@RequestParam("file") MultipartFile file) {
        int segments = knowledgeService.importDocument(file);
        return Result.success("导入成功，共切片 " + segments + " 个片段");
    }
}

package com.resumepilot.ai.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 应用启动时自动导入知识文档
 *
 * 策略：先检查 embedding_store 是否已有数据
 *   - 有数据 → 跳过，避免重复导入和浪费 Embedding API 调用
 *   - 无数据 → 首次导入
 *
 * 后续想更新知识库，调用 POST /knowledge/import 或手动清表后重启
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class RagInitializer implements CommandLineRunner {

    private final KnowledgeService knowledgeService;

    @Override
    public void run(String... args) {
        if (knowledgeService.hasData()) {
            log.info("知识库已有数据，跳过导入（共 {} 条）",
                    knowledgeService.count());
            return;
        }

        log.info("========== 首次启动，开始导入知识文档 ==========");
        knowledgeService.importDefaultDocuments();
        log.info("========== 知识文档导入完成 ==========");
    }
}

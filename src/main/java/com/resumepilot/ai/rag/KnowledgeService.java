package com.resumepilot.ai.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 知识库管理服务
 *
 * 两个入口：
 *   1. 启动时自动导入 resources/rag/documents/ 下的所有文档
 *   2. 运行时通过接口动态导入新文档
 *
 * 导入流程：
 *   文档 → Tika 提取文本 → 按 200 字切片（重叠 50 字） → Embedding 向量化 → 存入 pgvector
 *
 * 为什么要重叠 50 字？
 *   防止切片时把一个完整的知识点切断，重叠部分保证语义连贯
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;
    private final JdbcTemplate jdbcTemplate;
    private final Tika tika = new Tika();

    /**
     * 检查知识库是否已有数据
     * 有数据就跳过导入，避免重复
     */
    public boolean hasData() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM embedding_store", Integer.class);
        return count != null && count > 0;
    }

    /**
     * 获取知识库条目数量
     */
    public int count() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM embedding_store", Integer.class);
        return count == null ? 0 : count;
    }

    /**
     * 启动时自动导入 classpath 下的知识文档
     * 在 RagConfig 初始化后调用
     */
    public void importDefaultDocuments() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:rag/documents/*");

            for (Resource resource : resources) {
                String fileName = resource.getFilename();
                if (fileName == null) continue;

                log.info("导入知识文档：{}", fileName);
                String text = tika.parseToString(resource.getInputStream());
                importText(text, fileName);
            }

            log.info("知识文档导入完成，共 {} 个文件", resources.length);
        } catch (IOException | TikaException e) {
            log.error("知识文档导入失败", e);
        }
    }

    /**
     * 运行时动态导入文档（通过接口上传）
     *
     * @param file 上传的文件
     * @return 切片数量
     */
    public int importDocument(MultipartFile file) {
        try {
            String text = tika.parseToString(file.getInputStream());
            importText(text, file.getOriginalFilename());
            List<TextSegment> segments = DocumentSplitters.recursive(200, 50)
                    .split(Document.from(text));
            return segments.size();
        } catch (IOException | TikaException e) {
            log.error("文档导入失败：{}", file.getOriginalFilename(), e);
            throw new RuntimeException("文档导入失败：" + e.getMessage());
        }
    }

    /**
     * 核心：文本 → 切片 → 向量化 → 存储
     */
    private void importText(String text, String source) {
        // 切片：每片 200 字，重叠 50 字
        Document document = Document.from(text);
        List<TextSegment> segments = DocumentSplitters.recursive(200, 50).split(document);

        // 给每个片段打上来源标签（后续检索时知道出自哪个文档）
        segments.forEach(seg -> seg.metadata().put("source", source));

        // 向量化并存储
        embeddingStore.addAll(embeddingModel.embedAll(segments).content(), segments);

        log.info("文档 [{}] 切片完成，共 {} 个片段", source, segments.size());
    }
}

package com.resumepilot.ai.rag;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallzhv15.BgeSmallZhV15EmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import dev.langchain4j.data.segment.TextSegment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RAG 配置
 *
 * 三个核心 Bean：
 *   1. EmbeddingModel    → 把文本转成向量（512 维）
 *   2. EmbeddingStore    → pgvector 存储 + 检索向量
 *   3. RetrievalAugmentor → 给 Agent 自动注入检索结果
 *
 * 工作流程：
 *   导入知识时：文本 → EmbeddingModel 向量化 → 存入 pgvector
 *   Agent 回答时：用户问题 → EmbeddingModel 向量化 → pgvector 相似度检索 → 拼入 Prompt
 */
@Configuration
public class RagConfig {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    /**
     * Embedding 模型：bge-small-zh-v1.5（中文小模型，512 维）
     * 首次启动会自动下载模型文件（约 90MB），后续走缓存
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        return new BgeSmallZhV15EmbeddingModel();
    }

    /**
     * pgvector 向量存储
     * 从 application.yml 读取数据库连接信息
     */
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(EmbeddingModel embeddingModel) {
        // 从 JDBC URL 解析 host 和 port
        String host = "localhost";
        int port = 5432;
        String database = "resumepilot";

        // 简单解析 jdbc:postgresql://host:port/database
        String url = datasourceUrl.replace("jdbc:postgresql://", "");
        String[] parts = url.split("/");
        if (parts.length >= 2) {
            String[] hostPort = parts[0].split(":");
            host = hostPort[0];
            port = Integer.parseInt(hostPort[1]);
            database = parts[1].split("\\?")[0]; // 去掉 ?param 部分
        }

        return PgVectorEmbeddingStore.builder()
                .host(host)
                .port(port)
                .database(database)
                .user(username)
                .password(password)
                .table("embedding_store")
                .dimension(512)
                .build();
    }

    /**
     * 检索增强器
     * Agent 调用 LLM 前，自动用用户问题去 pgvector 检索 Top3 相关片段
     * 检索到的内容会自动拼入 Prompt，让 LLM 参考知识库回答
     */
    @Bean
    public RetrievalAugmentor retrievalAugmentor(EmbeddingStore<TextSegment> embeddingStore,
                                                  EmbeddingModel embeddingModel) {
        EmbeddingStoreContentRetriever retriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(3)       // 每次检索最多返回 3 个相关片段
                .minScore(0.5)       // 相似度低于 0.5 的不要（避免不相关内容干扰）
                .build();

        return DefaultRetrievalAugmentor.builder()
                .contentRetriever(retriever)
                .build();
    }
}

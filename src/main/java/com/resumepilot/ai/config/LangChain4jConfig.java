package com.resumepilot.ai.config;

import com.resumepilot.ai.agent.AtsAgent;
import com.resumepilot.ai.agent.ChatAgent;
import com.resumepilot.ai.agent.JdMatcherAgent;
import com.resumepilot.ai.agent.ResumeOptimizerAgent;
import com.resumepilot.ai.agent.ResumeParserAgent;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LangChain4j 配置
 *
 * Agent 分两类：
 *   无状态（不需要记忆）：ResumeParserAgent、AtsAgent、JdMatcherAgent
 *   有状态（需要记忆 + 流式）：ChatAgent
 */
@Configuration
public class LangChain4jConfig {

    /**
     * 简历解析 Agent —— 无状态，不需要 RAG
     */
    @Bean
    ResumeParserAgent resumeParserAgent(ChatLanguageModel chatLanguageModel) {
        return AiServices.builder(ResumeParserAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .build();
    }

    /**
     * ATS 评分 Agent —— 无状态，接入 RAG
     */
    @Bean
    AtsAgent atsAgent(ChatLanguageModel chatLanguageModel,
                      RetrievalAugmentor retrievalAugmentor) {
        return AiServices.builder(AtsAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .retrievalAugmentor(retrievalAugmentor)
                .build();
    }

    /**
     * JD 匹配 Agent —— 无状态，接入 RAG
     */
    @Bean
    JdMatcherAgent jdMatcherAgent(ChatLanguageModel chatLanguageModel,
                                   RetrievalAugmentor retrievalAugmentor) {
        return AiServices.builder(JdMatcherAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .retrievalAugmentor(retrievalAugmentor)
                .build();
    }

    /**
     * 通用聊天 Agent —— 有状态，支持 ChatMemory + Streaming + RAG
     *
     * 关键配置：
     *   streamingChatLanguageModel → 流式输出（实时打字效果）
     *   chatMemoryProvider         → 多轮对话记忆（Redis 存储）
     *   retrievalAugmentor         → RAG 检索增强
     */
    @Bean
    ChatAgent chatAgent(StreamingChatLanguageModel streamingChatLanguageModel,
                        ChatMemoryProvider chatMemoryProvider,
                        RetrievalAugmentor retrievalAugmentor) {
        return AiServices.builder(ChatAgent.class)
                .streamingChatLanguageModel(streamingChatLanguageModel)
                .chatMemoryProvider(chatMemoryProvider)
                .retrievalAugmentor(retrievalAugmentor)
                .build();
    }

    /**
     * 简历优化 Agent —— 有状态，支持 ChatMemory + Streaming + RAG
     * memoryId 格式：resume-optimize-{resumeId}
     * 每份简历的优化对话独立隔离
     */
    @Bean
    ResumeOptimizerAgent resumeOptimizerAgent(StreamingChatLanguageModel streamingChatLanguageModel,
                                               ChatMemoryProvider chatMemoryProvider,
                                               RetrievalAugmentor retrievalAugmentor) {
        return AiServices.builder(ResumeOptimizerAgent.class)
                .streamingChatLanguageModel(streamingChatLanguageModel)
                .chatMemoryProvider(chatMemoryProvider)
                .retrievalAugmentor(retrievalAugmentor)
                .build();
    }
}

package com.resumepilot.ai.config;

import com.resumepilot.ai.agent.AtsAgent;
import com.resumepilot.ai.agent.CareerAgent;
import com.resumepilot.ai.agent.ChatAgent;
import com.resumepilot.ai.agent.InterviewAgent;
import com.resumepilot.ai.agent.JdMatcherAgent;
import com.resumepilot.ai.agent.ResumeOptimizerAgent;
import com.resumepilot.ai.agent.ResumeParserAgent;
import com.resumepilot.ai.tool.HistoryTool;
import com.resumepilot.ai.tool.ResumeTool;
import com.resumepilot.ai.tool.SearchTool;
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
     * 通用聊天 Agent —— 有状态，支持 ChatMemory + Streaming + RAG + Tool Calling
     *
     * 关键配置：
     *   streamingChatLanguageModel → 流式输出（实时打字效果）
     *   chatMemoryProvider         → 多轮对话记忆（Redis 存储）
     *   retrievalAugmentor         → RAG 检索增强
     *   tools                      → 工具调用（简历查询、历史记录、知识搜索）
     */
    @Bean
    ChatAgent chatAgent(StreamingChatLanguageModel streamingChatLanguageModel,
                        ChatMemoryProvider chatMemoryProvider,
                        RetrievalAugmentor retrievalAugmentor,
                        ResumeTool resumeTool,
                        HistoryTool historyTool,
                        SearchTool searchTool) {
        return AiServices.builder(ChatAgent.class)
                .streamingChatLanguageModel(streamingChatLanguageModel)
                .chatMemoryProvider(chatMemoryProvider)
                .retrievalAugmentor(retrievalAugmentor)
                .tools(resumeTool, historyTool, searchTool)
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

    /**
     * 模拟面试 Agent —— 有状态，支持 ChatMemory + Streaming
     * memoryId 格式：interview-{userId}-{sessionId}
     * 每次面试的对话独立隔离
     */
    @Bean
    InterviewAgent interviewAgent(StreamingChatLanguageModel streamingChatLanguageModel,
                                   ChatMemoryProvider chatMemoryProvider) {
        return AiServices.builder(InterviewAgent.class)
                .streamingChatLanguageModel(streamingChatLanguageModel)
                .chatMemoryProvider(chatMemoryProvider)
                .build();
    }

    /**
     * 求职话术 Agent —— 无状态
     * 每次生成独立，不需要记忆
     */
    @Bean
    CareerAgent careerAgent(ChatLanguageModel chatLanguageModel) {
        return AiServices.builder(CareerAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .build();
    }
}

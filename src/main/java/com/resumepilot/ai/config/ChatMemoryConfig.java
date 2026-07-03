package com.resumepilot.ai.config;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ChatMemory 配置
 *
 * ChatMemoryProvider 的作用：
 *   根据 memoryId 创建/获取对应的 ChatMemory 实例
 *   每个 memoryId 对应一段独立的对话历史
 *
 * MessageWindowChatMemory：
 *   滑动窗口记忆，只保留最近 N 条消息
 *   为什么不用无限记忆？Token 数量有限，消息太多会超限
 */
@Configuration
public class ChatMemoryConfig {

    /**
     * 通用记忆策略：保留最近 20 条消息
     * 用于简历优化、模拟面试等需要多轮对话的 Agent
     */
    @Bean
    public ChatMemoryProvider chatMemoryProvider(ChatMemoryStore chatMemoryStore) {
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(20)
                .chatMemoryStore(chatMemoryStore)
                .build();
    }
}

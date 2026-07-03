package com.resumepilot.ai.config;

import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Streaming 模型配置
 *
 * StreamingChatLanguageModel vs ChatLanguageModel：
 *   - ChatLanguageModel：一次性返回完整回答（用户要等全部生成完）
 *   - StreamingChatLanguageModel：一个 Token 一个 Token 返回（实时打字效果）
 *
 * 底层都是调 DeepSeek API，区别是：
 *   - 普通模式：stream=false，等完整响应
 *   - 流式模式：stream=true，服务端推一个 SSE 事件就转发一个
 */
@Configuration
public class StreamingModelConfig {

    @Value("${langchain4j.open-ai.chat-model.base-url}")
    private String baseUrl;

    @Value("${langchain4j.open-ai.chat-model.api-key}")
    private String apiKey;

    @Value("${langchain4j.open-ai.chat-model.model-name}")
    private String modelName;

    @Value("${langchain4j.open-ai.chat-model.temperature:0.7}")
    private Double temperature;

    @Value("${langchain4j.open-ai.chat-model.timeout:PT120S}")
    private Duration timeout;

    /**
     * 流式聊天模型
     * 和普通 ChatLanguageModel 参数完全一样，只是底层请求加了 stream=true
     */
    @Bean
    public StreamingChatLanguageModel streamingChatLanguageModel() {
        return OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .timeout(timeout)
                .build();
    }
}

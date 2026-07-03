package com.resumepilot.ai.memory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis 版聊天记忆存储
 *
 * 实现 LangChain4j 的 ChatMemoryStore 接口
 * 把聊天记录序列化成 JSON 存进 Redis
 *
 * 为什么用 Redis 不用数据库？
 *   - 聊天记录是高频读写，Redis 比 MySQL 快 100 倍
 *   - 可以设置过期时间（比如 24 小时自动清理）
 *   - 天然支持 key-value 结构，一个 memoryId 对应一段对话
 *
 * Redis Key 格式：chat:memory:{memoryId}
 * 例如：chat:memory:user-1-resume-optimize
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisChatMemoryStore implements ChatMemoryStore {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String KEY_PREFIX = "chat:memory:";
    private static final long EXPIRE_HOURS = 24;

    /**
     * 保存聊天记录
     * 每次用户发消息或 Agent 回复时，LangChain4j 自动调用此方法
     */
    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String key = KEY_PREFIX + memoryId.toString();
        String json = ChatMessageSerializer.messagesToJson(messages);
        redisTemplate.opsForValue().set(key, json, EXPIRE_HOURS, TimeUnit.HOURS);
        log.debug("聊天记录已保存，memoryId={}, 消息数={}", memoryId, messages.size());
    }

    /**
     * 读取聊天记录
     * 每次调用 Agent 时，LangChain4j 自动调用此方法加载历史上下文
     */
    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String key = KEY_PREFIX + memoryId.toString();
        String json = redisTemplate.opsForValue().get(key);
        if (json == null || json.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return ChatMessageDeserializer.messagesFromJson(json);
        } catch (Exception e) {
            log.error("聊天记录反序列化失败，memoryId={}", memoryId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 删除聊天记录
     * 用户主动清空对话时调用
     */
    @Override
    public void deleteMessages(Object memoryId) {
        String key = KEY_PREFIX + memoryId.toString();
        redisTemplate.delete(key);
        log.info("聊天记录已删除，memoryId={}", memoryId);
    }
}

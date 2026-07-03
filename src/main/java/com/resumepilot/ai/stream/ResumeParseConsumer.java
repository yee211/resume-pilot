package com.resumepilot.ai.stream;

import com.resumepilot.resume.service.ResumeService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 简历解析消费者
 *
 * 基于 Redis Stream 实现的消息消费者
 *
 * Redis Stream 是 Redis 5.0+ 的消息队列功能，支持：
 *   - 消息持久化（不像 Pub/Sub 断线就丢）
 *   - 消费者组（多实例负载均衡）
 *   - ACK 确认（消息处理完才标记为已消费）
 *   - 挂起消息重处理（消费者挂了，消息不会丢）
 *
 * 工作流程：
 *   启动时 → 创建 Stream + 消费者组
 *   运行时 → 循环 XREADGROUP 读取新消息 → 处理 → XACK 确认
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResumeParseConsumer {

    private final StringRedisTemplate redisTemplate;
    private final ResumeService resumeService;

    private static final String STREAM_KEY = "resume:parse:stream";
    private static final String GROUP_NAME = "resume-parse-group";
    private static final String CONSUMER_NAME = "resume-parse-worker-1";

    private volatile boolean running = true;

    /**
     * 应用启动后，自动开始消费
     */
    @PostConstruct
    public void start() {
        // 确保 Stream 和消费者组存在
        try {
            redisTemplate.opsForStream().createGroup(STREAM_KEY, GROUP_NAME);
            log.info("Redis Stream 消费者组创建成功：{}", GROUP_NAME);
        } catch (Exception e) {
            // 组已存在时会报异常，忽略
            log.info("消费者组已存在：{}", GROUP_NAME);
        }

        // 启动消费线程
        Thread consumerThread = new Thread(this::consume, "resume-parse-consumer");
        consumerThread.setDaemon(true);
        consumerThread.start();
        log.info("简历解析消费者已启动");
    }

    /**
     * 应用关闭时停止消费
     */
    @PreDestroy
    public void stop() {
        running = false;
        log.info("简历解析消费者已停止");
    }

    /**
     * 核心消费循环
     */
    private void consume() {
        while (running) {
            try {
                // XREADGROUP：从消费者组读取新消息
                // COUNT 1：每次读 1 条
                // BLOCK 5000ms：没有消息时阻塞 5 秒（避免空转浪费 CPU）
                // NOACK：不自动确认，等处理完手动 ACK
                List<MapRecord<String, Object, Object>> messages = redisTemplate.opsForStream().read(
                        Consumer.from(GROUP_NAME, CONSUMER_NAME),
                        StreamReadOptions.empty().count(1).block(Duration.ofMillis(5000)),
                        StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed())
                );

                if (messages == null || messages.isEmpty()) {
                    continue;
                }

                for (MapRecord<String, Object, Object> message : messages) {
                    processMessage(message);
                }
            } catch (Exception e) {
                if (running) {
                    log.error("消费消息异常", e);
                    try {
                        Thread.sleep(2000); // 异常后等 2 秒再重试
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }
    }

    /**
     * 处理单条消息
     */
    private void processMessage(MapRecord<String, Object, Object> message) {
        Map<Object, Object> body = message.getValue();
        String resumeIdStr = (String) body.get("resumeId");

        if (resumeIdStr == null) {
            log.warn("消息缺少 resumeId，跳过：{}", message.getId());
            ackMessage(message.getId());
            return;
        }

        Long resumeId = Long.parseLong(resumeIdStr);
        log.info("收到简历解析消息，resumeId={}, messageId={}", resumeId, message.getId());

        try {
            // 调用 AI 解析（同步调用，但在线程池里不阻塞主线程）
            resumeService.parseSync(resumeId);
            log.info("简历解析完成，resumeId={}", resumeId);
        } catch (Exception e) {
            log.error("简历解析失败，resumeId={}", resumeId, e);
        } finally {
            // 无论成功失败，都 ACK（避免消息反复重试）
            ackMessage(message.getId());
        }
    }

    /**
     * 确认消息已处理
     * ACK 后消息不会被其他消费者读取
     */
    private void ackMessage(RecordId messageId) {
        try {
            redisTemplate.opsForStream().acknowledge(STREAM_KEY, GROUP_NAME, messageId);
        } catch (Exception e) {
            log.error("ACK 失败，messageId={}", messageId, e);
        }
    }
}

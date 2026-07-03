package com.resumepilot.ai.stream;

import com.resumepilot.jd.service.JdService;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class JdMatchConsumer {

    private final StringRedisTemplate redisTemplate;
    private final JdService jdService;

    private static final String STREAM_KEY = "jd:match:stream";
    private static final String GROUP_NAME = "jd-match-group";
    private static final String CONSUMER_NAME = "jd-match-worker-1";
    private volatile boolean running = true;

    @PostConstruct
    public void start() {
        try {
            redisTemplate.opsForStream().createGroup(STREAM_KEY, GROUP_NAME);
        } catch (Exception e) {}
        Thread t = new Thread(this::consume, "jd-match-consumer");
        t.setDaemon(true);
        t.start();
        log.info("JD 匹配消费者已启动");
    }

    @PreDestroy
    public void stop() { running = false; }

    private void consume() {
        while (running) {
            try {
                List<MapRecord<String, Object, Object>> messages = redisTemplate.opsForStream().read(
                        Consumer.from(GROUP_NAME, CONSUMER_NAME),
                        StreamReadOptions.empty().count(1).block(Duration.ofMillis(5000)),
                        StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed())
                );
                if (messages == null || messages.isEmpty()) continue;
                for (MapRecord<String, Object, Object> msg : messages) {
                    process(msg);
                }
            } catch (Exception e) {
                if (running) {
                    log.error("JD 消费异常", e);
                    try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                }
            }
        }
    }

    private void process(MapRecord<String, Object, Object> msg) {
        String recordIdStr = (String) msg.getValue().get("recordId");
        try {
            Long recordId = Long.parseLong(recordIdStr);
            log.info("收到 JD 匹配消息，recordId={}", recordId);
            jdService.processSync(recordId);
        } catch (Exception e) {
            log.error("JD 匹配失败，recordId={}", recordIdStr, e);
        } finally {
            redisTemplate.opsForStream().acknowledge(STREAM_KEY, GROUP_NAME, msg.getId());
        }
    }
}

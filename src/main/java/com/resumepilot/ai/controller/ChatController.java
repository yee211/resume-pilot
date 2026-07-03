package com.resumepilot.ai.controller;

import com.resumepilot.ai.agent.ChatAgent;
import com.resumepilot.common.result.Result;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 聊天 Controller
 *
 * SSE（Server-Sent Events）原理：
 *   1. 前端发起请求，连接不关闭
 *   2. 服务端每收到一个 Token，立即推给前端
 *   3. 前端收到一个就显示一个（打字机效果）
 *   4. 全部推完后，服务端关闭连接
 */
@Slf4j
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatAgent chatAgent;

    /**
     * 普通聊天（一次性返回完整回答）
     * POST /chat?memoryId=user-1&message=帮我优化简历
     */
    @PostMapping
    public Result<String> chat(@RequestParam String memoryId,
                                @RequestParam String message) {
        String answer = chatAgent.chat(memoryId, message);
        return Result.success(answer);
    }

    /**
     * 流式聊天（SSE 实时推送）
     * POST /chat/stream?memoryId=user-1&message=帮我优化简历
     */
    @PostMapping(value = "/stream", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter chatStream(@RequestParam String memoryId,
                                  @RequestParam String message,
                                  HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        SseEmitter emitter = new SseEmitter(0L);

        chatAgent.chatStream(memoryId, message)
                .onNext(token -> {
                    try {
                        emitter.send(token);
                    } catch (Exception e) {
                        log.warn("SSE 推送失败", e);
                    }
                })
                .onComplete(resp -> emitter.complete())
                .onError(emitter::completeWithError)
                .start();

        return emitter;
    }
}

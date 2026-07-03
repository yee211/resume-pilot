package com.resumepilot.resume.controller;

import com.resumepilot.resume.service.ResumeOptimizeService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 简历优化 Controller
 *
 * 两个接口：
 *   POST /resume/optimize    → 首次优化（传简历 ID + 优化方向）
 *   POST /resume/refine      → 多轮修改（传简历 ID + 修改指令）
 *
 * 两者都返回 SSE 流，前端实时显示优化过程
 *
 * 使用示例：
 *   1. POST /resume/optimize  resumeId=1  direction=项目经历       → AI 输出优化后的项目描述
 *   2. POST /resume/refine    resumeId=1  message=再短一点         → AI 基于上一版缩短
 *   3. POST /resume/refine    resumeId=1  message=偏大厂风格       → AI 继续调整风格
 */
@Slf4j
@RestController
@RequestMapping("/resume")
@RequiredArgsConstructor
public class ResumeOptimizeController {

    private final ResumeOptimizeService resumeOptimizeService;

    /**
     * 首次优化
     * POST /resume/optimize?resumeId=1&direction=项目经历
     */
    @PostMapping(value = "/optimize", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter optimize(@RequestParam Long resumeId,
                                @RequestParam String direction,
                                @RequestParam(required = false) String message,
                                HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        SseEmitter emitter = new SseEmitter(0L);

        resumeOptimizeService.optimize(resumeId, direction, message)
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

    /**
     * 多轮修改
     * POST /resume/refine?resumeId=1&message=再短一点
     */
    @PostMapping(value = "/refine", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter refine(@RequestParam Long resumeId,
                              @RequestParam String message,
                              HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        SseEmitter emitter = new SseEmitter(0L);

        resumeOptimizeService.refine(resumeId, message)
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

package com.resumepilot.interview.controller;

import com.resumepilot.common.result.Result;
import com.resumepilot.interview.dto.StartInterviewRequest;
import com.resumepilot.interview.service.InterviewService;
import com.resumepilot.interview.vo.InterviewVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 模拟面试 Controller
 *
 * POST /interview/start          → 开始面试
 * POST /interview/{id}/answer    → 提交回答
 * GET  /interview/{id}           → 获取面试详情
 * GET  /interview/my             → 获取面试历史
 */
@RestController
@RequestMapping("/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    /**
     * 开始面试
     * 基于简历和目标职位，生成第一道面试题
     */
    @PostMapping("/start")
    public Result<InterviewVO> startInterview(@Valid @RequestBody StartInterviewRequest request) {
        InterviewVO vo = interviewService.startInterview(request);
        return Result.success(vo);
    }

    /**
     * 提交回答
     * AI 评分 + 生成下一题（或结束面试）
     */
    @PostMapping("/{id}/answer")
    public Result<InterviewVO> submitAnswer(@PathVariable Long id,
                                             @RequestParam String answer) {
        InterviewVO vo = interviewService.submitAnswer(id, answer);
        return Result.success(vo);
    }

    /**
     * 获取面试详情
     */
    @GetMapping("/{id}")
    public Result<InterviewVO> getInterview(@PathVariable Long id) {
        InterviewVO vo = interviewService.getInterview(id);
        return Result.success(vo);
    }

    /**
     * 获取当前用户的面试历史
     */
    @GetMapping("/my")
    public Result<List<InterviewVO>> getMyInterviews() {
        List<InterviewVO> list = interviewService.getMyInterviews();
        return Result.success(list);
    }
}

package com.resumepilot.history.controller;

import com.resumepilot.ats.service.AtsService;
import com.resumepilot.ats.vo.AtsVO;
import com.resumepilot.common.result.Result;
import com.resumepilot.interview.service.InterviewService;
import com.resumepilot.interview.vo.InterviewVO;
import com.resumepilot.jd.service.JdService;
import com.resumepilot.jd.vo.JdVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 历史记录 Controller
 *
 * 统一查询所有历史记录：ATS、JD 匹配、简历优化、模拟面试
 *
 * GET /history/all       → 获取所有历史记录
 * GET /history/ats       → ATS 评分历史
 * GET /history/jd        → JD 匹配历史
 * GET /history/interview → 模拟面试历史
 */
@RestController
@RequestMapping("/history")
@RequiredArgsConstructor
public class HistoryController {

    private final AtsService atsService;
    private final JdService jdService;
    private final InterviewService interviewService;

    /**
     * 获取所有历史记录（按时间倒序）
     */
    @GetMapping("/all")
    public Result<Map<String, Object>> getAllHistory() {
        Map<String, Object> result = new HashMap<>();
        result.put("ats", atsService.getMyRecords());
        result.put("jd", jdService.getMyRecords());
        result.put("interview", interviewService.getMyInterviews());
        return Result.success(result);
    }

    /**
     * 获取 ATS 评分历史
     */
    @GetMapping("/ats")
    public Result<List<AtsVO>> getAtsHistory() {
        return Result.success(atsService.getMyRecords());
    }

    /**
     * 获取 JD 匹配历史
     */
    @GetMapping("/jd")
    public Result<List<JdVO>> getJdHistory() {
        return Result.success(jdService.getMyRecords());
    }

    /**
     * 获取模拟面试历史
     */
    @GetMapping("/interview")
    public Result<List<InterviewVO>> getInterviewHistory() {
        return Result.success(interviewService.getMyInterviews());
    }
}

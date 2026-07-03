package com.resumepilot.ai.tool;

import com.resumepilot.ats.service.AtsService;
import com.resumepilot.ats.vo.AtsVO;
import com.resumepilot.auth.service.UserService;
import com.resumepilot.interview.service.InterviewService;
import com.resumepilot.interview.vo.InterviewVO;
import com.resumepilot.jd.service.JdService;
import com.resumepilot.jd.vo.JdVO;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 历史记录工具
 *
 * 让 Agent 能够查询用户的历史记录（ATS、JD、面试）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HistoryTool {

    private final AtsService atsService;
    private final JdService jdService;
    private final InterviewService interviewService;
    private final UserService userService;

    /**
     * 获取 ATS 评分历史
     *
     * @return ATS 评分记录
     */
    @Tool("获取用户的 ATS 评分历史记录，返回最近的评分结果")
    public String getAtsHistory() {
        List<AtsVO> records = atsService.getMyRecords();

        if (records.isEmpty()) {
            return "暂无 ATS 评分记录";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("ATS 评分历史：\n");
        for (AtsVO record : records) {
            sb.append("- ID: ").append(record.getId())
              .append(", 状态: ").append(record.getStatus() == 1 ? "已完成" : "进行中");
            if (record.getResult() != null) {
                sb.append(", 评分: ").append(record.getResult().getOverallScore());
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * 获取 JD 匹配历史
     *
     * @return JD 匹配记录
     */
    @Tool("获取用户的 JD 匹配历史记录，返回最近的匹配结果")
    public String getJdHistory() {
        List<JdVO> records = jdService.getMyRecords();

        if (records.isEmpty()) {
            return "暂无 JD 匹配记录";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("JD 匹配历史：\n");
        for (JdVO record : records) {
            sb.append("- ID: ").append(record.getId())
              .append(", 状态: ").append(record.getStatus() == 1 ? "已完成" : "进行中");
            if (record.getResult() != null) {
                sb.append(", 匹配度: ").append(record.getResult().getMatchScore()).append("%");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * 获取模拟面试历史
     *
     * @return 面试记录
     */
    @Tool("获取用户的模拟面试历史记录，返回最近的面试结果")
    public String getInterviewHistory() {
        List<InterviewVO> records = interviewService.getMyInterviews();

        if (records.isEmpty()) {
            return "暂无模拟面试记录";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("模拟面试历史：\n");
        for (InterviewVO record : records) {
            sb.append("- ID: ").append(record.getId())
              .append(", 职位: ").append(record.getJobTitle())
              .append(", 题数: ").append(record.getQuestionCount())
              .append(", 状态: ").append("completed".equals(record.getStatus()) ? "已完成" : "进行中")
              .append("\n");
        }
        return sb.toString();
    }
}

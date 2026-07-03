package com.resumepilot.interview.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 开始面试请求 DTO
 */
@Data
public class StartInterviewRequest {

    /** 简历 ID（必填） */
    @NotNull(message = "简历ID不能为空")
    private Long resumeId;

    /** 目标职位（可选） */
    private String jobTitle;

    /** 目标公司（可选） */
    private String company;

    /** JD 匹配 ID（可选，如果有 JD 匹配结果，可以基于 JD 出题） */
    private Long jdMatchId;
}

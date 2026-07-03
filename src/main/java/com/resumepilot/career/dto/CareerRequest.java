package com.resumepilot.career.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 话术生成请求 DTO
 */
@Data
public class CareerRequest {

    /** 简历 ID（必填） */
    @NotNull(message = "简历ID不能为空")
    private Long resumeId;

    /** 目标职位（可选） */
    private String jobTitle;

    /** 目标公司（可选） */
    private String company;

    /** 话术类型（可选，默认生成所有类型） */
    private String type;
}

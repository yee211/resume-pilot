package com.resumepilot.ats.dto;

import lombok.Data;
import java.util.List;

/**
 * ATS 分析结果 DTO
 * 对应 AI 返回的 JSON 结构
 */
@Data
public class AtsResultDTO {

    /** 综合评分 0-100 */
    private Integer overallScore;

    /** 关键词覆盖率 0-100 */
    private Integer keywordScore;

    /** 技能评分 0-100 */
    private Integer skillScore;

    /** 项目评分 0-100 */
    private Integer projectScore;

    /** STAR 法则评分 0-100 */
    private Integer starScore;

    /** 缺失的关键词 */
    private List<String> missingKeywords;

    /** 优化建议列表 */
    private List<String> suggestions;

    /** 一句话总结 */
    private String summary;
}

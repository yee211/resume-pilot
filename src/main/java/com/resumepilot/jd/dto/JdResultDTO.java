package com.resumepilot.jd.dto;

import lombok.Data;
import java.util.List;

/**
 * JD 匹配结果 DTO
 */
@Data
public class JdResultDTO {

    /** 匹配度 0-100 */
    private Integer matchScore;

    /** 简历中已匹配的技能 */
    private List<String> matchedSkills;

    /** 简历中缺失的技能 */
    private List<String> missingSkills;

    /** 优化建议 */
    private List<String> suggestions;

    /** 一句话总结 */
    private String summary;
}

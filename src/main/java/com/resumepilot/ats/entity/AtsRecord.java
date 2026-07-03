package com.resumepilot.ats.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ats_record")
public class AtsRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("resume_id")
    private Long resumeId;

    @TableField("overall_score")
    private Integer overallScore;

    @TableField("keyword_score")
    private Integer keywordScore;

    @TableField("skill_score")
    private Integer skillScore;

    @TableField("project_score")
    private Integer projectScore;

    @TableField("star_score")
    private Integer starScore;

    @TableField("suggestions")
    private String suggestions;

    /** 状态：0=待分析  1=分析成功  2=分析失败 */
    @TableField("status")
    private Integer status;

    @TableField("analyzed_at")
    private LocalDateTime analyzedAt;
}

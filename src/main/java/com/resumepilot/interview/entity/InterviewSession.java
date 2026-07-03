package com.resumepilot.interview.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 模拟面试会话实体
 *
 * 一次模拟面试 = 一个会话，包含多道题目
 */
@Data
@TableName("interview_session")
public class InterviewSession {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("resume_id")
    private Long resumeId;

    @TableField("jd_match_id")
    private Long jdMatchId;

    @TableField("job_title")
    private String jobTitle;

    @TableField("company")
    private String company;

    /** 状态：pending=待开始  in_progress=进行中  completed=已完成 */
    @TableField("status")
    private String status;

    @TableField("question_count")
    private Integer questionCount;

    @TableField("score")
    private BigDecimal score;

    @TableField("feedback")
    private String feedback;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}

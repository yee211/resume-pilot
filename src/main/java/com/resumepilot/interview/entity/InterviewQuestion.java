package com.resumepilot.interview.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 面试题目实体
 *
 * 每道面试题 + 用户回答 + AI 评分
 */
@Data
@TableName("interview_question")
public class InterviewQuestion {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("session_id")
    private Long sessionId;

    @TableField("question_order")
    private Integer questionOrder;

    @TableField("question")
    private String question;

    @TableField("answer")
    private String answer;

    @TableField("score")
    private BigDecimal score;

    /** AI 反馈（JSON：优点、不足、参考答案） */
    @TableField("feedback")
    private String feedback;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}

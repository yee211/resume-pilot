package com.resumepilot.interview.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 面试会话 VO（返回给前端）
 */
@Data
public class InterviewVO {

    private Long id;
    private Long resumeId;
    private String jobTitle;
    private String company;
    private String status;
    private Integer questionCount;
    private BigDecimal score;
    private String feedback;
    private LocalDateTime createdAt;

    /** 面试题目列表 */
    private List<QuestionVO> questions;

    /**
     * 面试题目 VO
     */
    @Data
    public static class QuestionVO {
        private Long id;
        private Integer questionOrder;
        private String question;
        private String answer;
        private BigDecimal score;
        private String feedback;
    }
}

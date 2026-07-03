package com.resumepilot.resume.dto;

import lombok.Data;
import java.util.List;

/**
 * AI 解析简历后的结构化数据
 * 对应 parsed_json 字段存储的内容
 *
 * 为什么用 DTO 而不是直接存 JSON 字符串？
 * → DTO 有类型约束，前端拿到的是结构化数据，不会拿到一个巨大的字符串
 */
@Data
public class ParsedResumeDTO {

    /** 姓名 */
    private String name;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 求职意向 */
    private String jobTarget;

    /** 教育经历 */
    private List<Education> educations;

    /** 技能列表 */
    private List<String> skills;

    /** 项目经历 */
    private List<Project> projects;

    /** 实习经历 */
    private List<Internship> internships;

    /** 获奖/证书 */
    private List<String> awards;

    /** 自我评价 */
    private String summary;

    // ========== 内部类：嵌套结构 ==========

    @Data
    public static class Education {
        private String school;
        private String major;
        private String degree;
        private String startDate;
        private String endDate;
    }

    @Data
    public static class Project {
        private String name;
        private String role;
        private String startDate;
        private String endDate;
        private String description;
        private List<String> techStack;
    }

    @Data
    public static class Internship {
        private String company;
        private String position;
        private String startDate;
        private String endDate;
        private String description;
    }
}

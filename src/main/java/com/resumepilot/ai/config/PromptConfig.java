package com.resumepilot.ai.config;

import dev.langchain4j.model.input.PromptTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Prompt 模板配置
 *
 * 把 .md 文件加载为 PromptTemplate，注入到 Service 里使用
 * 这样 Prompt 和 Java 代码完全分离
 */
@Configuration
public class PromptConfig {

    /**
     * 简历解析的 Prompt 模板
     * 从 prompt/resume-parse.md 加载，包含 {{text}} 占位符
     */
    @Bean("resumeParsePromptTemplate")
    public PromptTemplate resumeParsePromptTemplate() throws IOException {
        return loadTemplate("prompt/resume-parse.md");
    }

    /**
     * ATS 评分的 Prompt 模板
     */
    @Bean("atsPromptTemplate")
    public PromptTemplate atsPromptTemplate() throws IOException {
        return loadTemplate("prompt/ats.md");
    }

    /**
     * JD 匹配的 Prompt 模板
     */
    @Bean("jdPromptTemplate")
    public PromptTemplate jdPromptTemplate() throws IOException {
        return loadTemplate("prompt/jd.md");
    }

    /**
     * 简历优化的 Prompt 模板
     */
    @Bean("resumeOptimizePromptTemplate")
    public PromptTemplate resumeOptimizePromptTemplate() throws IOException {
        return loadTemplate("prompt/resume-optimize.md");
    }

    /**
     * 模拟面试的 Prompt 模板
     */
    @Bean("interviewPromptTemplate")
    public PromptTemplate interviewPromptTemplate() throws IOException {
        return loadTemplate("prompt/interview.md");
    }

    /**
     * 求职话术的 Prompt 模板
     */
    @Bean("careerPromptTemplate")
    public PromptTemplate careerPromptTemplate() throws IOException {
        return loadTemplate("prompt/career.md");
    }

    /**
     * 通用：从 classpath 加载 .md 文件为 PromptTemplate
     */
    private PromptTemplate loadTemplate(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        String content = resource.getContentAsString(StandardCharsets.UTF_8);
        return PromptTemplate.from(content);
    }
}

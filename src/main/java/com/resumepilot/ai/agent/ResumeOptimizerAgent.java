package com.resumepilot.ai.agent;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

/**
 * 简历优化 Agent
 *
 * 和 ChatAgent 类似，但专门用于简历优化场景
 * 支持 ChatMemory → 用户说"再短一点"时，Agent 知道改的是上一版
 * 支持 Streaming  → 前端实时看到优化过程
 */
public interface ResumeOptimizerAgent {

    @SystemMessage("你是一个专业的简历优化顾问，专注于帮助求职者优化简历。"
            + "使用 STAR 法则、量化成果、行业关键词来改写内容。"
            + "保持真实性，不要编造经历。用中文回答，简洁专业。")
    String optimize(@MemoryId String memoryId, @UserMessage String prompt);

    @SystemMessage("你是一个专业的简历优化顾问，专注于帮助求职者优化简历。"
            + "使用 STAR 法则、量化成果、行业关键词来改写内容。"
            + "保持真实性，不要编造经历。用中文回答，简洁专业。")
    TokenStream optimizeStream(@MemoryId String memoryId, @UserMessage String prompt);
}

package com.resumepilot.ai.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * 简历解析 Agent
 *
 * LangChain4j 的 AiServices 会自动生成这个接口的实现类
 * 你只需要定义"方法签名 + 注解"，调用方法时它自动发 HTTP 请求给 LLM
 *
 * 注解说明：
 *   @SystemMessage → 告诉 LLM 它的角色和任务（相当于 system prompt）
 *   @UserMessage   → 用户传入的内容（相当于 user message）
 *
 * 为什么不用 @AiService 注解？
 *   因为我们需要在 Config 里手动构建，更灵活（后续加 ChatMemory、Tools 更方便）
 */
public interface ResumeParserAgent {

    @SystemMessage("你是一个专业的简历解析助手，请严格按照用户要求的 JSON 格式提取简历信息。"
            + "只返回 JSON，不要返回任何其他文字、解释或 markdown 标记。")
    String parse(@UserMessage String prompt);
}

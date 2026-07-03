package com.resumepilot.ai.agent;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

/**
 * 模拟面试 Agent
 *
 * 根据简历和目标岗位，生成面试问题、追问、评分和建议
 * 支持多轮对话，模拟真实面试场景
 *
 * 功能：
 *   - 根据简历生成针对性面试题
 *   - 根据岗位 JD 生成技术面试题
 *   - 支持追问（深入挖掘候选人能力）
 *   - 评分回答质量并给出改进建议
 *   - 支持多轮连续面试（保持上下文）
 */
public interface InterviewAgent {

    /**
     * 模拟面试（同步返回）
     *
     * @param memoryId 面试会话 ID（格式：interview-{userId}-{resumeId}）
     * @param message  用户回答或请求
     * @return 面试官的回复（问题/追问/评分/建议）
     */
    @SystemMessage("你是一位资深的技术面试官，正在对候选人进行面试。"
            + "你的任务是：\n"
            + "1. 根据候选人的简历和目标岗位，提出有针对性的面试问题\n"
            + "2. 根据候选人的回答进行追问，深入挖掘技术能力和项目经验\n"
            + "3. 评估回答质量，给出 1-10 分的评分\n"
            + "4. 提供改进建议，帮助候选人提升面试表现\n\n"
            + "面试风格：专业、友好、有深度\n"
            + "语言：中文\n"
            + "每次只问一个问题，等候选人回答后再继续\n"
            + "如果候选人请求总结，给出整体评分和建议。")
    String interview(@MemoryId String memoryId, @UserMessage String message);

    /**
     * 模拟面试（流式返回）
     *
     * @param memoryId 面试会话 ID
     * @param message  用户回答或请求
     * @return TokenStream 流式输出
     */
    @SystemMessage("你是一位资深的技术面试官，正在对候选人进行面试。"
            + "你的任务是：\n"
            + "1. 根据候选人的简历和目标岗位，提出有针对性的面试问题\n"
            + "2. 根据候选人的回答进行追问，深入挖掘技术能力和项目经验\n"
            + "3. 评估回答质量，给出 1-10 分的评分\n"
            + "4. 提供改进建议，帮助候选人提升面试表现\n\n"
            + "面试风格：专业、友好、有深度\n"
            + "语言：中文\n"
            + "每次只问一个问题，等候选人回答后再继续\n"
            + "如果候选人请求总结，给出整体评分和建议。")
    TokenStream interviewStream(@MemoryId String memoryId, @UserMessage String message);
}

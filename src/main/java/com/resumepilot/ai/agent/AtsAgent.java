package com.resumepilot.ai.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * ATS 评分 Agent
 * 专门负责简历的 ATS 系统模拟评分
 *
 * 和 ResumeParserAgent 一样，由 AiServices 自动生成实现
 * 无状态，不需要 ChatMemory（每次评分都是独立的）
 */
public interface AtsAgent {

    @SystemMessage("你是一个专业的 ATS 简历评分专家，请严格按照用户要求的 JSON 格式返回评分结果。"
            + "只返回 JSON，不要返回任何其他文字、解释或 markdown 标记。")
    String analyze(@UserMessage String prompt);
}

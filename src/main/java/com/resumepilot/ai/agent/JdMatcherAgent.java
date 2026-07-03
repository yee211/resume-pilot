package com.resumepilot.ai.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * JD 匹配 Agent
 * 对比简历和岗位描述，输出匹配度和缺失技能
 */
public interface JdMatcherAgent {

    @SystemMessage("你是一个专业的求职顾问，请严格按照用户要求的 JSON 格式返回 JD 匹配分析结果。"
            + "只返回 JSON，不要返回任何其他文字、解释或 markdown 标记。")
    String match(@UserMessage String prompt);
}

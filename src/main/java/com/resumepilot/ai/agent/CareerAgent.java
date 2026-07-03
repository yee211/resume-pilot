package com.resumepilot.ai.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * 职业助手 Agent
 *
 * 专门负责生成求职话术：
 *   - Boss 直聘打招呼话术
 *   - 智联招聘投递话术
 *   - 牛客网内推话术
 *   - 邮件投递话术
 *
 * 特点：
 *   - 无状态（每次生成独立）
 *   - 不需要 ChatMemory
 */
public interface CareerAgent {

    /**
     * 生成求职话术
     *
     * @param prompt 包含简历信息、目标职位、话术类型的完整 Prompt
     * @return 生成的话术（JSON 格式）
     */
    @SystemMessage("你是一位资深的求职顾问，专门为候选人生成求职话术。"
            + "你需要根据候选人的简历和目标职位，生成不同风格的求职话术。\n"
            + "要求：\n"
            + "1. 话术要简洁有力，突出核心优势\n"
            + "2. 针对不同平台调整风格（Boss直聘要亲切、智联要专业、牛客要技术范）\n"
            + "3. 控制在 100-200 字以内\n"
            + "4. 用中文输出\n"
            + "返回 JSON 格式，包含各平台的话术。")
    String generate(@UserMessage String prompt);
}

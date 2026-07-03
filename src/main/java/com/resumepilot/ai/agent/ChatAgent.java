package com.resumepilot.ai.agent;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

/**
 * 通用聊天 Agent（支持 ChatMemory + Streaming）
 *
 * 两个方法的区别：
 *   chat()       → 一次性返回完整回答（同步，适合 API 调用）
 *   chatStream() → 返回 TokenStream，一个 Token 一个 Token 推送（流式，适合前端实时显示）
 *
 * @MemoryId 注解：
 *   不同的 memoryId 对应不同的对话历史
 *   例如 "user-1-resume-optimize" 和 "user-1-interview" 是两段独立的对话
 */
public interface ChatAgent {

    @SystemMessage("你是一个专业的求职助手，帮助用户优化简历、分析岗位匹配度、准备面试。用中文回答，简洁专业。")
    String chat(@MemoryId String memoryId, @UserMessage String message);

    @SystemMessage("你是一个专业的求职助手，帮助用户优化简历、分析岗位匹配度、准备面试。用中文回答，简洁专业。")
    TokenStream chatStream(@MemoryId String memoryId, @UserMessage String message);
}

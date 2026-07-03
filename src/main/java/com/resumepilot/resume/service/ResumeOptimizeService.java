package com.resumepilot.resume.service;

import com.resumepilot.ai.agent.ResumeOptimizerAgent;
import com.resumepilot.common.exception.BusinessException;
import com.resumepilot.common.result.ResultCode;
import com.resumepilot.resume.entity.Resume;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.service.TokenStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 简历优化服务
 *
 * 核心设计：用 memoryId 把"简历内容 + 用户修改指令"绑定在一起
 *
 * 第一次调用（初始优化）：
 *   memoryId = "resume-optimize-1"
 *   prompt   = 加载简历原文 + 优化方向（项目经历/技能/自我评价）
 *   Agent 看到完整简历，输出优化版本
 *
 * 第二次调用（多轮修改）：
 *   memoryId = "resume-optimize-1"（同一个！）
 *   prompt   = "再短一点" / "偏大厂风格"
 *   Agent 从 Redis 读取上一轮对话历史，知道"再短一点"指的是什么
 *
 * 这就是 ChatMemory 的价值：用户不需要重复粘贴简历内容
 */
@Slf4j
@Service
public class ResumeOptimizeService {

    private final ResumeService resumeService;
    private final ResumeOptimizerAgent resumeOptimizerAgent;
    private final PromptTemplate optimizePromptTemplate;

    /**
     * 构造函数注入
     * @Qualifier 放在参数上，Spring 才知道注入哪个 PromptTemplate
     */
    public ResumeOptimizeService(
            ResumeService resumeService,
            ResumeOptimizerAgent resumeOptimizerAgent,
            @Qualifier("resumeOptimizePromptTemplate") PromptTemplate optimizePromptTemplate) {
        this.resumeService = resumeService;
        this.resumeOptimizerAgent = resumeOptimizerAgent;
        this.optimizePromptTemplate = optimizePromptTemplate;
    }

    /**
     * 优化简历（流式输出）
     *
     * @param resumeId  简历 ID
     * @param direction 优化方向，例如 "项目经历"、"技能描述"、"自我评价"、"全文优化"
     * @param message   用户的额外指令（可选，例如 "偏大厂风格"）
     * @return SSE 流
     */
    public TokenStream optimize(Long resumeId, String direction, String message) {
        Resume resume = resumeService.getById(resumeId);
        if (resume == null || resume.getRawText() == null) {
            throw new BusinessException(ResultCode.RESUME_NOT_FOUND);
        }

        // 第一次调用：把简历原文和优化方向填入 Prompt
        // 后续调用：用户只发 "再短一点"，Agent 从记忆里知道上下文
        String prompt;
        if (message == null || message.isBlank()) {
            prompt = optimizePromptTemplate.apply(Map.of(
                    "text", resume.getRawText(),
                    "direction", direction
            )).text();
        } else {
            prompt = message;
        }

        String memoryId = "resume-optimize-" + resumeId;
        log.info("简历优化，resumeId={}, direction={}, memoryId={}", resumeId, direction, memoryId);
        return resumeOptimizerAgent.optimizeStream(memoryId, prompt);
    }

    /**
     * 多轮修改（非首次，直接传用户指令）
     */
    public TokenStream refine(Long resumeId, String message) {
        Resume resume = resumeService.getById(resumeId);
        if (resume == null) {
            throw new BusinessException(ResultCode.RESUME_NOT_FOUND);
        }

        String memoryId = "resume-optimize-" + resumeId;
        log.info("简历修改，resumeId={}, message={}, memoryId={}", resumeId, message, memoryId);
        return resumeOptimizerAgent.optimizeStream(memoryId, message);
    }
}

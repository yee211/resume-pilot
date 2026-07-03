package com.resumepilot.ai.tool;

import com.resumepilot.ai.rag.KnowledgeService;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 搜索工具
 *
 * 让 Agent 能够主动搜索知识库
 * 用于回答用户关于简历优化、面试技巧等问题
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchTool {

    private final KnowledgeService knowledgeService;

    /**
     * 搜索知识库
     *
     * @param query 搜索关键词
     * @return 搜索结果
     */
    @Tool("搜索简历优化、面试技巧、求职建议等相关知识，返回最相关的 3 条结果")
    public String searchKnowledge(String query) {
        log.info("搜索知识库: {}", query);

        List<String> results = knowledgeService.search(query, 3);

        if (results.isEmpty()) {
            return "未找到相关知识";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("搜索结果：\n\n");
        for (int i = 0; i < results.size(); i++) {
            sb.append(i + 1).append(". ").append(results.get(i)).append("\n\n");
        }
        return sb.toString();
    }

    /**
     * 搜索面试题
     *
     * @param technology 技术关键词（如 Spring Boot、Redis）
     * @return 相关面试题
     */
    @Tool("根据技术关键词搜索相关面试题，返回常见面试问题和答案")
    public String searchInterviewQuestions(String technology) {
        log.info("搜索面试题: {}", technology);

        String query = technology + " 面试题 常见问题";
        List<String> results = knowledgeService.search(query, 3);

        if (results.isEmpty()) {
            return "未找到相关面试题";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(technology).append(" 相关面试题：\n\n");
        for (int i = 0; i < results.size(); i++) {
            sb.append(i + 1).append(". ").append(results.get(i)).append("\n\n");
        }
        return sb.toString();
    }
}

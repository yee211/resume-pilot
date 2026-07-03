package com.resumepilot.ai.tool;

import com.resumepilot.auth.service.UserService;
import com.resumepilot.resume.entity.Resume;
import com.resumepilot.resume.service.ResumeService;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 简历工具
 *
 * 让 Agent 能够查询和操作简历数据
 * 使用 @Tool 注解标记方法，LangChain4j 会自动注册
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResumeTool {

    private final ResumeService resumeService;
    private final UserService userService;

    /**
     * 获取当前用户的所有简历列表
     *
     * @return 简历列表（JSON 格式）
     */
    @Tool("获取当前用户的所有简历列表，返回简历ID、标题、创建时间等信息")
    public String listMyResumes() {
        Long userId = userService.getCurrentUserId();
        List<Resume> resumes = resumeService.listByUserId(userId);

        if (resumes.isEmpty()) {
            return "暂无简历记录";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("简历列表：\n");
        for (Resume resume : resumes) {
            sb.append("- ID: ").append(resume.getId())
              .append(", 标题: ").append(resume.getTitle())
              .append(", 类型: ").append(resume.getFileType())
              .append("\n");
        }
        return sb.toString();
    }

    /**
     * 获取简历详情
     *
     * @param resumeId 简历 ID
     * @return 简历内容
     */
    @Tool("根据简历ID获取简历的详细内容，包括解析后的文本")
    public String getResumeContent(long resumeId) {
        Resume resume = resumeService.getById(resumeId);
        if (resume == null) {
            return "简历不存在";
        }

        Long userId = userService.getCurrentUserId();
        if (!resume.getUserId().equals(userId)) {
            return "无权访问此简历";
        }

        return "简历标题: " + resume.getTitle() + "\n\n" +
               "简历内容:\n" + (resume.getContent() != null ? resume.getContent() : "暂未解析");
    }
}

package com.resumepilot.career.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumepilot.ai.agent.CareerAgent;
import com.resumepilot.career.dto.CareerRequest;
import com.resumepilot.career.vo.CareerVO;
import com.resumepilot.common.exception.BusinessException;
import com.resumepilot.common.result.ResultCode;
import com.resumepilot.resume.entity.Resume;
import com.resumepilot.resume.service.ResumeService;
import dev.langchain4j.model.input.PromptTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 求职话术服务
 *
 * 生成 Boss/智联/牛客/邮件不同风格的求职话术
 */
@Slf4j
@Service
public class CareerService {

    private final CareerAgent careerAgent;
    private final ResumeService resumeService;
    private final ObjectMapper objectMapper;
    private final PromptTemplate careerPromptTemplate;

    public CareerService(
            CareerAgent careerAgent,
            ResumeService resumeService,
            ObjectMapper objectMapper,
            @Qualifier("careerPromptTemplate") PromptTemplate careerPromptTemplate) {
        this.careerAgent = careerAgent;
        this.resumeService = resumeService;
        this.objectMapper = objectMapper;
        this.careerPromptTemplate = careerPromptTemplate;
    }

    /**
     * 生成求职话术
     *
     * @param request 请求参数
     * @return 各平台话术
     */
    public CareerVO generate(CareerRequest request) {
        // 1. 获取简历内容
        Resume resume = resumeService.getById(request.getResumeId());
        if (resume == null || resume.getRawText() == null) {
            throw new BusinessException(ResultCode.RESUME_NOT_FOUND, "简历不存在或内容为空");
        }

        // 2. 构建 Prompt
        String prompt = careerPromptTemplate.apply(
                Map.of(
                        "resume", resume.getRawText(),
                        "jobTitle", request.getJobTitle() != null ? request.getJobTitle() : "软件开发工程师",
                        "company", request.getCompany() != null ? request.getCompany() : "互联网公司"
                )
        ).text();

        // 3. 调用 AI 生成话术
        String resultJson = careerAgent.generate(prompt);
        resultJson = cleanJson(resultJson);

        // 4. 解析结果
        try {
            return objectMapper.readValue(resultJson, CareerVO.class);
        } catch (Exception e) {
            log.error("话术结果解析失败", e);
            throw new BusinessException(ResultCode.AI_SERVICE_ERROR, "话术生成失败");
        }
    }

    private String cleanJson(String json) {
        json = json.trim();
        if (json.startsWith("```")) {
            json = json.replaceAll("```json\\s*", "").replaceAll("```\\s*", "");
        }
        return json.trim();
    }
}

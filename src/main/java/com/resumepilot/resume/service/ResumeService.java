package com.resumepilot.resume.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumepilot.ai.agent.ResumeParserAgent;
import com.resumepilot.auth.service.UserService;
import com.resumepilot.common.exception.BusinessException;
import com.resumepilot.common.result.ResultCode;
import com.resumepilot.resume.dto.ParsedResumeDTO;
import com.resumepilot.resume.entity.Resume;
import com.resumepilot.resume.mapper.ResumeMapper;
import com.resumepilot.resume.vo.ResumeVO;
import dev.langchain4j.model.input.PromptTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 简历服务
 *
 * 上传和解析分离：
 *   upload()     → 同步：校验 + 提取文本 + 存库 + 发消息到 Redis Stream → 秒级返回
 *   parseSync()  → 同步：AI 解析 + 更新数据库（由消费者线程调用）
 */
@Slf4j
@Service
public class ResumeService extends ServiceImpl<ResumeMapper, Resume> {

    private static final String STREAM_KEY = "resume:parse:stream";

    private final FileParserService fileParserService;
    private final ResumeParserAgent resumeParserAgent;
    private final ObjectMapper objectMapper;
    private final PromptTemplate resumeParsePromptTemplate;
    private final StringRedisTemplate redisTemplate;
    private final UserService userService;

    public ResumeService(
            FileParserService fileParserService,
            ResumeParserAgent resumeParserAgent,
            ObjectMapper objectMapper,
            @Qualifier("resumeParsePromptTemplate") PromptTemplate resumeParsePromptTemplate,
            StringRedisTemplate redisTemplate,
            UserService userService) {
        this.fileParserService = fileParserService;
        this.resumeParserAgent = resumeParserAgent;
        this.objectMapper = objectMapper;
        this.resumeParsePromptTemplate = resumeParsePromptTemplate;
        this.redisTemplate = redisTemplate;
        this.userService = userService;
    }

    /**
     * 上传简历（同步，秒级返回）
     *
     * 流程：校验 → 提取文本 → 存库（status=0）→ 发消息到 Redis Stream → 返回
     */
    public ResumeVO upload(MultipartFile file) {
        validateFile(file);

        String rawText = fileParserService.extractText(file);

        Resume resume = new Resume();
        resume.setUserId(userService.getCurrentUserId());
        resume.setFileName(file.getOriginalFilename());
        resume.setRawText(rawText);
        resume.setStatus(0);
        this.save(resume);

        // 发消息到 Redis Stream，通知消费者开始解析
        redisTemplate.opsForStream().add(STREAM_KEY, Map.of("resumeId", resume.getId().toString()));

        log.info("简历上传成功，已发送解析消息，resumeId={}", resume.getId());
        return toVO(resume);
    }

    /**
     * 同步解析简历（由消费者线程调用）
     */
    public void parseSync(Long resumeId) {
        Resume resume = this.getById(resumeId);
        if (resume == null || resume.getRawText() == null) {
            log.error("简历不存在，resumeId={}", resumeId);
            return;
        }

        try {
            String prompt = resumeParsePromptTemplate.apply(Map.of("text", resume.getRawText())).text();
            String parsedJson = resumeParserAgent.parse(prompt);
            parsedJson = cleanJson(parsedJson);
            resume.setParsedJson(parsedJson);
            resume.setStatus(1);
            this.updateById(resume);
            log.info("简历解析完成，resumeId={}", resumeId);
        } catch (Exception e) {
            log.error("AI 解析简历失败，resumeId={}", resumeId, e);
            resume.setStatus(2);
            this.updateById(resume);
        }
    }

    /**
     * 根据 ID 查询简历
     */
    public ResumeVO getResumeById(Long id) {
        Resume resume = this.getById(id);
        if (resume == null) {
            throw new BusinessException(ResultCode.RESUME_NOT_FOUND);
        }
        return toVO(resume);
    }

    /**
     * 获取当前用户的所有简历（按时间倒序）
     */
    public List<ResumeVO> getMyResumes() {
        Long userId = userService.getCurrentUserId();
        List<Resume> list = this.list(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Resume>()
                        .eq(Resume::getUserId, userId)
                        .orderByDesc(Resume::getCreatedAt)
        );
        return list.stream().map(this::toVO).toList();
    }

    // ========== 私有方法 ==========

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ResultCode.FILE_UPLOAD_ERROR, "文件不能为空");
        }
        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".pdf") && !fileName.endsWith(".docx"))) {
            throw new BusinessException(ResultCode.FILE_FORMAT_ERROR, "只支持 PDF 和 DOCX 格式");
        }
    }

    /**
     * 清理 AI 返回的 JSON
     * 处理两种情况：
     *   1. markdown 包裹：```json ... ```
     *   2. 截断：JSON 不完整（最后一个 } 缺失），尝试补全
     */
    private String cleanJson(String json) {
        json = json.trim();
        // 去掉 markdown 包裹
        if (json.startsWith("```")) {
            json = json.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
        }
        // 尝试补全截断的 JSON（简单策略：数花括号，缺几个补几个）
        long open = json.chars().filter(c -> c == '{').count();
        long close = json.chars().filter(c -> c == '}').count();
        if (open > close) {
            // 如果最后一个有效字符是逗号或引号，先去掉
            json = json.replaceAll("[,\\s]*$", "");
            json += "}".repeat((int) (open - close));
        }
        return json;
    }

    private ResumeVO toVO(Resume resume) {
        ResumeVO vo = new ResumeVO();
        vo.setId(resume.getId());
        vo.setFileName(resume.getFileName());
        vo.setStatus(resume.getStatus());
        vo.setCreatedAt(resume.getCreatedAt());

        if (resume.getParsedJson() != null) {
            try {
                ParsedResumeDTO parsed = objectMapper.readValue(resume.getParsedJson(), ParsedResumeDTO.class);
                vo.setParsedData(parsed);
            } catch (JsonProcessingException e) {
                log.warn("parsed_json 反序列化失败，resumeId={}", resume.getId(), e);
            }
        }
        return vo;
    }
}

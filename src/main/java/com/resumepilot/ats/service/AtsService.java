package com.resumepilot.ats.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumepilot.ai.agent.AtsAgent;
import com.resumepilot.ats.dto.AtsResultDTO;
import com.resumepilot.ats.entity.AtsRecord;
import com.resumepilot.ats.mapper.AtsMapper;
import com.resumepilot.ats.vo.AtsVO;
import com.resumepilot.auth.service.UserService;
import com.resumepilot.common.exception.BusinessException;
import com.resumepilot.common.result.ResultCode;
import com.resumepilot.resume.entity.Resume;
import com.resumepilot.resume.service.ResumeService;
import dev.langchain4j.model.input.PromptTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * ATS 评分服务（异步模式）
 *
 * submit()      → 存库(status=0) + 发 Redis Stream 消息 → 立即返回
 * processSync() → 消费者线程调用：AI 评分 → 更新库
 * getResult()   → 前端轮询用：查单条记录
 * getMyRecords()→ 查当前用户的记录列表
 */
@Slf4j
@Service
public class AtsService extends ServiceImpl<AtsMapper, AtsRecord> {

    private static final String STREAM_KEY = "ats:analyze:stream";

    private final ResumeService resumeService;
    private final AtsAgent atsAgent;
    private final ObjectMapper objectMapper;
    private final PromptTemplate atsPromptTemplate;
    private final UserService userService;
    private final StringRedisTemplate redisTemplate;

    public AtsService(
            ResumeService resumeService,
            AtsAgent atsAgent,
            ObjectMapper objectMapper,
            @Qualifier("atsPromptTemplate") PromptTemplate atsPromptTemplate,
            UserService userService,
            StringRedisTemplate redisTemplate) {
        this.resumeService = resumeService;
        this.atsAgent = atsAgent;
        this.objectMapper = objectMapper;
        this.atsPromptTemplate = atsPromptTemplate;
        this.userService = userService;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 提交 ATS 评分（同步，秒级返回）
     * 存一条待分析记录，发消息到 Redis Stream
     */
    public AtsVO submit(Long resumeId) {
        AtsRecord record = new AtsRecord();
        record.setUserId(userService.getCurrentUserId());
        record.setResumeId(resumeId);
        record.setStatus(0);
        this.save(record);

        redisTemplate.opsForStream().add(STREAM_KEY, Map.of("recordId", record.getId().toString()));

        log.info("ATS 评分已提交，recordId={}, resumeId={}", record.getId(), resumeId);
        return toVO(record, null);
    }

    /**
     * 同步执行 ATS 评分（由消费者线程调用）
     */
    public void processSync(Long recordId) {
        AtsRecord record = this.getById(recordId);
        if (record == null) {
            log.error("ATS 记录不存在，recordId={}", recordId);
            return;
        }

        Resume resume = resumeService.getById(record.getResumeId());
        if (resume == null || resume.getRawText() == null) {
            record.setStatus(2);
            this.updateById(record);
            return;
        }

        try {
            String prompt = atsPromptTemplate.apply(Map.of("text", resume.getRawText())).text();
            String resultJson = atsAgent.analyze(prompt);
            resultJson = cleanJson(resultJson);

            AtsResultDTO result = objectMapper.readValue(resultJson, AtsResultDTO.class);
            record.setOverallScore(result.getOverallScore());
            record.setKeywordScore(result.getKeywordScore());
            record.setSkillScore(result.getSkillScore());
            record.setProjectScore(result.getProjectScore());
            record.setStarScore(result.getStarScore());
            record.setSuggestions(resultJson);
            record.setAnalyzedAt(LocalDateTime.now());
            record.setStatus(1);
            this.updateById(record);
            log.info("ATS 评分完成，recordId={}", recordId);
        } catch (Exception e) {
            log.error("ATS 评分失败，recordId={}", recordId, e);
            record.setStatus(2);
            this.updateById(record);
        }
    }

    /**
     * 查询单条记录（前端轮询用）
     */
    public AtsVO getResult(Long id) {
        AtsRecord record = this.getById(id);
        if (record == null) {
            throw new BusinessException(ResultCode.ATS_ANALYSIS_ERROR, "记录不存在");
        }
        AtsResultDTO result = null;
        if (record.getSuggestions() != null && record.getStatus() == 1) {
            try {
                result = objectMapper.readValue(record.getSuggestions(), AtsResultDTO.class);
            } catch (JsonProcessingException e) {
                log.warn("ATS 结果反序列化失败，recordId={}", id);
            }
        }
        return toVO(record, result);
    }

    /**
     * 获取当前用户的 ATS 记录列表
     */
    public List<AtsVO> getMyRecords() {
        Long userId = userService.getCurrentUserId();
        List<AtsRecord> list = this.list(
                new LambdaQueryWrapper<AtsRecord>()
                        .eq(AtsRecord::getUserId, userId)
                        .orderByDesc(AtsRecord::getAnalyzedAt)
        );
        return list.stream().map(r -> getResult(r.getId())).toList();
    }

    private String cleanJson(String json) {
        json = json.trim();
        if (json.startsWith("```")) {
            json = json.replaceAll("```json\\s*", "").replaceAll("```\\s*", "");
        }
        return json.trim();
    }

    private AtsVO toVO(AtsRecord record, AtsResultDTO result) {
        AtsVO vo = new AtsVO();
        vo.setId(record.getId());
        vo.setResumeId(record.getResumeId());
        vo.setStatus(record.getStatus());
        vo.setResult(result);
        vo.setAnalyzedAt(record.getAnalyzedAt());
        return vo;
    }
}

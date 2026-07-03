package com.resumepilot.jd.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumepilot.ai.agent.JdMatcherAgent;
import com.resumepilot.auth.service.UserService;
import com.resumepilot.common.exception.BusinessException;
import com.resumepilot.common.result.ResultCode;
import com.resumepilot.jd.dto.JdResultDTO;
import com.resumepilot.jd.entity.JdRecord;
import com.resumepilot.jd.mapper.JdMapper;
import com.resumepilot.jd.vo.JdVO;
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
 * JD 匹配服务（异步模式）
 */
@Slf4j
@Service
public class JdService extends ServiceImpl<JdMapper, JdRecord> {

    private static final String STREAM_KEY = "jd:match:stream";

    private final ResumeService resumeService;
    private final JdMatcherAgent jdMatcherAgent;
    private final ObjectMapper objectMapper;
    private final PromptTemplate jdPromptTemplate;
    private final UserService userService;
    private final StringRedisTemplate redisTemplate;

    public JdService(
            ResumeService resumeService,
            JdMatcherAgent jdMatcherAgent,
            ObjectMapper objectMapper,
            @Qualifier("jdPromptTemplate") PromptTemplate jdPromptTemplate,
            UserService userService,
            StringRedisTemplate redisTemplate) {
        this.resumeService = resumeService;
        this.jdMatcherAgent = jdMatcherAgent;
        this.objectMapper = objectMapper;
        this.jdPromptTemplate = jdPromptTemplate;
        this.userService = userService;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 提交 JD 匹配（同步，秒级返回）
     */
    public JdVO submit(Long resumeId, String jdText) {
        JdRecord record = new JdRecord();
        record.setUserId(userService.getCurrentUserId());
        record.setResumeId(resumeId);
        record.setJdText(jdText);
        record.setStatus(0);
        this.save(record);

        redisTemplate.opsForStream().add(STREAM_KEY, Map.of("recordId", record.getId().toString()));

        log.info("JD 匹配已提交，recordId={}", record.getId());
        return toVO(record, null);
    }

    /**
     * 同步执行 JD 匹配（由消费者线程调用）
     */
    public void processSync(Long recordId) {
        JdRecord record = this.getById(recordId);
        if (record == null) {
            log.error("JD 记录不存在，recordId={}", recordId);
            return;
        }

        Resume resume = resumeService.getById(record.getResumeId());
        if (resume == null || resume.getRawText() == null) {
            record.setStatus(2);
            this.updateById(record);
            return;
        }

        try {
            String prompt = jdPromptTemplate.apply(Map.of(
                    "text", resume.getRawText(),
                    "jd", record.getJdText()
            )).text();

            String resultJson = jdMatcherAgent.match(prompt);
            resultJson = cleanJson(resultJson);

            JdResultDTO result = objectMapper.readValue(resultJson, JdResultDTO.class);
            record.setMatchScore(result.getMatchScore());
            record.setResultJson(resultJson);
            record.setCreatedAt(LocalDateTime.now());
            record.setStatus(1);
            this.updateById(record);
            log.info("JD 匹配完成，recordId={}", recordId);
        } catch (Exception e) {
            log.error("JD 匹配失败，recordId={}", recordId, e);
            record.setStatus(2);
            this.updateById(record);
        }
    }

    /**
     * 查询单条记录（前端轮询用）
     */
    public JdVO getResult(Long id) {
        JdRecord record = this.getById(id);
        if (record == null) {
            throw new BusinessException(ResultCode.JD_MATCH_ERROR, "记录不存在");
        }
        JdResultDTO result = null;
        if (record.getResultJson() != null && record.getStatus() == 1) {
            try {
                result = objectMapper.readValue(record.getResultJson(), JdResultDTO.class);
            } catch (JsonProcessingException e) {
                log.warn("JD 结果反序列化失败，recordId={}", id);
            }
        }
        return toVO(record, result);
    }

    /**
     * 获取当前用户的 JD 记录列表
     */
    public List<JdVO> getMyRecords() {
        Long userId = userService.getCurrentUserId();
        List<JdRecord> list = this.list(
                new LambdaQueryWrapper<JdRecord>()
                        .eq(JdRecord::getUserId, userId)
                        .orderByDesc(JdRecord::getCreatedAt)
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

    private JdVO toVO(JdRecord record, JdResultDTO result) {
        JdVO vo = new JdVO();
        vo.setId(record.getId());
        vo.setResumeId(record.getResumeId());
        vo.setJdText(record.getJdText());
        vo.setStatus(record.getStatus());
        vo.setResult(result);
        vo.setCreatedAt(record.getCreatedAt());
        return vo;
    }
}

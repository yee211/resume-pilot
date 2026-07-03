package com.resumepilot.interview.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.resumepilot.ai.agent.InterviewAgent;
import com.resumepilot.auth.service.UserService;
import com.resumepilot.common.exception.BusinessException;
import com.resumepilot.common.result.ResultCode;
import com.resumepilot.interview.dto.StartInterviewRequest;
import com.resumepilot.interview.entity.InterviewQuestion;
import com.resumepilot.interview.entity.InterviewSession;
import com.resumepilot.interview.mapper.InterviewQuestionMapper;
import com.resumepilot.interview.mapper.InterviewSessionMapper;
import com.resumepilot.interview.vo.InterviewVO;
import com.resumepilot.resume.entity.Resume;
import com.resumepilot.resume.service.ResumeService;
import dev.langchain4j.model.input.PromptTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 模拟面试服务
 *
 * 功能：
 *   - 开始面试（创建会话 + 生成第一题）
 *   - 提交回答（AI 评分 + 生成下一题）
 *   - 结束面试（生成总结）
 *   - 查询历史记录
 */
@Slf4j
@Service
public class InterviewService extends ServiceImpl<InterviewSessionMapper, InterviewSession> {

    private final InterviewAgent interviewAgent;
    private final InterviewQuestionMapper questionMapper;
    private final ResumeService resumeService;
    private final UserService userService;
    private final PromptTemplate interviewPromptTemplate;

    public InterviewService(
            InterviewAgent interviewAgent,
            InterviewQuestionMapper questionMapper,
            ResumeService resumeService,
            UserService userService,
            @Qualifier("interviewPromptTemplate") PromptTemplate interviewPromptTemplate) {
        this.interviewAgent = interviewAgent;
        this.questionMapper = questionMapper;
        this.resumeService = resumeService;
        this.userService = userService;
        this.interviewPromptTemplate = interviewPromptTemplate;
    }

    /**
     * 开始面试
     *
     * @param request 面试配置
     * @return 面试会话（包含第一题）
     */
    @Transactional
    public InterviewVO startInterview(StartInterviewRequest request) {
        Long userId = userService.getCurrentUserId();

        // 1. 获取简历内容
        Resume resume = resumeService.getById(request.getResumeId());
        if (resume == null || resume.getContent() == null) {
            throw new BusinessException(ResultCode.RESUME_NOT_FOUND, "简历不存在或内容为空");
        }

        // 2. 创建面试会话
        InterviewSession session = new InterviewSession();
        session.setUserId(userId);
        session.setResumeId(request.getResumeId());
        session.setJdMatchId(request.getJdMatchId());
        session.setJobTitle(request.getJobTitle() != null ? request.getJobTitle() : "软件开发工程师");
        session.setCompany(request.getCompany() != null ? request.getCompany() : "互联网公司");
        session.setStatus("in_progress");
        session.setQuestionCount(0);
        this.save(session);

        // 3. 生成第一题
        String memoryId = "interview-" + userId + "-" + session.getId();
        String prompt = buildStartPrompt(resume.getContent(), session.getJobTitle(), session.getCompany());
        String firstQuestion = interviewAgent.interview(memoryId, prompt);

        // 4. 保存第一题
        InterviewQuestion question = new InterviewQuestion();
        question.setSessionId(session.getId());
        question.setQuestionOrder(1);
        question.setQuestion(firstQuestion);
        questionMapper.insert(question);

        // 5. 更新会话题数
        session.setQuestionCount(1);
        this.updateById(session);

        log.info("面试已开始，sessionId={}, userId={}", session.getId(), userId);
        return toVO(session, List.of(question));
    }

    /**
     * 提交回答
     *
     * @param sessionId 面试会话 ID
     * @param answer    用户回答
     * @return AI 的反馈 + 下一题
     */
    @Transactional
    public InterviewVO submitAnswer(Long sessionId, String answer) {
        Long userId = userService.getCurrentUserId();

        // 1. 获取会话
        InterviewSession session = this.getById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "面试会话不存在");
        }
        if (!"in_progress".equals(session.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "面试已结束");
        }

        // 2. 获取当前题目
        InterviewQuestion currentQuestion = questionMapper.selectOne(
                new LambdaQueryWrapper<InterviewQuestion>()
                        .eq(InterviewQuestion::getSessionId, sessionId)
                        .orderByDesc(InterviewQuestion::getQuestionOrder)
                        .last("LIMIT 1")
        );

        // 3. 更新当前题目的回答
        currentQuestion.setAnswer(answer);
        questionMapper.updateById(currentQuestion);

        // 4. 调用 AI 生成反馈 + 下一题
        String memoryId = "interview-" + userId + "-" + sessionId;
        String feedbackAndNext = interviewAgent.interview(memoryId, answer);

        // 5. 判断是否应该结束面试（超过 10 题或用户请求结束）
        int nextOrder = session.getQuestionCount() + 1;
        if (nextOrder > 10 || answer.contains("结束面试") || answer.contains("总结")) {
            return endInterview(session, feedbackAndNext);
        }

        // 6. 保存下一题
        InterviewQuestion nextQuestion = new InterviewQuestion();
        nextQuestion.setSessionId(sessionId);
        nextQuestion.setQuestionOrder(nextOrder);
        nextQuestion.setQuestion(feedbackAndNext);
        questionMapper.insert(nextQuestion);

        // 7. 更新会话题数
        session.setQuestionCount(nextOrder);
        this.updateById(session);

        // 8. 返回结果
        List<InterviewQuestion> questions = getQuestions(sessionId);
        return toVO(session, questions);
    }

    /**
     * 结束面试
     */
    private InterviewVO endInterview(InterviewSession session, String feedback) {
        session.setStatus("completed");
        session.setFeedback(feedback);
        session.setUpdatedAt(LocalDateTime.now());
        this.updateById(session);

        log.info("面试已结束，sessionId={}", session.getId());
        List<InterviewQuestion> questions = getQuestions(session.getId());
        return toVO(session, questions);
    }

    /**
     * 获取面试详情
     */
    public InterviewVO getInterview(Long sessionId) {
        Long userId = userService.getCurrentUserId();
        InterviewSession session = this.getById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "面试会话不存在");
        }
        List<InterviewQuestion> questions = getQuestions(sessionId);
        return toVO(session, questions);
    }

    /**
     * 获取当前用户的面试历史
     */
    public List<InterviewVO> getMyInterviews() {
        Long userId = userService.getCurrentUserId();
        List<InterviewSession> sessions = this.list(
                new LambdaQueryWrapper<InterviewSession>()
                        .eq(InterviewSession::getUserId, userId)
                        .orderByDesc(InterviewSession::getCreatedAt)
        );
        return sessions.stream().map(s -> {
            List<InterviewQuestion> questions = getQuestions(s.getId());
            return toVO(s, questions);
        }).toList();
    }

    /**
     * 获取会话的所有题目
     */
    private List<InterviewQuestion> getQuestions(Long sessionId) {
        return questionMapper.selectList(
                new LambdaQueryWrapper<InterviewQuestion>()
                        .eq(InterviewQuestion::getSessionId, sessionId)
                        .orderByAsc(InterviewQuestion::getQuestionOrder)
        );
    }

    /**
     * 构建开始面试的 Prompt
     */
    private String buildStartPrompt(String resumeContent, String jobTitle, String company) {
        return interviewPromptTemplate.apply(
                Map.of(
                        "resume", resumeContent,
                        "jobTitle", jobTitle,
                        "company", company
                )
        ).text();
    }

    /**
     * 转换为 VO
     */
    private InterviewVO toVO(InterviewSession session, List<InterviewQuestion> questions) {
        InterviewVO vo = new InterviewVO();
        vo.setId(session.getId());
        vo.setResumeId(session.getResumeId());
        vo.setJobTitle(session.getJobTitle());
        vo.setCompany(session.getCompany());
        vo.setStatus(session.getStatus());
        vo.setQuestionCount(session.getQuestionCount());
        vo.setScore(session.getScore());
        vo.setFeedback(session.getFeedback());
        vo.setCreatedAt(session.getCreatedAt());

        if (questions != null) {
            vo.setQuestions(questions.stream().map(q -> {
                InterviewVO.QuestionVO qvo = new InterviewVO.QuestionVO();
                qvo.setId(q.getId());
                qvo.setQuestionOrder(q.getQuestionOrder());
                qvo.setQuestion(q.getQuestion());
                qvo.setAnswer(q.getAnswer());
                qvo.setScore(q.getScore());
                qvo.setFeedback(q.getFeedback());
                return qvo;
            }).toList());
        }

        return vo;
    }
}

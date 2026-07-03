-- ============================================================
-- ResumePilot 数据库表结构
-- 数据库：PostgreSQL
-- 说明：所有业务表都有 user_id 字段，关联到 user 表
-- ============================================================

-- 1. 用户表（已有，此处为参考）
-- CREATE TABLE "user" (
--     id          BIGSERIAL PRIMARY KEY,
--     email       VARCHAR(255) NOT NULL UNIQUE,
--     password    VARCHAR(255) NOT NULL,
--     created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
-- );

-- ============================================================
-- 2. 简历表
-- 存储原始简历和优化后的简历（通过 parent_id 关联）
-- ============================================================
CREATE TABLE resume (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    title           VARCHAR(200)    NOT NULL,
    original_name   VARCHAR(255)    NOT NULL,
    file_path       VARCHAR(500)    NOT NULL,
    file_size       BIGINT          NOT NULL DEFAULT 0,
    file_type       VARCHAR(20)     NOT NULL,
    content         TEXT,
    parsed_content  JSONB,
    version         INT             NOT NULL DEFAULT 1,
    parent_id       BIGINT,
    deleted         INT             NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE  resume                  IS '简历表';
COMMENT ON COLUMN resume.user_id          IS '所属用户 ID';
COMMENT ON COLUMN resume.title            IS '简历标题';
COMMENT ON COLUMN resume.original_name    IS '原始文件名';
COMMENT ON COLUMN resume.file_path        IS '文件存储路径';
COMMENT ON COLUMN resume.file_size        IS '文件大小（字节）';
COMMENT ON COLUMN resume.file_type        IS '文件类型（pdf/docx/txt）';
COMMENT ON COLUMN resume.content          IS '解析后的纯文本内容';
COMMENT ON COLUMN resume.parsed_content   IS '结构化解析结果（JSON）';
COMMENT ON COLUMN resume.version          IS '版本号（原始=1，每次优化+1）';
COMMENT ON COLUMN resume.parent_id        IS '父简历ID（优化版本指向原始简历）';
COMMENT ON COLUMN resume.deleted          IS '逻辑删除（0=正常 1=已删）';

CREATE INDEX idx_resume_user_id ON resume(user_id);

-- ============================================================
-- 3. ATS 评分结果表
-- 每次 ATS 分析产生一条记录
-- ============================================================
CREATE TABLE ats_result (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    resume_id       BIGINT          NOT NULL,
    score           NUMERIC(5,2)    NOT NULL DEFAULT 0,
    analysis        JSONB,
    recommendations JSONB,
    job_description TEXT,
    deleted         INT             NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE  ats_result                  IS 'ATS 评分结果表';
COMMENT ON COLUMN ats_result.user_id          IS '所属用户 ID';
COMMENT ON COLUMN ats_result.resume_id        IS '关联简历 ID';
COMMENT ON COLUMN ats_result.score            IS 'ATS 评分（0-100）';
COMMENT ON COLUMN ats_result.analysis         IS '分析详情（JSON：格式、关键词、结构等维度分数）';
COMMENT ON COLUMN ats_result.recommendations  IS '优化建议（JSON 数组）';
COMMENT ON COLUMN ats_result.job_description  IS '匹配的 JD 原文（可选）';

CREATE INDEX idx_ats_result_user_id   ON ats_result(user_id);
CREATE INDEX idx_ats_result_resume_id ON ats_result(resume_id);

-- ============================================================
-- 4. JD 匹配记录表
-- 每次 JD 匹配分析产生一条记录
-- ============================================================
CREATE TABLE jd_match (
    id                BIGSERIAL       PRIMARY KEY,
    user_id           BIGINT          NOT NULL,
    resume_id         BIGINT          NOT NULL,
    jd_title          VARCHAR(300),
    jd_content        TEXT            NOT NULL,
    match_score       NUMERIC(5,2)    NOT NULL DEFAULT 0,
    matched_keywords  JSONB,
    missing_keywords  JSONB,
    suggestions       JSONB,
    deleted           INT             NOT NULL DEFAULT 0,
    created_at        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE  jd_match                     IS 'JD 匹配记录表';
COMMENT ON COLUMN jd_match.user_id             IS '所属用户 ID';
COMMENT ON COLUMN jd_match.resume_id           IS '关联简历 ID';
COMMENT ON COLUMN jd_match.jd_title            IS '职位名称';
COMMENT ON COLUMN jd_match.jd_content          IS 'JD 原文';
COMMENT ON COLUMN jd_match.match_score         IS '匹配度评分（0-100）';
COMMENT ON COLUMN jd_match.matched_keywords    IS '匹配到的关键词（JSON 数组）';
COMMENT ON COLUMN jd_match.missing_keywords    IS '缺失的关键词（JSON 数组）';
COMMENT ON COLUMN jd_match.suggestions         IS '优化建议（JSON 数组）';

CREATE INDEX idx_jd_match_user_id   ON jd_match(user_id);
CREATE INDEX idx_jd_match_resume_id ON jd_match(resume_id);

-- ============================================================
-- 5. 模拟面试会话表
-- 一次模拟面试 = 一个会话，包含多道题目
-- ============================================================
CREATE TABLE interview_session (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    resume_id       BIGINT,
    jd_match_id     BIGINT,
    job_title       VARCHAR(300),
    company         VARCHAR(200),
    status          VARCHAR(20)     NOT NULL DEFAULT 'pending',
    question_count  INT             NOT NULL DEFAULT 0,
    score           NUMERIC(5,2),
    feedback        TEXT,
    deleted         INT             NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE  interview_session              IS '模拟面试会话表';
COMMENT ON COLUMN interview_session.user_id      IS '所属用户 ID';
COMMENT ON COLUMN interview_session.resume_id    IS '关联简历 ID';
COMMENT ON COLUMN interview_session.jd_match_id  IS '关联 JD 匹配 ID';
COMMENT ON COLUMN interview_session.job_title    IS '面试职位';
COMMENT ON COLUMN interview_session.company      IS '目标公司';
COMMENT ON COLUMN interview_session.status       IS '状态（pending/in_progress/completed）';
COMMENT ON COLUMN interview_session.question_count IS '总题数';
COMMENT ON COLUMN interview_session.score        IS '综合评分';
COMMENT ON COLUMN interview_session.feedback     IS '整体反馈';

CREATE INDEX idx_interview_session_user_id ON interview_session(user_id);

-- ============================================================
-- 6. 面试题目表
-- 每道面试题 + 用户回答 + AI 评分
-- ============================================================
CREATE TABLE interview_question (
    id              BIGSERIAL       PRIMARY KEY,
    session_id      BIGINT          NOT NULL,
    question_order  INT             NOT NULL,
    question        TEXT            NOT NULL,
    answer          TEXT,
    score           NUMERIC(5,2),
    feedback        JSONB,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE  interview_question                  IS '面试题目表';
COMMENT ON COLUMN interview_question.session_id       IS '所属面试会话 ID';
COMMENT ON COLUMN interview_question.question_order   IS '题目序号';
COMMENT ON COLUMN interview_question.question         IS '题目内容';
COMMENT ON COLUMN interview_question.answer           IS '用户回答';
COMMENT ON COLUMN interview_question.score            IS '该题评分';
COMMENT ON COLUMN interview_question.feedback         IS 'AI 反馈（JSON：优点、不足、参考答案）';

CREATE INDEX idx_interview_question_session_id ON interview_question(session_id);

-- ============================================================
-- 7. 聊天记录表
-- 所有 Agent 对话的持久化存储（Redis 做实时缓存，MySQL 做持久化）
-- ============================================================
CREATE TABLE chat_history (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    session_type    VARCHAR(30)     NOT NULL,
    session_id      BIGINT          NOT NULL,
    role            VARCHAR(20)     NOT NULL,
    content         TEXT            NOT NULL,
    agent_name      VARCHAR(50),
    deleted         INT             NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE  chat_history               IS '聊天记录表';
COMMENT ON COLUMN chat_history.user_id       IS '所属用户 ID';
COMMENT ON COLUMN chat_history.session_type  IS '会话类型（resume/ats/jd_match/interview）';
COMMENT ON COLUMN chat_history.session_id    IS '关联业务 ID（简历ID/ATS结果ID等）';
COMMENT ON COLUMN chat_history.role          IS '角色（user/assistant/system/tool）';
COMMENT ON COLUMN chat_history.content       IS '消息内容';
COMMENT ON COLUMN chat_history.agent_name    IS 'Agent 名称（ResumeOptimizer/ATS/JDMatcher/Interview）';

CREATE INDEX idx_chat_history_user_id    ON chat_history(user_id);
CREATE INDEX idx_chat_history_session    ON chat_history(session_type, session_id);

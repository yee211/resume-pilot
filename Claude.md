# CLAUDE.md

> 本文件用于约束 Claude Code 的开发行为。

---

# 你的身份（Role）

你是一名拥有 10 年经验的 Java 架构师，同时也是 AI Agent 工程师。

你需要开发一个**企业级项目**，而不是 Demo。

所有代码必须符合 Spring Boot 企业开发规范。

项目名称：

ResumePilot

技术路线：

Spring Boot + LangChain4j + Vue3

---

# 开发原则

所有代码必须遵守：

* 高内聚
* 低耦合
* 易扩展
* 易维护
* 面向接口开发
* SOLID 原则

不要为了实现功能牺牲代码质量。

---

# 项目目标

这是一个 AI 求职 Agent。

不是聊天机器人。

所有 AI 能力都应围绕：

* 简历解析
* ATS 分析
* JD 匹配
* 简历优化
* 模拟面试

展开。

---

# 技术要求

后端：

* Java 21
* Spring Boot 3
* Maven
* MyBatis Plus
* Redis
* MySQL
* LangChain4j

前端：

* Vue3
* TypeScript
* Element Plus

禁止：

不要引入没有必要的新框架。

---

# LangChain4j 规范

必须使用：

* AiServices
* ChatModel
* ChatMemory
* PromptTemplate
* Tool Calling
* Streaming

不要自己封装 LLM HTTP 请求。

不要绕过 LangChain4j。

所有 AI 能力必须基于 LangChain4j。

---

# Agent 规范

一个功能对应一个 Agent。

例如：

ResumeParserAgent

ATSAgent

JDMatcherAgent

ResumeOptimizerAgent

InterviewAgent

不要写一个万能 Agent。

---

# Prompt 规范

Prompt 必须独立维护。

例如：

```id="uxm74w"
prompt/

ats.md

resume.md

interview.md

jd.md
```

禁止：

把 Prompt 写进 Java 代码。

---

# Tool Calling

所有工具必须放到：

```id="6drnfy"
ai/tools/
```

例如：

ResumeTool

PdfTool

DocTool

HistoryTool

KnowledgeTool

一个 Tool 只负责一件事。

---

# ChatMemory

必须支持：

用户连续修改简历。

例如：

用户：

> 再短一点

Agent 应理解：

修改的是上一版。

不要重新生成新的主题。

---

# RAG

必须预留：

```id="5tmc1j"
rag/

documents/

embedding/

vector/
```

不要把知识写死。

知识必须支持后续导入。

---

# Controller

Controller 只允许：

接收请求

返回响应

禁止：

业务逻辑

AI 调用

数据库操作

---

# Service

Service：

负责业务逻辑。

例如：

ResumeService

ATSService

InterviewService

不要让 Controller 调 Service 之外的任何对象。

---

# Repository

Repository：

只负责数据库。

不要写业务逻辑。

---

# DTO

请求：

必须使用 DTO。

返回：

必须使用 VO。

不要直接返回 Entity。

---

# Entity

Entity：

只负责数据库映射。

不要添加业务逻辑。

---

# Exception

统一异常处理。

使用：

GlobalExceptionHandler。

不要：

try catch 到处都是。

---

# Response

统一返回：

```json id="ux0qcw"
{
  "code":200,
  "message":"success",
  "data":{}
}
```

不要返回杂乱格式。

---

# 日志

使用：

Slf4j。

禁止：

System.out.println()

---

# 配置

所有配置：

放 application.yml。

敏感信息：

放环境变量。

不要把 API Key 提交 Git。

---

# Streaming

所有 AI 对话：

使用 SSE。

不要轮询。

---

# 前端

使用：

Composition API。

Axios。

Pinia。

不要：

Options API。

---

# UI 风格

整体风格：

现代

极简

AI 产品

类似：

ChatGPT

Claude

Cursor

不要：

后台管理系统风格。

---

# 数据库

优先：

MySQL。

缓存：

Redis。

聊天记录：

Redis + MySQL。

---

# Docker

项目必须支持：

Docker Compose。

一键启动：

MySQL

Redis

Backend

Frontend

---

# Git Commit

Commit 示例：

```id="hm3o3m"
feat: 完成ATS评分Agent

feat: 新增JD匹配

refactor: 重构ResumeService

fix: 修复PDF解析异常
```

---

# 代码风格

每个类：

不要超过 300 行。

每个方法：

尽量控制在 50 行以内。

保持命名清晰。

不要缩写。

---

# 注释

公共方法：

必须写 JavaDoc。

复杂逻辑：

必须解释。

不要写无意义注释。

---

# 输出要求

Claude 每完成一个功能：

必须：

1. 修改 TODO
2. 更新 README
3. 更新接口文档
4. 保持代码可以运行

不要一次生成几千行代码。

优先：

小步提交。

每完成一个模块后等待确认。

---

# 开发流程

每开发一个模块：

1. 分析需求

2. 设计接口

3. 创建数据库

4. 编写 Entity

5. 编写 DTO

6. 编写 Mapper

7. 编写 Service

8. 编写 Controller

9. 编写 AI Agent

10. 编写 Prompt

11. 编写 Tool

12. 编写测试

13. 更新文档

完成后再开发下一模块。

不要跳步骤。

---

# 最终目标

最终代码应达到：

* 企业级代码质量
* 可直接部署
* 可用于个人简历展示
* 易于后续扩展多 Agent、MCP、RAG 等高级能力

任何新增功能，都必须遵循以上规范。

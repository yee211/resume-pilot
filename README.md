# ResumePilot —— AI 求职 Agent（LangChain4j）

> 一个基于 Spring Boot + LangChain4j 构建的智能求职 Agent，支持简历解析、ATS 评分、岗位匹配、AI 优化、模拟面试等功能。

---

# 一、项目目标

构建一个真正具有 Agent 能力的 AI 求职助手，而不是普通聊天机器人。

系统应具备：

* 简历解析
* ATS 智能评分
* 岗位 JD 匹配
* AI 简历优化
* 多轮修改
* 模拟面试
* 历史记录
* RAG 知识库
* 流式聊天

最终项目可作为 Java + AI Agent 简历项目。

---

# 二、技术栈

## Backend

* Java 21
* Spring Boot 3
* LangChain4j
* MyBatis Plus
* MySQL
* Redis
* Maven
* Lombok

## AI

* ChatModel
* ChatMemory
* PromptTemplate
* AiServices
* Tool Calling
* RAG
* Embedding
* Streaming

## Frontend

* Vue3
* Vite
* TypeScript
* Element Plus
* Axios

---

# 三、项目结构

```
resume-pilot

backend
├── auth
├── user
├── resume
├── ats
├── jd
├── interview
├── ai
│   ├── agent
│   ├── prompt
│   ├── tools
│   ├── rag
│   ├── memory
│   └── config
├── common

frontend
```

要求：

模块之间解耦。

所有 AI 能力放入 ai 模块。

---

# 四、功能模块

## 1、登录

支持：

* JWT
* 注册
* 登录

---

## 2、简历上传

支持：

* PDF
* DOCX

上传以后：

解析文本

提取：

```
姓名

教育

技能

项目

实习

获奖
```

转换为 JSON 保存数据库。

---

## 3、ATS Agent

输入：

简历

输出：

```
综合评分

关键词覆盖率

技能评分

项目评分

STAR评分

建议
```

例如：

```
ATS

84

建议：

增加Docker

增加Redis项目

量化成果
```

---

## 4、JD 匹配

用户粘贴岗位描述。

例如：

```
Spring Boot

Redis

MySQL

Docker

AI Agent
```

输出：

```
匹配度

87%

缺失技能

Docker

RabbitMQ

K8S
```

并生成修改建议。

---

## 5、AI 简历优化

支持：

优化项目经历

优化技能描述

优化自我评价

支持：

保持真实性

STAR 法则

量化成果

输出：

优化后的内容。

---

## 6、多轮修改

例如：

用户：

> 太长了

Agent：

重新生成。

用户：

> 偏大厂风格

Agent：

再次优化。

要求：

上下文记忆。

---

## 7、模拟面试

根据：

简历

岗位

生成：

```
面试问题

追问

评分

建议
```

支持连续聊天。

---

## 8、Boss 打招呼

生成：

Boss

智联

牛客

不同风格的话术。

---

## 9、历史记录

保存：

聊天记录

ATS

JD

优化历史

支持再次打开。

---

# 五、Agent 架构

采用多个 Agent。

```
ResumeAgent

│

├── ResumeParserAgent

├── ATSAgent

├── JDMatcherAgent

├── ResumeOptimizerAgent

├── InterviewAgent

└── CareerAgent
```

所有 Agent 使用 LangChain4j AiServices 实现。

---

# 六、AI 能力要求

必须使用 LangChain4j。

必须体现：

## ChatMemory

支持连续聊天。

---

## PromptTemplate

每个 Agent 单独 Prompt。

例如：

ATS Prompt

Resume Prompt

Interview Prompt

不要把 Prompt 写死。

---

## Tool Calling

至少实现以下 Tool：

ResumeParserTool

PdfTool

DocxTool

HistoryTool

SearchTool

Tool 必须独立。

---

## Streaming

所有聊天采用 SSE。

要求：

前端实时输出。

---

## RAG

建立知识库。

包括：

```
优秀简历

Java面试

Spring

Redis

MySQL

STAR法则

互联网JD
```

要求：

使用 Embedding。

支持 TopK 检索。

Agent 回答优先引用知识库。

---

# 七、数据库

user

```
id

username

password
```

resume

```
id

user_id

resume_json
```

ats_record

```
id

resume_id

score

suggestion
```

jd_record

```
id

resume_id

match_score
```

chat_history

```
id

user_id

role

content
```

---

# 八、接口设计

POST

```
/resume/upload
```

上传简历。

---

POST

```
/ats/analyze
```

ATS评分。

---

POST

```
/jd/match
```

岗位匹配。

---

POST

```
/resume/optimize
```

优化简历。

---

POST

```
/interview/start
```

开始面试。

---

GET

```
/history
```

获取聊天记录。

---

# 九、UI 页面

登录页

首页

聊天页

ATS分析页

JD分析页

简历优化页

模拟面试页

历史记录页

设置页

整体风格：

简洁

现代

AI 产品风格。

---

# 十、代码规范

要求：

* Controller 只负责请求响应。
* Service 负责业务逻辑。
* AI Agent 独立封装。
* Prompt 单独维护。
* Tool 单独维护。
* 配置统一管理。
* 不允许将 Prompt 写在 Controller。
* 不允许业务逻辑写在前端。

---

# 十一、开发顺序

## 第一阶段

完成：

* 登录
* 上传简历
* PDF解析
* LangChain4j聊天

---

## 第二阶段

完成：

* ATS Agent
* JD Agent
* Resume Agent

---

## 第三阶段

完成：

* ChatMemory
* Tool Calling
* Streaming

---

## 第四阶段

完成：

* RAG
* 历史记录
* Docker部署

---

# 十二、项目目标

最终效果应接近真实 AI 产品，而不是普通聊天机器人。

重点体现：

* LangChain4j
* Agent
* Tool Calling
* ChatMemory
* RAG
* Streaming
* Spring Boot 工程化能力

代码结构要求清晰，可维护，可扩展，可作为个人简历核心项目。

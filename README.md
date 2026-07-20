<div align="center">
  <p>中文 | <a href="./README-en.md">English</a></p>
  <h1>Phoenix — 企业级智能体管理平台</h1>
  <p>
    <strong>基于 <a href="https://github.com/alibaba/spring-ai-alibaba" target="_blank">Spring AI Alibaba 2.0.0-M1.1</a> Graph 的企业级 AI Agent 管理与分析引擎</strong>
  </p>
  <p>
    NL2SQL 智能报表 | Python 深度分析 | 自定义智能体 | MCP 服务器 | RAG 增强 | Vben5 管理后台
  </p>
  <p>
    <img src="https://img.shields.io/badge/Spring%20Boot-4.0.0-blue" alt="Spring Boot">
    <img src="https://img.shields.io/badge/Java-21-orange" alt="Java">
    <img src="https://img.shields.io/badge/MyBatis--Flex-1.11.7-green" alt="MyBatis-Flex">
    <img src="https://img.shields.io/badge/Vben-5-blueviolet" alt="Vben5">
    <img src="https://img.shields.io/badge/License-Apache%202.0-red" alt="License">
  </p>
  <p>
    <a href="#-项目简介">项目简介</a> •
    <a href="#-核心特性">核心特性</a> •
    <a href="#-模块架构">模块架构</a> •
    <a href="#-快速开始">快速开始</a> •
    <a href="#-前端说明">前端说明</a> •
    <a href="#-数据库支持">数据库支持</a> •
    <a href="#-文档导航">文档导航</a>
  </p>
</div>

<br/>

## 📖 项目简介

**Phoenix** 是一个基于 **Spring AI Alibaba Graph** 打造的企业级智能体（AI Agent）管理与数据分析平台。平台分为两大核心模块：

- **phoenix-data**（智能报表引擎）：以 **StateGraph** 状态机驱动，将自然语言转换为可执行的 SQL 与 Python 分析流程，超越传统 Text-to-SQL 工具，进化为具备多轮对话、意图识别、知识增强、人工反馈的全链路 AI 数据分析师。
- **phoenix-agent**（自定义智能体框架）：提供 React Agent、Workflow Graph、会话管理、长期记忆、用户画像、MCP 工具等完整的智能体开发能力，支持快速构建和发布企业级 AI 智能体。

系统采用 **Vben 5** 现代前端框架，实现前台（数据分析师交互）与后台（智能体管理、数据源配置、用户权限）的统一管理，提供开箱即用的企业级管理界面。

技术上，系统采用高度可扩展的多模块架构设计，**全面兼容 OpenAI 接口规范**的对话模型与 Embedding 模型，支持**灵活挂载多种向量数据库**，并提供 MySQL、PostgreSQL、Oracle、SQL Server、H2、Hive、达梦等主流数据库的即开即用支持。同时原生支持 **MCP 协议**，可无缝集成 Claude Desktop 等 MCP 生态工具。

## ✨ 核心特性

| 特性 | 说明 |
| :--- | :--- |
| **自定义智能体** | 基于 React Agent / Workflow Graph 框架，可快速构建和发布企业级 AI 智能体。 |
| **StateGraph 驱动** | 基于状态图的 NL2SQL 工作流，支持意图识别 → 证据召回 → Schema 召回 → 规划 → 执行的全链路编排。 |
| **Python 深度分析** | 内置 Docker/Local Python 执行器，自动生成并执行 Python 代码进行统计分析、机器学习预测。 |
| **智能报告生成** | 分析结果自动汇总为包含 ECharts 图表的 HTML/Markdown 报告，所见即所得。 |
| **人工反馈机制** | Human-in-the-Loop 机制，支持用户在计划生成阶段进行干预和调整。 |
| **RAG 检索增强** | 混合检索策略（向量 + 关键词 + 融合排序），支持业务元数据与术语库的语义检索。 |
| **多模型调度** | 内置模型注册表，支持运行时动态切换不同的 LLM 和 Embedding 模型。 |
| **MCP 服务器** | 遵循 MCP 协议，支持作为 Tool Server 对外提供 NL2SQL 和智能体管理能力。 |
| **多数据源支持** | 开箱即用支持 MySQL、PostgreSQL、Oracle、SQL Server、H2、Hive、达梦。 |
| **智能体记忆** | 支持短期记忆（Redis 检查点）、长期记忆（用户画像 + 事实记忆 + 向量语义检索）。 |
| **多轮对话** | 可配置的多轮对话上下文管理（支持历史最多 5 轮），保持对话连贯性。 |
| **RBAC 权限** | 完善的用户-角色-部门-公司四级权限体系，细粒度 API Key 管理。 |
| **Vben5 管理后台** | 基于 Vue 3 + Vben 5 的现代化管理界面，统一管理智能体、数据源、模型、用户权限。 |
| **多租户与三方集成** | 支持租户隔离以及钉钉、飞书、企业微信集成。 |
| **全链路追踪** | 集成 OpenTelemetry → Langfuse，LLM 调用全过程可观测。 |

## 🏗️ 模块架构

```
phoenix/
├── phoenix-parent          # BOM 依赖管理与插件管理
│
├── phoenix-data            # 智能报表引擎 — 入口、Graph 工作流、NL2SQL
│   ├── phoenix-data-api
│   ├── phoenix-data-core
│   └── phoenix-data-rest
│
├── phoenix-agent           # 自定义智能体框架 — React Agent、会话、记忆、工作流
│   ├── phoenix-agent-api   #   接口定义、DTO、配置属性
│   ├── phoenix-agent-core  #   AgentManager、ReactAgent、Workflow、记忆、工具
│   └── phoenix-agent-rest  #   REST & SSE 端点
│
├── phoenix-admin           # 统一部署入口（端口 8066）
│   ├── phoenix-admin-api
│   ├── phoenix-admin-core
│   ├── phoenix-admin-manager  # @SpringBootApplication，打包所有模块
│   └── phoenix-admin-rest
│
├── phoenix-privilege       # 权限认证 — RBAC、用户、角色、ACL
│   ├── phoenix-privilege-api
│   ├── phoenix-privilege-core
│   └── phoenix-privilege-rest
│
├── phoenix-platform        # 平台管理 — 租户、组织、三方账号
│   ├── phoenix-platform-api
│   ├── phoenix-platform-core
│   └── phoenix-platform-rest
│
├── phoenix-common          # 公共模块 — 基础模型、异常、SDK 封装
│   ├── phoenix-common-api
│   ├── phoenix-common-core
│   └── phoenix-common-rest
│
├── phoenix-tool            # 工具模块 — 返回值封装、SQL 安全校验
├── phoenix-codegen         # MyBatis-Flex 代码生成器
├── phoenix-flink           # Apache Flink 大数据集成
│   ├── phoenix-flink-api
│   ├── phoenix-flink-core
│   └── phoenix-flink-server
├── phoenix-rag             # 检索增强生成
│   ├── phoenix-rag-api
│   ├── phoenix-rag-core
│   └── phoenix-rag-rest
└── phoenix-kg              # 知识图谱
    ├── phoenix-kg-api
    ├── phoenix-kg-core
    └── phoenix-kg-rest
```

### phoenix-data: NL2SQL 工作流

Phoenix 的核心是 StateGraph 驱动的 NL2SQL 工作流，包含以下节点：

`IntentRecognition` → `EvidenceRecall` → `QueryEnhance` → `SchemaRecall` → `TableRelation` → `FeasibilityAssessment` → `Planner` → `PlanExecutor` → [`SqlGenerate→SemanticConsistency→SqlExecute` | `PythonGenerate→PythonExecute→PythonAnalyze` | `ReportGenerator` | `HumanFeedback`]

### phoenix-agent: 自定义智能体开发

Phoenix 提供完整的智能体开发框架，支持两种范式：

| 范式 | 说明 | 适用场景 |
|:---|:---|:---|
| **React Agent** | LLM 驱动的 ReAct 模式（推理-行动-观察循环），自动选择工具 | 流程分析、知识问答、文档检索等工具型智能体 |
| **Workflow Graph** | 预定义的 StateGraph 工作流，节点间显式编排 | 有固定业务步骤的复杂任务（如审批、多步分析） |

**智能体核心能力：**

- **会话管理**：完整的多轮对话 Session 管理，自动保存对话历史
- **长期记忆**：支持用户画像（Profile）、事实记忆（Fact）、向量语义检索
- **短期记忆**：基于 RedisSaver 的状态检查点
- **工具系统**：基于 `@Tool` 注解的工具注册，支持 SQL 查询、知识检索、外部 API
- **MCP 集成**：可对外暴露 MCP 工具，接入 Claude Desktop 等 MCP 客户端
- **Human-in-the-Loop**：支持工具调用的人工审核与干预
- **用量追踪**：按用户按智能体的调用次数统计与限流

## 🌐 前端说明

Phoenix 前端基于 **Vben 5**（Vue 3 + TypeScript + Vite）构建，实现前台分析交互与后台统一管理的现代化 SPA。

| 功能区域 | 说明 |
|:---|:---|
| **前台（数据分析）** | 对话式自然语言查询、结果图表展示、报告预览、多轮交互 |
| **后台（智能体管理）** | 智能体创建/配置、数据源管理、模型配置、知识库管理、Prompt 优化 |
| **系统管理** | 用户管理、角色权限、API Key、租户配置、操作日志 |

前端源码位于独立仓库，与本后端通过 REST + SSE 接口通信。

## 🚀 快速开始

### 环境要求
- JDK 21+
- Maven 3.9+（需在 PATH 中）
- PostgreSQL 14+（含 pgvector 插件）
- Redis 7+
- （可选）Docker — Python 深度分析

### 启动服务

```bash
# 1. 克隆项目
git clone https://github.com/liu463805737-collab/Phoenix-Agent-Java.git
或
git clone https://gitee.com/lwj/phoenix-agent-java.git
cd phoenix

# 2. 导入数据库（PostgreSQL）
## 注意PostgreSQL需要安装支持向量插件（pgvector）
psql -U your_user -d your_db < sql/all_data.sql

# 3. 构建项目（跳过格式检查）
mvn clean install -Dspring-javaformat.skip=true


# 4. 启动服务

# 启动统一管理平台（端口 8066，推荐）
mvn spring-boot:run -pl phoenix-admin/phoenix-admin-manager
```

### 环境变量配置

| 变量 | 说明 |
| :--- | :--- |
| `SPRING_AI_DASHSCOPE_API_KEY` | DashScope/通义千问 API Key |
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL（默认 `jdbc:postgresql://127.0.0.1:5432/phoenix`） |
| `SPRING_DATASOURCE_USERNAME` | 数据库用户名 |
| `SPRING_DATASOURCE_PASSWORD` | 数据库密码 |
| `SPRING_DATA_REDIS_HOST` | Redis 地址（默认 `127.0.0.1`） |


## 🗄️ 数据库支持

### 目标数据源（查询与分析）

| 数据库 | 驱动 |
|--------|------|
| MySQL | mysql-connector-j |
| PostgreSQL | postgresql 42.4.1 |
| Oracle | ojdbc8 |
| SQL Server | mssql-jdbc |
| H2 | h2 2.3.232 |
| Hive | hive-jdbc 3.1.3 |
| 达梦 DM | DmJdbcDriver18 |

### 向量存储

| 存储 | 说明 |
|------|------|
| PostgreSQL pgvector | 默认向量库（HNSW 索引，余弦距离，512 维） |
| Elasticsearch | 可选的向量存储方案 |

### 应用存储

- **PostgreSQL** — 业务数据存储
- **Redis** — Graph 状态持久化（RedisSaver）、缓存、分布式锁

## 🔧 开发命令

| 命令 | 说明 |
|------|------|
| `mvn clean install` | 全量构建（含格式检查） |
| `mvn clean install -Dspring-javaformat.skip=true` | 跳过格式检查构建 |
| `mvn spring-javaformat:validate` | 格式检查 |
| `mvn spring-javaformat:apply` | 格式修复 |
| `mvn spring-boot:run -pl phoenix-admin/phoenix-admin-manager` | 启动统一管理平台（端口 8066） |

> Maven settings 文件：`/Users/liuwenjun/java/doc/maven-setting/dragon-settings.xml`

## 📚 文档导航

| 文档                                  | 内容                                              |
|:------------------------------------|:------------------------------------------------|
| [架构设计](docs/ARCHITECTURE.md)        | 系统分层架构、StateGraph 工作流设计、核心时序图                   |
| [快速开始](docs/QUICK_START.md)         | 环境要求、数据库导入、基础配置、系统初体验                           |
| [开发者指南](docs/DEVELOPER_GUIDE.md)    | 开发环境搭建、配置手册、自定义智能体开发                            |
| [高级功能](docs/ADVANCED_FEATURES.md)   | API Key 管理、MCP 配置、混合检索、Python 执行器、Langfuse 可观测性 |
| [知识配置最佳实践](docs/KNOWLEDGE_USAGE.md) | 语义模型、业务知识、智能体知识配置                               |
| [权限管理](docs/PRIVILEGE.md)           | RBAC的权限管理                                       |
| [组织管理](docs/RNG.md)                 | 人员组织管理                                          |
| [前台管理](docs/FRNT.md)                | 前台账号管理                                          |
| [基础管理](docs/FRNT.md)                | 配置钉钉、企微、飞书自建应用信息                                |

## 🤝 加入社区 & 贡献

- **贡献指南**：欢迎提交 Issue 和 PR
- **开发规范**：代码需通过 `spring-javaformat:validate` 格式检查
## 💬 欢迎交流

感兴趣的朋友欢迎加微信一起交流探讨～

<div align="left">
  <img src="img/weixin.png" alt="微信" width="300" height="300">
  <img src="img/qq.png" alt="微信" width="300" height="300">
</div>

## 📄 许可证

本项目采用 Apache License 2.0 许可证。

---

<div align="center">
    Made with ❤️ by Phoenix Team
</div>

<div align="center">
  <p><a href="./README.md">中文</a> | English</p>
  <h1>Phoenix — Enterprise AI Agent Management Platform</h1>
  <p>
    <strong>Enterprise-grade AI Agent Management & Analytics Engine powered by <a href="https://github.com/alibaba/spring-ai-alibaba" target="_blank">Spring AI Alibaba</a> Graph</strong>
  </p>

  <p>
    NL2SQL Smart Reports | Python Deep Analysis | Custom Agents | MCP Server | RAG | Vben5 Admin UI
  </p>

  <p>
    <img src="https://img.shields.io/badge/Spring%20Boot-4.0.0-blue" alt="Spring Boot">
    <img src="https://img.shields.io/badge/Java-21-orange" alt="Java">
    <img src="https://img.shields.io/badge/MyBatis--Flex-1.11.7-green" alt="MyBatis-Flex">
    <img src="https://img.shields.io/badge/Vben-5-blueviolet" alt="Vben5">
    <img src="https://img.shields.io/badge/License-Apache%202.0-red" alt="License">
  </p>

  <p>
    <a href="#-introduction">Introduction</a> •
    <a href="#-features">Features</a> •
    <a href="#-module-architecture">Architecture</a> •
    <a href="#-quick-start">Quick Start</a> •
    <a href="#-frontend">Frontend</a> •
    <a href="#-database-support">Databases</a> •
    <a href="#-documentation">Documentation</a>
  </p>
</div>

<br/>

## 📖 Introduction

**Phoenix** is an enterprise-grade **AI Agent Management and Data Analytics Platform** built on **Spring AI Alibaba Graph**. It consists of two core modules:

- **phoenix-data** (Smart Report Engine): StateGraph-driven NL2SQL workflow that transforms natural language into executable SQL and Python analysis pipelines. It evolves beyond traditional Text-to-SQL tools into a full-chain AI data analyst with multi-turn conversation, intent recognition, knowledge enhancement, and human feedback.
- **phoenix-agent** (Custom Agent Framework): A complete agent development framework providing React Agent, Workflow Graph, session management, long-term memory, user profiling, and MCP tool integration for building and deploying enterprise AI agents.

The frontend is built with **Vben 5** (Vue 3 + TypeScript + Vite), providing a unified management interface for both data analyst interactions and back-office administration.

Technically, the system features a highly extensible multi-module architecture with **full OpenAI API compatibility** for both chat and embedding models, **pluggable vector store support**, and out-of-the-box support for MySQL, PostgreSQL, Oracle, SQL Server, H2, Hive, and Dameng databases. Native **MCP protocol** support enables seamless integration with Claude Desktop and other MCP ecosystem tools.

## ✨ Features

| Feature | Description |
| :--- | :--- |
| **Custom Agents** | Build and deploy enterprise AI agents with React Agent / Workflow Graph framework. |
| **StateGraph Workflow** | NL2SQL workflow with full pipeline: Intent → Evidence → Schema → Planner → Execute. |
| **Python Deep Analysis** | Docker/Local Python executor for statistical analysis and ML predictions. |
| **Smart Reports** | Auto-generated HTML/Markdown reports with ECharts visualizations. |
| **Human Feedback** | Human-in-the-Loop intervention during plan generation. |
| **RAG Enhancement** | Hybrid retrieval (vector + keyword + fusion) with business metadata and termbase. |
| **Multi-Model Routing** | Runtime dynamic switching between different LLM and Embedding models. |
| **MCP Server** | Expose NL2SQL and agent management as MCP tools for Claude Desktop etc. |
| **Multi-Datasource** | MySQL, PostgreSQL, Oracle, SQL Server, H2, Hive, Dameng. |
| **Agent Memory** | Short-term (Redis checkpoints) + long-term (profile, facts, vector search). |
| **Multi-Turn Dialog** | Configurable context management (up to 5 turns). |
| **RBAC** | User-Role-Department-Company 4-tier permissions with API Key management. |
| **Vben5 Admin UI** | Modern management interface for agents, datasources, models, and users. |
| **Multi-Tenant** | Tenant isolation with DingTalk, Feishu, WeCom integration. |
| **LLM Tracing** | OpenTelemetry → Langfuse for full observability. |

## 🏗️ Module Architecture

```
phoenix/
├── phoenix-parent          # BOM dependency & plugin management
│
├── phoenix-data            # Smart Report Engine — Graph workflow, NL2SQL
│   ├── phoenix-data-api
│   ├── phoenix-data-core
│   └── phoenix-data-rest
│
├── phoenix-agent           # Custom Agent Framework — React Agent, sessions, memory, workflow
│   ├── phoenix-agent-api   #   DTOs, config properties, interfaces
│   ├── phoenix-agent-core  #   AgentManager, ReactAgent, Workflow, memory, tools
│   └── phoenix-agent-rest  #   REST & SSE endpoints
│
├── phoenix-admin           # Unified deployment entry (port 8066)
│   ├── phoenix-admin-api
│   ├── phoenix-admin-core
│   ├── phoenix-admin-manager  # @SpringBootApplication, aggregates all modules
│   └── phoenix-admin-rest
│
├── phoenix-privilege       # Auth — RBAC, users, roles, ACL
│   ├── phoenix-privilege-api
│   ├── phoenix-privilege-core
│   └── phoenix-privilege-rest
│
├── phoenix-platform        # Platform — tenants, orgs, third-party accounts
│   ├── phoenix-platform-api
│   ├── phoenix-platform-core
│   └── phoenix-platform-rest
│
├── phoenix-common          # Shared — base models, exceptions, SDK
│   ├── phoenix-common-api
│   ├── phoenix-common-core
│   └── phoenix-common-rest
│
├── phoenix-tool            # Utilities — response wrapper, SQL security
├── phoenix-codegen         # MyBatis-Flex code generator
├── phoenix-flink           # Apache Flink integration
│   ├── phoenix-flink-api
│   ├── phoenix-flink-core
│   └── phoenix-flink-server
├── phoenix-rag             # Retrieval Augmented Generation
│   ├── phoenix-rag-api
│   ├── phoenix-rag-core
│   └── phoenix-rag-rest
└── phoenix-kg              # Knowledge Graph
    ├── phoenix-kg-api
    ├── phoenix-kg-core
    └── phoenix-kg-rest
```

### phoenix-data: NL2SQL Workflow

`IntentRecognition` → `EvidenceRecall` → `QueryEnhance` → `SchemaRecall` → `TableRelation` → `FeasibilityAssessment` → `Planner` → `PlanExecutor` → [`SqlGenerate→SemanticConsistency→SqlExecute` | `PythonGenerate→PythonExecute→PythonAnalyze` | `ReportGenerator` | `HumanFeedback`]

### phoenix-agent: Custom Agent Development

Phoenix provides a complete agent development framework supporting two paradigms:

| Paradigm | Description | Use Cases |
|:---|:---|:---|
| **React Agent** | LLM-driven ReAct loop (Reason-Act-Observe), auto-tool selection | Process analysis, Q&A, document retrieval |
| **Workflow Graph** | Predefined StateGraph with explicit node orchestration | Fixed-step tasks (approvals, multi-step analysis) |

**Agent Core Capabilities:**

- **Session Management**: Full multi-turn session management with auto-saved history
- **Long-term Memory**: User profiles, factual memory, vector semantic retrieval
- **Short-term Memory**: RedisSaver state checkpoints
- **Tool System**: `@Tool`-annotated tool registration for SQL, knowledge retrieval, external APIs
- **MCP Integration**: Expose tools via MCP protocol for Claude Desktop and others
- **Human-in-the-Loop**: Tool call review and intervention
- **Usage Tracking**: Per-user per-agent invocation counting and rate limiting

## 🌐 Frontend

Phoenix frontend is built with **Vben 5** (Vue 3 + TypeScript + Vite), providing a modern SPA for unified analytics and administration.

| Area | Description |
|:---|:---|
| **Analytics UI** | Conversational natural language queries, chart visualization, report preview |
| **Admin Console** | Agent config, datasource management, model config, knowledge base, prompt optimization |
| **System Admin** | User management, RBAC, API Keys, tenant config, audit logs |

The frontend source code resides in a separate repository and communicates with the backend via REST + SSE APIs.

## 🚀 Quick Start

### Prerequisites
- JDK 21+
- Maven 3.9+
- PostgreSQL 14+ (with pgvector extension)
- Redis 7+
- (Optional) Docker — Python execution

### Start Services

```bash
# 1. Clone
git clone <repo-url>
cd phoenix

# 2. Import database schema
psql -U your_user -d your_db < sql/all_schema.sql

# 3. Build (skip format check)
mvn clean install -Dspring-javaformat.skip=true

# 4. Launch (choose one)

# Option A: Smart Report Engine (port 8080)
mvn spring-boot:run -pl phoenix-data

# Option B: Unified Admin Platform (port 8066, recommended)
mvn spring-boot:run -pl phoenix-admin/phoenix-admin-manager
```

### Environment Variables

| Variable | Description |
| :--- | :--- |
| `SPRING_AI_DASHSCOPE_API_KEY` | DashScope/Tongyi Qianwen API Key |
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | Database password |
| `SPRING_DATA_REDIS_HOST` | Redis host |

### Health Check

```bash
curl http://localhost:8080/echo/ok
```

### Streaming Query

```bash
curl -N "http://localhost:8080/api/stream/search?agentId=1&query=上个月销售额是多少"
```

## 🗄️ Database Support

### Target Datasources

| Database | Driver |
|----------|--------|
| MySQL | mysql-connector-j |
| PostgreSQL | postgresql 42.4.1 |
| Oracle | ojdbc8 |
| SQL Server | mssql-jdbc |
| H2 | h2 2.3.232 |
| Hive | hive-jdbc 3.1.3 |
| Dameng DM | DmJdbcDriver18 |

### Vector Stores

| Store | Notes |
|-------|-------|
| PostgreSQL pgvector | Default (HNSW index, cosine distance, 512d) |
| Elasticsearch | Optional |

## 🔧 Dev Commands

| Command | Description |
|---------|-------------|
| `mvn clean install` | Full build with format check |
| `mvn clean install -Dspring-javaformat.skip=true` | Build skipping format check |
| `mvn spring-javaformat:validate` | Format validation |
| `mvn spring-javaformat:apply` | Auto-format |
| `mvn spring-boot:run -pl phoenix-data` | Start report engine (port 8080) |
| `mvn spring-boot:run -pl phoenix-admin/phoenix-admin-manager` | Start admin platform (port 8066) |

## 📚 Documentation

| Doc | Description |
| :--- | :--- |
| [Architecture](docs/ARCHITECTURE.md) | System architecture, StateGraph workflow, sequence diagrams |
| [Quick Start](docs/QUICK_START.md) | Environment setup, DB import, configuration, first run |
| [Developer Guide](docs/DEVELOPER_GUIDE.md) | Dev environment, config reference, custom agent development |
| [Advanced Features](docs/ADVANCED_FEATURES.md) | API Key, MCP, hybrid search, Python executor, Langfuse |
| [Knowledge Best Practices](docs/KNOWLEDGE_USAGE.md) | Semantic model, business knowledge, agent knowledge config |

## 🤝 Contributing

Pull requests and issues are welcome. Code must pass `mvn spring-javaformat:validate`.

## 📄 License

Apache License 2.0

---

<div align="center">
    Made with ❤️ by Phoenix Team
</div>

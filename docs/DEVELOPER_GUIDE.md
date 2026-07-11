中文 | [English](./DEVELOPER_GUIDE-en.md)

> ⚠️ 英文版文档正在翻译中，当前请参考 [English README](../README.md)

# 开发者文档

欢迎参与 Phoenix 项目的开发！本文档将帮助您了解如何为项目做出贡献。

## 🚀 开发环境搭建

### 前置要求

- **JDK**: 21 或更高版本
- **Maven**: 3.9 或更高版本（需在 `PATH` 中）
- **PostgreSQL**: 14 或更高版本（含 pgvector 插件）
- **Redis**: 7 或更高版本
- **Git**: 版本控制工具
- **IDE**: IntelliJ IDEA（推荐）

### 克隆项目

```bash
git clone <repo-url>
cd phoenix
```

### 后端开发环境

1. **导入项目到 IDE**
   - 使用 IntelliJ IDEA 打开项目根目录
   - IDE 会自动识别为 Maven 项目并下载依赖

2. **配置数据库**
   - 创建 PostgreSQL 数据库（需启用 pgvector 扩展）
   - 通过环境变量配置数据库连接（项目无 `application.yml`）

3. **构建项目**
   ```bash
   mvn clean install -Dspring-javaformat.skip=true
   ```

4. **启动后端服务**
   ```bash
   mvn spring-boot:run -pl phoenix-data
   ```

### Maven Settings

项目需要自定义 Maven settings 文件：

```bash
mvn clean install -s /path/to/dragon-settings.xml
```

默认 settings 文件位置：`/Users/liuwenjun/java/doc/maven-setting/dragon-settings.xml`

### 项目构建命令

| 命令 | 说明 |
|------|------|
| `mvn clean install` | 全量构建（含 spring-javaformat 格式检查） |
| `mvn clean install -Dspring-javaformat.skip=true` | 跳过格式检查构建 |
| `mvn spring-javaformat:validate` | 格式检查 |
| `mvn spring-javaformat:apply` | 自动修复格式 |
| `mvn spring-boot:run -pl phoenix-data` | 启动主应用 |

> **注意**：项目没有 Maven Wrapper，`mvn` 必须在 `PATH` 中可用。
> `spring-javaformat-maven-plugin` 绑定在默认生命周期中，每次构建都会运行。

## 🏗️ 模块结构

### 模块概览

| 模块 | 层级 | 说明 |
|------|------|------|
| `phoenix-parent` | BOM | 依赖管理和插件管理 |
| `phoenix-data` | 应用层 | **主应用** — 入口、Graph 工作流、Controller、Service、MyBatis Mapper |
| `phoenix-agent` | 子域 | 智能体 — Session、记忆、React Agent 工作流 |
| `phoenix-admin` | 子域 | 管理平台 |
| `phoenix-privilege` | 子域 | 权限认证 — RBAC、用户、角色、ACL |
| `phoenix-platform` | 子域 | 平台管理 — 租户、组织、三方账号 |
| `phoenix-common` | 共享 | 基础模型、异常、枚举、平台 SDK |
| `phoenix-tool` | 工具 | 返回值封装、SQL 安全校验 |
| `phoenix-codegen` | 代码生成 | MyBatis-Flex 代码生成器 |
| `phoenix-flink` | Flink | Apache Flink 集成 |
| `phoenix-rag` | RAG | 检索增强生成 |
| `phoenix-kg` | 知识图谱 | 知识图谱模块 |

### 子域模块规范

除 `phoenix-parent`、`phoenix-tool`、`phoenix-codegen` 外，每个子域模块均遵循三层结构：

- **`-api`**：接口定义、DTO、Feign 接口
- **`-core`**：业务实现、MyBatis-Flex Mapper
- **`-rest`**：REST Controller、异常处理

### 持久化分层

- **`phoenix-data`**：使用 MyBatis 4（`mybatis-spring-boot-starter`）
- **其他子域**（`phoenix-agent-core` 等）：使用 MyBatis-Flex 1.11.7（`mybatis-flex-spring-boot4-starter`），Mapper 继承 `BaseMapper<T>`

## 🔧 核心模块说明

### 1. StateGraph 工作流引擎

工作流基于 Spring AI Alibaba 的 `StateGraph` 实现，定义在 `DataAgentConfiguration.nl2sqlGraph()` 中。核心节点包括：

- **IntentRecognitionNode**: 意图识别（闲聊 vs 数据分析）
- **EvidenceRecallNode**: 证据召回（RAG 检索）
- **QueryEnhanceNode**: 查询增强
- **SchemaRecallNode**: Schema 召回
- **TableRelationNode**: 表关系推断
- **FeasibilityAssessmentNode**: 可行性评估
- **PlannerNode**: 计划生成
- **PlanExecutorNode**: 计划执行与路由
- **SqlGenerateNode**: SQL 生成（含重试）
- **SemanticConsistencyNode**: 语义一致性校验
- **SqlExecuteNode**: SQL 执行
- **PythonGenerateNode**: Python 代码生成
- **PythonExecuteNode**: Python 代码执行
- **PythonAnalyzeNode**: Python 执行结果分析
- **ReportGeneratorNode**: 报告生成
- **HumanFeedbackNode**: 人工反馈节点

每个节点对应一个 **Dispatcher**（条件边路由器），例如 `IntentRecognitionDispatcher`、`PlanExecutorDispatcher` 等。

### 2. 多模型调度

通过 `AiModelRegistry` 实现多模型管理和热切换：

```java
@Service
public class AiModelRegistry {
    private ChatModel currentChatModel;
    private EmbeddingModel currentEmbeddingModel;

    public void refreshChatModel(ModelConfig config) {
        // 动态创建和切换 Chat 模型
    }

    public void refreshEmbeddingModel(ModelConfig config) {
        // 动态创建和切换 Embedding 模型
    }
}
```

### 3. 向量检索服务

`AgentVectorStoreService` 提供统一的向量检索接口，支持混合检索（向量 + 关键词）：

```java
@Service
public class AgentVectorStoreService {
    public List<Document> retrieve(String query,
                                   String agentId,
                                   VectorType vectorType) {
        // 向量检索逻辑
    }
}
```

### 4. 代码执行器

支持三种 Python 执行模式：

- **Docker Executor**（推荐）：使用 Docker 容器执行 Python 代码
- **Local Executor**：在本地直接执行 Python
- **AI Simulation Executor**：AI 模拟执行（测试环境使用）

## 🧩 自定义智能体开发

Phoenix 的 `phoenix-agent` 模块提供了完整的智能体开发框架，支持两种范式：

### 范式一：React Agent（工具型智能体）

继承 `AbstractReactAgent` 抽象类，实现以下方法：

```java
@Component("myCustomAgent")
public class MyCustomAgent extends AbstractReactAgent {

    @Override
    public String getSn() {
        return "MyCustomAgent";  // 全局唯一标识
    }

    @Override
    public String getName() {
        return "自定义分析小助手";  // 智能体名称
    }

    @Override
    public String getDescription() {
        return "用于自定义业务分析的智能体";  // 智能体描述
    }

    @Override
    protected Agent<Object> createReactAgent() {
        return ReactiveReactAgent.builder()
            .chatClient(createChatModel())       // 创建 Chat 模型
            .tools(tool1, tool2)                 // 注册工具
            .systemPrompt(systemPrompt)           // 系统提示词
            // .hooks(getCommonHooks())           // 可选：注册生命周期钩子
            // .interceptors(getCommonInterceptors()) // 可选：注册拦截器
            .build();
    }
}
```

**关键说明：**

- `@Component` 注解使智能体注册到 Spring 容器
- `SmartInitializingSingleton` 确保启动时自动注册到 `AgentStaticLoader`
- `createChatModel()` 自动从数据库读取 `CHAT` 类型的模型配置
- `getCommonHooks()` 提供 `SummaryHook`、`ModelCallLimitHook`、`CombinedDbHook` 等

### 范式二：Workflow Graph（工作流型智能体）

继承 `AbstractCompiledGragph` 抽象类，实现以下方法：

```java
@Component("myWorkflowAgent")
public class MyWorkflowAgent extends AbstractCompiledGragph {

    @Override
    public String getSn() {
        return "MyWorkflowAgent";
    }

    @Override
    public String getName() {
        return "自定义工作流智能体";
    }

    @Override
    public String getDescription() {
        return "多步骤业务分析工作流";
    }

    @Override
    protected CompiledGraph createParolCompiledGraph() {
        StateGraph<AgentState> graph = new StateGraph<>(AgentState::new)
            .addNode("node1", new MyNode1())
            .addNode("node2", new MyNode2())
            .addEdge("__start__", "node1")
            .addEdge("node1", "node2")
            .addEdge("node2", "__end__");
        return graph.compile(buildCompileConfig());
    }
}
```

**关键说明：**

- 节点可实现 `NodeAction` 接口，接收 `State` 并返回 `Map<String, Object>`
- 支持嵌套 React Agent 作为子节点
- 使用 `ReplaceStrategy` 控制状态合并策略

### @Tool 注解注册工具

```java
@Component
public class MyCustomTool {

    @Tool(name = "searchData", description = "查询业务数据")
    public String searchData(String query) {
        // 工具逻辑
        return result;
    }
}
```

### 智能体注册与发现

- `AgentStaticLoader`：管理所有 React Agent 的内存注册表
- `CompiledGraphStaticLoader`：管理所有 Workflow Graph 的内存注册表
- `AgentManager`：运行时根据 agent type 自动路由到对应的执行引擎

### 参考实现

- **BpmReactAgent**：流程分析智能体（BPM 流程查询）
- **ZhiduReactAgent**：制度分析智能体（知识文档检索）
- **ParolCompiledGraph**：警务巡逻工作流（多节点编排）

## 🎨 编码规范

### Java 编码规范

1. **命名规范**
   - 类名：大驼峰命名法（PascalCase）
   - 方法名：小驼峰命名法（camelCase）
   - 常量：全大写下划线分隔（UPPER_SNAKE_CASE）

2. **代码格式**
   - 使用 4 个空格缩进
   - 使用 `spring-javaformat-maven-plugin` 进行格式管理
   - 运行 `mvn spring-javaformat:apply` 自动修复格式

3. **依赖注入**
   - 推荐使用 Lombok `@AllArgsConstructor` 实现构造器注入

### 编译器配置

- 启用 `-parameters` 编译器参数（MyBatis 参数名反射）

## ⚙️ 开发配置手册

本项目的所有配置项均位于 `spring.ai.phoenix.data-agent` 前缀下。

### 1. 通用配置

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `spring.ai.phoenix.data-agent.llm-service-type` | LLM服务类型（STREAM/BLOCK） | STREAM |
| `spring.ai.phoenix.data-agent.max-sql-retry-count` | SQL执行失败重试次数 | 10 |
| `spring.ai.phoenix.data-agent.max-sql-optimize-count` | SQL优化最多次数 | 10 |
| `spring.ai.phoenix.data-agent.sql-score-threshold` | SQL优化分数阈值 | 0.95 |
| `spring.ai.phoenix.data-agent.maxturnhistory` | 最多保留的对话轮数 | 5 |
| `spring.ai.phoenix.data-agent.maxplanlength` | 单次规划最大长度限制 | 2000 |
| `spring.ai.phoenix.data-agent.max-columns-per-table` | 每张表的最大预估列数 | 50 |
| `spring.ai.phoenix.data-agent.fusion-strategy` | 多路召回结果融合策略 | rrf |
| `spring.ai.phoenix.data-agent.enable-sql-result-chart` | 是否启用SQL执行结果图表判断 | true |
| `spring.ai.phoenix.data-agent.enrich-sql-result-timeout` | 执行SQL结果图表化超时时间（毫秒） | 3000 |

### 2. 嵌入模型批处理策略（Embedding Batch）

配置前缀：`spring.ai.phoenix.data-agent.embedding-batch`

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `encoding-type` | 文本编码类型（参考 EncodingType） | cl100k_base |
| `max-token-count` | 每批次最大令牌数 | 8000 |
| `reserve-percentage` | 预留百分比（用于缓冲空间） | 0.2 |
| `max-text-count` | 每批次最大文本数量（DashScope限制为10） | 10 |

### 3. 向量库配置（Vector Store）

配置前缀：`spring.ai.phoenix.data-agent.vector-store`

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `default-similarity-threshold` | 全局默认相似度阈值 | 0.4 |
| `table-similarity-threshold` | 召回表的相似度阈值 | 0.2 |
| `batch-del-topk-limit` | 批量删除时的最大文档数量 | 5000 |
| `default-topk-limit` | 默认查询返回的最大文档数量 | 8 |
| `table-topk-limit` | 召回表的最大文档数量 | 10 |
| `enable-hybrid-search` | 是否启用混合搜索 | false |
| `elasticsearch-min-score` | ES关键词搜索的最小分数阈值 | 0.5 |

#### 向量库依赖扩展

项目默认使用 PostgreSQL pgvector 向量库。若需切换：

1. **引入依赖**：在 `pom.xml` 中添加对应的 Spring AI Starter
   ```xml
   <dependency>
       <groupId>org.springframework.ai</groupId>
       <artifactId>spring-ai-starter-vector-store-pgvector</artifactId>
   </dependency>
   ```

2. **配置 `spring.ai.vectorstore.type`**，具体值参考对应 Starter 的自动配置类

#### ES Schema 配置示例

```json
{
  "mappings": {
    "properties": {
      "content": {
        "type": "text",
        "fields": {
          "keyword": { "type": "keyword", "ignore_above": 256 }
        }
      },
      "embedding": {
        "type": "dense_vector",
        "dims": 1024,
        "index": true,
        "similarity": "cosine",
        "index_options": {
          "type": "int8_hnsw",
          "m": 16,
          "ef_construction": 100
        }
      },
      "id": {
        "type": "text",
        "fields": {
          "keyword": { "type": "keyword", "ignore_above": 256 }
        }
      },
      "metadata": {
        "properties": {
          "agentId": { "type": "text", "fields": { "keyword": { "type": "keyword", "ignore_above": 256 } } },
          "agentKnowledgeId": { "type": "long" },
          "businessTermId": { "type": "long" },
          "concreteAgentKnowledgeType": { "type": "text", "fields": { "keyword": { "type": "keyword", "ignore_above": 256 } } },
          "vectorType": { "type": "text", "fields": { "keyword": { "type": "keyword", "ignore_above": 256 } } }
        }
      }
    }
  }
}
```

### 4. 文本切分配置（Text Splitter）

配置前缀：`spring.ai.phoenix.data-agent.text-splitter`

#### 4.1 全局配置

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `chunk-size` | 默认分块大小（基于token数量） | 1000 |

#### 4.2 TokenTextSplitter 配置（token）

配置前缀：`spring.ai.phoenix.data-agent.text-splitter.token`

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `min-chunk-size-chars` | 最小分块字符数 | 400 |
| `min-chunk-length-to-embed` | 嵌入最小分块长度 | 10 |
| `max-num-chunks` | 最大分块数量 | 5000 |
| `keep-separator` | 是否保留分隔符 | true |

#### 4.3 RecursiveCharacterTextSplitter 配置（recursive）

配置前缀：`spring.ai.phoenix.data-agent.text-splitter.recursive`

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `chunk-overlap` | 重叠区域字符数 | 200 |
| `separators` | 自定义分隔符列表 | null |

#### 4.4 SentenceTextSplitter 配置（sentence）

配置前缀：`spring.ai.phoenix.data-agent.text-splitter.sentence`

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `sentence-overlap` | 句子重叠数量 | 1 |

#### 4.5 SemanticTextSplitter 配置（semantic）

配置前缀：`spring.ai.phoenix.data-agent.text-splitter.semantic`

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `min-chunk-size` | 最小分块大小（字符数） | 200 |
| `max-chunk-size` | 最大分块大小（字符数） | 1000 |
| `similarity-threshold` | 语义相似度阈值 | 0.5 |

#### 4.6 ParagraphTextSplitter 配置（paragraph）

配置前缀：`spring.ai.phoenix.data-agent.text-splitter.paragraph`

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `paragraph-overlap-chars` | 段落重叠字符数 | 200 |

### 5. 代码执行器配置（Code Executor）

配置前缀：`spring.ai.phoenix.data-agent.code-executor`

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `code-pool-executor` | 执行器类型（DOCKER/LOCAL） | DOCKER |
| `image-name` | Docker镜像名称 | continuumio/anaconda3:latest |
| `container-name-prefix` | 容器名称前缀 | nl2sql-python-exec- |
| `host` | 服务主机地址 | null |
| `task-queue-size` | 任务阻塞队列大小 | 5 |
| `core-container-num` | 核心容器数量最大值 | 2 |
| `temp-container-num` | 临时容器数量最大值 | 2 |
| `core-thread-size` | 线程池核心线程数 | 5 |
| `max-thread-size` | 线程池最大线程数 | 5 |
| `code-timeout` | Python代码执行超时时间 | 60s |
| `container-timeout` | 容器最大运行时长 | 3000 (ms) |
| `limit-memory` | 容器内存限制（MB） | 500 |
| `cpu-core` | 容器CPU核数 | 1 |

### 6. 文件存储配置（File Storage）

配置前缀：`spring.ai.phoenix.data-agent.file`

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `type` | 存储类型（LOCAL/OSS） | LOCAL |
| `path` | 本地上传目录路径 | ./uploads |
| `url-prefix` | 对外暴露的访问前缀 | /uploads |
| `image-size` | 图片大小上限（字节） | 2097152 (2MB) |
| `path-prefix` | 对象存储路径前缀 | "" |

### 7. 阿里云 OSS 配置

配置前缀：`spring.ai.phoenix.data-agent.file.oss`

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `access-key-id` | OSS 访问密钥 ID | - |
| `access-key-secret` | OSS 访问密钥 Secret | - |
| `endpoint` | OSS 端点地址 | - |
| `bucket-name` | OSS 存储桶名称 | - |
| `custom-domain` | 自定义域名 | - |

### 8. 报告资源配置

配置前缀：`spring.ai.phoenix.data-agent.report-template`

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `marked-url` | Marked.js 路径（Markdown渲染库） | https://mirrors.sustech.edu.cn/cdnjs/ajax/libs/marked/12.0.0/marked.min.js |
| `echarts-url` | ECharts 路径（图表库） | https://mirrors.sustech.edu.cn/cdnjs/ajax/libs/echarts/5.5.0/echarts.min.js |

### 9. Langfuse 可观测性配置

配置前缀：`spring.ai.phoenix.data-agent.langfuse`

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `enabled` | 是否启用 Langfuse 可观测性 | true |
| `host` | Langfuse 服务地址 | - |
| `public-key` | Langfuse 项目的 Public Key | - |
| `secret-key` | Langfuse 项目的 Secret Key | - |

对应环境变量：`LANGFUSE_ENABLED`、`LANGFUSE_HOST`、`LANGFUSE_PUBLIC_KEY`、`LANGFUSE_SECRET_KEY`

## 📚 学习资源

### 官方文档

- [Spring AI Alibaba 文档](https://springdoc.cn/spring-ai/)
- [Spring Boot 文档](https://spring.io/projects/spring-boot)

### 相关技术

- StateGraph 工作流引擎
- MyBatis / MyBatis-Flex 数据访问框架
- Vector Store 向量数据库
- Server-Sent Events (SSE)

## 🤝 贡献指南

### 贡献类型

- 🐛 报告 Bug
- 💡 提出新功能建议
- 📝 改进文档
- 🔧 提交代码修复
- ✨ 开发新功能

### 代码贡献流程

1. Fork 项目并创建特性分支
2. 编写代码并确保通过格式检查：`mvn spring-javaformat:validate`
3. 提交 PR

### 行为准则

- 尊重所有贡献者
- 保持友好和专业
- 接受建设性批评
- 关注项目目标

---

感谢您对 Phoenix 项目的贡献！🎉

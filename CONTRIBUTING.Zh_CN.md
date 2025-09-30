# 贡献指南

感谢您对 Accordion 项目的关注！本文档提供了贡献代码的指南和说明。

## 目录

- [行为准则](#行为准则)
- [开始贡献](#开始贡献)
- [开发环境配置](#开发环境配置)
- [开发工作流](#开发工作流)
- [编码规范](#编码规范)
- [测试指南](#测试指南)
- [Pull Request 流程](#pull-request-流程)
- [提交信息规范](#提交信息规范)
- [Issue 指南](#issue-指南)
- [发布流程](#发布流程)

## 行为准则

本项目遵循以下行为准则，所有贡献者都应遵守：

- **互相尊重**：以尊重和体谅的态度对待每个人
- **协作共赢**：建设性地合作
- **保持专业**：在所有互动中保持专业态度
- **包容开放**：欢迎和支持来自不同背景的人

## 开始贡献

### 前置要求

在开始之前，请确保已安装以下工具：

- **Java 17 或更高版本**：[下载 JDK](https://adoptium.net/)
- **Maven 3.6+**：[下载 Maven](https://maven.apache.org/download.cgi)
- **Git**：[下载 Git](https://git-scm.com/downloads)
- **IDE**：推荐使用 IntelliJ IDEA 或 Eclipse

### Fork 和 Clone

1. **Fork 仓库**：在 GitHub 上 fork 本仓库
2. **克隆 fork**：

   ```bash
   git clone https://github.com/YOUR_USERNAME/accordion.git
   cd accordion
   ```

3. **添加上游远程仓库**：

   ```bash
   git remote add upstream https://github.com/eoctet/accordion.git
   ```

### 验证环境

运行以下命令验证环境配置：

```bash
# 构建项目
mvn clean install

# 运行测试
mvn test

# 运行代码质量检查
mvn checkstyle:check spotbugs:check pmd:check
```

如果所有命令都成功执行，说明环境配置正确！

## 开发环境配置

### IDE 配置

#### IntelliJ IDEA

1. **导入项目**：`File → Open` → 选择 `pom.xml`
2. **代码风格**：
   - `File → Settings → Editor → Code Style → Java`
   - 从 `checkstyle.xml` 导入代码风格
3. **启用 Lombok**：
   - 安装 Lombok 插件
   - `File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors`
   - 启用注解处理
4. **Checkstyle 插件**：
   - 安装 CheckStyle-IDEA 插件
   - 配置使用项目的 `checkstyle.xml`

#### Eclipse

1. **导入项目**：`File → Import → Maven → Existing Maven Projects`
2. **安装 Lombok**：参考 [Lombok Eclipse 配置](https://projectlombok.org/setup/eclipse)
3. **Checkstyle 插件**：安装 Eclipse Checkstyle 插件

### 本地开发

```bash
# 快速编译（跳过测试）
mvn clean compile -DskipTests

# 运行特定测试类
mvn test -Dtest=AccordionTest

# 运行特定测试方法
mvn test -Dtest=AccordionTest#shouldExecuteSequentialPlan

# 生成测试覆盖率报告
mvn clean test jacoco:report
# 打开: target/site/jacoco/index.html

# 运行安全扫描
mvn dependency-check:check
# 打开: target/dependency-check-report.html
```

## 开发工作流

### 1. 创建分支

始终在新分支上进行开发：

```bash
# 更新主分支
git checkout main
git pull upstream main

# 创建功能分支
git checkout -b feature/your-feature-name

# 或创建修复分支
git checkout -b fix/issue-description

# 或创建文档分支
git checkout -b docs/documentation-update
```

**分支命名规范**：

- `feature/*` - 新功能
- `fix/*` - Bug 修复
- `docs/*` - 文档更新
- `refactor/*` - 代码重构
- `test/*` - 测试相关
- `chore/*` - 构建/工具更新

### 2. 进行修改

修改代码时请遵循以下原则：

1. **编写整洁代码**：遵循[编码规范](#编码规范)
2. **添加测试**：确保代码有充分的测试覆盖
3. **更新文档**：如需要，更新 JavaDoc 和 README
4. **保持提交原子性**：每次提交应该代表一个逻辑变更

### 3. 测试修改

提交前确保所有测试通过：

```bash
# 运行所有测试
mvn clean test

# 检查代码覆盖率
mvn jacoco:check
# 要求：行覆盖率 ≥80%，分支覆盖率 ≥70%

# 运行代码质量检查
mvn checkstyle:check spotbugs:check pmd:check

# 运行安全扫描
mvn dependency-check:check
```

### 4. 提交修改

遵循[提交信息规范](#提交信息规范)：

```bash
git add .
git commit -m "feat(api): 为 API action 添加重试机制

- 实现指数退避重试策略
- 添加可配置的最大重试次数
- 添加重试间隔超时配置

Closes #123"
```

### 5. 推送并创建 Pull Request

```bash
# 推送到 fork 仓库
git push origin feature/your-feature-name

# 在 GitHub 上创建 Pull Request
```

## 编码规范

### Java 代码风格

我们遵循严格的代码风格，基于行业最佳实践：

#### 命名规范

```java
// 类名：PascalCase
public class ApiAction extends AbstractAction { }

// 方法名：camelCase
public ExecuteResult executeAction() { }

// 变量名：camelCase
private String actionName;
private int retryCount;

// 常量：UPPER_SNAKE_CASE
private static final int MAX_RETRY_COUNT = 3;
private static final String DEFAULT_TIMEOUT = "30s";

// 包名：小写加点号
package chat.octet.accordion.action.api;
```

#### 代码结构

类成员按以下顺序排列：

```java
public class ExampleAction extends AbstractAction {
    // 1. 静态常量
    private static final String DEFAULT_VALUE = "default";

    // 2. 静态字段
    private static final OkHttpClient SHARED_CLIENT = ...;

    // 3. 实例字段
    private final ActionConfig config;
    private String actionName;

    // 4. 构造函数
    public ExampleAction(ActionConfig config) {
        super(config);
    }

    // 5. 公共方法
    @Override
    public ExecuteResult execute() throws ActionException {
        // 实现代码
    }

    // 6. 保护方法
    protected void validate() {
        // 验证逻辑
    }

    // 7. 私有方法
    private void performTask() {
        // 任务逻辑
    }
}
```

#### 文档注释

**每个公共类和方法必须有 JavaDoc 注释**：

```java
/**
 * 执行带重试机制的 API 动作。
 *
 * <p>此动作调用外部 REST API，具有可配置的重试逻辑。
 * 如果请求失败，将使用指数退避策略进行重试。</p>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * ActionConfig config = ActionConfig.builder()
 *     .id("api-001")
 *     .actionType(ActionType.API.name())
 *     .actionParams(ApiParameter.builder()
 *         .url("https://api.example.com")
 *         .method("GET")
 *         .maxRetries(3)
 *         .build())
 *     .build();
 * }</pre>
 *
 * @param session 包含参数的执行会话
 * @return 包含响应数据的执行结果
 * @throws ActionException 如果所有重试都失败后抛出
 * @see AbstractAction
 * @see ApiParameter
 * @since 1.0.0
 */
public ExecuteResult execute(Session session) throws ActionException {
    // 实现代码
}
```

#### 异常处理

```java
// 推荐：捕获具体异常
try {
    result = performAction();
} catch (IOException e) {
    log.error("动作 {} 发生 IO 错误: {}", actionId, e.getMessage(), e);
    setExecuteThrowable(e);
    throw new ActionException("执行动作失败", e);
} catch (TimeoutException e) {
    log.error("动作 {} 超时: {}", actionId, e.getMessage(), e);
    setExecuteThrowable(e);
    throw new ActionException("动作执行超时", e);
}

// 不推荐：捕获泛型异常而不重新抛出
try {
    result = performAction();
} catch (Exception e) {
    // 静默失败 - 不好的实践！
}
```

#### 资源管理

```java
// 推荐：使用 try-with-resources
try (OkHttpClient client = new OkHttpClient();
     Response response = client.newCall(request).execute()) {
    return processResponse(response);
}

// 推荐：实现 AutoCloseable 进行清理
@Override
public void close() {
    if (resource != null) {
        try {
            resource.close();
        } catch (Exception e) {
            log.warn("关闭资源失败: {}", e.getMessage());
        }
    }
}
```

#### Lombok 使用

```java
// 推荐：适当使用 Lombok
@Slf4j
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiParameter {
    private String url;
    private String method;
    private Map<String, String> headers;
    private int timeout;
}

// 不推荐：滥用 @SneakyThrows
// 不推荐：在子类中使用 @EqualsAndHashCode 而不带 callSuper
```

### 代码质量工具

所有代码必须通过以下检查：

1. **Checkstyle**：代码风格验证
   - 配置文件：`checkstyle.xml`
   - 抑制规则：`checkstyle-suppressions.xml`

2. **SpotBugs**：Bug 模式检测
   - 排除规则：`spotbugs-exclude.xml`

3. **PMD**：代码分析
   - 规则集：`pmd-ruleset.xml`

4. **JaCoCo**：代码覆盖率
   - 最低要求：80% 行覆盖率，70% 分支覆盖率

## 测试指南

### 测试结构

```java
package chat.octet.accordion.action.api;

import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;

/**
 * ApiAction 的单元测试。
 *
 * 测试覆盖：
 * - 正常执行流程
 * - 错误处理
 * - 参数验证
 * - 边界条件
 */
@DisplayName("ApiAction 测试")
class ApiActionTest {

    private ApiAction action;
    private Session session;

    @BeforeEach
    void setUp() {
        session = new Session();
        action = new ApiAction(createTestConfig());
    }

    @AfterEach
    void tearDown() {
        if (action != null) {
            action.close();
        }
    }

    @Nested
    @DisplayName("正常执行测试")
    class NormalExecutionTests {

        @Test
        @DisplayName("应该成功执行 GET 请求")
        void shouldExecuteGetRequestSuccessfully() {
            // Given
            action.prepare(session);

            // When
            ExecuteResult result = action.execute();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isTrue();
        }
    }

    @Nested
    @DisplayName("错误处理测试")
    class ErrorHandlingTests {

        @Test
        @DisplayName("应该处理无效的 URL")
        void shouldHandleInvalidUrl() {
            // When/Then
            assertThatThrownBy(() -> action.execute())
                .isInstanceOf(ActionException.class)
                .hasMessageContaining("Invalid URL");
        }
    }
}
```

### 测试要求

1. **覆盖率**：
   - 核心类：100% 覆盖
   - Action 实现：≥90% 覆盖
   - 整体：≥80% 行覆盖，≥70% 分支覆盖

2. **命名**：使用描述性测试名称
   - ✅ `shouldExecuteGetRequestSuccessfully()`
   - ❌ `test1()`

3. **AAA 模式**：Arrange-Act-Assert

   ```java
   @Test
   void testExample() {
       // Arrange (Given) - 准备
       ActionConfig config = createConfig();

       // Act (When) - 执行
       ExecuteResult result = action.execute();

       // Assert (Then) - 断言
       assertThat(result.isSuccess()).isTrue();
   }
   ```

4. **测试分类**：
   - 单元测试：`src/test/java/**/*Test.java`
   - 集成测试：`src/test/java/**/integration/*IntegrationTest.java`
   - 性能测试：`src/test/java/**/performance/*PerformanceTest.java`

## Pull Request 流程

### 提交前检查

提交 PR 前请完成以下检查清单：

- [ ] 代码符合[编码规范](#编码规范)
- [ ] 所有测试通过：`mvn test`
- [ ] 代码覆盖率达标：`mvn jacoco:check`
- [ ] 代码质量检查通过：`mvn checkstyle:check spotbugs:check pmd:check`
- [ ] 安全扫描通过：`mvn dependency-check:check`
- [ ] 文档已更新（JavaDoc、README、示例）
- [ ] 提交信息符合[规范](#提交信息规范)
- [ ] 分支已与 main 同步

### PR 模板

创建 PR 时应包含：

```markdown
## 描述
简要描述所做的修改

## 变更类型
- [ ] Bug 修复（不破坏现有功能的修复）
- [ ] 新功能（不破坏现有功能的新增）
- [ ] 破坏性变更（可能导致现有功能失效的修复或功能）
- [ ] 文档更新

## 相关 Issue
Closes #123

## 变更内容
- 为 API action 添加了重试机制
- 实现了指数退避策略
- 添加了最大重试次数配置

## 测试
- [ ] 单元测试已添加/更新
- [ ] 集成测试已添加/更新
- [ ] 所有测试在本地通过
- [ ] 测试覆盖率 ≥80%

## 检查清单
- [ ] 代码符合项目风格规范
- [ ] 已完成自我代码审查
- [ ] 文档已更新
- [ ] 没有引入新的警告
```

### PR 审查流程

1. **自动检查**：CI/CD 流水线将自动运行
   - 构建和编译
   - 运行所有测试
   - 代码质量检查（Checkstyle、SpotBugs、PMD）
   - 安全扫描（OWASP）

2. **代码审查**：至少一位维护者必须批准
   - 代码质量和风格
   - 测试覆盖率
   - 文档完整性
   - 设计和架构

3. **合并**：审查通过且所有检查通过后
   - Squash and merge（默认）
   - Rebase and merge（保持清晰历史）

## 提交信息规范

我们遵循 [Conventional Commits](https://www.conventionalcommits.org/) 规范。

### 格式

```text
<type>(<scope>): <subject>

<body>

<footer>
```

### 类型

- `feat`: 新功能
- `fix`: Bug 修复
- `docs`: 仅文档更改
- `style`: 代码格式更改（格式化、缺少分号等）
- `refactor`: 代码重构（无功能变更）
- `perf`: 性能改进
- `test`: 添加或更新测试
- `chore`: 构建过程或辅助工具的变动
- `ci`: CI/CD 配置变更

### 范围

范围应该是受影响的代码区域：

- `api`: API action
- `email`: Email action
- `script`: Script action
- `shell`: Shell action
- `core`: 核心框架
- `graph`: 图引擎
- `docs`: 文档
- `ci`: CI/CD 配置

### 示例

```bash
# 新功能
feat(api): 为 API action 添加重试机制

实现指数退避重试策略，支持配置最大重试次数和重试间隔超时。

Closes #123

# Bug 修复
fix(script): 修正脚本执行错误处理

脚本 action 之前没有正确捕获脚本语法错误，导致整个计划失败。
现在将错误包装在 ActionException 中。

Fixes #456

# 文档
docs(readme): 更新安装说明

添加 Gradle 依赖配置说明并更新 Maven Central 版本徽章。

# 破坏性变更
feat(core)!: 更改 Session 参数 API

BREAKING CHANGE: Session.add() 现在需要显式的类型参数
以提高类型安全性。迁移指南已添加到文档中。

Closes #789
```

### 指南

1. **主题行**：
   - 使用祈使语气（"添加"而非"已添加"或"添加了"）
   - 首字母不大写
   - 末尾不加句号
   - 最多 72 个字符

2. **正文**：
   - 解释"是什么"和"为什么"，而非"怎么做"
   - 每行不超过 72 个字符
   - 与主题行用空行分隔

3. **页脚**：
   - 引用 Issue：`Closes #123`、`Fixes #456`
   - 注明破坏性变更：`BREAKING CHANGE: 描述`

## Issue 指南

### 创建 Issue 前

1. **搜索现有 Issue**：检查问题是否已存在
2. **查看文档**：查阅 README 和 Wiki
3. **验证最新版本**：确保使用的是最新发布版本

### Issue 类型

使用合适的 Issue 模板：

#### Bug 报告

```markdown
**描述 Bug**
清晰描述 Bug 是什么。

**复现步骤**
复现行为的步骤：
1. 创建 AccordionPlan，包含 '...'
2. 使用 '...' 执行
3. 看到错误

**期望行为**
描述您期望发生的行为。

**实际行为**
描述实际发生的行为。

**环境**
- Accordion 版本: [例如，1.0.1]
- Java 版本: [例如，17]
- 操作系统: [例如，Ubuntu 20.04]

**附加信息**
堆栈跟踪、日志或截图。
```

#### 功能请求

```markdown
**您的功能请求是否与问题相关？**
清晰描述问题。

**描述您想要的解决方案**
清晰描述您希望实现的功能。

**描述您考虑过的替代方案**
您考虑过的其他解决方案或功能。

**附加信息**
任何其他上下文或截图。
```

## 发布流程

### 版本号规范

我们遵循[语义化版本](https://semver.org/)：

- `主版本.次版本.修订版本`（例如，1.0.1）
- **主版本**：破坏性变更
- **次版本**：新功能（向后兼容）
- **修订版本**：Bug 修复（向后兼容）

### 发布步骤

发布由维护者管理：

1. **更新版本号**：在 `pom.xml` 中
2. **更新 CHANGELOG.md**：添加发布说明
3. **创建发布分支**：`release/v1.0.1`
4. **标记发布**：`v1.0.1`
5. **推送标签**：触发自动部署到 Maven Central
6. **创建 GitHub Release**：添加发布说明

### 变更日志

每个发布版本必须有 CHANGELOG 条目：

```markdown
## [1.0.1] - 2024-01-15

### 新增
- API action 的重试机制
- 指数退避配置

### 变更
- 改进 script action 的错误消息
- 更新依赖项

### 修复
- 修复脚本执行错误处理
- 修正 email action 参数验证

### 安全
- 更新 Jackson 到 2.16.1（CVE-2023-xxxxx）
```

## 安全

### 报告安全漏洞

**请勿**为安全漏洞创建公开 Issue。

相反：

1. 向维护者发送电子邮件报告安全问题
2. 包含关于漏洞的详细信息
3. 等待确认后再公开披露

### 安全扫描

所有依赖项每周扫描漏洞：

- OWASP Dependency Check 自动运行
- 高危/严重漏洞会导致构建失败
- 安全问题在 GitHub Issues 中跟踪

## 获取帮助

如果需要帮助：

1. **文档**：查看 [README](README.md) 和 [Wiki](https://github.com/eoctet/accordion/wiki)
2. **Issues**：搜索[现有 Issue](https://github.com/eoctet/accordion/issues)
3. **讨论**：开始[讨论](https://github.com/eoctet/accordion/discussions)
4. **示例**：查看[示例代码](src/test/java/chat/octet/accordion/examples/)

## 许可证

通过为 Accordion 做出贡献，您同意您的贡献将根据 [Apache License 2.0](LICENSE) 进行许可。

---

感谢您为 Accordion 做出贡献！🎉

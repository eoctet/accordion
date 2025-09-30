# Accordion 开发规范完整指南

**版本：** 1.0
**最后更新：** 2025-01-15
**维护者：** Accordion 开发团队

---

## 📋 目录

- [Accordion 开发规范完整指南](#accordion-开发规范完整指南)
  - [📋 目录](#-目录)
  - [概述](#概述)
    - [目标](#目标)
    - [执行方式](#执行方式)
  - [Java 开发规范](#java-开发规范)
    - [1. 代码风格](#1-代码风格)
      - [1.1 命名规范](#11-命名规范)
      - [1.2 文件组织](#12-文件组织)
      - [1.3 代码格式化](#13-代码格式化)
      - [1.4 JavaDoc 规范](#14-javadoc-规范)
    - [2. 异常处理](#2-异常处理)
    - [3. Lombok 使用指南](#3-lombok-使用指南)
    - [4. 日志规范](#4-日志规范)
  - [测试规范](#测试规范)
    - [1. 测试结构](#1-测试结构)
    - [2. 测试覆盖率要求](#2-测试覆盖率要求)
  - [Git 工作流和提交规范](#git-工作流和提交规范)
    - [1. 分支策略](#1-分支策略)
      - [1.1 分支命名](#11-分支命名)
    - [2. 提交信息规范](#2-提交信息规范)
      - [2.1 提交信息格式](#21-提交信息格式)
      - [2.2 类型](#22-类型)
      - [2.3 范围](#23-范围)
      - [2.4 示例](#24-示例)
    - [3. Git 工作流程](#3-git-工作流程)
  - [安全规范](#安全规范)
    - [1. 输入验证](#1-输入验证)
    - [2. 敏感数据处理](#2-敏感数据处理)
    - [3. 依赖安全](#3-依赖安全)
  - [快速参考](#快速参考)
    - [开发检查清单](#开发检查清单)
    - [常用命令](#常用命令)

---

## 概述

本文档定义了 Accordion 项目的全面开发标准。所有贡献者必须遵守这些标准以确保代码质量、可维护性和安全性。

### 目标

- **一致性**：在整个代码库中保持统一的代码风格
- **质量**：通过自动化检查和审查确保高代码质量
- **安全性**：通过最佳实践防止安全漏洞
- **可维护性**：编写清晰、文档完善的代码
- **性能**：优化效率和可扩展性

### 执行方式

- 通过 CI/CD 自动化检查（Checkstyle、SpotBugs、PMD、OWASP）
- 至少一位维护者进行代码审查
- 测试覆盖率要求（≥80% 行覆盖，≥70% 分支覆盖）
- 每次发布前进行安全扫描

---

## Java 开发规范

### 1. 代码风格

#### 1.1 命名规范

```java
// 包名：小写加点号
package chat.octet.accordion.action.api;

// 类名：PascalCase
public class ApiAction extends AbstractAction { }
public interface ActionService { }
public enum ActionType { }

// 方法名：camelCase，使用描述性动词
public ExecuteResult executeAction() { }
public void validateInput() { }
public boolean isValid() { }
public String getActionName() { }

// 变量名：camelCase，使用描述性名词
private String actionName;
private int retryCount;
private boolean isEnabled;
private List<String> actionIds;

// 常量：UPPER_SNAKE_CASE
private static final int MAX_RETRY_COUNT = 3;
private static final String DEFAULT_TIMEOUT = "30s";

// 布尔变量：使用 is/has/can 前缀
private boolean isActive;
private boolean hasError;
private boolean canRetry;
```

#### 1.2 文件组织

```java
package chat.octet.accordion.action.api;

// 1. 导入语句（有序且不使用通配符）
import chat.octet.accordion.action.AbstractAction;
import chat.octet.accordion.exceptions.ActionException;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;

/**
 * 2. 类/接口 JavaDoc
 */
@Slf4j
public class ApiAction extends AbstractAction {

    // 3. 静态常量
    private static final int DEFAULT_TIMEOUT = 30;

    // 4. 静态字段
    private static final OkHttpClient SHARED_CLIENT = buildClient();

    // 5. 实例字段
    private final ActionConfig config;
    private String actionId;

    // 6. 构造函数
    public ApiAction(ActionConfig config) {
        super(config);
    }

    // 7. 公共方法
    @Override
    public ExecuteResult execute() throws ActionException {
        // 实现
    }

    // 8. 保护方法
    protected void validate() {
        // 验证逻辑
    }

    // 9. 私有方法
    private void performTask() {
        // 任务逻辑
    }

    // 10. 内部类/接口
    private static class RetryPolicy {
        // 内部类
    }
}
```

#### 1.3 代码格式化

```java
// 行长度：最大 120 字符
public ExecuteResult executeWithRetry(String url, Map<String, String> headers,
                                      int maxRetries, Duration timeout) {
    // 方法体
}

// 缩进：4 个空格（禁用 Tab）
public void example() {
    if (condition) {
        doSomething();
        if (anotherCondition) {
            doAnotherThing();
        }
    }
}

// 大括号：K&R 风格（左大括号在同一行）
public void method() {
    if (condition) {
        // 代码
    } else {
        // 代码
    }
}

// 空格：一致使用
int result = a + b;             // ✓ 运算符周围有空格
method(arg1, arg2, arg3);       // ✓ 逗号后有空格
if (condition) {                // ✓ 关键字后有空格
```

#### 1.4 JavaDoc 规范

每个公共类、接口和方法必须有 JavaDoc：

```java
/**
 * 执行带重试机制和超时控制的 API 动作。
 *
 * <p>此动作向外部 REST API 发送 HTTP 请求，具有可配置的重试逻辑。
 * 如果请求因网络问题或服务器错误而失败，将使用指数退避策略自动重试。</p>
 *
 * <p><strong>功能特性：</strong></p>
 * <ul>
 *   <li>每个请求可配置超时时间</li>
 *   <li>指数退避重试策略</li>
 *   <li>支持所有标准 HTTP 方法（GET、POST、PUT、DELETE 等）</li>
 *   <li>自定义请求头支持</li>
 *   <li>请求/响应体处理</li>
 * </ul>
 *
 * <p><strong>使用示例：</strong></p>
 * <pre>{@code
 * ActionConfig config = ActionConfig.builder()
 *     .id("api-001")
 *     .actionType(ActionType.API.name())
 *     .actionName("获取用户数据")
 *     .actionParams(ApiParameter.builder()
 *         .url("https://api.example.com/users/123")
 *         .method(HttpMethod.GET.name())
 *         .timeout(30)
 *         .maxRetries(3)
 *         .build())
 *     .build();
 *
 * try (ApiAction action = new ApiAction(config)) {
 *     ExecuteResult result = action.execute();
 *     if (result.isSuccess()) {
 *         String response = result.get("response");
 *         System.out.println("响应: " + response);
 *     }
 * }
 * }</pre>
 *
 * <p><strong>线程安全：</strong>此类不是线程安全的。
 * 每个线程应创建自己的实例。</p>
 *
 * @param session 包含运行时参数和上下文的执行会话
 * @return 包含 HTTP 响应和状态码的执行结果
 * @throws ActionException 如果所有重试尝试后执行失败
 * @throws IllegalArgumentException 如果缺少必需参数或参数无效
 * @throws TimeoutException 如果请求超过配置的超时时间
 * @see AbstractAction
 * @see ApiParameter
 * @since 1.0.0
 * @author William
 */
public ExecuteResult execute(Session session) throws ActionException {
    // 实现
}
```

### 2. 异常处理

```java
// ✓ 推荐：捕获具体异常
public ExecuteResult execute() throws ActionException {
    try {
        Response response = httpClient.newCall(request).execute();
        return processResponse(response);
    } catch (SocketTimeoutException e) {
        log.error("动作 {} 请求超时: {}", actionId, e.getMessage(), e);
        throw new ActionException("请求超时", e);
    } catch (IOException e) {
        log.error("动作 {} IO 错误: {}", actionId, e.getMessage(), e);
        throw new ActionException("HTTP 请求失败", e);
    }
}

// ✗ 不推荐：捕获泛型异常而不重新抛出
public void badExample() {
    try {
        riskyOperation();
    } catch (Exception e) {
        // 静默失败 - 非常糟糕！
        log.error("发生错误", e);
    }
}

// ✓ 推荐：对 AutoCloseable 使用 try-with-resources
public String readFile(String path) throws IOException {
    try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
        return reader.lines().collect(Collectors.joining("\n"));
    }
}

// ✓ 推荐：在异常消息中提供上下文
throw new ActionException(
    String.format("执行动作 '%s' (ID: %s) 失败: %s",
                  actionName, actionId, e.getMessage()),
    e
);
```

### 3. Lombok 使用指南

```java
// ✓ 推荐：适当使用 Lombok
@Slf4j                          // 日志
@Builder                        // 构建器模式
@Data                           // Getter/Setter/toString/equals/hashCode
@NoArgsConstructor              // 无参构造函数
@AllArgsConstructor             // 全参构造函数
public class ApiParameter {
    private String url;
    private String method;
    private int timeout;
}

// ✗ 不推荐：滥用 @SneakyThrows（使异常处理不清晰）
@SneakyThrows  // 不好！
public void method() {
    riskyOperation();
}

// ✗ 不推荐：在子类中使用 @EqualsAndHashCode 而不带 callSuper
@EqualsAndHashCode  // 对子类不好！
public class ApiAction extends AbstractAction { }

// ✓ 推荐：在子类中使用 @EqualsAndHashCode 时带 callSuper
@EqualsAndHashCode(callSuper = true)
public class ApiAction extends AbstractAction { }

// ✗ 不推荐：在包含敏感数据时使用 @ToString
@ToString  // 不好 - 可能暴露密码！
public class DatabaseConfig {
    private String password;  // 会在 toString() 中显示
}

// ✓ 推荐：从 @ToString 中排除敏感字段
@ToString(exclude = {"password", "apiKey"})
public class DatabaseConfig {
    private String username;
    private String password;
}
```

### 4. 日志规范

```java
@Slf4j
public class ApiAction {
    // ✓ 推荐：使用适当的日志级别
    public ExecuteResult execute() {
        log.trace("进入 execute() 方法");  // TRACE: 非常详细的流程
        log.debug("请求参数: {}", params);  // DEBUG: 诊断信息
        log.info("执行动作: {}", actionId);   // INFO: 重要里程碑
        log.warn("重试尝试 {} 失败", attempt); // WARN: 潜在问题
        log.error("动作失败: {}", error, e);     // ERROR: 实际错误
    }

    // ✓ 推荐：使用参数化日志（高效）
    log.info("处理动作 {} 超时 {}", actionId, timeout);

    // ✗ 不推荐：使用字符串连接（低效）
    log.info("处理动作 " + actionId + " 超时 " + timeout);

    // ✓ 推荐：在错误日志中包含上下文
    log.error("执行动作 '{}' (类型: {}, ID: {}) 失败: {}",
              actionName, actionType, actionId, e.getMessage(), e);

    // ✗ 不推荐：记录敏感信息
    log.debug("密码: {}", password);  // 不好！

    // ✓ 推荐：掩码敏感信息
    log.debug("密码: {}", maskPassword(password));
    log.info("API密钥: {}***", apiKey.substring(0, 4));
}
```

---

## 测试规范

### 1. 测试结构

```java
/**
 * ApiAction 的单元测试。
 *
 * <p>测试覆盖：</p>
 * <ul>
 *   <li>正常执行场景</li>
 *   <li>错误处理和边界情况</li>
 *   <li>参数验证</li>
 *   <li>重试机制</li>
 *   <li>超时处理</li>
 * </ul>
 */
@DisplayName("ApiAction 测试")
class ApiActionTest {

    private ApiAction action;
    private Session session;

    @BeforeEach
    void setUp() {
        session = new Session();
        action = createTestAction();
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
            // Given - 准备
            action.prepare(session);

            // When - 执行
            ExecuteResult result = action.execute();

            // Then - 断言
            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isTrue();
        }
    }

    @Nested
    @DisplayName("错误处理测试")
    class ErrorHandlingTests {

        @Test
        @DisplayName("应该对无效 URL 抛出异常")
        void shouldThrowExceptionForInvalidUrl() {
            // Given
            ActionConfig config = createConfigWithInvalidUrl();

            // When/Then
            assertThatThrownBy(() -> new ApiAction(config).execute())
                .isInstanceOf(ActionException.class)
                .hasMessageContaining("Invalid URL");
        }
    }
}
```

### 2. 测试覆盖率要求

- **核心类**（Accordion、AccordionPlan）：100% 覆盖
- **Action 实现**：≥90% 覆盖
- **工具类**：≥85% 覆盖
- **整体项目**：≥80% 行覆盖，≥70% 分支覆盖

```bash
# 运行覆盖率检查
mvn jacoco:check

# 查看覆盖率报告
mvn jacoco:report
# 打开: target/site/jacoco/index.html
```

---

## Git 工作流和提交规范

### 1. 分支策略

```text
main (受保护)
  ├── develop (受保护)
  │   ├── feature/add-api-retry-mechanism
  │   ├── fix/script-execution-bug
  │   └── refactor/optimize-graph-traversal
  ├── release/v1.0.x
  └── hotfix/critical-security-fix
```

#### 1.1 分支命名

```bash
# 格式: <类型>/<简短描述-使用-kebab-case>

# 好的示例
feature/add-retry-mechanism
fix/null-pointer-in-session
hotfix/security-vulnerability
refactor/simplify-action-registration
docs/update-api-documentation

# 不好的示例
feature/Feature1              # 不够描述性
fix/bug                       # 太笼统
my-branch                     # 没有类型前缀
```

### 2. 提交信息规范

我们遵循 [Conventional Commits](https://www.conventionalcommits.org/) 规范。

#### 2.1 提交信息格式

```text
<type>(<scope>): <subject>

<body>

<footer>
```

#### 2.2 类型

- `feat`: 新功能
- `fix`: Bug 修复
- `docs`: 仅文档更改
- `style`: 代码格式（格式化、缺少分号等）
- `refactor`: 代码重构（无功能变更）
- `perf`: 性能改进
- `test`: 添加或更新测试
- `chore`: 其他更改（依赖更新等）
- `ci`: CI/CD 配置更改

#### 2.3 范围

常见范围：

- `api`: API action
- `email`: Email action
- `script`: Script action
- `core`: 核心框架
- `graph`: 图引擎
- `docs`: 文档
- `ci`: CI/CD 配置

#### 2.4 示例

```bash
# 新功能
feat(api): 为 API action 添加重试机制

实现指数退避重试策略，支持配置最大重试次数和重试间隔超时。

- 为 ApiParameter 添加 maxRetries 参数
- 实现指数退避算法
- 添加初始延迟和退避倍数配置
- 更新测试以覆盖重试场景

Closes #123

# Bug 修复
fix(script): 修正脚本执行中的空指针异常

脚本 action 在脚本参数为 null 时抛出 NPE。
添加空检查并抛出带清晰消息的 IllegalArgumentException。

Fixes #456

# 破坏性变更
feat(core)!: 更改 Session 参数 API 以提高类型安全

BREAKING CHANGE: Session.add() 现在需要显式的类型参数。
这提高了类型安全性但需要更新现有代码。

迁移指南：
- 之前: session.add("key", value)
- 之后: session.add("key", value, String.class)

Closes #789

# 多个 Issue
fix(email): 正确处理多个收件人

- 修复只有第一个收件人收到邮件的bug
- 添加邮箱地址格式验证
- 改进无效地址的错误消息

Fixes #111, #222, #333
```

### 3. Git 工作流程

```bash
# 1. 更新本地 main/develop 分支
git checkout main
git pull upstream main

# 2. 从 main/develop 创建新分支
git checkout -b feature/add-retry-mechanism

# 3. 进行修改
# ... 编辑文件 ...

# 4. 暂存并提交更改
git add .
git commit -m "feat(api): 为 API action 添加重试机制"

# 5. 保持分支最新
git fetch upstream
git rebase upstream/main

# 6. 推送到 fork
git push origin feature/add-retry-mechanism
```

---

## 安全规范

### 1. 输入验证

```java
public class SecurityValidator {

    // ✓ 推荐：验证所有输入参数
    public void validateUrl(String url) {
        Objects.requireNonNull(url, "URL 不能为 null");

        if (url.trim().isEmpty()) {
            throw new IllegalArgumentException("URL 不能为空");
        }

        if (url.length() > MAX_URL_LENGTH) {
            throw new IllegalArgumentException("URL 超过最大长度");
        }

        if (!isValidUrlFormat(url)) {
            throw new IllegalArgumentException("无效的 URL 格式");
        }

        if (!isAllowedProtocol(url)) {
            throw new SecurityException("不允许的 URL 协议: " + extractProtocol(url));
        }
    }

    // ✓ 推荐：白名单验证（比黑名单更安全）
    private boolean isAllowedProtocol(String url) {
        String protocol = extractProtocol(url);
        return ALLOWED_PROTOCOLS.contains(protocol);
    }

    private static final Set<String> ALLOWED_PROTOCOLS = Set.of("http", "https");

    // ✓ 推荐：清理输入以防止注入
    public String sanitizeScriptInput(String script) {
        if (script == null) {
            return "";
        }

        // 移除危险模式
        String sanitized = script
            .replaceAll("(?i)System\\.exit", "")
            .replaceAll("(?i)Runtime\\.getRuntime", "")
            .replaceAll("(?i)ProcessBuilder", "");

        return sanitized;
    }

    // ✓ 推荐：验证文件路径以防止目录遍历
    public void validateFilePath(String filePath) {
        if (filePath.contains("..")) {
            throw new SecurityException("检测到路径遍历");
        }

        Path normalizedPath = Paths.get(filePath).normalize();
        Path basePath = Paths.get("/allowed/base/path").normalize();

        if (!normalizedPath.startsWith(basePath)) {
            throw new SecurityException("拒绝访问路径");
        }
    }
}
```

### 2. 敏感数据处理

```java
// ✓ 推荐：永远不要记录敏感信息
@Slf4j
public class ApiAction {
    public void execute(String apiKey, String password) {
        // 不好！
        // log.info("API 密钥: {}", apiKey);

        // 好！
        log.info("API 密钥: {}***", apiKey.substring(0, 4));
        log.info("凭证已验证");
    }
}

// ✓ 推荐：在 toString() 中掩码敏感数据
@ToString(exclude = {"password", "apiKey", "secret"})
public class DatabaseConfig {
    private String host;
    private String username;
    private String password;  // 从 toString 排除
}

// ✓ 推荐：对密码使用 char[]（可以从内存中清除）
public void authenticateUser(String username, char[] password) {
    try {
        authenticate(username, password);
    } finally {
        // 从内存中清除密码
        Arrays.fill(password, '\0');
    }
}
```

### 3. 依赖安全

```bash
# 检查漏洞
mvn dependency-check:check

# 更新依赖
mvn versions:display-dependency-updates

# 在 owasp-suppressions.xml 中抑制误报
```

---

## 快速参考

### 开发检查清单

提交代码前：

- [ ] 代码符合风格规范
- [ ] 所有测试通过：`mvn test`
- [ ] 覆盖率 ≥80%：`mvn jacoco:check`
- [ ] 质量检查通过：`mvn checkstyle:check spotbugs:check pmd:check`
- [ ] 安全扫描通过：`mvn dependency-check:check`
- [ ] JavaDoc 完整
- [ ] 提交信息遵循 Conventional Commits
- [ ] 代码中没有敏感数据

### 常用命令

```bash
# 构建和测试
mvn clean install

# 仅运行测试
mvn test

# 检查覆盖率
mvn jacoco:report && open target/site/jacoco/index.html

# 运行质量检查
mvn checkstyle:check spotbugs:check pmd:check

# 安全扫描
mvn dependency-check:check
```

---

**文档版本：** 1.0
**最后更新：** 2025-01-15
**下次审查日期：** 2024-05-15

如有疑问或需要澄清，请：

- 在 GitHub 上创建 Issue
- 参考[贡献指南](../CONTRIBUTING.Zh_CN.md)
- 查看 [Wiki](https://github.com/eoctet/accordion/wiki)

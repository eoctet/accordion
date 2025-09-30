# 安全政策

## 支持的版本

我们会为安全漏洞发布补丁。以下版本的 Accordion 目前支持安全更新：

| 版本    | 支持状态             |
|-------|------------------|
| 1.0.x | :white_check_mark: |
| < 1.0 | :x:              |

## 报告漏洞

Accordion 团队非常重视安全问题。我们感谢您负责任地披露发现的问题，并将尽最大努力确认您的贡献。

### 如何报告安全漏洞

**请不要通过公开的 GitHub Issues 报告安全漏洞。**

请通过以下方式之一报告：

1. **电子邮件**：向维护者发送邮件 [从 GitHub 个人资料获取安全联系方式]
2. **GitHub 安全公告**：使用 [GitHub Security Advisory](https://github.com/eoctet/accordion/security/advisories/new) 功能

请在报告中包含以下信息：

- 问题类型（例如：缓冲区溢出、SQL 注入、跨站脚本等）
- 与问题相关的源文件完整路径
- 受影响源代码的位置（标签/分支/提交或直接 URL）
- 复现问题所需的任何特殊配置
- 复现问题的详细步骤
- 概念验证或漏洞利用代码（如有可能）
- 问题的影响，包括攻击者可能如何利用它

### 预期响应

提交漏洞报告后，您可以期待：

- **确认**：我们将在 48 小时内确认收到您的漏洞报告
- **沟通**：我们将定期向您发送进展更新
- **时间表**：我们的目标是在 7 天内修补关键漏洞，在 30 天内修补中等漏洞
- **致谢**：如果您愿意，我们将公开感谢您的负责任披露（除非您希望保持匿名）

## 用户安全最佳实践

### 依赖管理

Accordion 使用 OWASP Dependency-Check 扫描依赖项中的已知漏洞：

```bash
# 运行安全扫描
mvn dependency-check:check

# 如果 CVSS 评分 ≥ 7.0，安全扫描将导致构建失败
```

### 安全配置

在应用程序中使用 Accordion 时，请遵循以下安全最佳实践：

#### 1. 输入验证

在创建 Action 配置之前，始终验证和清理输入参数：

```java
// ❌ 错误：没有验证
ActionConfig apiAction = ActionConfig.builder()
    .actionParams(ApiParameter.builder()
        .url(userInput)  // 潜在的恶意 URL
        .build())
    .build();

// ✅ 正确：验证输入
public void createApiAction(String userInput) {
    if (!isValidUrl(userInput)) {
        throw new IllegalArgumentException("Invalid URL format");
    }
    if (!isAllowedDomain(userInput)) {
        throw new SecurityException("URL domain not in allowlist");
    }

    ActionConfig apiAction = ActionConfig.builder()
        .actionParams(ApiParameter.builder()
            .url(userInput)
            .build())
        .build();
}
```

#### 2. 脚本执行安全

使用 `ScriptAction` 或 `ShellAction` 时，对用户输入要极其谨慎：

```java
// ❌ 错误：命令注入风险
ScriptParameter.builder()
    .script("echo " + userInput)
    .build();

// ✅ 正确：使用参数化方法或严格验证
ScriptParameter.builder()
    .script("echo ${safeInput}")
    .build();
```

**建议**：避免在 `ScriptAction` 和 `ShellAction` 中使用不受信任的输入。如有必要：

- 使用命令白名单
- 实施严格的输入验证
- 在沙箱环境中运行
- 应用最小权限原则

#### 3. 敏感数据处理

永远不要记录或暴露敏感信息：

```java
// ❌ 错误：记录敏感数据
log.info("API request with token: {}", apiToken);

// ✅ 正确：屏蔽或省略敏感数据
log.info("API request initiated for endpoint: {}", endpoint);
log.debug("Request headers: {}", sanitizeHeaders(headers));

private Map<String, String> sanitizeHeaders(Map<String, String> headers) {
    return headers.entrySet().stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            e -> e.getKey().toLowerCase().contains("auth")
                ? "***REDACTED***"
                : e.getValue()
        ));
}
```

#### 4. 安全的 API 通信

始终使用 HTTPS 并验证证书：

```java
// ✅ 正确：强制使用 HTTPS
ApiParameter.builder()
    .url(url)
    .validateCertificate(true)  // 确保证书验证
    .timeout(30000)
    .build();
```

#### 5. 资源限制

实施超时和资源限制以防止拒绝服务攻击：

```java
// ✅ 正确：设置适当的超时
ActionConfig.builder()
    .timeout(Duration.ofSeconds(30))
    .retryPolicy(RetryPolicy.builder()
        .maxRetries(3)
        .backoff(Duration.ofSeconds(2))
        .build())
    .build();
```

#### 6. 错误处理

不要在错误消息中暴露内部系统细节：

```java
// ❌ 错误：暴露内部细节
catch (Exception e) {
    throw new ActionException("Database connection failed: " +
        "jdbc:mysql://internal-db.company.local:3306/app", e);
}

// ✅ 正确：通用错误消息
catch (Exception e) {
    log.error("Database operation failed", e);
    throw new ActionException("Database operation failed. Please contact support.");
}
```

### 依赖安全

#### 定期更新

保持 Accordion 及其依赖项的更新：

```xml
<!-- 检查依赖更新 -->
<dependency>
    <groupId>chat.octet</groupId>
    <artifactId>accordion</artifactId>
    <version>LATEST_STABLE_VERSION</version>
</dependency>
```

监控以下内容以获取安全更新：

- [Accordion 发布版本](https://github.com/eoctet/accordion/releases)
- [Accordion 安全公告](https://github.com/eoctet/accordion/security/advisories)
- OWASP Dependency-Check 报告

#### 传递依赖

定期审计传递依赖：

```bash
# 查看依赖树
mvn dependency:tree

# 检查更新
mvn versions:display-dependency-updates

# 运行安全扫描
mvn dependency-check:check
```

### CI/CD 安全

#### GitHub Actions 安全

Accordion 项目使用 GitHub Actions 进行 CI/CD。安全措施包括：

1. **依赖扫描**：自动化 OWASP 依赖检查
2. **代码质量**：使用 Checkstyle、SpotBugs、PMD 进行静态分析
3. **密钥扫描**：启用 GitHub 的密钥扫描
4. **Dependabot**：自动化依赖更新
5. **分支保护**：主分支需要审查和状态检查

#### 工作流安全

```yaml
# .github/workflows/security-scan.yml
- name: OWASP Dependency Check
  run: mvn dependency-check:check

- name: Upload Security Report
  if: failure()
  uses: actions/upload-artifact@v3
  with:
    name: dependency-check-report
    path: target/dependency-check-report.html
```

## 已知安全注意事项

### 1. Script 和 Shell Actions

**风险级别**：高

Script 和 Shell actions 可以执行任意代码。使用时要极其谨慎：

- 永远不要将未经清理的用户输入传递给这些 actions
- 尽可能在隔离环境中运行
- 对允许的操作应用严格的白名单
- 使用具有最小权限的专用服务账户

### 2. API Actions

**风险级别**：中

API actions 向外部服务发起 HTTP 请求：

- 根据白名单验证 URL
- 仅使用 HTTPS
- 实施请求超时
- 安全地处理认证令牌
- 验证和清理响应数据

### 3. Email Actions

**风险级别**：中

Email actions 可能暴露敏感信息：

- 验证收件人地址
- 清理邮件内容以防止注入
- 使用安全的 SMTP 连接（TLS/SSL）
- 安全存储凭据（环境变量或密钥管理）

### 4. 反序列化

**风险级别**：中

Accordion 使用 Jackson 进行 JSON 序列化：

- 保持 Jackson 依赖项更新
- 避免反序列化不受信任的数据
- 尽可能使用显式类型引用
- 仅在绝对必要时启用默认类型

## 安全测试

### 静态分析

```bash
# 运行所有静态分析工具
mvn verify

# 单独运行工具
mvn checkstyle:check
mvn spotbugs:check
mvn pmd:check
```

### 依赖扫描

```bash
# 运行 OWASP Dependency Check
mvn dependency-check:check

# 查看报告
open target/dependency-check-report.html
```

### 代码覆盖率

保持高测试覆盖率以捕获潜在的安全问题：

```bash
# 运行带覆盖率的测试
mvn test

# 检查覆盖率阈值
mvn jacoco:check

# 查看覆盖率报告
open target/site/jacoco/index.html
```

## 贡献者安全检查清单

在提交代码之前：

- [ ] 对所有外部数据实施输入验证
- [ ] 没有硬编码的密钥或凭据
- [ ] 日志中正确屏蔽敏感数据
- [ ] 错误消息不暴露内部细节
- [ ] 依赖项是最新的并已扫描
- [ ] 单元测试覆盖与安全相关的代码路径
- [ ] 至少一位维护者进行了代码审查
- [ ] 静态分析工具无警告通过
- [ ] OWASP 依赖检查通过（CVSS < 7.0）

## 安全资源

### 工具

- [OWASP Dependency-Check](https://owasp.org/www-project-dependency-check/)
- [SpotBugs](https://spotbugs.github.io/)
- [PMD](https://pmd.github.io/)
- [Checkstyle](https://checkstyle.org/)

### 参考资料

- [OWASP Top Ten](https://owasp.org/www-project-top-ten/)
- [CWE Top 25](https://cwe.mitre.org/top25/)
- [NIST NVD](https://nvd.nist.gov/)
- [GitHub Security Advisories](https://github.com/advisories)

## 披露政策

当我们收到安全漏洞报告时，我们将：

1. 确认问题并确定受影响的版本
2. 审计代码以查找类似问题
3. 为所有支持的版本准备修复
4. 尽快发布补丁
5. 在 GitHub 上发布安全公告

## 安全名人堂

我们感谢以下负责任地披露漏洞的安全研究人员：

<!-- 收到安全报告后将更新此部分 -->

_尚未收到报告。_

---

**最后更新**：2025-01-15

有关此政策的问题，请提交 issue 或联系维护者。

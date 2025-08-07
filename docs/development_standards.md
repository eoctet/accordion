# Accordion 项目开发规范

## 1. 技术规范

### 1.1 技术栈要求

#### 核心技术

- **Java版本**: Java 17 (最低要求)
- **构建工具**: Maven 3.8+
- **编码格式**: UTF-8
- **IDE**: IntelliJ IDEA (推荐)

#### 核心依赖库

- **Lombok**: 减少样板代码
- **Google Guava**: 工具类和集合框架
- **Apache Commons**: Lang3, Email, Text 工具
- **Jackson**: JSON序列化/反序列化
- **SLF4J + Logback**: 日志框架
- **OkHttp**: HTTP客户端
- **Aviator**: 脚本执行引擎
- **DOM4J**: XML处理

#### 测试框架

- **JUnit 5**: 单元测试框架
- **Mockito**: Mock框架
- **AssertJ**: 断言库

### 1.2 项目结构规范

```text
src/main/java/chat/octet/accordion/
├── Accordion.java                    # 主执行引擎
├── AccordionPlan.java               # 计划构建器
├── action/                          # 动作框架
│   ├── AbstractAction.java         # 动作基类
│   ├── ActionService.java          # 动作服务接口
│   ├── ActionRegister.java         # 动作注册器
│   ├── model/                      # 动作模型
│   ├── api/                        # API动作
│   ├── base/                       # 基础动作
│   ├── email/                      # 邮件动作
│   ├── script/                     # 脚本动作
│   └── shell/                      # Shell动作
├── core/                           # 核心框架
│   ├── entity/                     # 数据实体
│   ├── enums/                      # 枚举类型
│   ├── condition/                  # 条件框架
│   └── handler/                    # 类型处理器
├── graph/                          # 图引擎
│   ├── entity/                     # 图实体
│   └── model/                      # 图模型
├── utils/                          # 工具类
└── exceptions/                     # 自定义异常
```

### 1.3 编码规范

#### 命名规范

- **类名**: 使用PascalCase，如 `AccordionPlan`
- **方法名**: 使用camelCase，如 `executeAction`
- **变量名**: 使用camelCase，如 `actionResult`
- **常量名**: 使用UPPER_SNAKE_CASE，如 `DEFAULT_TIMEOUT`
- **包名**: 全小写，使用点分隔，如 `chat.octet.accordion.action`

#### 代码风格

- 使用4个空格缩进，不使用Tab
- 行长度不超过120字符
- 左大括号不换行
- 方法间空一行
- 类成员按顺序：常量、字段、构造函数、方法

#### 注释规范

```java
/**
 * 动作执行服务接口
 * 
 * @author 作者名
 * @since 1.0.0
 */
public interface ActionService {
    
    /**
     * 执行指定动作
     * 
     * @param action 要执行的动作
     * @param session 会话上下文
     * @return 执行结果
     * @throws ActionException 动作执行异常
     */
    ActionResult execute(AbstractAction action, Session session) throws ActionException;
}
```

#### Lombok使用规范

- 优先使用 `@Data` 替代getter/setter
- 使用 `@Builder` 构建复杂对象
- 使用 `@Slf4j` 替代手动创建Logger
- 避免在实体类上使用 `@EqualsAndHashCode(callSuper = true)`

## 2. 代码版本控制规范

### 2.1 Git工作流

采用 **Git Flow** 工作流模式：

#### 分支类型

- **main**: 主分支，保持稳定可发布状态
- **develop**: 开发分支，集成最新开发功能
- **feature/**: 功能分支，格式：`feature/功能描述`
- **release/**: 发布分支，格式：`release/版本号`
- **hotfix/**: 热修复分支，格式：`hotfix/问题描述`

#### 分支命名规范

```bash
# 功能分支
feature/add-email-action
feature/improve-graph-visualization

# 发布分支
release/v1.2.0

# 热修复分支
hotfix/fix-memory-leak
hotfix/security-patch
```

### 2.2 提交规范

#### 提交信息格式

```text
<type>(<scope>): <subject>

<body>

<footer>
```

#### 提交类型

- **feat**: 新功能
- **fix**: 修复bug
- **docs**: 文档更新
- **style**: 代码格式调整
- **refactor**: 代码重构
- **test**: 测试相关
- **chore**: 构建过程或辅助工具变动

#### 提交示例

```bash
feat(action): add email action support

- Add EmailAction class with SMTP configuration
- Implement email template rendering
- Add unit tests for email functionality

Closes #123
```

### 2.3 代码审查规范

#### Pull Request要求

- 标题清晰描述变更内容
- 提供详细的变更说明
- 包含相关的测试用例
- 通过所有CI检查
- 至少一个团队成员审查通过

#### 审查检查点

- [ ] 代码符合编码规范
- [ ] 包含充分的单元测试
- [ ] 文档已更新
- [ ] 无明显性能问题
- [ ] 异常处理完善

## 3. 发布管理规范

### 3.1 版本号规范

采用 **语义化版本控制 (SemVer)**：`MAJOR.MINOR.PATCH`

- **MAJOR**: 不兼容的API变更
- **MINOR**: 向后兼容的功能新增
- **PATCH**: 向后兼容的问题修正

#### 版本示例

```text
1.0.0 - 首个稳定版本
1.1.0 - 新增功能
1.1.1 - 修复bug
2.0.0 - 重大变更，不向后兼容
```

### 3.2 发布流程

#### 发布前检查

1. 所有测试通过
2. 代码覆盖率达标 (>80%)
3. 文档更新完成
4. CHANGELOG.md 更新
5. 版本号更新

#### 发布步骤

```bash
# 1. 创建发布分支
git checkout -b release/v1.2.0 develop

# 2. 更新版本号
mvn versions:set -DnewVersion=1.2.0

# 3. 运行完整测试
mvn clean test

# 4. 构建和打包
mvn clean package

# 5. 合并到main分支
git checkout main
git merge --no-ff release/v1.2.0

# 6. 创建标签
git tag -a v1.2.0 -m "Release version 1.2.0"

# 7. 推送到远程
git push origin main --tags
```

### 3.3 Maven Central发布

```bash
# 部署到Maven Central
mvn clean deploy -P release
```

## 4. 测试管理规范

### 4.1 测试策略

#### 测试金字塔

- **单元测试**: 70% - 测试单个类或方法
- **集成测试**: 20% - 测试组件间交互
- **端到端测试**: 10% - 测试完整工作流

#### 测试覆盖率要求

- **总体覆盖率**: ≥80%
- **核心业务逻辑**: ≥90%
- **工具类**: ≥85%

### 4.2 测试组织结构

```text
src/test/java/chat/octet/accordion/
├── examples/                        # 示例测试
├── test/                           # 单元测试
│   ├── action/                     # 动作测试
│   ├── core/                       # 核心测试
│   ├── graph/                      # 图引擎测试
│   └── utils/                      # 工具类测试
└── integration/                    # 集成测试
```

### 4.3 测试编写规范

#### 测试类命名

```java
// 被测试类: EmailAction
// 测试类: EmailActionTest

// 被测试类: JsonUtils
// 测试类: JsonUtilsTest
```

#### 测试方法命名

```java
@Test
void shouldReturnTrueWhenEmailIsValid() {
    // 测试逻辑
}

@Test
void shouldThrowExceptionWhenEmailIsNull() {
    // 测试逻辑
}
```

#### 测试结构 (AAA模式)

```java
@Test
void shouldCalculateCorrectResult() {
    // Arrange - 准备测试数据
    Calculator calculator = new Calculator();
    int a = 5;
    int b = 3;
    
    // Act - 执行被测试方法
    int result = calculator.add(a, b);
    
    // Assert - 验证结果
    assertThat(result).isEqualTo(8);
}
```

### 4.4 测试执行

#### 本地测试

```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=EmailActionTest

# 运行特定测试方法
mvn test -Dtest=EmailActionTest#shouldSendEmailSuccessfully
```

#### CI/CD集成

- 每次提交自动运行测试
- Pull Request必须通过所有测试
- 发布前运行完整测试套件

### 4.5 Mock使用规范

```java
@ExtendWith(MockitoExtension.class)
class ActionServiceTest {
    
    @Mock
    private EmailService emailService;
    
    @InjectMocks
    private ActionService actionService;
    
    @Test
    void shouldSendEmailWhenActionExecuted() {
        // Given
        EmailAction action = EmailAction.builder()
            .to("test@example.com")
            .subject("Test")
            .build();
        
        when(emailService.send(any())).thenReturn(true);
        
        // When
        ActionResult result = actionService.execute(action, session);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        verify(emailService).send(any());
    }
}
```

## 5. 持续集成规范

### 5.1 GitHub Actions配置

```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Cache Maven packages
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
    
    - name: Run tests
      run: mvn clean test
    
    - name: Generate test report
      uses: dorny/test-reporter@v1
      if: success() || failure()
      with:
        name: Maven Tests
        path: target/surefire-reports/*.xml
        reporter: java-junit
```

### 5.2 质量门禁

- 所有测试必须通过
- 代码覆盖率达到要求
- 静态代码分析通过
- 安全扫描无高危漏洞

## 6. 文档规范

### 6.1 文档类型

- **README.md**: 项目介绍和快速开始
- **API文档**: JavaDoc生成
- **开发手册**: 详细开发指南
- **CHANGELOG.md**: 版本变更记录

### 6.2 文档更新要求

- 新功能必须更新相关文档
- API变更必须更新JavaDoc
- 重大变更必须更新开发手册
- 每次发布必须更新CHANGELOG

---

*本规范将根据项目发展持续更新和完善。*

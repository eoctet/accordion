# Contributing to Accordion

Thank you for your interest in contributing to Accordion! This document provides guidelines and instructions for contributing to the project.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Development Workflow](#development-workflow)
- [Coding Standards](#coding-standards)
- [Testing Guidelines](#testing-guidelines)
- [Pull Request Process](#pull-request-process)
- [Commit Message Guidelines](#commit-message-guidelines)
- [Issue Guidelines](#issue-guidelines)
- [Release Process](#release-process)

## Code of Conduct

This project adheres to a code of conduct that all contributors are expected to follow:

- **Be respectful**: Treat everyone with respect and consideration
- **Be collaborative**: Work together constructively
- **Be professional**: Maintain professionalism in all interactions
- **Be inclusive**: Welcome and support people of all backgrounds

## Getting Started

### Prerequisites

Before you begin, ensure you have the following installed:

- **Java 17 or higher**: [Download JDK](https://adoptium.net/)
- **Maven 3.6+**: [Download Maven](https://maven.apache.org/download.cgi)
- **Git**: [Download Git](https://git-scm.com/downloads)
- **IDE**: IntelliJ IDEA (recommended) or Eclipse

### Fork and Clone

1. **Fork the repository** on GitHub
2. **Clone your fork**:

   ```bash
   git clone https://github.com/YOUR_USERNAME/accordion.git
   cd accordion
   ```

3. **Add upstream remote**:

   ```bash
   git remote add upstream https://github.com/eoctet/accordion.git
   ```

### Verify Your Setup

Run the following commands to verify your setup:

```bash
# Build the project
mvn clean install

# Run tests
mvn test

# Run code quality checks
mvn checkstyle:check spotbugs:check pmd:check
```

If all commands succeed, you're ready to contribute!

## Development Setup

### IDE Configuration

#### IntelliJ IDEA

1. **Import Project**: `File → Open` → Select `pom.xml`
2. **Code Style**:
   - `File → Settings → Editor → Code Style → Java`
   - Import scheme from `checkstyle.xml`
3. **Enable Lombok**:
   - Install Lombok plugin
   - `File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors`
   - Enable annotation processing
4. **Checkstyle Plugin**:
   - Install CheckStyle-IDEA plugin
   - Configure to use project's `checkstyle.xml`

#### Eclipse

1. **Import Project**: `File → Import → Maven → Existing Maven Projects`
2. **Install Lombok**: Follow [Lombok Eclipse setup](https://projectlombok.org/setup/eclipse)
3. **Checkstyle Plugin**: Install Eclipse Checkstyle Plugin

### Local Development

```bash
# Quick compile (skip tests)
mvn clean compile -DskipTests

# Run specific test
mvn test -Dtest=AccordionTest

# Run specific test method
mvn test -Dtest=AccordionTest#shouldExecuteSequentialPlan

# Generate test coverage report
mvn clean test jacoco:report
# Open: target/site/jacoco/index.html

# Run security scan
mvn dependency-check:check
# Open: target/dependency-check-report.html
```

## Development Workflow

### 1. Create a Branch

Always create a new branch for your work:

```bash
# Update your main branch
git checkout main
git pull upstream main

# Create a feature branch
git checkout -b feature/your-feature-name

# Or for bug fixes
git checkout -b fix/issue-description

# Or for documentation
git checkout -b docs/documentation-update
```

**Branch Naming Convention**:

- `feature/*` - New features
- `fix/*` - Bug fixes
- `docs/*` - Documentation updates
- `refactor/*` - Code refactoring
- `test/*` - Test additions or modifications
- `chore/*` - Build/tool updates

### 2. Make Your Changes

Follow these guidelines when making changes:

1. **Write clean code**: Follow the [Coding Standards](#coding-standards)
2. **Add tests**: Ensure your code is well-tested
3. **Update documentation**: Update JavaDoc and README if needed
4. **Keep commits atomic**: Each commit should represent a logical change

### 3. Test Your Changes

Before committing, ensure all tests pass:

```bash
# Run all tests
mvn clean test

# Check code coverage
mvn jacoco:check
# Requirement: Line coverage ≥80%, Branch coverage ≥70%

# Run code quality checks
mvn checkstyle:check spotbugs:check pmd:check

# Run security scan
mvn dependency-check:check
```

### 4. Commit Your Changes

Follow the [Commit Message Guidelines](#commit-message-guidelines):

```bash
git add .
git commit -m "feat(api): add retry mechanism for API action

- Implement exponential backoff retry
- Add configurable max retry attempts
- Add timeout between retries

Closes #123"
```

### 5. Push and Create Pull Request

```bash
# Push to your fork
git push origin feature/your-feature-name

# Create a Pull Request on GitHub
```

## Coding Standards

### Java Code Style

We follow a strict code style based on industry best practices:

#### Naming Conventions

```java
// Classes: PascalCase
public class ApiAction extends AbstractAction { }

// Methods: camelCase
public ExecuteResult executeAction() { }

// Variables: camelCase
private String actionName;
private int retryCount;

// Constants: UPPER_SNAKE_CASE
private static final int MAX_RETRY_COUNT = 3;
private static final String DEFAULT_TIMEOUT = "30s";

// Packages: lowercase with dots
package chat.octet.accordion.action.api;
```

#### Code Structure

Order class members as follows:

```java
public class ExampleAction extends AbstractAction {
    // 1. Static constants
    private static final String DEFAULT_VALUE = "default";

    // 2. Static fields
    private static final OkHttpClient SHARED_CLIENT = ...;

    // 3. Instance fields
    private final ActionConfig config;
    private String actionName;

    // 4. Constructors
    public ExampleAction(ActionConfig config) {
        super(config);
    }

    // 5. Public methods
    @Override
    public ExecuteResult execute() throws ActionException {
        // Implementation
    }

    // 6. Protected methods
    protected void validate() {
        // Validation logic
    }

    // 7. Private methods
    private void performTask() {
        // Task logic
    }
}
```

#### Documentation

**Every public class and method must have JavaDoc**:

```java
/**
 * Executes API action with retry mechanism.
 *
 * <p>This action calls external REST APIs with configurable retry logic.
 * If the request fails, it will retry with exponential backoff.</p>
 *
 * <p>Example usage:</p>
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
 * @param session the execution session containing parameters
 * @return execution result with response data
 * @throws ActionException if execution fails after all retries
 * @see AbstractAction
 * @see ApiParameter
 * @since 1.0.0
 */
public ExecuteResult execute(Session session) throws ActionException {
    // Implementation
}
```

#### Exception Handling

```java
// DO: Catch specific exceptions
try {
    result = performAction();
} catch (IOException e) {
    log.error("IO error in action {}: {}", actionId, e.getMessage(), e);
    setExecuteThrowable(e);
    throw new ActionException("Failed to execute action", e);
} catch (TimeoutException e) {
    log.error("Timeout in action {}: {}", actionId, e.getMessage(), e);
    setExecuteThrowable(e);
    throw new ActionException("Action timed out", e);
}

// DON'T: Catch generic Exception without re-throwing
try {
    result = performAction();
} catch (Exception e) {
    // Silent failure - BAD!
}
```

#### Resource Management

```java
// DO: Use try-with-resources
try (OkHttpClient client = new OkHttpClient();
     Response response = client.newCall(request).execute()) {
    return processResponse(response);
}

// DO: Implement AutoCloseable for cleanup
@Override
public void close() {
    if (resource != null) {
        try {
            resource.close();
        } catch (Exception e) {
            log.warn("Failed to close resource: {}", e.getMessage());
        }
    }
}
```

#### Lombok Usage

```java
// DO: Use Lombok appropriately
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

// DON'T: Overuse @SneakyThrows
// DON'T: Use @EqualsAndHashCode without callSuper in subclasses
```

### Code Quality Tools

All code must pass the following checks:

1. **Checkstyle**: Code style validation
   - Configuration: `checkstyle.xml`
   - Suppressions: `checkstyle-suppressions.xml`

2. **SpotBugs**: Bug pattern detection
   - Exclusions: `spotbugs-exclude.xml`

3. **PMD**: Code analysis
   - Ruleset: `pmd-ruleset.xml`

4. **JaCoCo**: Code coverage
   - Minimum: 80% line coverage, 70% branch coverage

## Testing Guidelines

### Test Structure

```java
package chat.octet.accordion.action.api;

import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for ApiAction.
 *
 * Test Coverage:
 * - Normal execution flow
 * - Error handling
 * - Parameter validation
 * - Edge cases
 */
@DisplayName("ApiAction Tests")
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
    @DisplayName("Normal Execution Tests")
    class NormalExecutionTests {

        @Test
        @DisplayName("Should execute GET request successfully")
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
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle invalid URL")
        void shouldHandleInvalidUrl() {
            // When/Then
            assertThatThrownBy(() -> action.execute())
                .isInstanceOf(ActionException.class)
                .hasMessageContaining("Invalid URL");
        }
    }
}
```

### Test Requirements

1. **Coverage**:
   - Core classes: 100% coverage
   - Action implementations: ≥90% coverage
   - Overall: ≥80% line coverage, ≥70% branch coverage

2. **Naming**: Use descriptive test names
   - ✅ `shouldExecuteGetRequestSuccessfully()`
   - ❌ `test1()`

3. **AAA Pattern**: Arrange-Act-Assert

   ```java
   @Test
   void testExample() {
       // Arrange (Given)
       ActionConfig config = createConfig();

       // Act (When)
       ExecuteResult result = action.execute();

       // Assert (Then)
       assertThat(result.isSuccess()).isTrue();
   }
   ```

4. **Test Categories**:
   - Unit tests: `src/test/java/**/*Test.java`
   - Integration tests: `src/test/java/**/integration/*IntegrationTest.java`
   - Performance tests: `src/test/java/**/performance/*PerformanceTest.java`

## Pull Request Process

### Before Submitting

Complete this checklist before submitting a PR:

- [ ] Code follows the [Coding Standards](#coding-standards)
- [ ] All tests pass: `mvn test`
- [ ] Code coverage meets requirements: `mvn jacoco:check`
- [ ] Code quality checks pass: `mvn checkstyle:check spotbugs:check pmd:check`
- [ ] Security scan passes: `mvn dependency-check:check`
- [ ] Documentation is updated (JavaDoc, README, examples)
- [ ] Commit messages follow the [guidelines](#commit-message-guidelines)
- [ ] Branch is up-to-date with main

### PR Template

When creating a PR, include:

```markdown
## Description
Brief description of the changes

## Type of Change
- [ ] Bug fix (non-breaking change which fixes an issue)
- [ ] New feature (non-breaking change which adds functionality)
- [ ] Breaking change (fix or feature that would cause existing functionality to not work as expected)
- [ ] Documentation update

## Related Issues
Closes #123

## Changes Made
- Added retry mechanism to API action
- Implemented exponential backoff
- Added configuration for max retries

## Testing
- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] All tests pass locally
- [ ] Test coverage ≥80%

## Checklist
- [ ] Code follows project style guidelines
- [ ] Self-review completed
- [ ] Documentation updated
- [ ] No new warnings introduced
```

### PR Review Process

1. **Automated Checks**: CI/CD pipeline will run automatically
   - Build and compile
   - Run all tests
   - Code quality checks (Checkstyle, SpotBugs, PMD)
   - Security scan (OWASP)

2. **Code Review**: At least one maintainer must approve
   - Code quality and style
   - Test coverage
   - Documentation
   - Design and architecture

3. **Merge**: Once approved and all checks pass
   - Squash and merge (default)
   - Rebase and merge (for clean history)

## Commit Message Guidelines

We follow the [Conventional Commits](https://www.conventionalcommits.org/) specification.

### Format

```text
<type>(<scope>): <subject>

<body>

<footer>
```

### Type

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation only
- `style`: Code style changes (formatting, missing semicolons, etc.)
- `refactor`: Code refactoring (no functional changes)
- `perf`: Performance improvements
- `test`: Adding or updating tests
- `chore`: Build process or auxiliary tool changes
- `ci`: CI/CD configuration changes

### Scope

The scope should be the area of the codebase affected:

- `api`: API action
- `email`: Email action
- `script`: Script action
- `shell`: Shell action
- `core`: Core framework
- `graph`: Graph engine
- `docs`: Documentation
- `ci`: CI/CD configuration

### Examples

```bash
# Feature
feat(api): add retry mechanism for API action

Implement exponential backoff retry strategy with configurable
max retry attempts and timeout between retries.

Closes #123

# Bug fix
fix(script): correct script execution error handling

The script action was not properly catching script syntax errors,
causing the entire plan to fail. Now wraps errors in ActionException.

Fixes #456

# Documentation
docs(readme): update installation instructions

Add instructions for Gradle dependency configuration and
update Maven central version badge.

# Breaking change
feat(core)!: change Session parameter API

BREAKING CHANGE: Session.add() now requires explicit type parameter
to improve type safety. Migration guide added to docs.

Closes #789
```

### Guidelines

1. **Subject line**:
   - Use imperative mood ("add" not "added" or "adds")
   - Don't capitalize first letter
   - No period at the end
   - Maximum 72 characters

2. **Body**:
   - Explain what and why, not how
   - Wrap at 72 characters
   - Separate from subject with blank line

3. **Footer**:
   - Reference issues: `Closes #123`, `Fixes #456`
   - Note breaking changes: `BREAKING CHANGE: description`

## Issue Guidelines

### Before Creating an Issue

1. **Search existing issues**: Check if the issue already exists
2. **Check documentation**: Review README and Wiki
3. **Verify with latest version**: Ensure you're using the latest release

### Issue Types

Use the appropriate issue template:

#### Bug Report

```markdown
**Describe the bug**
A clear description of what the bug is.

**To Reproduce**
Steps to reproduce the behavior:
1. Create AccordionPlan with '...'
2. Execute with '...'
3. See error

**Expected behavior**
What you expected to happen.

**Actual behavior**
What actually happened.

**Environment**
- Accordion version: [e.g., 1.0.1]
- Java version: [e.g., 17]
- OS: [e.g., Ubuntu 20.04]

**Additional context**
Stack traces, logs, or screenshots.
```

#### Feature Request

```markdown
**Is your feature request related to a problem?**
A clear description of the problem.

**Describe the solution you'd like**
A clear description of what you want to happen.

**Describe alternatives you've considered**
Other solutions or features you've considered.

**Additional context**
Any other context or screenshots.
```

## Release Process

### Version Numbering

We follow [Semantic Versioning](https://semver.org/):

- `MAJOR.MINOR.PATCH` (e.g., 1.0.1)
- **MAJOR**: Breaking changes
- **MINOR**: New features (backward compatible)
- **PATCH**: Bug fixes (backward compatible)

### Release Steps

Releases are managed by maintainers only:

1. **Update version** in `pom.xml`
2. **Update CHANGELOG.md** with release notes
3. **Create release branch**: `release/v1.0.1`
4. **Tag the release**: `v1.0.1`
5. **Push tag**: Triggers automated deployment to Maven Central
6. **Create GitHub Release** with release notes

### Changelog

Each release must have a CHANGELOG entry:

```markdown
## [1.0.1] - 2024-01-15

### Added
- New retry mechanism for API action
- Exponential backoff configuration

### Changed
- Improved error messages in script action
- Updated dependencies

### Fixed
- Fixed script execution error handling
- Corrected email action parameter validation

### Security
- Updated Jackson to 2.16.1 (CVE-2023-xxxxx)
```

## Security

### Reporting Security Vulnerabilities

**DO NOT** create public issues for security vulnerabilities.

Instead:

1. Email security concerns to the maintainers
2. Include detailed information about the vulnerability
3. Wait for confirmation before public disclosure

### Security Scanning

All dependencies are scanned weekly for vulnerabilities:

- OWASP Dependency Check runs automatically
- High/Critical vulnerabilities fail the build
- Security issues are tracked in GitHub Issues

## Getting Help

If you need help:

1. **Documentation**: Check [README](README.md) and [Wiki](https://github.com/eoctet/accordion/wiki)
2. **Issues**: Search [existing issues](https://github.com/eoctet/accordion/issues)
3. **Discussions**: Start a [discussion](https://github.com/eoctet/accordion/discussions)
4. **Examples**: Review [examples](src/test/java/chat/octet/accordion/examples/)

## License

By contributing to Accordion, you agree that your contributions will be licensed under the [Apache License 2.0](LICENSE).

---

Thank you for contributing to Accordion! 🎉

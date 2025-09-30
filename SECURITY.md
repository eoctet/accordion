# Security Policy

## Supported Versions

We release patches for security vulnerabilities. The following versions of Accordion are currently being supported with
security updates:

| Version | Supported          |
|---------|--------------------|
| 1.0.x   | :white_check_mark: |
| < 1.0   | :x:                |

## Reporting a Vulnerability

The Accordion team takes security bugs seriously. We appreciate your efforts to responsibly disclose your findings and
will make every effort to acknowledge your contributions.

### How to Report a Security Vulnerability

**Please do not report security vulnerabilities through public GitHub issues.**

Instead, please report them via one of the following methods:

1. **Email**: Send an email to the maintainer at [security contact from GitHub profile]
2. **GitHub Security Advisory**: Use
   the [GitHub Security Advisory](https://github.com/eoctet/accordion/security/advisories/new) feature

Please include the following information in your report:

- Type of issue (e.g., buffer overflow, SQL injection, cross-site scripting, etc.)
- Full paths of source file(s) related to the manifestation of the issue
- The location of the affected source code (tag/branch/commit or direct URL)
- Any special configuration required to reproduce the issue
- Step-by-step instructions to reproduce the issue
- Proof-of-concept or exploit code (if possible)
- Impact of the issue, including how an attacker might exploit it

### What to Expect

After you have submitted a vulnerability report, you can expect:

- **Acknowledgment**: We will acknowledge receipt of your vulnerability report within 48 hours
- **Communication**: We will send you regular updates about our progress
- **Timeline**: We aim to patch critical vulnerabilities within 7 days and moderate vulnerabilities within 30 days
- **Credit**: If you desire, we will publicly acknowledge your responsible disclosure (unless you prefer to remain
  anonymous)

## Security Best Practices for Users

### Dependency Management

Accordion uses OWASP Dependency-Check to scan for known vulnerabilities in dependencies:

```bash
# Run security scan
mvn dependency-check:check

# Security scan fails build if CVSS score ≥ 7.0
```

### Secure Configuration

When using Accordion in your applications, follow these security best practices:

#### 1. Input Validation

Always validate and sanitize input parameters before creating Action configurations:

```java
// ❌ BAD: No validation
ActionConfig apiAction = ActionConfig.builder()
                .actionParams(ApiParameter.builder()
                        .url(userInput)  // Potentially malicious URL
                        .build())
                .build();

// ✅ GOOD: Validate input
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

#### 2. Script Execution Safety

When using `ScriptAction` or `ShellAction`, be extremely cautious with user input:

```java
// ❌ BAD: Command injection risk
ScriptParameter.builder()
    .

script("echo "+userInput)
    .

build();

// ✅ GOOD: Use parameterized approach or strict validation
ScriptParameter.

builder()
    .

script("echo ${safeInput}")
    .

build();
```

**Recommendation**: Avoid using `ScriptAction` and `ShellAction` with untrusted input. If necessary:

- Use allowlists for permitted commands
- Implement strict input validation
- Run in sandboxed environments
- Apply principle of least privilege

#### 3. Sensitive Data Handling

Never log or expose sensitive information:

```java
// ❌ BAD: Logging sensitive data
log.info("API request with token: {}",apiToken);

// ✅ GOOD: Mask or omit sensitive data
log.

info("API request initiated for endpoint: {}",endpoint);
log.

debug("Request headers: {}",sanitizeHeaders(headers));

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

#### 4. Secure API Communication

Always use HTTPS and validate certificates:

```java
// ✅ GOOD: Enforce HTTPS
ApiParameter.builder()
    .

url(url)
    .

validateCertificate(true)  // Ensure certificate validation
    .

timeout(30000)
    .

build();
```

#### 5. Resource Limits

Implement timeouts and resource limits to prevent denial of service:

```java
// ✅ GOOD: Set appropriate timeouts
ActionConfig.builder()
    .

timeout(Duration.ofSeconds(30))
        .

retryPolicy(RetryPolicy.builder()
        .

maxRetries(3)
        .

backoff(Duration.ofSeconds(2))
        .

build())
        .

build();
```

#### 6. Error Handling

Don't expose internal system details in error messages:

```java
// ❌ BAD: Exposing internal details
catch(Exception e){
        throw new

ActionException("Database connection failed: "+
                        "jdbc:mysql://internal-db.company.local:3306/app",e);
}

// ✅ GOOD: Generic error message
        catch(
Exception e){
        log.

error("Database operation failed",e);
    throw new

ActionException("Database operation failed. Please contact support.");
}
```

### Dependency Security

#### Regular Updates

Keep Accordion and its dependencies up to date:

```xml
<!-- Check for dependency updates -->
<dependency>
    <groupId>chat.octet</groupId>
    <artifactId>accordion</artifactId>
    <version>LATEST_STABLE_VERSION</version>
</dependency>
```

Monitor the following for security updates:

- [Accordion Releases](https://github.com/eoctet/accordion/releases)
- [Accordion Security Advisories](https://github.com/eoctet/accordion/security/advisories)
- OWASP Dependency-Check reports

#### Transitive Dependencies

Regularly audit transitive dependencies:

```bash
# View dependency tree
mvn dependency:tree

# Check for updates
mvn versions:display-dependency-updates

# Run security scan
mvn dependency-check:check
```

### CI/CD Security

#### GitHub Actions Security

The Accordion project uses GitHub Actions for CI/CD. Security measures include:

1. **Dependency Scanning**: Automated OWASP dependency checks
2. **Code Quality**: Static analysis with Checkstyle, SpotBugs, PMD
3. **Secret Scanning**: GitHub's secret scanning enabled
4. **Dependabot**: Automated dependency updates
5. **Branch Protection**: Required reviews and status checks on main branch

#### Workflow Security

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

## Known Security Considerations

### 1. Script and Shell Actions

**Risk Level**: High

Script and Shell actions can execute arbitrary code. Use with extreme caution:

- Never pass unsanitized user input to these actions
- Run in isolated environments when possible
- Apply strict allowlists for permitted operations
- Use dedicated service accounts with minimal privileges

### 2. API Actions

**Risk Level**: Medium

API actions make HTTP requests to external services:

- Validate URLs against allowlists
- Use HTTPS only
- Implement request timeouts
- Handle authentication tokens securely
- Validate and sanitize response data

### 3. Email Actions

**Risk Level**: Medium

Email actions may expose sensitive information:

- Validate recipient addresses
- Sanitize email content to prevent injection
- Use secure SMTP connections (TLS/SSL)
- Store credentials securely (environment variables or secret management)

### 4. Deserialization

**Risk Level**: Medium

Accordion uses Jackson for JSON serialization:

- Keep Jackson dependencies updated
- Avoid deserializing untrusted data
- Use explicit type references when possible
- Enable default typing only when absolutely necessary

## Security Testing

### Static Analysis

```bash
# Run all static analysis tools
mvn verify

# Individual tools
mvn checkstyle:check
mvn spotbugs:check
mvn pmd:check
```

### Dependency Scanning

```bash
# Run OWASP Dependency Check
mvn dependency-check:check

# View report
open target/dependency-check-report.html
```

### Code Coverage

Maintain high test coverage to catch potential security issues:

```bash
# Run tests with coverage
mvn test

# Check coverage thresholds
mvn jacoco:check

# View coverage report
open target/site/jacoco/index.html
```

## Security Checklist for Contributors

Before submitting code:

- [ ] Input validation implemented for all external data
- [ ] No hardcoded secrets or credentials
- [ ] Sensitive data properly masked in logs
- [ ] Error messages don't expose internal details
- [ ] Dependencies are up to date and scanned
- [ ] Unit tests cover security-related code paths
- [ ] Code reviewed by at least one maintainer
- [ ] Static analysis tools pass without warnings
- [ ] OWASP dependency check passes (CVSS < 7.0)

## Security Resources

### Tools

- [OWASP Dependency-Check](https://owasp.org/www-project-dependency-check/)
- [SpotBugs](https://spotbugs.github.io/)
- [PMD](https://pmd.github.io/)
- [Checkstyle](https://checkstyle.org/)

### References

- [OWASP Top Ten](https://owasp.org/www-project-top-ten/)
- [CWE Top 25](https://cwe.mitre.org/top25/)
- [NIST NVD](https://nvd.nist.gov/)
- [GitHub Security Advisories](https://github.com/advisories)

## Disclosure Policy

When we receive a security bug report, we will:

1. Confirm the problem and determine affected versions
2. Audit code to find similar problems
3. Prepare fixes for all supported versions
4. Release patches as soon as possible
5. Publish a security advisory on GitHub

## Security Hall of Fame

We appreciate the following security researchers who have responsibly disclosed vulnerabilities:

<!-- This section will be updated as security reports are received -->

_No reports received yet._

---

**Last Updated**: 2025-01-15

For questions about this policy, please open an issue or contact the maintainers.

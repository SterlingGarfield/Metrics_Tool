# Java Metrics Tool Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a local Java software metrics web tool that analyzes pasted code, uploaded files, and browser-selected source folders, then presents project, class, and method metrics with charts, risk summaries, and report exports.

**Architecture:** Use a Spring Boot backend to parse Java source with Eclipse JDT and compute project, class, and method metrics. Use a Vue 3 + Vite frontend for uploads, visualization, and exports during development, then package the built frontend into Spring Boot static resources for demo mode.

**Tech Stack:** Java 17, Spring Boot 3.3, Maven, Eclipse JDT ASTParser, Vue 3, Vite, Axios, ECharts, JUnit 5, MockMvc, Vitest, PowerShell

---

## File Structure

### Backend

- Create: `metrics-backend/pom.xml`
- Create: `metrics-backend/src/main/java/com/metrics/MetricsApplication.java`
- Create: `metrics-backend/src/main/java/com/metrics/controller/HealthController.java`
- Create: `metrics-backend/src/main/java/com/metrics/controller/MetricsController.java`
- Create: `metrics-backend/src/main/java/com/metrics/service/MetricsAnalysisService.java`
- Create: `metrics-backend/src/main/java/com/metrics/parser/JavaSourceParser.java`
- Create: `metrics-backend/src/main/java/com/metrics/analyzer/MethodMetricsAnalyzer.java`
- Create: `metrics-backend/src/main/java/com/metrics/analyzer/ClassMetricsAnalyzer.java`
- Create: `metrics-backend/src/main/java/com/metrics/analyzer/ProjectMetricsAnalyzer.java`
- Create: `metrics-backend/src/main/java/com/metrics/model/request/TextAnalyzeRequest.java`
- Create: `metrics-backend/src/main/java/com/metrics/model/response/AnalysisResponse.java`
- Create: `metrics-backend/src/main/java/com/metrics/model/response/ProjectSummary.java`
- Create: `metrics-backend/src/main/java/com/metrics/model/response/ClassMetrics.java`
- Create: `metrics-backend/src/main/java/com/metrics/model/response/MethodMetrics.java`
- Create: `metrics-backend/src/main/java/com/metrics/model/response/ParseIssue.java`
- Create: `metrics-backend/src/main/java/com/metrics/model/response/RiskFinding.java`
- Create: `metrics-backend/src/main/java/com/metrics/model/SourceInput.java`
- Create: `metrics-backend/src/main/java/com/metrics/util/SourceNormalizationUtils.java`
- Create: `metrics-backend/src/main/resources/application.yml`
- Create: `metrics-backend/src/test/java/com/metrics/controller/HealthControllerTest.java`
- Create: `metrics-backend/src/test/java/com/metrics/service/MetricsAnalysisServiceTest.java`
- Create: `metrics-backend/src/test/java/com/metrics/analyzer/ClassMetricsAnalyzerTest.java`
- Create: `metrics-backend/src/test/java/com/metrics/controller/MetricsControllerMultipartTest.java`

### Frontend

- Create: `metrics-frontend/package.json`
- Create: `metrics-frontend/vite.config.js`
- Create: `metrics-frontend/vitest.setup.js`
- Create: `metrics-frontend/index.html`
- Create: `metrics-frontend/src/main.js`
- Create: `metrics-frontend/src/App.vue`
- Create: `metrics-frontend/src/api/metrics.js`
- Create: `metrics-frontend/src/composables/useAnalysis.js`
- Create: `metrics-frontend/src/components/InputWorkspace.vue`
- Create: `metrics-frontend/src/components/OverviewCards.vue`
- Create: `metrics-frontend/src/components/MetricsCharts.vue`
- Create: `metrics-frontend/src/components/MetricsTables.vue`
- Create: `metrics-frontend/src/components/RiskPanel.vue`
- Create: `metrics-frontend/src/components/MetricInfoDrawer.vue`
- Create: `metrics-frontend/src/utils/exporters.js`
- Create: `metrics-frontend/src/styles/theme.css`
- Create: `metrics-frontend/src/components/__tests__/InputWorkspace.test.js`
- Create: `metrics-frontend/src/components/__tests__/DashboardFlow.test.js`

### Samples, scripts, and docs

- Create: `samples/demo-projects/basic/src/main/java/demo/BaseAccount.java`
- Create: `samples/demo-projects/basic/src/main/java/demo/PremiumAccount.java`
- Create: `samples/demo-projects/basic/src/main/java/demo/AuditService.java`
- Create: `scripts/dev.ps1`
- Create: `scripts/build-demo.ps1`
- Create: `scripts/run-demo.ps1`
- Create: `scripts/smoke-demo.ps1`
- Create: `README.md`
- Create: `docs/metric-definitions.md`
- Create: `docs/report-outline.md`

## Task 1: Bootstrap The Backend Application

**Files:**
- Create: `metrics-backend/pom.xml`
- Create: `metrics-backend/src/test/java/com/metrics/controller/HealthControllerTest.java`
- Create: `metrics-backend/src/main/java/com/metrics/MetricsApplication.java`
- Create: `metrics-backend/src/main/java/com/metrics/controller/HealthController.java`
- Create: `metrics-backend/src/main/resources/application.yml`

- [ ] **Step 1: Write the failing backend health test and test harness**

```xml
<!-- metrics-backend/pom.xml -->
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.1</version>
        <relativePath/>
    </parent>

    <groupId>com.metrics</groupId>
    <artifactId>metrics-backend</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>metrics-backend</name>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.eclipse.jdt</groupId>
            <artifactId>org.eclipse.jdt.core</artifactId>
            <version>3.37.0</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

```java
// metrics-backend/src/test/java/com/metrics/controller/HealthControllerTest.java
package com.metrics.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpointReturnsOkStatus() throws Exception {
        mockMvc.perform(get("/api/metrics/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.service").value("metrics-backend"));
    }
}
```

- [ ] **Step 2: Run the new test to verify it fails**

Run: `mvn -f metrics-backend/pom.xml -q -Dtest=HealthControllerTest test`

Expected: FAIL because `com.metrics.MetricsApplication` and `HealthController` do not exist yet.

- [ ] **Step 3: Write the minimal backend bootstrap**

```java
// metrics-backend/src/main/java/com/metrics/MetricsApplication.java
package com.metrics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MetricsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MetricsApplication.class, args);
    }
}
```

```java
// metrics-backend/src/main/java/com/metrics/controller/HealthController.java
package com.metrics.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metrics")
public class HealthController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
            "status", "UP",
            "service", "metrics-backend"
        );
    }
}
```

```yaml
# metrics-backend/src/main/resources/application.yml
spring:
  application:
    name: metrics-backend

server:
  port: 8080
```

- [ ] **Step 4: Run the backend test again to verify it passes**

Run: `mvn -f metrics-backend/pom.xml -q -Dtest=HealthControllerTest test`

Expected: PASS with `BUILD SUCCESS`.

- [ ] **Step 5: Commit the backend bootstrap**

```bash
git add metrics-backend/pom.xml metrics-backend/src/main/java/com/metrics/MetricsApplication.java metrics-backend/src/main/java/com/metrics/controller/HealthController.java metrics-backend/src/main/resources/application.yml metrics-backend/src/test/java/com/metrics/controller/HealthControllerTest.java
git commit -m "feat: bootstrap backend health endpoint"
```

## Task 2: Add Text Analysis And Baseline Metrics

**Files:**
- Create: `metrics-backend/src/main/java/com/metrics/model/request/TextAnalyzeRequest.java`
- Create: `metrics-backend/src/main/java/com/metrics/model/response/ProjectSummary.java`
- Create: `metrics-backend/src/main/java/com/metrics/model/response/MethodMetrics.java`
- Create: `metrics-backend/src/main/java/com/metrics/model/response/ClassMetrics.java`
- Create: `metrics-backend/src/main/java/com/metrics/model/response/ParseIssue.java`
- Create: `metrics-backend/src/main/java/com/metrics/model/response/RiskFinding.java`
- Create: `metrics-backend/src/main/java/com/metrics/model/response/AnalysisResponse.java`
- Create: `metrics-backend/src/main/java/com/metrics/parser/JavaSourceParser.java`
- Create: `metrics-backend/src/main/java/com/metrics/analyzer/MethodMetricsAnalyzer.java`
- Create: `metrics-backend/src/main/java/com/metrics/analyzer/ProjectMetricsAnalyzer.java`
- Create: `metrics-backend/src/main/java/com/metrics/service/MetricsAnalysisService.java`
- Create: `metrics-backend/src/main/java/com/metrics/controller/MetricsController.java`
- Create: `metrics-backend/src/test/java/com/metrics/service/MetricsAnalysisServiceTest.java`

- [ ] **Step 1: Write the failing service test for text analysis**

```java
// metrics-backend/src/test/java/com/metrics/service/MetricsAnalysisServiceTest.java
package com.metrics.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.metrics.model.request.TextAnalyzeRequest;
import com.metrics.model.response.AnalysisResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MetricsAnalysisServiceTest {

    @Autowired
    private MetricsAnalysisService service;

    @Test
    void analyzeTextComputesProjectAndMethodMetrics() {
        String source = """
            public class Sample {
                private int total;

                public int sum(int a, int b) {
                    if (a > b) {
                        return a + b;
                    }
                    return b;
                }
            }
            """;

        AnalysisResponse response = service.analyzeText(new TextAnalyzeRequest("Sample.java", source));

        assertThat(response.projectSummary().totalFiles()).isEqualTo(1);
        assertThat(response.projectSummary().totalClasses()).isEqualTo(1);
        assertThat(response.projectSummary().totalMethods()).isEqualTo(1);
        assertThat(response.methodMetrics()).hasSize(1);
        assertThat(response.methodMetrics().get(0).cyclomaticComplexity()).isEqualTo(2);
        assertThat(response.parseIssues()).isEmpty();
    }
}
```

- [ ] **Step 2: Run the service test to verify it fails**

Run: `mvn -f metrics-backend/pom.xml -q -Dtest=MetricsAnalysisServiceTest test`

Expected: FAIL because the request model, service, parser, and response models do not exist yet.

- [ ] **Step 3: Implement text analysis, parser, and baseline metrics**

```java
// metrics-backend/src/main/java/com/metrics/model/request/TextAnalyzeRequest.java
package com.metrics.model.request;

import jakarta.validation.constraints.NotBlank;

public record TextAnalyzeRequest(
    @NotBlank String fileName,
    @NotBlank String sourceCode
) {}
```

```java
// metrics-backend/src/main/java/com/metrics/model/response/ProjectSummary.java
package com.metrics.model.response;

public record ProjectSummary(
    int totalFiles,
    int totalClasses,
    int totalMethods,
    int totalLoc,
    int blankLines,
    int commentLines,
    double commentRatio,
    int highRiskClasses,
    int highRiskMethods
) {}
```

```java
// metrics-backend/src/main/java/com/metrics/model/response/MethodMetrics.java
package com.metrics.model.response;

public record MethodMetrics(
    String fileName,
    String className,
    String methodName,
    int loc,
    int cyclomaticComplexity,
    int parameterCount,
    int maxNestingDepth,
    int branchCount
) {}
```

```java
// metrics-backend/src/main/java/com/metrics/model/response/ClassMetrics.java
package com.metrics.model.response;

public record ClassMetrics(
    String fileName,
    String className,
    int loc,
    int wmc,
    int cbo,
    int rfc,
    int lcom,
    int dit,
    int noc,
    int nom,
    int noa,
    int publicMethodCount,
    double commentRatio,
    boolean partial
) {}
```

```java
// metrics-backend/src/main/java/com/metrics/model/response/ParseIssue.java
package com.metrics.model.response;

public record ParseIssue(
    String fileName,
    String message
) {}
```

```java
// metrics-backend/src/main/java/com/metrics/model/response/RiskFinding.java
package com.metrics.model.response;

public record RiskFinding(
    String level,
    String scope,
    String target,
    String message
) {}
```

```java
// metrics-backend/src/main/java/com/metrics/model/response/AnalysisResponse.java
package com.metrics.model.response;

import java.util.List;

public record AnalysisResponse(
    ProjectSummary projectSummary,
    List<ClassMetrics> classMetrics,
    List<MethodMetrics> methodMetrics,
    List<RiskFinding> riskFindings,
    List<ParseIssue> parseIssues,
    boolean partial
) {}
```

```java
// metrics-backend/src/main/java/com/metrics/parser/JavaSourceParser.java
package com.metrics.parser;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.springframework.stereotype.Component;

@Component
public class JavaSourceParser {

    public CompilationUnit parse(String source) {
        ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setResolveBindings(false);
        parser.setSource(source.toCharArray());
        return (CompilationUnit) parser.createAST(null);
    }

    public List<String> collectProblems(CompilationUnit compilationUnit) {
        return Arrays.stream(compilationUnit.getProblems())
            .map(problem -> problem.getMessage())
            .collect(Collectors.toList());
    }
}
```

```java
// metrics-backend/src/main/java/com/metrics/analyzer/MethodMetricsAnalyzer.java
package com.metrics.analyzer;

import com.metrics.model.response.MethodMetrics;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.springframework.stereotype.Component;

@Component
public class MethodMetricsAnalyzer {

    public List<MethodMetrics> analyze(String fileName, String source, CompilationUnit compilationUnit) {
        List<MethodMetrics> metrics = new ArrayList<>();
        compilationUnit.accept(new ASTVisitor() {
            private final ArrayDeque<String> typeStack = new ArrayDeque<>();

            @Override
            public boolean visit(org.eclipse.jdt.core.dom.TypeDeclaration node) {
                typeStack.push(node.getName().getIdentifier());
                return true;
            }

            @Override
            public void endVisit(org.eclipse.jdt.core.dom.TypeDeclaration node) {
                typeStack.pop();
            }

            @Override
            public boolean visit(MethodDeclaration node) {
                int startLine = compilationUnit.getLineNumber(node.getStartPosition());
                int endLine = compilationUnit.getLineNumber(node.getStartPosition() + node.getLength());
                ComplexityVisitor complexityVisitor = new ComplexityVisitor();
                node.accept(complexityVisitor);
                metrics.add(new MethodMetrics(
                    fileName,
                    typeStack.isEmpty() ? "UnknownType" : typeStack.peek(),
                    node.getName().getIdentifier(),
                    Math.max(1, endLine - startLine + 1),
                    complexityVisitor.complexity,
                    node.parameters().size(),
                    complexityVisitor.maxDepth,
                    complexityVisitor.branchCount
                ));
                return false;
            }
        });
        return metrics;
    }

    private static final class ComplexityVisitor extends ASTVisitor {
        private int complexity = 1;
        private int branchCount = 0;
        private int depth = 0;
        private int maxDepth = 0;

        @Override
        public boolean visit(IfStatement node) {
            complexity++;
            branchCount++;
            depth++;
            maxDepth = Math.max(maxDepth, depth);
            return true;
        }

        @Override
        public void endVisit(IfStatement node) {
            depth--;
        }

        @Override
        public boolean visit(WhileStatement node) {
            complexity++;
            branchCount++;
            depth++;
            maxDepth = Math.max(maxDepth, depth);
            return true;
        }

        @Override
        public void endVisit(WhileStatement node) {
            depth--;
        }

        @Override
        public boolean visit(SwitchCase node) {
            if (!node.isDefault()) {
                complexity++;
                branchCount++;
            }
            return true;
        }
    }
}
```

```java
// metrics-backend/src/main/java/com/metrics/analyzer/ProjectMetricsAnalyzer.java
package com.metrics.analyzer;

import com.metrics.model.response.MethodMetrics;
import com.metrics.model.response.ProjectSummary;
import java.util.List;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.springframework.stereotype.Component;

@Component
public class ProjectMetricsAnalyzer {

    public ProjectSummary summarize(String source, CompilationUnit compilationUnit, List<MethodMetrics> methodMetrics) {
        int totalLines = source.split("\\R", -1).length;
        int blankLines = (int) source.lines().filter(String::isBlank).count();
        int commentLines = (int) source.lines().filter(line -> line.trim().startsWith("//")).count();
        Counter counter = new Counter();
        compilationUnit.accept(counter);
        long highRiskMethods = methodMetrics.stream().filter(method -> method.cyclomaticComplexity() >= 10).count();
        double commentRatio = totalLines == 0 ? 0.0 : (double) commentLines / totalLines;
        return new ProjectSummary(
            1,
            counter.totalClasses,
            methodMetrics.size(),
            totalLines,
            blankLines,
            commentLines,
            commentRatio,
            0,
            (int) highRiskMethods
        );
    }

    private static final class Counter extends ASTVisitor {
        private int totalClasses;

        @Override
        public boolean visit(org.eclipse.jdt.core.dom.TypeDeclaration node) {
            totalClasses++;
            return true;
        }
    }
}
```

```java
// metrics-backend/src/main/java/com/metrics/service/MetricsAnalysisService.java
package com.metrics.service;

import com.metrics.analyzer.MethodMetricsAnalyzer;
import com.metrics.analyzer.ProjectMetricsAnalyzer;
import com.metrics.model.request.TextAnalyzeRequest;
import com.metrics.model.response.AnalysisResponse;
import com.metrics.model.response.ParseIssue;
import com.metrics.parser.JavaSourceParser;
import java.util.List;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.springframework.stereotype.Service;

@Service
public class MetricsAnalysisService {

    private final JavaSourceParser parser;
    private final MethodMetricsAnalyzer methodMetricsAnalyzer;
    private final ProjectMetricsAnalyzer projectMetricsAnalyzer;

    public MetricsAnalysisService(
        JavaSourceParser parser,
        MethodMetricsAnalyzer methodMetricsAnalyzer,
        ProjectMetricsAnalyzer projectMetricsAnalyzer
    ) {
        this.parser = parser;
        this.methodMetricsAnalyzer = methodMetricsAnalyzer;
        this.projectMetricsAnalyzer = projectMetricsAnalyzer;
    }

    public AnalysisResponse analyzeText(TextAnalyzeRequest request) {
        CompilationUnit compilationUnit = parser.parse(request.sourceCode());
        List<ParseIssue> parseIssues = parser.collectProblems(compilationUnit).stream()
            .map(problem -> new ParseIssue(request.fileName(), problem))
            .toList();
        var methodMetrics = methodMetricsAnalyzer.analyze(request.fileName(), request.sourceCode(), compilationUnit);
        var projectSummary = projectMetricsAnalyzer.summarize(request.sourceCode(), compilationUnit, methodMetrics);
        return new AnalysisResponse(projectSummary, List.of(), methodMetrics, List.of(), parseIssues, !parseIssues.isEmpty());
    }
}
```

```java
// metrics-backend/src/main/java/com/metrics/controller/MetricsController.java
package com.metrics.controller;

import com.metrics.model.request.TextAnalyzeRequest;
import com.metrics.model.response.AnalysisResponse;
import com.metrics.service.MetricsAnalysisService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("/api/metrics/analyze")
public class MetricsController {

    private final MetricsAnalysisService service;

    public MetricsController(MetricsAnalysisService service) {
        this.service = service;
    }

    @PostMapping("/text")
    public AnalysisResponse analyzeText(@Valid @RequestBody TextAnalyzeRequest request) {
        return service.analyzeText(request);
    }
}
```

- [ ] **Step 4: Run backend tests to verify baseline analysis works**

Run: `mvn -f metrics-backend/pom.xml -q -Dtest=HealthControllerTest,MetricsAnalysisServiceTest test`

Expected: PASS with `BUILD SUCCESS`.

- [ ] **Step 5: Commit the baseline text analysis flow**

```bash
git add metrics-backend/src/main/java/com/metrics/controller/MetricsController.java metrics-backend/src/main/java/com/metrics/service/MetricsAnalysisService.java metrics-backend/src/main/java/com/metrics/parser/JavaSourceParser.java metrics-backend/src/main/java/com/metrics/analyzer/MethodMetricsAnalyzer.java metrics-backend/src/main/java/com/metrics/analyzer/ProjectMetricsAnalyzer.java metrics-backend/src/main/java/com/metrics/model/request/TextAnalyzeRequest.java metrics-backend/src/main/java/com/metrics/model/response/ProjectSummary.java metrics-backend/src/main/java/com/metrics/model/response/MethodMetrics.java metrics-backend/src/main/java/com/metrics/model/response/ClassMetrics.java metrics-backend/src/main/java/com/metrics/model/response/ParseIssue.java metrics-backend/src/main/java/com/metrics/model/response/RiskFinding.java metrics-backend/src/main/java/com/metrics/model/response/AnalysisResponse.java metrics-backend/src/test/java/com/metrics/service/MetricsAnalysisServiceTest.java
git commit -m "feat: add text analysis baseline metrics"
```

## Task 3: Add Class Metrics And Multi-Source Aggregation

**Files:**
- Create: `metrics-backend/src/main/java/com/metrics/model/SourceInput.java`
- Create: `metrics-backend/src/main/java/com/metrics/analyzer/ClassMetricsAnalyzer.java`
- Modify: `metrics-backend/src/main/java/com/metrics/service/MetricsAnalysisService.java`
- Modify: `metrics-backend/src/main/java/com/metrics/model/response/AnalysisResponse.java`
- Modify: `metrics-backend/src/main/java/com/metrics/model/response/ProjectSummary.java`
- Create: `metrics-backend/src/test/java/com/metrics/analyzer/ClassMetricsAnalyzerTest.java`

- [ ] **Step 1: Write the failing class metrics test with a multi-file inheritance fixture**

```java
// metrics-backend/src/test/java/com/metrics/analyzer/ClassMetricsAnalyzerTest.java
package com.metrics.analyzer;

import static org.assertj.core.api.Assertions.assertThat;

import com.metrics.model.SourceInput;
import com.metrics.service.MetricsAnalysisService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ClassMetricsAnalyzerTest {

    @Autowired
    private MetricsAnalysisService service;

    @Test
    void analyzeSourcesComputesInheritanceAndCouplingMetrics() {
        List<SourceInput> inputs = List.of(
            new SourceInput("demo/BaseAccount.java", """
                package demo;
                public class BaseAccount {
                    protected int balance;
                    public void deposit(int amount) {
                        balance += amount;
                    }
                }
                """),
            new SourceInput("demo/PremiumAccount.java", """
                package demo;
                public class PremiumAccount extends BaseAccount {
                    private AuditService auditService = new AuditService();
                    public void withdraw(int amount) {
                        if (amount < balance) {
                            balance -= amount;
                            auditService.record(amount);
                        }
                    }
                }
                """),
            new SourceInput("demo/AuditService.java", """
                package demo;
                public class AuditService {
                    public void record(int amount) {
                        System.out.println(amount);
                    }
                }
                """)
        );

        var response = service.analyzeSources(inputs);
        var premium = response.classMetrics().stream()
            .filter(metric -> metric.className().equals("PremiumAccount"))
            .findFirst()
            .orElseThrow();
        var base = response.classMetrics().stream()
            .filter(metric -> metric.className().equals("BaseAccount"))
            .findFirst()
            .orElseThrow();

        assertThat(premium.dit()).isEqualTo(1);
        assertThat(base.noc()).isEqualTo(1);
        assertThat(premium.noa()).isEqualTo(1);
        assertThat(premium.nom()).isEqualTo(1);
        assertThat(premium.wmc()).isEqualTo(2);
        assertThat(premium.cbo()).isGreaterThanOrEqualTo(2);
    }
}
```

- [ ] **Step 2: Run the class metrics test to verify it fails**

Run: `mvn -f metrics-backend/pom.xml -q -Dtest=ClassMetricsAnalyzerTest test`

Expected: FAIL because `SourceInput`, `analyzeSources`, and `ClassMetricsAnalyzer` do not exist yet.

- [ ] **Step 3: Implement class metrics and multi-source analysis**

```java
// metrics-backend/src/main/java/com/metrics/model/SourceInput.java
package com.metrics.model;

public record SourceInput(
    String fileName,
    String sourceCode
) {}
```

```java
// metrics-backend/src/main/java/com/metrics/analyzer/ClassMetricsAnalyzer.java
package com.metrics.analyzer;

import com.metrics.model.SourceInput;
import com.metrics.model.response.ClassMetrics;
import com.metrics.model.response.MethodMetrics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.springframework.stereotype.Component;

@Component
public class ClassMetricsAnalyzer {

    public List<ClassMetrics> analyze(
        List<SourceInput> inputs,
        Map<String, CompilationUnit> compilationUnits,
        List<MethodMetrics> methodMetrics
    ) {
        Map<String, Integer> childrenCount = new HashMap<>();
        Map<String, String> parentByClass = new HashMap<>();
        compilationUnits.values().forEach(unit -> unit.accept(new ASTVisitor() {
            @Override
            public boolean visit(TypeDeclaration node) {
                if (node.getSuperclassType() != null) {
                    parentByClass.put(node.getName().getIdentifier(), node.getSuperclassType().toString());
                    childrenCount.merge(node.getSuperclassType().toString(), 1, Integer::sum);
                }
                return true;
            }
        }));

        List<ClassMetrics> results = new ArrayList<>();
        for (SourceInput input : inputs) {
            CompilationUnit unit = compilationUnits.get(input.fileName());
            unit.accept(new ASTVisitor() {
                @Override
                public boolean visit(TypeDeclaration node) {
                    String className = node.getName().getIdentifier();
                    int startLine = unit.getLineNumber(node.getStartPosition());
                    int endLine = unit.getLineNumber(node.getStartPosition() + node.getLength());
                    int loc = Math.max(1, endLine - startLine + 1);
                    int noa = 0;
                    for (FieldDeclaration field : node.getFields()) {
                        noa += field.fragments().size();
                    }
                    int nom = node.getMethods().length;
                    int publicMethodCount = (int) java.util.Arrays.stream(node.getMethods())
                        .filter(method -> java.lang.reflect.Modifier.isPublic(method.getModifiers()))
                        .count();

                    Set<String> referencedTypes = new HashSet<>();
                    Set<String> invokedMethods = new HashSet<>();
                    node.accept(new ASTVisitor() {
                        @Override
                        public boolean visit(SimpleType inner) {
                            referencedTypes.add(inner.getName().getFullyQualifiedName());
                            return true;
                        }

                        @Override
                        public boolean visit(MethodInvocation inner) {
                            invokedMethods.add(inner.getName().getIdentifier());
                            return true;
                        }
                    });

                    int wmc = methodMetrics.stream()
                        .filter(metric -> metric.fileName().equals(input.fileName()) && metric.className().equals(className))
                        .mapToInt(MethodMetrics::cyclomaticComplexity)
                        .sum();
                    int rfc = nom + invokedMethods.size();
                    int dit = computeDit(className, parentByClass);
                    int noc = childrenCount.getOrDefault(className, 0);
                    int lcom = computeLcom(node);

                    results.add(new ClassMetrics(
                        input.fileName(),
                        className,
                        loc,
                        wmc,
                        referencedTypes.size(),
                        rfc,
                        lcom,
                        dit,
                        noc,
                        nom,
                        noa,
                        publicMethodCount,
                        0.0,
                        false
                    ));
                    return true;
                }
            });
        }
        return results;
    }

    private int computeDit(String className, Map<String, String> parentByClass) {
        int depth = 0;
        String cursor = parentByClass.get(className);
        while (cursor != null && !"Object".equals(cursor)) {
            depth++;
            cursor = parentByClass.get(cursor);
        }
        return depth;
    }

    private int computeLcom(TypeDeclaration node) {
        List<MethodDeclaration> methods = List.of(node.getMethods());
        if (methods.size() < 2) {
            return 0;
        }
        int share = 0;
        int notShare = 0;
        for (int i = 0; i < methods.size(); i++) {
            Set<String> fieldsA = referencedFields(methods.get(i));
            for (int j = i + 1; j < methods.size(); j++) {
                Set<String> fieldsB = referencedFields(methods.get(j));
                Set<String> intersection = new HashSet<>(fieldsA);
                intersection.retainAll(fieldsB);
                if (intersection.isEmpty()) {
                    notShare++;
                } else {
                    share++;
                }
            }
        }
        return Math.max(notShare - share, 0);
    }

    private Set<String> referencedFields(MethodDeclaration method) {
        Set<String> fields = new HashSet<>();
        method.accept(new ASTVisitor() {
            @Override
            public boolean visit(org.eclipse.jdt.core.dom.SimpleName node) {
                fields.add(node.getIdentifier());
                return true;
            }
        });
        return fields;
    }
}
```

```java
// metrics-backend/src/main/java/com/metrics/service/MetricsAnalysisService.java
package com.metrics.service;

import com.metrics.analyzer.ClassMetricsAnalyzer;
import com.metrics.analyzer.MethodMetricsAnalyzer;
import com.metrics.analyzer.ProjectMetricsAnalyzer;
import com.metrics.model.SourceInput;
import com.metrics.model.request.TextAnalyzeRequest;
import com.metrics.model.response.AnalysisResponse;
import com.metrics.model.response.ParseIssue;
import com.metrics.model.response.ProjectSummary;
import com.metrics.model.response.RiskFinding;
import com.metrics.parser.JavaSourceParser;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.springframework.stereotype.Service;

@Service
public class MetricsAnalysisService {

    private final JavaSourceParser parser;
    private final MethodMetricsAnalyzer methodMetricsAnalyzer;
    private final ProjectMetricsAnalyzer projectMetricsAnalyzer;
    private final ClassMetricsAnalyzer classMetricsAnalyzer;

    public MetricsAnalysisService(
        JavaSourceParser parser,
        MethodMetricsAnalyzer methodMetricsAnalyzer,
        ProjectMetricsAnalyzer projectMetricsAnalyzer,
        ClassMetricsAnalyzer classMetricsAnalyzer
    ) {
        this.parser = parser;
        this.methodMetricsAnalyzer = methodMetricsAnalyzer;
        this.projectMetricsAnalyzer = projectMetricsAnalyzer;
        this.classMetricsAnalyzer = classMetricsAnalyzer;
    }

    public AnalysisResponse analyzeText(TextAnalyzeRequest request) {
        return analyzeSources(List.of(new SourceInput(request.fileName(), request.sourceCode())));
    }

    public AnalysisResponse analyzeSources(List<SourceInput> inputs) {
        Map<String, CompilationUnit> compilationUnits = new LinkedHashMap<>();
        List<ParseIssue> issues = new ArrayList<>();
        List<com.metrics.model.response.MethodMetrics> methods = new ArrayList<>();

        for (SourceInput input : inputs) {
            CompilationUnit unit = parser.parse(input.sourceCode());
            compilationUnits.put(input.fileName(), unit);
            parser.collectProblems(unit).stream()
                .map(problem -> new ParseIssue(input.fileName(), problem))
                .forEach(issues::add);
            methods.addAll(methodMetricsAnalyzer.analyze(input.fileName(), input.sourceCode(), unit));
        }

        var classes = classMetricsAnalyzer.analyze(inputs, compilationUnits, methods);
        ProjectSummary projectSummary = projectMetricsAnalyzer.summarizeBatch(inputs, compilationUnits, methods, classes);
        List<RiskFinding> riskFindings = buildRiskFindings(classes, methods);
        return new AnalysisResponse(projectSummary, classes, methods, riskFindings, issues, !issues.isEmpty());
    }

    private List<RiskFinding> buildRiskFindings(
        List<com.metrics.model.response.ClassMetrics> classes,
        List<com.metrics.model.response.MethodMetrics> methods
    ) {
        List<RiskFinding> findings = new ArrayList<>();
        methods.stream()
            .filter(method -> method.cyclomaticComplexity() >= 10)
            .forEach(method -> findings.add(new RiskFinding("HIGH", "METHOD", method.className() + "#" + method.methodName(), "Cyclomatic complexity is high")));
        classes.stream()
            .filter(clazz -> clazz.wmc() >= 20 || clazz.cbo() >= 10)
            .forEach(clazz -> findings.add(new RiskFinding("HIGH", "CLASS", clazz.className(), "Class complexity or coupling is high")));
        return findings;
    }
}
```

```java
// metrics-backend/src/main/java/com/metrics/analyzer/ProjectMetricsAnalyzer.java
package com.metrics.analyzer;

import com.metrics.model.SourceInput;
import com.metrics.model.response.ClassMetrics;
import com.metrics.model.response.MethodMetrics;
import com.metrics.model.response.ProjectSummary;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.springframework.stereotype.Component;

@Component
public class ProjectMetricsAnalyzer {

    public ProjectSummary summarizeBatch(
        List<SourceInput> inputs,
        Map<String, CompilationUnit> compilationUnits,
        List<MethodMetrics> methodMetrics,
        List<ClassMetrics> classMetrics
    ) {
        int totalLoc = inputs.stream().mapToInt(input -> input.sourceCode().split("\\R", -1).length).sum();
        int blankLines = inputs.stream().mapToInt(input -> (int) input.sourceCode().lines().filter(String::isBlank).count()).sum();
        int commentLines = inputs.stream().mapToInt(input -> (int) input.sourceCode().lines().filter(line -> line.trim().startsWith("//")).count()).sum();
        double commentRatio = totalLoc == 0 ? 0.0 : (double) commentLines / totalLoc;
        int highRiskMethods = (int) methodMetrics.stream().filter(method -> method.cyclomaticComplexity() >= 10).count();
        int highRiskClasses = (int) classMetrics.stream().filter(clazz -> clazz.wmc() >= 20 || clazz.cbo() >= 10).count();
        return new ProjectSummary(
            inputs.size(),
            classMetrics.size(),
            methodMetrics.size(),
            totalLoc,
            blankLines,
            commentLines,
            commentRatio,
            highRiskClasses,
            highRiskMethods
        );
    }
}
```

- [ ] **Step 4: Run the class metrics tests to verify they pass**

Run: `mvn -f metrics-backend/pom.xml -q -Dtest=MetricsAnalysisServiceTest,ClassMetricsAnalyzerTest test`

Expected: PASS with `BUILD SUCCESS`.

- [ ] **Step 5: Commit the class metrics implementation**

```bash
git add metrics-backend/src/main/java/com/metrics/model/SourceInput.java metrics-backend/src/main/java/com/metrics/analyzer/ClassMetricsAnalyzer.java metrics-backend/src/main/java/com/metrics/analyzer/ProjectMetricsAnalyzer.java metrics-backend/src/main/java/com/metrics/service/MetricsAnalysisService.java metrics-backend/src/test/java/com/metrics/analyzer/ClassMetricsAnalyzerTest.java
git commit -m "feat: compute class metrics across source sets"
```

## Task 4: Add File Upload And Folder Analysis Endpoints

**Files:**
- Create: `metrics-backend/src/main/java/com/metrics/util/SourceNormalizationUtils.java`
- Modify: `metrics-backend/src/main/java/com/metrics/controller/MetricsController.java`
- Create: `metrics-backend/src/test/java/com/metrics/controller/MetricsControllerMultipartTest.java`

- [ ] **Step 1: Write the failing multipart controller test**

```java
// metrics-backend/src/test/java/com/metrics/controller/MetricsControllerMultipartTest.java
package com.metrics.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class MetricsControllerMultipartTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void analyzeFolderContinuesWhenOneFileHasParseProblems() throws Exception {
        MockMultipartFile good = new MockMultipartFile(
            "files",
            "demo/BaseAccount.java",
            MediaType.TEXT_PLAIN_VALUE,
            "public class BaseAccount { void deposit() {} }".getBytes()
        );
        MockMultipartFile bad = new MockMultipartFile(
            "files",
            "demo/Broken.java",
            MediaType.TEXT_PLAIN_VALUE,
            "public class Broken { void oops( }".getBytes()
        );
        MockMultipartFile paths = new MockMultipartFile(
            "relativePaths",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            "[\"demo/BaseAccount.java\",\"demo/Broken.java\"]".getBytes()
        );

        mockMvc.perform(multipart("/api/metrics/analyze/folder").file(good).file(bad).file(paths))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.projectSummary.totalFiles").value(2))
            .andExpect(jsonPath("$.parseIssues.length()").value(1));
    }
}
```

- [ ] **Step 2: Run the multipart test to verify it fails**

Run: `mvn -f metrics-backend/pom.xml -q -Dtest=MetricsControllerMultipartTest test`

Expected: FAIL because the multipart endpoints and source normalization utility do not exist yet.

- [ ] **Step 3: Implement file and folder upload handling**

```java
// metrics-backend/src/main/java/com/metrics/util/SourceNormalizationUtils.java
package com.metrics.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metrics.model.SourceInput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class SourceNormalizationUtils {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<SourceInput> normalizeFiles(List<MultipartFile> files) throws IOException {
        List<SourceInput> inputs = new ArrayList<>();
        for (MultipartFile file : files) {
            inputs.add(new SourceInput(file.getOriginalFilename(), new String(file.getBytes(), StandardCharsets.UTF_8)));
        }
        return inputs;
    }

    public List<SourceInput> normalizeFolder(List<MultipartFile> files, String relativePathsJson) throws IOException {
        List<String> relativePaths = objectMapper.readValue(relativePathsJson, new TypeReference<List<String>>() {});
        List<SourceInput> inputs = new ArrayList<>();
        for (int index = 0; index < files.size(); index++) {
            MultipartFile file = files.get(index);
            String relativePath = index < relativePaths.size() ? relativePaths.get(index) : file.getOriginalFilename();
            inputs.add(new SourceInput(relativePath, new String(file.getBytes(), StandardCharsets.UTF_8)));
        }
        return inputs;
    }
}
```

```java
// metrics-backend/src/main/java/com/metrics/controller/MetricsController.java
package com.metrics.controller;

import com.metrics.model.request.TextAnalyzeRequest;
import com.metrics.model.response.AnalysisResponse;
import com.metrics.service.MetricsAnalysisService;
import com.metrics.util.SourceNormalizationUtils;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin
@RequestMapping("/api/metrics/analyze")
public class MetricsController {

    private final MetricsAnalysisService service;
    private final SourceNormalizationUtils normalizationUtils;

    public MetricsController(MetricsAnalysisService service, SourceNormalizationUtils normalizationUtils) {
        this.service = service;
        this.normalizationUtils = normalizationUtils;
    }

    @PostMapping("/text")
    public AnalysisResponse analyzeText(@Valid @RequestBody TextAnalyzeRequest request) {
        return service.analyzeText(request);
    }

    @PostMapping(value = "/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AnalysisResponse analyzeFiles(@RequestPart("files") List<MultipartFile> files) throws IOException {
        return service.analyzeSources(normalizationUtils.normalizeFiles(files));
    }

    @PostMapping(value = "/folder", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AnalysisResponse analyzeFolder(
        @RequestPart("files") List<MultipartFile> files,
        @RequestPart("relativePaths") String relativePaths
    ) throws IOException {
        return service.analyzeSources(normalizationUtils.normalizeFolder(files, relativePaths));
    }
}
```

- [ ] **Step 4: Run controller tests to verify upload flows pass**

Run: `mvn -f metrics-backend/pom.xml -q -Dtest=HealthControllerTest,MetricsControllerMultipartTest,ClassMetricsAnalyzerTest test`

Expected: PASS with `BUILD SUCCESS`.

- [ ] **Step 5: Commit the batch upload endpoints**

```bash
git add metrics-backend/src/main/java/com/metrics/controller/MetricsController.java metrics-backend/src/main/java/com/metrics/util/SourceNormalizationUtils.java metrics-backend/src/test/java/com/metrics/controller/MetricsControllerMultipartTest.java
git commit -m "feat: add file and folder analysis endpoints"
```

## Task 5: Bootstrap The Frontend And Input Workspace

**Files:**
- Create: `metrics-frontend/package.json`
- Create: `metrics-frontend/vite.config.js`
- Create: `metrics-frontend/vitest.setup.js`
- Create: `metrics-frontend/index.html`
- Create: `metrics-frontend/src/main.js`
- Create: `metrics-frontend/src/App.vue`
- Create: `metrics-frontend/src/api/metrics.js`
- Create: `metrics-frontend/src/components/InputWorkspace.vue`
- Create: `metrics-frontend/src/styles/theme.css`
- Create: `metrics-frontend/src/components/__tests__/InputWorkspace.test.js`

- [ ] **Step 1: Write the failing frontend workspace test and test harness**

```json
// metrics-frontend/package.json
{
  "name": "metrics-frontend",
  "private": true,
  "version": "0.0.1",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "test": "vitest"
  },
  "dependencies": {
    "axios": "^1.7.2",
    "echarts": "^5.5.1",
    "vue": "^3.4.31"
  },
  "devDependencies": {
    "@testing-library/jest-dom": "^6.4.8",
    "@testing-library/vue": "^8.0.1",
    "@vitejs/plugin-vue": "^5.0.5",
    "jsdom": "^24.1.0",
    "vite": "^5.3.1",
    "vitest": "^2.0.2"
  }
}
```

```javascript
// metrics-frontend/vite.config.js
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  test: {
    environment: 'jsdom',
    setupFiles: './vitest.setup.js'
  },
  server: {
    port: 5173,
    proxy: {
      '/api': 'http://localhost:8080'
    }
  }
})
```

```javascript
// metrics-frontend/vitest.setup.js
import '@testing-library/jest-dom/vitest'
```

```javascript
// metrics-frontend/src/components/__tests__/InputWorkspace.test.js
import { fireEvent, render, screen } from '@testing-library/vue'
import InputWorkspace from '../InputWorkspace.vue'

describe('InputWorkspace', () => {
  test('renders all input modes and emits text analysis payload', async () => {
    const { emitted } = render(InputWorkspace)

    expect(screen.getByRole('button', { name: 'Code Input' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Single File' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Multiple Files' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Folder Scan' })).toBeInTheDocument()

    await fireEvent.update(screen.getByLabelText('Java Source'), 'public class Demo {}')
    await fireEvent.click(screen.getByRole('button', { name: 'Analyze Text' }))

    expect(emitted()['submit-text'][0][0]).toEqual({
      fileName: 'Snippet.java',
      sourceCode: 'public class Demo {}'
    })
  })
})
```

- [ ] **Step 2: Install dependencies and run the frontend test to verify it fails**

Run: `npm --prefix metrics-frontend install`

Run: `npm --prefix metrics-frontend run test -- --run src/components/__tests__/InputWorkspace.test.js`

Expected: FAIL because `InputWorkspace.vue` and the app shell do not exist yet.

- [ ] **Step 3: Implement the frontend shell and input workspace**

```html
<!-- metrics-frontend/index.html -->
<!doctype html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Java Metrics Tool</title>
  </head>
  <body>
    <div id="app"></div>
    <script type="module" src="/src/main.js"></script>
  </body>
</html>
```

```javascript
// metrics-frontend/src/main.js
import { createApp } from 'vue'
import App from './App.vue'
import './styles/theme.css'

createApp(App).mount('#app')
```

```javascript
// metrics-frontend/src/api/metrics.js
import axios from 'axios'

const client = axios.create({
  baseURL: '/api/metrics'
})

export function checkHealth() {
  return client.get('/health')
}

export function analyzeText(payload) {
  return client.post('/analyze/text', payload)
}

export function analyzeFiles(files) {
  const formData = new FormData()
  files.forEach((file) => formData.append('files', file))
  return client.post('/analyze/files', formData)
}

export function analyzeFolder(files) {
  const formData = new FormData()
  const relativePaths = files.map((file) => file.webkitRelativePath || file.name)
  files.forEach((file) => formData.append('files', file))
  formData.append('relativePaths', JSON.stringify(relativePaths))
  return client.post('/analyze/folder', formData)
}
```

```vue
<!-- metrics-frontend/src/components/InputWorkspace.vue -->
<template>
  <section class="panel input-panel">
    <div class="mode-row">
      <button v-for="item in modes" :key="item.key" type="button" class="mode-pill" :class="{ active: mode === item.key }" @click="mode = item.key">
        {{ item.label }}
      </button>
    </div>

    <div v-if="mode === 'text'" class="pane">
      <label for="source-input">Java Source</label>
      <textarea id="source-input" v-model="textSource" rows="14"></textarea>
      <button type="button" class="primary-button" @click="emitText">Analyze Text</button>
    </div>

    <div v-else-if="mode === 'file'" class="pane">
      <input type="file" accept=".java" @change="emitSingleFile" />
    </div>

    <div v-else-if="mode === 'files'" class="pane">
      <input type="file" accept=".java" multiple @change="emitMultipleFiles" />
    </div>

    <div v-else class="pane">
      <input type="file" accept=".java" multiple webkitdirectory directory @change="emitFolder" />
    </div>
  </section>
</template>

<script setup>
import { ref } from 'vue'

const emit = defineEmits(['submit-text', 'submit-file', 'submit-files', 'submit-folder'])

const mode = ref('text')
const textSource = ref('')
const modes = [
  { key: 'text', label: 'Code Input' },
  { key: 'file', label: 'Single File' },
  { key: 'files', label: 'Multiple Files' },
  { key: 'folder', label: 'Folder Scan' }
]

function emitText() {
  emit('submit-text', {
    fileName: 'Snippet.java',
    sourceCode: textSource.value
  })
}

function emitSingleFile(event) {
  emit('submit-file', Array.from(event.target.files))
}

function emitMultipleFiles(event) {
  emit('submit-files', Array.from(event.target.files))
}

function emitFolder(event) {
  emit('submit-folder', Array.from(event.target.files))
}
</script>
```

```vue
<!-- metrics-frontend/src/App.vue -->
<template>
  <main class="app-shell">
    <section class="hero">
      <p class="eyebrow">Software Quality Assurance</p>
      <h1>Java Metrics Tool</h1>
      <p class="lede">Analyze Java code, inspect CK metrics, and export report-ready results for your course project.</p>
      <p class="health-status">Backend status: {{ healthStatus }}</p>
    </section>

    <InputWorkspace
      @submit-text="handleTextSubmit"
      @submit-file="handleFilesSubmit"
      @submit-files="handleFilesSubmit"
      @submit-folder="handleFolderSubmit"
    />
  </main>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import InputWorkspace from './components/InputWorkspace.vue'
import { analyzeFiles, analyzeFolder, analyzeText, checkHealth } from './api/metrics'

const healthStatus = ref('checking')

onMounted(async () => {
  const response = await checkHealth()
  healthStatus.value = response.data.status
})

async function handleTextSubmit(payload) {
  await analyzeText(payload)
}

async function handleFilesSubmit(files) {
  await analyzeFiles(files)
}

async function handleFolderSubmit(files) {
  await analyzeFolder(files)
}
</script>
```

```css
/* metrics-frontend/src/styles/theme.css */
:root {
  --ink: #13212f;
  --paper: #f5f1e8;
  --accent: #c45b2d;
  --accent-soft: #f3c3a7;
  --panel: #fffdfa;
  --line: #d7c9b7;
  --shadow: 0 18px 40px rgba(19, 33, 47, 0.08);
  font-family: "Georgia", "Times New Roman", serif;
}

* {
  box-sizing: border-box;
}

body {
  margin: 0;
  color: var(--ink);
  background:
    radial-gradient(circle at top left, rgba(196, 91, 45, 0.18), transparent 34%),
    linear-gradient(180deg, #f6efe3 0%, #ece4d9 100%);
}

.app-shell {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 24px 80px;
}

.hero {
  margin-bottom: 24px;
}

.eyebrow {
  margin: 0 0 8px;
  text-transform: uppercase;
  letter-spacing: 0.2em;
  color: var(--accent);
}

.lede {
  max-width: 680px;
}

.panel {
  background: var(--panel);
  border: 1px solid var(--line);
  border-radius: 24px;
  box-shadow: var(--shadow);
  padding: 24px;
}

.mode-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}

.mode-pill,
.primary-button {
  border: 1px solid var(--line);
  background: white;
  color: var(--ink);
  border-radius: 999px;
  padding: 10px 16px;
  cursor: pointer;
}

.mode-pill.active,
.primary-button {
  background: var(--accent);
  color: white;
  border-color: var(--accent);
}

.pane {
  display: grid;
  gap: 12px;
}

textarea {
  width: 100%;
  min-height: 280px;
  border-radius: 16px;
  border: 1px solid var(--line);
  padding: 16px;
  font-family: "Consolas", "Courier New", monospace;
}
```

- [ ] **Step 4: Run frontend tests and build to verify the workspace passes**

Run: `npm --prefix metrics-frontend run test -- --run src/components/__tests__/InputWorkspace.test.js`

Expected: PASS.

Run: `npm --prefix metrics-frontend run build`

Expected: PASS with a generated `metrics-frontend/dist` directory.

- [ ] **Step 5: Commit the frontend workspace**

```bash
git add metrics-frontend/package.json metrics-frontend/vite.config.js metrics-frontend/vitest.setup.js metrics-frontend/index.html metrics-frontend/src/main.js metrics-frontend/src/App.vue metrics-frontend/src/api/metrics.js metrics-frontend/src/components/InputWorkspace.vue metrics-frontend/src/styles/theme.css metrics-frontend/src/components/__tests__/InputWorkspace.test.js
git commit -m "feat: add frontend upload workspace"
```

## Task 6: Build The Dashboard, Charts, Tables, And Exports

**Files:**
- Create: `metrics-frontend/src/composables/useAnalysis.js`
- Create: `metrics-frontend/src/components/OverviewCards.vue`
- Create: `metrics-frontend/src/components/MetricsCharts.vue`
- Create: `metrics-frontend/src/components/MetricsTables.vue`
- Create: `metrics-frontend/src/components/RiskPanel.vue`
- Create: `metrics-frontend/src/components/MetricInfoDrawer.vue`
- Create: `metrics-frontend/src/utils/exporters.js`
- Modify: `metrics-frontend/src/App.vue`
- Create: `metrics-frontend/src/components/__tests__/DashboardFlow.test.js`

- [ ] **Step 1: Write the failing dashboard flow test**

```javascript
// metrics-frontend/src/components/__tests__/DashboardFlow.test.js
import { fireEvent, render, screen } from '@testing-library/vue'
import App from '../../App.vue'

vi.mock('../../api/metrics', () => ({
  checkHealth: vi.fn().mockResolvedValue({ data: { status: 'UP' } }),
  analyzeText: vi.fn().mockResolvedValue({
    data: {
      projectSummary: {
        totalFiles: 1,
        totalClasses: 1,
        totalMethods: 1,
        totalLoc: 10,
        blankLines: 1,
        commentLines: 0,
        commentRatio: 0,
        highRiskClasses: 0,
        highRiskMethods: 0
      },
      classMetrics: [
        {
          fileName: 'Snippet.java',
          className: 'Demo',
          loc: 10,
          wmc: 2,
          cbo: 1,
          rfc: 2,
          lcom: 0,
          dit: 0,
          noc: 0,
          nom: 1,
          noa: 0,
          publicMethodCount: 1,
          commentRatio: 0,
          partial: false
        }
      ],
      methodMetrics: [
        {
          fileName: 'Snippet.java',
          className: 'Demo',
          methodName: 'go',
          loc: 4,
          cyclomaticComplexity: 2,
          parameterCount: 0,
          maxNestingDepth: 1,
          branchCount: 1
        }
      ],
      riskFindings: [],
      parseIssues: [],
      partial: false
    }
  }),
  analyzeFiles: vi.fn(),
  analyzeFolder: vi.fn()
}))

test('renders overview cards after analysis completes', async () => {
  render(App)

  await fireEvent.update(screen.getByLabelText('Java Source'), 'public class Demo { void go() {} }')
  await fireEvent.click(screen.getByRole('button', { name: 'Analyze Text' }))

  expect(await screen.findByText('Files')).toBeInTheDocument()
  expect(await screen.findByText('Methods')).toBeInTheDocument()
  expect(await screen.findByText('Export CSV')).toBeInTheDocument()
})
```

- [ ] **Step 2: Run the dashboard test to verify it fails**

Run: `npm --prefix metrics-frontend run test -- --run src/components/__tests__/DashboardFlow.test.js`

Expected: FAIL because overview cards, charts, export buttons, and the analysis composable do not exist yet.

- [ ] **Step 3: Implement the dashboard and export flow**

```javascript
// metrics-frontend/src/composables/useAnalysis.js
import { ref } from 'vue'
import { analyzeFiles, analyzeFolder, analyzeText } from '../api/metrics'

export function useAnalysis() {
  const loading = ref(false)
  const result = ref(null)
  const error = ref('')

  async function runTextAnalysis(payload) {
    loading.value = true
    error.value = ''
    try {
      const response = await analyzeText(payload)
      result.value = response.data
    } catch (err) {
      error.value = err.message
    } finally {
      loading.value = false
    }
  }

  async function runFileAnalysis(files) {
    loading.value = true
    error.value = ''
    try {
      const response = await analyzeFiles(files)
      result.value = response.data
    } catch (err) {
      error.value = err.message
    } finally {
      loading.value = false
    }
  }

  async function runFolderAnalysis(files) {
    loading.value = true
    error.value = ''
    try {
      const response = await analyzeFolder(files)
      result.value = response.data
    } catch (err) {
      error.value = err.message
    } finally {
      loading.value = false
    }
  }

  return { loading, result, error, runTextAnalysis, runFileAnalysis, runFolderAnalysis }
}
```

```javascript
// metrics-frontend/src/utils/exporters.js
export function buildCsv(result) {
  const header = 'className,wmc,cbo,rfc,lcom,dit,noc,nom,noa'
  const rows = result.classMetrics.map((item) =>
    [item.className, item.wmc, item.cbo, item.rfc, item.lcom, item.dit, item.noc, item.nom, item.noa].join(',')
  )
  return [header, ...rows].join('\n')
}

export function buildMarkdownReport(result) {
  const summary = result.projectSummary
  const riskLines = result.riskFindings.length === 0
    ? ['- No high-risk findings in this run']
    : result.riskFindings.map((item) => `- ${item.scope}: ${item.target} - ${item.message}`)

  return [
    '# Java Metrics Analysis Report',
    '',
    '## Project Summary',
    `- Files: ${summary.totalFiles}`,
    `- Classes: ${summary.totalClasses}`,
    `- Methods: ${summary.totalMethods}`,
    `- LOC: ${summary.totalLoc}`,
    '',
    '## Risk Findings',
    ...riskLines
  ].join('\n')
}
```

```vue
<!-- metrics-frontend/src/components/OverviewCards.vue -->
<template>
  <section class="card-grid">
    <article class="metric-card">
      <span>Files</span>
      <strong>{{ summary.totalFiles }}</strong>
    </article>
    <article class="metric-card">
      <span>Classes</span>
      <strong>{{ summary.totalClasses }}</strong>
    </article>
    <article class="metric-card">
      <span>Methods</span>
      <strong>{{ summary.totalMethods }}</strong>
    </article>
    <article class="metric-card">
      <span>LOC</span>
      <strong>{{ summary.totalLoc }}</strong>
    </article>
  </section>
</template>

<script setup>
defineProps({
  summary: {
    type: Object,
    required: true
  }
})
</script>
```

```vue
<!-- metrics-frontend/src/components/MetricsCharts.vue -->
<template>
  <section class="panel">
    <div ref="complexityChart" class="chart-surface"></div>
  </section>
</template>

<script setup>
import * as echarts from 'echarts'
import { onMounted, ref, watch } from 'vue'

const props = defineProps({
  methodMetrics: {
    type: Array,
    required: true
  }
})

const complexityChart = ref(null)
let chart

function renderChart() {
  if (!complexityChart.value) {
    return
  }
  chart ??= echarts.init(complexityChart.value)
  const topMethods = [...props.methodMetrics]
    .sort((a, b) => b.cyclomaticComplexity - a.cyclomaticComplexity)
    .slice(0, 10)
  chart.setOption({
    xAxis: { type: 'category', data: topMethods.map((item) => item.methodName) },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', data: topMethods.map((item) => item.cyclomaticComplexity), itemStyle: { color: '#c45b2d' } }]
  })
}

onMounted(renderChart)
watch(() => props.methodMetrics, renderChart, { deep: true })
</script>
```

```vue
<!-- metrics-frontend/src/components/MetricsTables.vue -->
<template>
  <section class="panel table-stack">
    <h2>Class Metrics</h2>
    <table>
      <thead>
        <tr>
          <th>Class</th>
          <th>WMC</th>
          <th>CBO</th>
          <th>RFC</th>
          <th>DIT</th>
          <th>NOC</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in classMetrics" :key="item.fileName + item.className">
          <td>{{ item.className }}</td>
          <td>{{ item.wmc }}</td>
          <td>{{ item.cbo }}</td>
          <td>{{ item.rfc }}</td>
          <td>{{ item.dit }}</td>
          <td>{{ item.noc }}</td>
        </tr>
      </tbody>
    </table>
  </section>
</template>

<script setup>
defineProps({
  classMetrics: {
    type: Array,
    required: true
  }
})
</script>
```

```vue
<!-- metrics-frontend/src/components/RiskPanel.vue -->
<template>
  <section class="panel">
    <h2>Risk Findings</h2>
    <ul v-if="riskFindings.length">
      <li v-for="item in riskFindings" :key="item.scope + item.target">{{ item.scope }}: {{ item.target }} - {{ item.message }}</li>
    </ul>
    <p v-else>No critical findings in this run.</p>
  </section>
</template>

<script setup>
defineProps({
  riskFindings: {
    type: Array,
    required: true
  }
})
</script>
```

```vue
<!-- metrics-frontend/src/components/MetricInfoDrawer.vue -->
<template>
  <aside class="panel">
    <h2>Metric Guide</h2>
    <ul>
      <li>WMC: weighted methods per class, approximated here by summed method complexity.</li>
      <li>CBO: number of referenced peer types.</li>
      <li>RFC: declared methods plus distinct method invocations.</li>
      <li>LCOM: lack of cohesion estimate from method-field overlap.</li>
    </ul>
  </aside>
</template>
```

```vue
<!-- metrics-frontend/src/App.vue -->
<template>
  <main class="app-shell">
    <section class="hero">
      <p class="eyebrow">Software Quality Assurance</p>
      <h1>Java Metrics Tool</h1>
      <p class="lede">Analyze Java code, inspect CK metrics, and export report-ready results for your course project.</p>
      <p class="health-status">Backend status: {{ healthStatus }}</p>
    </section>

    <InputWorkspace
      @submit-text="runTextAnalysis"
      @submit-file="runFileAnalysis"
      @submit-files="runFileAnalysis"
      @submit-folder="runFolderAnalysis"
    />

    <p v-if="loading" class="status-banner">Analyzing source set...</p>
    <p v-if="error" class="status-banner error">{{ error }}</p>

    <template v-if="result">
      <OverviewCards :summary="result.projectSummary" />
      <div class="action-row">
        <button type="button" class="primary-button" @click="downloadCsv">Export CSV</button>
        <button type="button" class="primary-button" @click="downloadMarkdown">Export Markdown</button>
      </div>
      <MetricsCharts :method-metrics="result.methodMetrics" />
      <MetricsTables :class-metrics="result.classMetrics" />
      <RiskPanel :risk-findings="result.riskFindings" />
      <MetricInfoDrawer />
    </template>
  </main>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { checkHealth } from './api/metrics'
import InputWorkspace from './components/InputWorkspace.vue'
import MetricsCharts from './components/MetricsCharts.vue'
import MetricsTables from './components/MetricsTables.vue'
import MetricInfoDrawer from './components/MetricInfoDrawer.vue'
import OverviewCards from './components/OverviewCards.vue'
import RiskPanel from './components/RiskPanel.vue'
import { useAnalysis } from './composables/useAnalysis'
import { buildCsv, buildMarkdownReport } from './utils/exporters'

const healthStatus = ref('checking')
const { loading, result, error, runTextAnalysis, runFileAnalysis, runFolderAnalysis } = useAnalysis()

onMounted(async () => {
  const response = await checkHealth()
  healthStatus.value = response.data.status
})

function downloadBlob(filename, content, type) {
  const blob = new Blob([content], { type })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = filename
  link.click()
  URL.revokeObjectURL(link.href)
}

function downloadCsv() {
  downloadBlob('metrics-report.csv', buildCsv(result.value), 'text/csv')
}

function downloadMarkdown() {
  downloadBlob('metrics-report.md', buildMarkdownReport(result.value), 'text/markdown')
}
</script>
```

- [ ] **Step 4: Run frontend dashboard tests and build**

Run: `npm --prefix metrics-frontend run test -- --run src/components/__tests__/DashboardFlow.test.js`

Expected: PASS.

Run: `npm --prefix metrics-frontend run build`

Expected: PASS with the dashboard bundle generated in `metrics-frontend/dist`.

- [ ] **Step 5: Commit the dashboard and export workflow**

```bash
git add metrics-frontend/src/composables/useAnalysis.js metrics-frontend/src/components/OverviewCards.vue metrics-frontend/src/components/MetricsCharts.vue metrics-frontend/src/components/MetricsTables.vue metrics-frontend/src/components/RiskPanel.vue metrics-frontend/src/components/MetricInfoDrawer.vue metrics-frontend/src/utils/exporters.js metrics-frontend/src/App.vue metrics-frontend/src/components/__tests__/DashboardFlow.test.js
git commit -m "feat: add metrics dashboard and exports"
```

## Task 7: Add Samples, Docs, Demo Scripts, And Packaging Verification

**Files:**
- Create: `samples/demo-projects/basic/src/main/java/demo/BaseAccount.java`
- Create: `samples/demo-projects/basic/src/main/java/demo/PremiumAccount.java`
- Create: `samples/demo-projects/basic/src/main/java/demo/AuditService.java`
- Create: `scripts/dev.ps1`
- Create: `scripts/build-demo.ps1`
- Create: `scripts/run-demo.ps1`
- Create: `scripts/smoke-demo.ps1`
- Create: `README.md`
- Create: `docs/metric-definitions.md`
- Create: `docs/report-outline.md`

- [ ] **Step 1: Write the failing demo smoke script**

```powershell
# scripts/smoke-demo.ps1
param(
    [string]$JarPath = ".\metrics-backend\target\metrics-backend-0.0.1-SNAPSHOT.jar"
)

if (-not (Test-Path -LiteralPath $JarPath)) {
    throw "Missing demo jar: $JarPath"
}

$process = Start-Process java -ArgumentList "-jar", $JarPath -PassThru
Start-Sleep -Seconds 8

try {
    $health = Invoke-RestMethod -Uri "http://localhost:8080/api/metrics/health"
    if ($health.status -ne "UP") {
        throw "Health endpoint did not report UP"
    }
} finally {
    Stop-Process -Id $process.Id -Force
}
```

- [ ] **Step 2: Run the smoke script to verify it fails before packaging exists**

Run: `powershell -ExecutionPolicy Bypass -File .\scripts\smoke-demo.ps1`

Expected: FAIL with `Missing demo jar`.

- [ ] **Step 3: Add demo fixtures, scripts, and report support docs**

```java
// samples/demo-projects/basic/src/main/java/demo/BaseAccount.java
package demo;

public class BaseAccount {
    protected int balance;

    public void deposit(int amount) {
        balance += amount;
    }
}
```

```java
// samples/demo-projects/basic/src/main/java/demo/PremiumAccount.java
package demo;

public class PremiumAccount extends BaseAccount {
    private final AuditService auditService = new AuditService();

    public void withdraw(int amount) {
        if (amount < balance) {
            balance -= amount;
            auditService.record(amount);
        }
    }
}
```

```java
// samples/demo-projects/basic/src/main/java/demo/AuditService.java
package demo;

public class AuditService {
    public void record(int amount) {
        System.out.println("audit:" + amount);
    }
}
```

```powershell
# scripts/dev.ps1
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\..\metrics-backend'; mvn spring-boot:run"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\..\metrics-frontend'; npm run dev"
Write-Host "Backend: http://localhost:8080"
Write-Host "Frontend: http://localhost:5173"
```

```powershell
# scripts/build-demo.ps1
$root = Resolve-Path "$PSScriptRoot\.."
$frontend = Join-Path $root "metrics-frontend"
$backend = Join-Path $root "metrics-backend"
$staticDir = Join-Path $backend "src\main\resources\static"

if (Test-Path -LiteralPath $staticDir) {
    Remove-Item -LiteralPath $staticDir -Recurse -Force
}

Push-Location $frontend
npm install
npm run build
Pop-Location

New-Item -ItemType Directory -Path $staticDir | Out-Null
Copy-Item -LiteralPath (Join-Path $frontend "dist\*") -Destination $staticDir -Recurse -Force

Push-Location $backend
mvn clean package
Pop-Location

Write-Host "Demo package created at metrics-backend\target\metrics-backend-0.0.1-SNAPSHOT.jar"
```

```powershell
# scripts/run-demo.ps1
$jar = Resolve-Path "$PSScriptRoot\..\metrics-backend\target\metrics-backend-0.0.1-SNAPSHOT.jar"
java -jar $jar
```

````markdown
<!-- README.md -->
# Java Metrics Tool

## Environment

- JDK 17
- Maven 3.9+
- Node.js 18 or 20

## Development

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\dev.ps1
```

## Demo Packaging

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build-demo.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\run-demo.ps1
```

## Features

- pasted code analysis
- single file upload
- multiple file upload
- folder scan from the browser
- project, class, and method metrics
- CSV and Markdown export
````

```markdown
<!-- docs/metric-definitions.md -->
# Metric Definitions

## Method Metrics

- Cyclomatic Complexity: `1 + number of branch points`
- Method LOC: end line minus start line plus one
- Parameter Count: declared parameter total
- Maximum Nesting Depth: deepest conditional or loop nesting

## Class Metrics

- WMC: sum of method cyclomatic complexity values in one class
- CBO: count of distinct referenced peer types
- RFC: declared methods plus distinct invoked methods
- LCOM: lack of cohesion estimate from method-field overlap
- DIT: inheritance depth from the analyzed source set
- NOC: direct child class count in the analyzed source set
- NOM: declared method count
- NOA: declared attribute count
```

```markdown
<!-- docs/report-outline.md -->
# Report Outline

## 1. Requirement Analysis

- software metrics automation tool goals
- functional, data, and non-functional requirements

## 2. System Design

- frontend and backend architecture
- AST parsing workflow
- metric calculation modules

## 3. Implementation

- key interfaces
- metric algorithms
- visualization modules

## 4. Result Analysis

- sample project metrics
- accuracy comparison against expected values
- usability and effectiveness discussion

## 5. Improvement Suggestions

- known limitations
- next-step metrics
- future tooling extensions
```

- [ ] **Step 4: Build the demo package and run the smoke test**

Run: `powershell -ExecutionPolicy Bypass -File .\scripts\build-demo.ps1`

Expected: PASS with `metrics-backend\target\metrics-backend-0.0.1-SNAPSHOT.jar` created.

Run: `powershell -ExecutionPolicy Bypass -File .\scripts\smoke-demo.ps1`

Expected: PASS with the health endpoint returning `UP`.

- [ ] **Step 5: Commit the demo packaging and report assets**

```bash
git add samples/demo-projects/basic/src/main/java/demo/BaseAccount.java samples/demo-projects/basic/src/main/java/demo/PremiumAccount.java samples/demo-projects/basic/src/main/java/demo/AuditService.java scripts/dev.ps1 scripts/build-demo.ps1 scripts/run-demo.ps1 scripts/smoke-demo.ps1 README.md docs/metric-definitions.md docs/report-outline.md
git commit -m "feat: add demo scripts and report assets"
```

## Self-Review Checklist

- Spec coverage: backend analysis, frontend dashboard, export, docs, and local demo packaging are each represented by at least one task.
- Placeholder scan: this plan intentionally contains no unfinished placeholder markers or deferred implementation notes.
- Type consistency: `AnalysisResponse`, `ProjectSummary`, `ClassMetrics`, `MethodMetrics`, and `SourceInput` are referenced consistently across backend tasks.

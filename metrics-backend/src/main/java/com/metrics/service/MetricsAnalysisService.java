package com.metrics.service;

import com.metrics.analyzer.ClassMetricsAnalyzer;
import com.metrics.analyzer.MethodMetricsAnalyzer;
import com.metrics.analyzer.ProjectMetricsAnalyzer;
import com.metrics.model.SourceInput;
import com.metrics.model.request.TextAnalyzeRequest;
import com.metrics.model.response.AnalysisResponse;
import com.metrics.model.response.ParseIssue;
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

    public MetricsAnalysisService(JavaSourceParser parser, MethodMetricsAnalyzer methodMetricsAnalyzer, ProjectMetricsAnalyzer projectMetricsAnalyzer, ClassMetricsAnalyzer classMetricsAnalyzer) {
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
        var projectSummary = projectMetricsAnalyzer.summarizeBatch(inputs, compilationUnits, methods, classes);
        var findings = buildRiskFindings(classes, methods);
        return new AnalysisResponse(projectSummary, classes, methods, findings, issues, !issues.isEmpty());
    }

    private List<RiskFinding> buildRiskFindings(List<com.metrics.model.response.ClassMetrics> classes, List<com.metrics.model.response.MethodMetrics> methods) {
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

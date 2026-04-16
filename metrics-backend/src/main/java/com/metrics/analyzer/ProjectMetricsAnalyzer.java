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

    public ProjectSummary summarizeBatch(List<SourceInput> inputs, Map<String, CompilationUnit> compilationUnits, List<MethodMetrics> methodMetrics, List<ClassMetrics> classMetrics) {
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

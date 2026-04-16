package com.metrics.analyzer;

import com.metrics.model.SourceInput;
import com.metrics.model.response.ClassMetrics;
import com.metrics.model.response.MethodMetrics;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.springframework.stereotype.Component;

@Component
public class ClassMetricsAnalyzer {

    public List<ClassMetrics> analyze(List<SourceInput> inputs, Map<String, CompilationUnit> compilationUnits, List<MethodMetrics> methodMetrics) {
        Map<String, Integer> childrenCount = new HashMap<>();
        Map<String, String> parentByClass = new HashMap<>();

        for (CompilationUnit unit : compilationUnits.values()) {
            unit.accept(new ASTVisitor() {
                @Override
                public boolean visit(TypeDeclaration node) {
                    if (node.getSuperclassType() != null) {
                        String parent = node.getSuperclassType().toString();
                        parentByClass.put(node.getName().getIdentifier(), parent);
                        childrenCount.merge(parent, 1, Integer::sum);
                    }
                    return true;
                }
            });
        }

        List<ClassMetrics> results = new ArrayList<>();
        for (SourceInput input : inputs) {
            CompilationUnit unit = compilationUnits.get(input.fileName());
            unit.accept(new ASTVisitor() {
                @Override
                public boolean visit(TypeDeclaration node) {
                    String className = node.getName().getIdentifier();
                    int startLine = unit.getLineNumber(node.getStartPosition());
                    int endLine = unit.getLineNumber(node.getStartPosition() + Math.max(0, node.getLength() - 1));
                    int loc = Math.max(1, endLine - startLine + 1);
                    int noa = 0;
                    for (FieldDeclaration field : node.getFields()) {
                        noa += field.fragments().size();
                    }
                    int nom = node.getMethods().length;
                    int publicMethodCount = (int) Arrays.stream(node.getMethods())
                        .filter(method -> Modifier.isPublic(method.getModifiers()))
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
        List<MethodDeclaration> methods = Arrays.asList(node.getMethods());
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
            public boolean visit(SimpleName node) {
                fields.add(node.getIdentifier());
                return true;
            }
        });
        return fields;
    }
}

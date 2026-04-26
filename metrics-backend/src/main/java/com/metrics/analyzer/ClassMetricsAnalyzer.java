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
        Map<String, Set<String>> declaredMethodSignatures = new HashMap<>();

        for (CompilationUnit unit : compilationUnits.values()) {
            unit.accept(new ASTVisitor() {
                @Override
                public boolean visit(TypeDeclaration node) {
                    if (node.getSuperclassType() != null) {
                        String parent = simpleClassName(node.getSuperclassType().toString());
                        parentByClass.put(node.getName().getIdentifier(), parent);
                        childrenCount.merge(parent, 1, Integer::sum);
                    }
                    declaredMethodSignatures.put(
                        node.getName().getIdentifier(),
                        extractMethodSignatures(node)
                    );
                    return true;
                }
            });
        }

        Map<String, Set<String>> inheritedMethodCache = new HashMap<>();
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
                    Set<String> ownMethodSignatures = declaredMethodSignatures.getOrDefault(className, Set.of());
                    Set<String> inheritedSignatures = collectInheritedMethodSignatures(
                        className,
                        parentByClass,
                        declaredMethodSignatures,
                        inheritedMethodCache
                    );
                    int overriddenMethodCount = (int) ownMethodSignatures.stream()
                        .filter(inheritedSignatures::contains)
                        .count();
                    int addedMethodCount = Math.max(ownMethodSignatures.size() - overriddenMethodCount, 0);
                    double specializationIndex = nom == 0
                        ? 0.0
                        : (double) overriddenMethodCount * Math.max(dit, 1) / Math.max(nom, 1);

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
                        addedMethodCount,
                        overriddenMethodCount,
                        specializationIndex,
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

    private Set<String> extractMethodSignatures(TypeDeclaration node) {
        Set<String> signatures = new HashSet<>();
        for (MethodDeclaration method : node.getMethods()) {
            if (!method.isConstructor()) {
                signatures.add(buildMethodSignature(method));
            }
        }
        return signatures;
    }

    private Set<String> collectInheritedMethodSignatures(
        String className,
        Map<String, String> parentByClass,
        Map<String, Set<String>> declaredMethodSignatures,
        Map<String, Set<String>> cache
    ) {
        if (cache.containsKey(className)) {
            return cache.get(className);
        }

        String parent = parentByClass.get(className);
        if (parent == null || "Object".equals(parent)) {
            Set<String> empty = Set.of();
            cache.put(className, empty);
            return empty;
        }

        Set<String> inherited = new HashSet<>(declaredMethodSignatures.getOrDefault(parent, Set.of()));
        inherited.addAll(collectInheritedMethodSignatures(parent, parentByClass, declaredMethodSignatures, cache));
        cache.put(className, inherited);
        return inherited;
    }

    private String buildMethodSignature(MethodDeclaration method) {
        return method.getName().getIdentifier() + "#" + method.parameters().size();
    }

    private String simpleClassName(String typeName) {
        int index = typeName.lastIndexOf('.');
        if (index < 0) {
            return typeName;
        }
        return typeName.substring(index + 1);
    }
}

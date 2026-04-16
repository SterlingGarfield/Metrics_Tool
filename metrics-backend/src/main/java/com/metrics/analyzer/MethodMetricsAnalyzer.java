package com.metrics.analyzer;

import com.metrics.model.response.MethodMetrics;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.springframework.stereotype.Component;

@Component
public class MethodMetricsAnalyzer {

    public List<MethodMetrics> analyze(String fileName, String source, CompilationUnit compilationUnit) {
        List<MethodMetrics> metrics = new ArrayList<>();
        compilationUnit.accept(new ASTVisitor() {
            private final ArrayDeque<String> typeStack = new ArrayDeque<>();

            @Override
            public boolean visit(TypeDeclaration node) {
                typeStack.push(node.getName().getIdentifier());
                return true;
            }

            @Override
            public void endVisit(TypeDeclaration node) {
                typeStack.pop();
            }

            @Override
            public boolean visit(MethodDeclaration node) {
                int startLine = compilationUnit.getLineNumber(node.getStartPosition());
                int endLine = compilationUnit.getLineNumber(node.getStartPosition() + Math.max(0, node.getLength() - 1));
                ComplexityVisitor visitor = new ComplexityVisitor();
                if (node.getBody() != null) {
                    node.getBody().accept(visitor);
                }
                metrics.add(new MethodMetrics(
                    fileName,
                    typeStack.isEmpty() ? "UnknownType" : typeStack.peek(),
                    node.getName().getIdentifier(),
                    Math.max(1, endLine - startLine + 1),
                    visitor.complexity,
                    node.parameters().size(),
                    visitor.maxDepth,
                    visitor.branchCount
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
            registerBranch();
            return true;
        }

        @Override
        public void endVisit(IfStatement node) {
            exitBranch();
        }

        @Override
        public boolean visit(ForStatement node) {
            registerBranch();
            return true;
        }

        @Override
        public void endVisit(ForStatement node) {
            exitBranch();
        }

        @Override
        public boolean visit(EnhancedForStatement node) {
            registerBranch();
            return true;
        }

        @Override
        public void endVisit(EnhancedForStatement node) {
            exitBranch();
        }

        @Override
        public boolean visit(WhileStatement node) {
            registerBranch();
            return true;
        }

        @Override
        public void endVisit(WhileStatement node) {
            exitBranch();
        }

        @Override
        public boolean visit(DoStatement node) {
            registerBranch();
            return true;
        }

        @Override
        public void endVisit(DoStatement node) {
            exitBranch();
        }

        @Override
        public boolean visit(TryStatement node) {
            if (!node.catchClauses().isEmpty()) {
                registerBranch();
            }
            return true;
        }

        @Override
        public void endVisit(TryStatement node) {
            if (!node.catchClauses().isEmpty()) {
                exitBranch();
            }
        }

        @Override
        public boolean visit(SwitchCase node) {
            if (!node.isDefault()) {
                complexity++;
                branchCount++;
            }
            return true;
        }

        private void registerBranch() {
            complexity++;
            branchCount++;
            depth++;
            maxDepth = Math.max(maxDepth, depth);
        }

        private void exitBranch() {
            if (depth > 0) {
                depth--;
            }
        }
    }
}

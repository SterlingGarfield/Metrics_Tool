package com.metrics.parser;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
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
        parser.setBindingsRecovery(false);
        parser.setStatementsRecovery(true);
        parser.setCompilerOptions(JavaCore.getOptions());
        parser.setSource(source.toCharArray());
        return (CompilationUnit) parser.createAST(null);
    }

    public List<String> collectProblems(CompilationUnit compilationUnit) {
        Optional<String> firstProblem = Arrays.stream(compilationUnit.getProblems())
            .filter(IProblem::isError)
            .map(IProblem::getMessage)
            .findFirst();
        return firstProblem.map(List::of).orElseGet(List::of);
    }
}

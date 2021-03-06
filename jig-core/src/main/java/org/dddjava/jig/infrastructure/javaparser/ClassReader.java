package org.dddjava.jig.infrastructure.javaparser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import org.dddjava.jig.domain.model.implementation.raw.JavaSource;

class ClassReader {

    TypeSourceResult read(JavaSource javaSource) {
        CompilationUnit cu = JavaParser.parse(javaSource.toInputStream());

        String packageName = cu.getPackageDeclaration()
                .map(PackageDeclaration::getNameAsString)
                .map(name -> name + ".")
                .orElse("");

        ClassVisitor typeVisitor = new ClassVisitor(packageName);
        cu.accept(typeVisitor, null);

        return typeVisitor.toTypeSourceResult();
    }
}

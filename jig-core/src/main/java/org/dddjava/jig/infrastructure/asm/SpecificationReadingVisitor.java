package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.declaration.annotation.AnnotationDescription;
import org.dddjava.jig.domain.model.declaration.annotation.FieldAnnotationDeclaration;
import org.dddjava.jig.domain.model.declaration.annotation.MethodAnnotationDeclaration;
import org.dddjava.jig.domain.model.declaration.annotation.TypeAnnotationDeclaration;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodSignature;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.implementation.bytecode.Implementation;
import org.dddjava.jig.domain.model.implementation.bytecode.ImplementationAnalyzeContext;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodImplementation;
import org.objectweb.asm.*;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

class SpecificationReadingVisitor extends ClassVisitor {

    final ImplementationAnalyzeContext implementationAnalyzeContext;
    Implementation implementation;

    public SpecificationReadingVisitor(ImplementationAnalyzeContext implementationAnalyzeContext) {
        super(Opcodes.ASM6);
        this.implementationAnalyzeContext = implementationAnalyzeContext;
    }

    public Implementation specification() {
        return implementation;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        List<TypeIdentifier> useTypes = extractClassTypeFromGenericsSignature(signature);

        this.implementation = new Implementation(
                implementationAnalyzeContext,
                new TypeIdentifier(name),
                new TypeIdentifier(superName),
                Arrays.stream(interfaces).map(TypeIdentifier::new).collect(TypeIdentifiers.collector()),
                useTypes,
                (access & Opcodes.ACC_FINAL) == 0);

        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        TypeAnnotationDeclaration typeAnnotationDeclaration = implementation.newAnnotationDeclaration(typeDescriptorToIdentifier(descriptor));
        implementation.registerTypeAnnotation(typeAnnotationDeclaration);
        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        List<TypeIdentifier> genericsTypes = extractClassTypeFromGenericsSignature(signature);
        genericsTypes.forEach(implementation::registerUseType);

        // 配列フィールドの型
        if (descriptor.charAt(0) == '[') {
            Type elementType = Type.getType(descriptor).getElementType();
            implementation.registerUseType(toTypeIdentifier(elementType));
        }

        FieldDeclaration fieldDeclaration = implementation.newFieldDeclaration(name, typeDescriptorToIdentifier(descriptor));

        if ((access & Opcodes.ACC_STATIC) == 0) {
            // インスタンスフィールドだけ相手にする
            implementation.registerField(fieldDeclaration);
        } else {
            if (!name.equals("$VALUES")) {
                // 定数だけどenumの $VALUES は除く
                implementation.registerStaticField(fieldDeclaration);
            }
        }
        return new FieldVisitor(this.api) {
            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                TypeIdentifier annotationTypeIdentifier = typeDescriptorToIdentifier(descriptor);
                implementation.registerUseType(annotationTypeIdentifier);
                return new MyAnnotationVisitor(this.api, annotationDescription ->
                        implementation.registerFieldAnnotation(new FieldAnnotationDeclaration(fieldDeclaration, annotationTypeIdentifier, annotationDescription)));
            }
        };
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {

        MethodDeclaration methodDeclaration = new MethodDeclaration(implementation.typeIdentifier(), toMethodSignature(name, descriptor), methodDescriptorToReturnIdentifier(descriptor));

        List<TypeIdentifier> useTypes = extractClassTypeFromGenericsSignature(signature);
        if (exceptions != null) {
            for (String exception : exceptions) {
                useTypes.add(new TypeIdentifier(exception));
            }
        }
        MethodImplementation methodImplementation = new MethodImplementation(
                methodDeclaration,
                methodDescriptorToReturnIdentifier(descriptor),
                useTypes,
                access
        );
        methodImplementation.bind(implementation);

        return new MethodVisitor(this.api) {

            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                return new MyAnnotationVisitor(this.api, annotationDescription ->
                        methodImplementation.registerAnnotation(new MethodAnnotationDeclaration(methodDeclaration, typeDescriptorToIdentifier(descriptor), annotationDescription)));
            }

            @Override
            public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                methodImplementation.registerFieldInstruction(
                        new FieldDeclaration(new TypeIdentifier(owner), name, typeDescriptorToIdentifier(descriptor)));

                super.visitFieldInsn(opcode, owner, name, descriptor);
            }

            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                methodImplementation.registerMethodInstruction(
                        new MethodDeclaration(
                                new TypeIdentifier(owner),
                                toMethodSignature(name, descriptor),
                                methodDescriptorToReturnIdentifier(descriptor)));

                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            }

            @Override
            public void visitLdcInsn(Object value) {
                if (value instanceof Type) {
                    // `Xxx.class` などのクラス参照を読み込む
                    methodImplementation.registerClassReference(toTypeIdentifier((Type) value));
                }

                super.visitLdcInsn(value);
            }

            @Override
            public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
                for (Object bootstrapMethodArgument : bootstrapMethodArguments) {

                    if (bootstrapMethodArgument instanceof Type) {
                        Type type = (Type) bootstrapMethodArgument;
                        if (type.getSort() == Type.METHOD) {
                            // lambdaやメソッドリファレンスの引数と戻り値型を読み込む
                            methodImplementation.registerInvokeDynamic(toTypeIdentifier(type.getReturnType()));
                            for (Type argumentType : type.getArgumentTypes()) {
                                methodImplementation.registerInvokeDynamic(toTypeIdentifier(argumentType));
                            }
                        }
                    }

                    // lambdaで記述されているハンドラメソッド
                    if (bootstrapMethodArgument instanceof Handle) {
                        Handle handle = (Handle) bootstrapMethodArgument;
                        methodImplementation.registerMethodInstruction(
                                new MethodDeclaration(
                                        new TypeIdentifier(handle.getOwner()),
                                        toMethodSignature(handle.getName(), handle.getDesc()),
                                        methodDescriptorToReturnIdentifier(handle.getDesc()))
                        );
                    }
                }

                super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
            }

            @Override
            public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
                // switchがある
                methodImplementation.registerLookupSwitchInstruction();
                super.visitLookupSwitchInsn(dflt, keys, labels);
            }

            @Override
            public void visitJumpInsn(int opcode, Label label) {
                // 何かしらの分岐がある
                methodImplementation.registerJumpInstruction();
                super.visitJumpInsn(opcode, label);
            }
        };
    }

    private MethodSignature toMethodSignature(String name, String descriptor) {
        List<TypeIdentifier> argumentTypes = Arrays.stream(Type.getArgumentTypes(descriptor))
                .map(this::toTypeIdentifier)
                .collect(Collectors.toList());
        return new MethodSignature(name, argumentTypes);
    }

    private TypeIdentifier methodDescriptorToReturnIdentifier(String descriptor) {
        return toTypeIdentifier(Type.getReturnType(descriptor));
    }

    private TypeIdentifier typeDescriptorToIdentifier(String descriptor) {
        Type type = Type.getType(descriptor);
        return toTypeIdentifier(type);
    }

    private TypeIdentifier toTypeIdentifier(Type type) {
        return new TypeIdentifier(type.getClassName());
    }

    private List<TypeIdentifier> extractClassTypeFromGenericsSignature(String signature) {
        // ジェネリクスを使用している場合だけsignatureが入る
        List<TypeIdentifier> useTypes = new ArrayList<>();
        if (signature != null) {
            new SignatureReader(signature).accept(
                    new SignatureVisitor(this.api) {
                        @Override
                        public void visitClassType(String name) {
                            // 引数と戻り値に登場するクラスを収集
                            useTypes.add(new TypeIdentifier(name));
                        }
                    }
            );
        }
        return useTypes;
    }

    private static class MyAnnotationVisitor extends AnnotationVisitor {
        final AnnotationDescription annotationDescription = new AnnotationDescription();
        final Consumer<AnnotationDescription> finisher;

        public MyAnnotationVisitor(int api, Consumer<AnnotationDescription> finisher) {
            super(api);
            this.finisher = finisher;
        }

        @Override
        public void visit(String name, Object value) {
            annotationDescription.addParam(name, value);
            super.visit(name, value);
        }

        @Override
        public void visitEnum(String name, String descriptor, String value) {
            annotationDescription.addEnum(name, value);
            super.visitEnum(name, descriptor, value);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String name, String descriptor) {
            annotationDescription.addAnnotation(name, descriptor);
            return super.visitAnnotation(name, descriptor);
        }

        @Override
        public AnnotationVisitor visitArray(String name) {
            annotationDescription.addArray(name);
            return super.visitArray(name);
        }

        @Override
        public void visitEnd() {
            finisher.accept(annotationDescription);
        }
    }
}

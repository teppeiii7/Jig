package jig.domain.model.relation;

import jig.domain.model.identifier.Identifier;
import jig.domain.model.identifier.Identifiers;
import jig.domain.model.identifier.MethodIdentifier;
import jig.domain.model.specification.MethodSpecification;
import jig.domain.model.specification.Specification;
import org.objectweb.asm.Type;

import java.util.Arrays;

public enum RelationType {
    DEPENDENCY,
    FIELD,
    METHOD,
    METHOD_RETURN_TYPE,
    METHOD_PARAMETER,
    METHOD_USE_TYPE,
    IMPLEMENT,
    METHOD_USE_METHOD;

    public Relation of(Identifier from, Identifier to) {
        return new Relation(from, to, this);
    }

    public static void register(RelationRepository repository, Specification specification) {

        specification.fieldDescriptors.forEach(descriptor -> {
            Type fieldType = Type.getType(descriptor.toString());
            repository.registerField(specification.identifier, new Identifier(fieldType.getClassName()));
        });

        specification.methodSpecifications.forEach(methodDescriptor -> {
            String descriptor = methodDescriptor.descriptor;
            String name = methodDescriptor.methodName;
            Identifier classIdentifier = specification.identifier;

            // パラメーターの型
            Type[] argumentTypes = Type.getArgumentTypes(descriptor);

            // メソッド
            Identifiers argumentTypeIdentifiers = Arrays.stream(argumentTypes)
                    .map(Type::getClassName)
                    .map(Identifier::new)
                    .collect(Identifiers.collector());
            MethodIdentifier methodIdentifier = new MethodIdentifier(classIdentifier, name, argumentTypeIdentifiers);
            repository.registerMethod(classIdentifier, methodIdentifier);

            // 戻り値の型
            Identifier returnTypeIdentifier = methodDescriptor.getReturnTypeName();
            repository.registerMethodReturnType(methodIdentifier, returnTypeIdentifier);

            for (Identifier argumentTypeIdentifier : argumentTypeIdentifiers.list()) {
                repository.registerMethodParameter(methodIdentifier, argumentTypeIdentifier);
            }

            for (Identifier interfaceIdentifier : specification.interfaceIdentifiers.list()) {
                repository.registerImplementation(methodIdentifier, new MethodIdentifier(interfaceIdentifier, name, argumentTypeIdentifiers));
            }

            registerMethodInstruction(repository, methodDescriptor);
        });
    }

    private static void registerMethodInstruction(RelationRepository repository, MethodSpecification methodSpecification) {
        methodSpecification.usingFieldTypeIdentifiers.forEach(fieldTypeName -> {
            repository.registerMethodUseType(methodSpecification.identifier, fieldTypeName);
        });

        methodSpecification.usingMethodIdentifiers.forEach(methodName -> {
            repository.registerMethodUseMethod(methodSpecification.identifier, methodName);
        });
    }
}

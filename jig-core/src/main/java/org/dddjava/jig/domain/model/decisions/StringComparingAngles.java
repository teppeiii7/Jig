package org.dddjava.jig.domain.model.decisions;

import org.dddjava.jig.domain.model.declaration.method.Arguments;
import org.dddjava.jig.domain.model.declaration.method.MethodIdentifier;
import org.dddjava.jig.domain.model.declaration.method.MethodSignature;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodRelations;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文字列比較の切り口
 *
 * 文字列比較を行なっているメソッドはビジネスルールの分類判定を行なっている可能性が高い。
 * サービスなどに登場した場合はかなり拙いし、そうでなくても列挙を使用するなど改善の余地がある。
 */
public class StringComparingAngles {

    private final List<StringComparingAngle> list;

    public StringComparingAngles(List<StringComparingAngle> list) {
        this.list = list;
    }

    public static StringComparingAngles of(MethodRelations methodRelations) {
        // String#equals(Object)
        MethodIdentifier equalsMethod = new MethodIdentifier(
                new TypeIdentifier(String.class),
                new MethodSignature(
                        "equals",
                        new Arguments(Collections.singletonList(new TypeIdentifier(Object.class)))));

        List<StringComparingAngle> list = methodRelations.stream()
                .filterTo(equalsMethod)
                .fromMethods()
                .list()
                .stream()
                .map(StringComparingAngle::new)
                .collect(Collectors.toList());
        return new StringComparingAngles(list);
    }

    public List<StringComparingAngle> list() {
        return list;
    }
}
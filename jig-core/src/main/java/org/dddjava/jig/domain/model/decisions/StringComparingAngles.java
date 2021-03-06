package org.dddjava.jig.domain.model.decisions;

import org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.Arguments;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.MethodIdentifier;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.MethodSignature;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.analyzed.networks.method.MethodRelations;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文字列比較の切り口一覧
 *
 * 文字列比較を行なっているメソッドはビジネスルールの分類判定を行なっている可能性が高い。
 * サービスなどに登場した場合はかなり拙いし、そうでなくても列挙を使用するなど改善の余地がある。
 */
public class StringComparingAngles {

    private final List<StringComparingAngle> list;

    public StringComparingAngles(MethodRelations methodRelations) {
        // String#equals(Object)
        MethodIdentifier equalsMethod = new MethodIdentifier(
                new TypeIdentifier(String.class),
                new MethodSignature(
                        "equals",
                        new Arguments(Collections.singletonList(new TypeIdentifier(Object.class)))));

        List<StringComparingAngle> list = methodRelations.userMethodsOf(equalsMethod)
                .list()
                .stream()
                .map(StringComparingAngle::new)
                .collect(Collectors.toList());
        this.list = list;
    }

    public List<StringComparingAngle> list() {
        return list;
    }
}

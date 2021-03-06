package org.dddjava.jig.domain.model.implementation.analyzed.bytecode;

import org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.type.usernumber.UserNumber;

import java.util.List;

/**
 * 呼び出しメソッド一覧
 */
public class CallerMethods {
    List<MethodDeclaration> list;

    public CallerMethods(List<MethodDeclaration> list) {
        this.list = list;
    }

    public UserNumber toUserNumber() {
        return new UserNumber(list.size());
    }
}

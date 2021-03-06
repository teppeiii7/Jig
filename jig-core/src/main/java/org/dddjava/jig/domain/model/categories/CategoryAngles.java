package org.dddjava.jig.domain.model.categories;

import org.dddjava.jig.domain.model.implementation.analyzed.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.field.StaticFieldDeclarations;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.implementation.analyzed.networks.type.TypeRelations;

import java.util.ArrayList;
import java.util.List;

/**
 * 区分の切り口一覧
 */
public class CategoryAngles {

    List<CategoryAngle> list;

    public CategoryAngles(CategoryTypes categoryTypes, TypeRelations typeRelations, FieldDeclarations fieldDeclarations, StaticFieldDeclarations staticFieldDeclarations) {
        List<CategoryAngle> list = new ArrayList<>();
        for (CategoryType categoryType : categoryTypes.list()) {
            list.add(new CategoryAngle(categoryType, typeRelations, fieldDeclarations, staticFieldDeclarations));
        }
        this.list = list;
    }

    public List<CategoryAngle> list() {
        return list;
    }

    public TypeIdentifiers typeIdentifiers() {
        return list.stream().map(CategoryAngle::typeIdentifier).collect(TypeIdentifiers.collector());
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }
}

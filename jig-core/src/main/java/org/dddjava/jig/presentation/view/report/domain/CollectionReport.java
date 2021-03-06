package org.dddjava.jig.presentation.view.report.domain;

import org.dddjava.jig.domain.model.collections.CollectionAngle;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.TypeIdentifiers;
import org.dddjava.jig.presentation.view.report.ReportItem;
import org.dddjava.jig.presentation.view.report.ReportItemFor;
import org.dddjava.jig.presentation.view.report.ReportTitle;

@ReportTitle("COLLECTION")
public class CollectionReport {

    CollectionAngle angle;

    public CollectionReport(CollectionAngle angle) {
        this.angle = angle;
    }

    @ReportItemFor(ReportItem.クラス名)
    @ReportItemFor(ReportItem.クラス和名)
    public TypeIdentifier typeIdentifier() {
        return angle.typeIdentifier();
    }

    @ReportItemFor(ReportItem.使用箇所数)
    @ReportItemFor(ReportItem.使用箇所)
    public TypeIdentifiers userTypeIdentifiers() {
        return angle.userTypeIdentifiers();
    }

    @ReportItemFor(ReportItem.メソッド数)
    @ReportItemFor(ReportItem.メソッド一覧)
    public MethodDeclarations methods() {
        return angle.methods();
    }
}

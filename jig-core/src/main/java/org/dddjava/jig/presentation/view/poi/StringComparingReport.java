package org.dddjava.jig.presentation.view.poi;

import org.dddjava.jig.domain.model.decisions.StringComparingAngle;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.presentation.view.poi.report.ConvertibleItem;
import org.dddjava.jig.presentation.view.poi.report.Report;

import java.util.List;

/**
 * 文字列比較レポート
 */
public class StringComparingReport {

    /**
     * レポート項目
     */
    enum Items implements ConvertibleItem<MethodDeclaration> {
        クラス名 {
            @Override
            public String convert(MethodDeclaration row) {
                return row.declaringType().fullQualifiedName();
            }
        },
        メソッド名 {
            @Override
            public String convert(MethodDeclaration row) {
                return row.asSignatureSimpleText();
            }
        };
    }

    private final StringComparingAngle stringComparingAngle;

    public StringComparingReport(StringComparingAngle stringComparingAngle) {
        this.stringComparingAngle = stringComparingAngle;
    }

    public Report<?> toReport() {
        List<MethodDeclaration> list = stringComparingAngle.stringComparingMethods().list();
        return new Report<>("文字列比較箇所", list, Items.values());
    }
}
package org.dddjava.jig.presentation.view;

import org.dddjava.jig.domain.model.businessrules.BusinessRuleNetwork;
import org.dddjava.jig.domain.model.categories.CategoryAngles;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.namespace.AllPackageIdentifiers;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.namespace.PackageIdentifierFormatter;
import org.dddjava.jig.domain.model.implementation.analyzed.japanese.JapaneseNameFinder;
import org.dddjava.jig.domain.model.implementation.analyzed.networks.packages.PackageNetworks;
import org.dddjava.jig.domain.model.services.ServiceAngles;
import org.dddjava.jig.presentation.view.graphvizj.*;

public class ViewResolver {

    private final PackageIdentifierFormatter packageIdentifierFormatter;
    private final MethodNodeLabelStyle methodNodeLabelStyle;
    private final DiagramFormat diagramFormat;

    public ViewResolver(PackageIdentifierFormatter packageIdentifierFormatter, MethodNodeLabelStyle methodNodeLabelStyle, DiagramFormat diagramFormat) {
        this.packageIdentifierFormatter = packageIdentifierFormatter;
        this.methodNodeLabelStyle = methodNodeLabelStyle;
        this.diagramFormat = diagramFormat;
    }

    public JigView<PackageNetworks> dependencyWriter(JapaneseNameFinder japaneseNameFinder) {
        return newGraphvizjView(new PackageDependencyDiagram(packageIdentifierFormatter, japaneseNameFinder));
    }

    public JigView<ServiceAngles> serviceMethodCallHierarchy(JapaneseNameFinder japaneseNameFinder) {
        return newGraphvizjView(new ServiceMethodCallDiagram(japaneseNameFinder, methodNodeLabelStyle));
    }

    public JigView<CategoryAngles> enumUsage(JapaneseNameFinder japaneseNameFinder) {
        return newGraphvizjView(new CategoryUsageDiagram(japaneseNameFinder));
    }

    private <T> JigView<T> newGraphvizjView(DotTextEditor<T> diagram) {
        return new GraphvizjView<>(diagram, diagramFormat);
    }

    public JigView<ServiceAngles> booleanServiceTrace(JapaneseNameFinder japaneseNameFinder) {
        return newGraphvizjView(new BooleanServiceTraceDiagram(japaneseNameFinder, methodNodeLabelStyle));
    }

    public JigView<BusinessRuleNetwork> businessRuleNetworkWriter(JapaneseNameFinder japaneseNameFinder) {
        return newGraphvizjView(new BusinessRuleNetworkDiagram(packageIdentifierFormatter, japaneseNameFinder));
    }

    public JigView<CategoryAngles> categories(JapaneseNameFinder japaneseNameFinder) {
        return newGraphvizjView(new CategoryDiagram(japaneseNameFinder));
    }

    public JigView<AllPackageIdentifiers> packageTreeWriter(JapaneseNameFinder japaneseNameFinder) {
        return newGraphvizjView(new PackageTreeDiagram(packageIdentifierFormatter, japaneseNameFinder));
    }
}

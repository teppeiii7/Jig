package jig.diagram.graphvizj;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import jig.domain.model.identifier.namespace.PackageIdentifierFormatter;
import jig.domain.model.japanese.JapaneseName;
import jig.domain.model.japanese.JapaneseNameRepository;
import jig.domain.model.relation.dependency.PackageDependencies;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.StringJoiner;

import static java.util.stream.Collectors.joining;

@Component
public class GraphvizJavaDriver {

    final PackageIdentifierFormatter formatter;
    final JapaneseNameRepository repository;

    public GraphvizJavaDriver(PackageIdentifierFormatter formatter, JapaneseNameRepository repository) {
        this.formatter = formatter;
        this.repository = repository;
    }

    public void output(PackageDependencies packageDependencies, Path outputPath) {
        try {
            PackageIdentifierFormatter doubleQuote = value -> "\"" + value + "\"";

            String dependenciesText = packageDependencies.list().stream()
                    .map(item -> String.format("%s -> %s;",
                            item.from().format(doubleQuote),
                            item.to().format(doubleQuote)))
                    .collect(joining("\n"));

            String labelsText = packageDependencies.allPackages().stream()
                    .map(packageIdentifier -> {
                        String labelText = packageIdentifier.format(formatter);
                        if (repository.exists(packageIdentifier)) {
                            JapaneseName japaneseName = repository.get(packageIdentifier);
                            labelText = japaneseName.summarySentence() + "\\n" + labelText;
                        }

                        return String.format("%s [label=%s];",
                                packageIdentifier.format(doubleQuote),
                                doubleQuote.format(labelText));
                    })
                    .collect(joining("\n"));

            Graphviz.fromString(
                    new StringJoiner("\n", "digraph {", "}")
                            .add("node [shape=box];")
                            .add(dependenciesText)
                            .add(labelsText)
                            .toString())
                    .render(Format.PNG)
                    .toFile(outputPath.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

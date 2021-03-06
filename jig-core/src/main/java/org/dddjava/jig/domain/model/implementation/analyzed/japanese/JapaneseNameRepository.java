package org.dddjava.jig.domain.model.implementation.analyzed.japanese;

import org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.MethodIdentifier;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.namespace.PackageIdentifier;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.TypeIdentifier;

/**
 * 和名リポジトリ
 */
public interface JapaneseNameRepository {

    JapaneseName get(TypeIdentifier typeIdentifier);

    boolean exists(PackageIdentifier packageIdentifier);

    JapaneseName get(PackageIdentifier packageIdentifier);

    void register(TypeJapaneseName typeJapaneseName);

    void register(PackageJapaneseName packageJapaneseName);

    JapaneseName get(MethodIdentifier methodIdentifier);

    void register(MethodJapaneseName methodJapaneseName);
}

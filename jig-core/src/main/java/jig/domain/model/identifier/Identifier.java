package jig.domain.model.identifier;

import jig.domain.model.relation.Depth;

import java.util.Objects;
import java.util.StringJoiner;

public class Identifier {

    String value;

    public Identifier(Class<?> clz) {
        this(clz.getName());
    }

    public Identifier(String value) {
        this.value = value.replace('/', '.');
    }

    public String value() {
        return value;
    }

    public String asCompressText() {
        return value.replaceAll("(\\w)\\w+\\.", "$1.");
    }

    public String asSimpleText() {
        return value.replaceAll("([\\w]+\\.)*(\\w+)", "$2");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Identifier that = (Identifier) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    public Identifier asPackage() {
        if (!value.contains(".")) return new Identifier("(default)");
        return new Identifier(value.substring(0, value.lastIndexOf(".")));
    }

    public Identifier applyDepth(Depth depth) {
        String[] split = value.split("\\.");
        if (split.length < depth.value()) return this;

        StringJoiner sj = new StringJoiner(".");
        for (int i = 0; i < depth.value(); i++) {
            sj.add(split[i]);
        }
        return new Identifier(sj.toString());
    }
}

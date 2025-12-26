package io.github.mohmk10.changeloghub.parser.graphql.util;

public enum GraphQLTypeKind {
    OBJECT,
    INPUT_OBJECT,
    INTERFACE,
    UNION,
    ENUM,
    SCALAR;

    public boolean isInputType() {
        return this == INPUT_OBJECT || this == SCALAR || this == ENUM;
    }

    public boolean isOutputType() {
        return this == OBJECT || this == INTERFACE || this == UNION || this == SCALAR || this == ENUM;
    }

    public boolean isCompositeType() {
        return this == OBJECT || this == INTERFACE || this == UNION;
    }

    public boolean isAbstractType() {
        return this == INTERFACE || this == UNION;
    }
}

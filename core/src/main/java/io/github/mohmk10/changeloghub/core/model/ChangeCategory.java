package io.github.mohmk10.changeloghub.core.model;

public enum ChangeCategory {
    ENDPOINT,
    PARAMETER,
    REQUEST_BODY,
    RESPONSE,
    SCHEMA,
    SECURITY,
    // GraphQL specific
    TYPE,
    FIELD,
    ENUM_VALUE,
    UNION_MEMBER,
    INTERFACE
}

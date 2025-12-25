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
    INTERFACE,
    // gRPC/Protocol Buffers specific
    SERVICE,
    RPC_METHOD,
    MESSAGE,
    FIELD_NUMBER,
    STREAMING_TYPE,
    PACKAGE,
    // AsyncAPI/Event-Driven specific
    CHANNEL,
    SERVER,
    OPERATION,
    MESSAGE_PAYLOAD,
    MESSAGE_HEADERS,
    BINDING,
    PROTOCOL
}

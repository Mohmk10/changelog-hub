package io.github.mohmk10.changeloghub.parser.grpc.util;

public enum StreamType {
    
    UNARY,

    SERVER_STREAMING,

    CLIENT_STREAMING,

    BIDIRECTIONAL;

    public boolean isStreaming() {
        return this != UNARY;
    }

    public boolean hasStreamingInput() {
        return this == CLIENT_STREAMING || this == BIDIRECTIONAL;
    }

    public boolean hasStreamingOutput() {
        return this == SERVER_STREAMING || this == BIDIRECTIONAL;
    }
}

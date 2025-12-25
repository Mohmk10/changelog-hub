package io.github.mohmk10.changeloghub.parser.grpc.util;

/**
 * Enum representing gRPC streaming types.
 */
public enum StreamType {
    /**
     * Unary RPC - single request, single response.
     */
    UNARY,

    /**
     * Server streaming - single request, stream of responses.
     */
    SERVER_STREAMING,

    /**
     * Client streaming - stream of requests, single response.
     */
    CLIENT_STREAMING,

    /**
     * Bidirectional streaming - stream of requests, stream of responses.
     */
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

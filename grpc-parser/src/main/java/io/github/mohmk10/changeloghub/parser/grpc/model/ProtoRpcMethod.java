package io.github.mohmk10.changeloghub.parser.grpc.model;

import io.github.mohmk10.changeloghub.parser.grpc.util.StreamType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProtoRpcMethod {

    private final String name;
    private final String inputType;
    private final String outputType;
    private final boolean clientStreaming;
    private final boolean serverStreaming;
    private final boolean deprecated;
    private final Map<String, String> options;

    private ProtoRpcMethod(Builder builder) {
        this.name = Objects.requireNonNull(builder.name, "RPC method name is required");
        this.inputType = Objects.requireNonNull(builder.inputType, "Input type is required");
        this.outputType = Objects.requireNonNull(builder.outputType, "Output type is required");
        this.clientStreaming = builder.clientStreaming;
        this.serverStreaming = builder.serverStreaming;
        this.deprecated = builder.deprecated;
        this.options = Map.copyOf(builder.options);
    }

    public String getName() {
        return name;
    }

    public String getInputType() {
        return inputType;
    }

    public String getOutputType() {
        return outputType;
    }

    public boolean isClientStreaming() {
        return clientStreaming;
    }

    public boolean isServerStreaming() {
        return serverStreaming;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public String getOption(String key) {
        return options.get(key);
    }

    public StreamType getStreamType() {
        if (clientStreaming && serverStreaming) {
            return StreamType.BIDIRECTIONAL;
        } else if (clientStreaming) {
            return StreamType.CLIENT_STREAMING;
        } else if (serverStreaming) {
            return StreamType.SERVER_STREAMING;
        } else {
            return StreamType.UNARY;
        }
    }

    public boolean isStreaming() {
        return clientStreaming || serverStreaming;
    }

    public String getSignature() {
        StringBuilder sb = new StringBuilder("rpc ");
        sb.append(name).append("(");
        if (clientStreaming) {
            sb.append("stream ");
        }
        sb.append(inputType);
        sb.append(") returns (");
        if (serverStreaming) {
            sb.append("stream ");
        }
        sb.append(outputType);
        sb.append(")");
        return sb.toString();
    }

    public String getGrpcPath(String packageName, String serviceName) {
        if (packageName == null || packageName.isEmpty()) {
            return "/" + serviceName + "/" + name;
        }
        return "/" + packageName + "." + serviceName + "/" + name;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(String name, String inputType, String outputType) {
        return new Builder().name(name).inputType(inputType).outputType(outputType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProtoRpcMethod that = (ProtoRpcMethod) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return getSignature();
    }

    public static class Builder {
        private String name;
        private String inputType;
        private String outputType;
        private boolean clientStreaming = false;
        private boolean serverStreaming = false;
        private boolean deprecated = false;
        private final Map<String, String> options = new HashMap<>();

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder inputType(String inputType) {
            this.inputType = inputType;
            return this;
        }

        public Builder outputType(String outputType) {
            this.outputType = outputType;
            return this;
        }

        public Builder clientStreaming(boolean clientStreaming) {
            this.clientStreaming = clientStreaming;
            return this;
        }

        public Builder serverStreaming(boolean serverStreaming) {
            this.serverStreaming = serverStreaming;
            return this;
        }

        public Builder deprecated(boolean deprecated) {
            this.deprecated = deprecated;
            return this;
        }

        public Builder option(String key, String value) {
            this.options.put(key, value);
            return this;
        }

        public Builder options(Map<String, String> options) {
            this.options.putAll(options);
            return this;
        }

        public ProtoRpcMethod build() {
            return new ProtoRpcMethod(this);
        }
    }
}

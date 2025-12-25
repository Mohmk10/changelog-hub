package io.github.mohmk10.changeloghub.parser.grpc.analyzer;

import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoRpcMethod;
import io.github.mohmk10.changeloghub.parser.grpc.util.ProtoConstants;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Analyzer for parsing Protocol Buffers RPC method definitions.
 */
public class RpcMethodAnalyzer {

    private static final Pattern RPC_PATTERN = Pattern.compile(
            "rpc\\s+(\\w+)\\s*\\(\\s*(stream\\s+)?(\\w+)\\s*\\)\\s*returns\\s*\\(\\s*(stream\\s+)?(\\w+)\\s*\\)\\s*(?:\\{([^}]*)\\}|;)",
            Pattern.DOTALL
    );

    private static final Pattern OPTION_PATTERN = Pattern.compile(
            "option\\s+\\(?([\\w.]+)\\)?\\s*=\\s*([^;]+)\\s*;"
    );

    private static final Pattern DEPRECATED_PATTERN = Pattern.compile(
            "option\\s+deprecated\\s*=\\s*true\\s*;"
    );

    /**
     * Parse all RPC methods from a service body.
     */
    public List<ProtoRpcMethod> analyzeRpcMethods(String serviceBody) {
        List<ProtoRpcMethod> methods = new ArrayList<>();

        if (serviceBody == null || serviceBody.isBlank()) {
            return methods;
        }

        Matcher rpcMatcher = RPC_PATTERN.matcher(serviceBody);
        while (rpcMatcher.find()) {
            String methodName = rpcMatcher.group(1);
            boolean clientStreaming = rpcMatcher.group(2) != null;
            String inputType = rpcMatcher.group(3);
            boolean serverStreaming = rpcMatcher.group(4) != null;
            String outputType = rpcMatcher.group(5);
            String rpcBody = rpcMatcher.group(6); // May be null if no body (ends with ;)

            ProtoRpcMethod.Builder builder = ProtoRpcMethod.builder()
                    .name(methodName)
                    .inputType(inputType)
                    .outputType(outputType)
                    .clientStreaming(clientStreaming)
                    .serverStreaming(serverStreaming);

            // Parse RPC options if body exists
            if (rpcBody != null && !rpcBody.isBlank()) {
                Map<String, String> options = parseRpcOptions(rpcBody);
                builder.options(options);

                // Check for deprecated
                if (DEPRECATED_PATTERN.matcher(rpcBody).find()) {
                    builder.deprecated(true);
                }
            }

            methods.add(builder.build());
        }

        return methods;
    }

    /**
     * Parse a single RPC method definition.
     */
    public ProtoRpcMethod parseRpcMethod(String rpcDefinition) {
        Matcher matcher = RPC_PATTERN.matcher(rpcDefinition);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid RPC definition: " + rpcDefinition);
        }

        String methodName = matcher.group(1);
        boolean clientStreaming = matcher.group(2) != null;
        String inputType = matcher.group(3);
        boolean serverStreaming = matcher.group(4) != null;
        String outputType = matcher.group(5);
        String rpcBody = matcher.group(6);

        ProtoRpcMethod.Builder builder = ProtoRpcMethod.builder()
                .name(methodName)
                .inputType(inputType)
                .outputType(outputType)
                .clientStreaming(clientStreaming)
                .serverStreaming(serverStreaming);

        if (rpcBody != null && !rpcBody.isBlank()) {
            Map<String, String> options = parseRpcOptions(rpcBody);
            builder.options(options);

            if (DEPRECATED_PATTERN.matcher(rpcBody).find()) {
                builder.deprecated(true);
            }
        }

        return builder.build();
    }

    /**
     * Parse RPC-level options from an RPC body.
     */
    private Map<String, String> parseRpcOptions(String rpcBody) {
        Map<String, String> options = new HashMap<>();

        Matcher optionMatcher = OPTION_PATTERN.matcher(rpcBody);
        while (optionMatcher.find()) {
            String key = optionMatcher.group(1);
            String value = optionMatcher.group(2).trim();

            // Remove quotes from string values
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }

            // Handle nested options like (google.api.http)
            options.put(key, value);
        }

        return options;
    }

    /**
     * Extract HTTP annotations from RPC options.
     * Supports google.api.http annotations.
     */
    public Map<String, String> extractHttpAnnotations(String rpcBody) {
        Map<String, String> httpAnnotations = new HashMap<>();

        if (rpcBody == null || rpcBody.isBlank()) {
            return httpAnnotations;
        }

        // Pattern for google.api.http annotation
        Pattern httpPattern = Pattern.compile(
                "option\\s*\\(google\\.api\\.http\\)\\s*=\\s*\\{([^}]+)\\}",
                Pattern.DOTALL
        );

        Matcher matcher = httpPattern.matcher(rpcBody);
        if (matcher.find()) {
            String httpBody = matcher.group(1);

            // Extract HTTP method and path
            Pattern methodPattern = Pattern.compile(
                    "(get|post|put|delete|patch)\\s*:\\s*\"([^\"]+)\""
            );

            Matcher methodMatcher = methodPattern.matcher(httpBody);
            if (methodMatcher.find()) {
                httpAnnotations.put("method", methodMatcher.group(1).toUpperCase());
                httpAnnotations.put("path", methodMatcher.group(2));
            }

            // Extract body field if present
            Pattern bodyPattern = Pattern.compile(
                    "body\\s*:\\s*\"([^\"]+)\""
            );

            Matcher bodyMatcher = bodyPattern.matcher(httpBody);
            if (bodyMatcher.find()) {
                httpAnnotations.put("body", bodyMatcher.group(1));
            }
        }

        return httpAnnotations;
    }

    /**
     * Get all input types used by RPC methods.
     */
    public Set<String> getInputTypes(List<ProtoRpcMethod> methods) {
        Set<String> types = new LinkedHashSet<>();
        for (ProtoRpcMethod method : methods) {
            types.add(method.getInputType());
        }
        return types;
    }

    /**
     * Get all output types used by RPC methods.
     */
    public Set<String> getOutputTypes(List<ProtoRpcMethod> methods) {
        Set<String> types = new LinkedHashSet<>();
        for (ProtoRpcMethod method : methods) {
            types.add(method.getOutputType());
        }
        return types;
    }

    /**
     * Get all types (input and output) used by RPC methods.
     */
    public Set<String> getAllMessageTypes(List<ProtoRpcMethod> methods) {
        Set<String> types = new LinkedHashSet<>();
        types.addAll(getInputTypes(methods));
        types.addAll(getOutputTypes(methods));
        return types;
    }
}

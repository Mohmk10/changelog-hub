package io.github.mohmk10.changeloghub.parser.grpc.analyzer;

import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoRpcMethod;
import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoService;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Analyzer for parsing Protocol Buffers service definitions.
 */
public class ServiceAnalyzer {

    private final RpcMethodAnalyzer rpcMethodAnalyzer;

    // Pattern to match service blocks with nested content handling
    private static final Pattern SERVICE_START_PATTERN = Pattern.compile(
            "service\\s+(\\w+)\\s*\\{"
    );

    private static final Pattern DEPRECATED_OPTION_PATTERN = Pattern.compile(
            "option\\s+deprecated\\s*=\\s*true\\s*;"
    );

    public ServiceAnalyzer() {
        this.rpcMethodAnalyzer = new RpcMethodAnalyzer();
    }

    public ServiceAnalyzer(RpcMethodAnalyzer rpcMethodAnalyzer) {
        this.rpcMethodAnalyzer = rpcMethodAnalyzer;
    }

    /**
     * Parse all services from proto content.
     */
    public List<ProtoService> analyzeServices(String content, String packageName) {
        List<ProtoService> services = new ArrayList<>();

        if (content == null || content.isBlank()) {
            return services;
        }

        // Strip comments before parsing
        String cleanContent = stripComments(content);

        // Find all service blocks
        List<ServiceBlock> serviceBlocks = findServiceBlocks(cleanContent);

        for (ServiceBlock block : serviceBlocks) {
            ProtoService service = parseService(block.name, block.body, packageName);
            services.add(service);
        }

        return services;
    }

    /**
     * Parse a single service definition.
     */
    public ProtoService parseService(String name, String body, String packageName) {
        ProtoService.Builder builder = ProtoService.builder()
                .name(name);

        // Set full name
        if (packageName != null && !packageName.isEmpty()) {
            builder.fullName(packageName + "." + name);
        }

        // Parse RPC methods
        List<ProtoRpcMethod> methods = rpcMethodAnalyzer.analyzeRpcMethods(body);
        builder.methods(methods);

        // Check for deprecated option
        if (DEPRECATED_OPTION_PATTERN.matcher(body).find()) {
            builder.deprecated(true);
        }

        // Parse service-level options
        Map<String, String> options = parseServiceOptions(body);
        builder.options(options);

        return builder.build();
    }

    /**
     * Find service blocks handling nested braces.
     */
    private List<ServiceBlock> findServiceBlocks(String content) {
        List<ServiceBlock> blocks = new ArrayList<>();

        Matcher startMatcher = SERVICE_START_PATTERN.matcher(content);
        while (startMatcher.find()) {
            String serviceName = startMatcher.group(1);
            int startIndex = startMatcher.end() - 1; // Position of opening brace

            // Find the matching closing brace
            int braceCount = 1;
            int endIndex = startIndex + 1;

            while (endIndex < content.length() && braceCount > 0) {
                char c = content.charAt(endIndex);
                if (c == '{') {
                    braceCount++;
                } else if (c == '}') {
                    braceCount--;
                }
                endIndex++;
            }

            if (braceCount == 0) {
                // Extract the body (between braces)
                String body = content.substring(startIndex + 1, endIndex - 1);
                blocks.add(new ServiceBlock(serviceName, body));
            }
        }

        return blocks;
    }

    /**
     * Parse service-level options.
     */
    private Map<String, String> parseServiceOptions(String serviceBody) {
        Map<String, String> options = new HashMap<>();

        Pattern optionPattern = Pattern.compile(
                "option\\s+\\(?([\\w.]+)\\)?\\s*=\\s*([^;]+)\\s*;"
        );

        Matcher matcher = optionPattern.matcher(serviceBody);
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2).trim();

            // Remove quotes from string values
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }

            options.put(key, value);
        }

        return options;
    }

    /**
     * Get all message types referenced by services (inputs and outputs).
     */
    public Set<String> getReferencedMessageTypes(List<ProtoService> services) {
        Set<String> types = new LinkedHashSet<>();

        for (ProtoService service : services) {
            for (ProtoRpcMethod method : service.getMethods()) {
                types.add(method.getInputType());
                types.add(method.getOutputType());
            }
        }

        return types;
    }

    /**
     * Strip comments from proto content.
     */
    private String stripComments(String content) {
        // Remove multi-line comments /* ... */
        String result = content.replaceAll("/\\*[\\s\\S]*?\\*/", "");

        // Remove single-line comments // ...
        result = result.replaceAll("//[^\n]*", "");

        return result;
    }

    /**
     * Internal class to hold service block data.
     */
    private static class ServiceBlock {
        final String name;
        final String body;

        ServiceBlock(String name, String body) {
            this.name = name;
            this.body = body;
        }
    }
}

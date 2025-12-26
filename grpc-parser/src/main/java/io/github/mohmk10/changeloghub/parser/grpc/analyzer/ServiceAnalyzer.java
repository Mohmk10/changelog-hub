package io.github.mohmk10.changeloghub.parser.grpc.analyzer;

import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoRpcMethod;
import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoService;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceAnalyzer {

    private final RpcMethodAnalyzer rpcMethodAnalyzer;

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

    public List<ProtoService> analyzeServices(String content, String packageName) {
        List<ProtoService> services = new ArrayList<>();

        if (content == null || content.isBlank()) {
            return services;
        }

        String cleanContent = stripComments(content);

        List<ServiceBlock> serviceBlocks = findServiceBlocks(cleanContent);

        for (ServiceBlock block : serviceBlocks) {
            ProtoService service = parseService(block.name, block.body, packageName);
            services.add(service);
        }

        return services;
    }

    public ProtoService parseService(String name, String body, String packageName) {
        ProtoService.Builder builder = ProtoService.builder()
                .name(name);

        if (packageName != null && !packageName.isEmpty()) {
            builder.fullName(packageName + "." + name);
        }

        List<ProtoRpcMethod> methods = rpcMethodAnalyzer.analyzeRpcMethods(body);
        builder.methods(methods);

        if (DEPRECATED_OPTION_PATTERN.matcher(body).find()) {
            builder.deprecated(true);
        }

        Map<String, String> options = parseServiceOptions(body);
        builder.options(options);

        return builder.build();
    }

    private List<ServiceBlock> findServiceBlocks(String content) {
        List<ServiceBlock> blocks = new ArrayList<>();

        Matcher startMatcher = SERVICE_START_PATTERN.matcher(content);
        while (startMatcher.find()) {
            String serviceName = startMatcher.group(1);
            int startIndex = startMatcher.end() - 1; 

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
                
                String body = content.substring(startIndex + 1, endIndex - 1);
                blocks.add(new ServiceBlock(serviceName, body));
            }
        }

        return blocks;
    }

    private Map<String, String> parseServiceOptions(String serviceBody) {
        Map<String, String> options = new HashMap<>();

        Pattern optionPattern = Pattern.compile(
                "option\\s+\\(?([\\w.]+)\\)?\\s*=\\s*([^;]+)\\s*;"
        );

        Matcher matcher = optionPattern.matcher(serviceBody);
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2).trim();

            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }

            options.put(key, value);
        }

        return options;
    }

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

    private String stripComments(String content) {
        
        String result = content.replaceAll("/\\*[\\s\\S]*?\\*/", "");

        result = result.replaceAll("//[^\n]*", "");

        return result;
    }

    private static class ServiceBlock {
        final String name;
        final String body;

        ServiceBlock(String name, String body) {
            this.name = name;
            this.body = body;
        }
    }
}

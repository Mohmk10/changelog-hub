package io.github.mohmk10.changeloghub.parser.grpc.mapper;

import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.core.model.ApiType;
import io.github.mohmk10.changeloghub.core.model.Endpoint;
import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoEnum;
import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoFile;
import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoMessage;
import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoService;

import java.time.LocalDateTime;
import java.util.*;

public class GrpcModelMapper {

    private final GrpcEndpointMapper endpointMapper;
    private final GrpcTypeMapper typeMapper;
    private final GrpcParameterMapper parameterMapper;

    public GrpcModelMapper() {
        this.parameterMapper = new GrpcParameterMapper();
        this.endpointMapper = new GrpcEndpointMapper(parameterMapper);
        this.typeMapper = new GrpcTypeMapper();
    }

    public GrpcModelMapper(GrpcEndpointMapper endpointMapper, GrpcTypeMapper typeMapper,
                           GrpcParameterMapper parameterMapper) {
        this.endpointMapper = endpointMapper;
        this.typeMapper = typeMapper;
        this.parameterMapper = parameterMapper;
    }

    public ApiSpec mapProtoFile(ProtoFile protoFile) {
        
        Map<String, ProtoMessage> messageMap = buildMessageMap(protoFile);

        List<Endpoint> endpoints = endpointMapper.mapServices(
                protoFile.getServices(),
                protoFile.getPackageName(),
                messageMap
        );

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("syntax", protoFile.getSyntax());
        metadata.put("package", protoFile.getPackageName());
        metadata.put("fileName", protoFile.getFileName());
        metadata.put("statistics", protoFile.getStatistics());

        Map<String, Map<String, Object>> schemas = typeMapper.buildSchemaMap(
                protoFile.getMessages(),
                protoFile.getEnums()
        );
        metadata.put("schemas", schemas);

        String name = buildApiName(protoFile);
        String version = extractVersion(protoFile);

        return ApiSpec.builder()
                .name(name)
                .version(version)
                .type(ApiType.GRPC)
                .endpoints(endpoints)
                .metadata(metadata)
                .parsedAt(LocalDateTime.now())
                .build();
    }

    public ApiSpec mapProtoFiles(List<ProtoFile> protoFiles) {
        if (protoFiles.isEmpty()) {
            return ApiSpec.builder()
                    .name("Empty API")
                    .version("0.0.0")
                    .type(ApiType.GRPC)
                    .parsedAt(LocalDateTime.now())
                    .build();
        }

        ProtoFile primaryFile = protoFiles.get(0);

        List<Endpoint> allEndpoints = new ArrayList<>();
        Map<String, Map<String, Object>> allSchemas = new LinkedHashMap<>();

        for (ProtoFile protoFile : protoFiles) {
            Map<String, ProtoMessage> messageMap = buildMessageMap(protoFile);

            allEndpoints.addAll(endpointMapper.mapServices(
                    protoFile.getServices(),
                    protoFile.getPackageName(),
                    messageMap
            ));

            allSchemas.putAll(typeMapper.buildSchemaMap(
                    protoFile.getMessages(),
                    protoFile.getEnums()
            ));
        }

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("syntax", primaryFile.getSyntax());
        metadata.put("package", primaryFile.getPackageName());
        metadata.put("fileCount", protoFiles.size());
        metadata.put("schemas", allSchemas);

        return ApiSpec.builder()
                .name(buildApiName(primaryFile))
                .version(extractVersion(primaryFile))
                .type(ApiType.GRPC)
                .endpoints(allEndpoints)
                .metadata(metadata)
                .parsedAt(LocalDateTime.now())
                .build();
    }

    private String buildApiName(ProtoFile protoFile) {
        String packageName = protoFile.getPackageName();
        if (packageName != null && !packageName.isEmpty()) {
            return packageName;
        }

        String fileName = protoFile.getFileName();
        if (fileName != null) {
            return fileName.replace(".proto", "");
        }

        return "gRPC API";
    }

    private String extractVersion(ProtoFile protoFile) {
        
        String version = protoFile.getOption("api_version");
        if (version != null) {
            return version;
        }

        String fileName = protoFile.getFileName();
        if (fileName != null) {
            if (fileName.contains("_v")) {
                int vIndex = fileName.lastIndexOf("_v");
                int dotIndex = fileName.indexOf(".", vIndex);
                if (dotIndex > vIndex) {
                    return fileName.substring(vIndex + 2, dotIndex) + ".0.0";
                }
            }
        }

        String packageName = protoFile.getPackageName();
        if (packageName != null && packageName.contains(".v")) {
            String[] parts = packageName.split("\\.");
            for (String part : parts) {
                if (part.startsWith("v") && part.length() > 1) {
                    char c = part.charAt(1);
                    if (Character.isDigit(c)) {
                        return part.substring(1) + ".0.0";
                    }
                }
            }
        }

        return "1.0.0";
    }

    private Map<String, ProtoMessage> buildMessageMap(ProtoFile protoFile) {
        Map<String, ProtoMessage> messageMap = new LinkedHashMap<>();

        for (ProtoMessage message : protoFile.getMessages()) {
            
            messageMap.put(message.getName(), message);
            
            messageMap.put(message.getFullName(), message);

            addNestedMessages(message, messageMap);
        }

        return messageMap;
    }

    private void addNestedMessages(ProtoMessage parent, Map<String, ProtoMessage> messageMap) {
        for (ProtoMessage nested : parent.getNestedMessages()) {
            messageMap.put(nested.getName(), nested);
            messageMap.put(nested.getFullName(), nested);
            addNestedMessages(nested, messageMap);
        }
    }

    public List<String> getServiceNames(ProtoFile protoFile) {
        List<String> names = new ArrayList<>();
        for (ProtoService service : protoFile.getServices()) {
            names.add(service.getName());
        }
        return names;
    }

    public List<String> getMessageNames(ProtoFile protoFile) {
        List<String> names = new ArrayList<>();
        for (ProtoMessage message : protoFile.getMessages()) {
            names.add(message.getName());
            
            addNestedMessageNames(message, names);
        }
        return names;
    }

    private void addNestedMessageNames(ProtoMessage parent, List<String> names) {
        for (ProtoMessage nested : parent.getNestedMessages()) {
            names.add(nested.getFullName());
            addNestedMessageNames(nested, names);
        }
    }

    public List<String> getEnumNames(ProtoFile protoFile) {
        List<String> names = new ArrayList<>();
        for (ProtoEnum protoEnum : protoFile.getEnums()) {
            names.add(protoEnum.getName());
        }
        return names;
    }
}

package io.github.mohmk10.changeloghub.parser.grpc.mapper;

import io.github.mohmk10.changeloghub.core.model.Endpoint;
import io.github.mohmk10.changeloghub.core.model.HttpMethod;
import io.github.mohmk10.changeloghub.core.model.Parameter;
import io.github.mohmk10.changeloghub.core.model.Response;
import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoMessage;
import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoRpcMethod;
import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoService;
import io.github.mohmk10.changeloghub.parser.grpc.util.StreamType;

import java.util.*;

public class GrpcEndpointMapper {

    private final GrpcParameterMapper parameterMapper;

    public GrpcEndpointMapper() {
        this.parameterMapper = new GrpcParameterMapper();
    }

    public GrpcEndpointMapper(GrpcParameterMapper parameterMapper) {
        this.parameterMapper = parameterMapper;
    }

    public Endpoint mapRpcMethod(ProtoRpcMethod method, ProtoService service,
                                  String packageName, Map<String, ProtoMessage> messages) {
        String path = method.getGrpcPath(packageName, service.getName());

        List<Parameter> parameters = parameterMapper.mapRpcInput(method, messages);

        Response response = createResponse(method, messages);

        StringBuilder description = new StringBuilder();
        description.append("gRPC method: ").append(method.getSignature());
        if (method.isStreaming()) {
            description.append("\nStreaming type: ").append(method.getStreamType());
        }
        if (method.isDeprecated()) {
            description.append("\n[DEPRECATED]");
        }

        String operationId = service.getName() + "." + method.getName();

        return Endpoint.builder()
                .path(path)
                .method(HttpMethod.POST) 
                .operationId(operationId)
                .summary(method.getName())
                .description(description.toString())
                .parameters(parameters)
                .addResponse(response)
                .addTag(service.getName())
                .deprecated(method.isDeprecated())
                .build();
    }

    public List<Endpoint> mapService(ProtoService service, String packageName,
                                      Map<String, ProtoMessage> messages) {
        List<Endpoint> endpoints = new ArrayList<>();

        for (ProtoRpcMethod method : service.getMethods()) {
            endpoints.add(mapRpcMethod(method, service, packageName, messages));
        }

        return endpoints;
    }

    public List<Endpoint> mapServices(List<ProtoService> services, String packageName,
                                       Map<String, ProtoMessage> messages) {
        List<Endpoint> endpoints = new ArrayList<>();

        for (ProtoService service : services) {
            endpoints.addAll(mapService(service, packageName, messages));
        }

        return endpoints;
    }

    private Response createResponse(ProtoRpcMethod method, Map<String, ProtoMessage> messages) {
        String outputType = method.getOutputType();
        boolean isStreaming = method.isServerStreaming();

        StringBuilder description = new StringBuilder();
        description.append("Response: ").append(outputType);
        if (isStreaming) {
            description.append(" (streaming)");
        }

        Response response = new Response();
        response.setStatusCode("200");
        response.setDescription(description.toString());
        response.setContentType("application/grpc");
        response.setSchemaRef("#/definitions/" + outputType);

        return response;
    }

    public String getGrpcPath(String packageName, String serviceName, String methodName) {
        if (packageName == null || packageName.isEmpty()) {
            return "/" + serviceName + "/" + methodName;
        }
        return "/" + packageName + "." + serviceName + "/" + methodName;
    }

    public String extractServiceName(String grpcPath) {
        if (grpcPath == null || grpcPath.isEmpty()) {
            return null;
        }

        String path = grpcPath.startsWith("/") ? grpcPath.substring(1) : grpcPath;

        int lastSlash = path.lastIndexOf('/');
        if (lastSlash > 0) {
            String serviceFullName = path.substring(0, lastSlash);
            int lastDot = serviceFullName.lastIndexOf('.');
            return lastDot > 0 ? serviceFullName.substring(lastDot + 1) : serviceFullName;
        }

        return null;
    }

    public String extractMethodName(String grpcPath) {
        if (grpcPath == null || grpcPath.isEmpty()) {
            return null;
        }

        int lastSlash = grpcPath.lastIndexOf('/');
        return lastSlash >= 0 ? grpcPath.substring(lastSlash + 1) : grpcPath;
    }

    public Map<String, Object> createEndpointMetadata(ProtoRpcMethod method, ProtoService service) {
        Map<String, Object> metadata = new LinkedHashMap<>();

        metadata.put("service", service.getName());
        metadata.put("method", method.getName());
        metadata.put("inputType", method.getInputType());
        metadata.put("outputType", method.getOutputType());
        metadata.put("streamType", method.getStreamType().name());
        metadata.put("clientStreaming", method.isClientStreaming());
        metadata.put("serverStreaming", method.isServerStreaming());
        metadata.put("deprecated", method.isDeprecated());

        return metadata;
    }
}

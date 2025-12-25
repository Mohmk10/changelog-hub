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

/**
 * Mapper for converting Protocol Buffers RPC methods to core Endpoint objects.
 */
public class GrpcEndpointMapper {

    private final GrpcParameterMapper parameterMapper;

    public GrpcEndpointMapper() {
        this.parameterMapper = new GrpcParameterMapper();
    }

    public GrpcEndpointMapper(GrpcParameterMapper parameterMapper) {
        this.parameterMapper = parameterMapper;
    }

    /**
     * Map an RPC method to an Endpoint.
     */
    public Endpoint mapRpcMethod(ProtoRpcMethod method, ProtoService service,
                                  String packageName, Map<String, ProtoMessage> messages) {
        String path = method.getGrpcPath(packageName, service.getName());

        // Map parameters
        List<Parameter> parameters = parameterMapper.mapRpcInput(method, messages);

        // Create response
        Response response = createResponse(method, messages);

        // Build description
        StringBuilder description = new StringBuilder();
        description.append("gRPC method: ").append(method.getSignature());
        if (method.isStreaming()) {
            description.append("\nStreaming type: ").append(method.getStreamType());
        }
        if (method.isDeprecated()) {
            description.append("\n[DEPRECATED]");
        }

        // Build operation ID
        String operationId = service.getName() + "." + method.getName();

        return Endpoint.builder()
                .path(path)
                .method(HttpMethod.POST) // gRPC always uses POST
                .operationId(operationId)
                .summary(method.getName())
                .description(description.toString())
                .parameters(parameters)
                .addResponse(response)
                .addTag(service.getName())
                .deprecated(method.isDeprecated())
                .build();
    }

    /**
     * Map all RPC methods from a service to endpoints.
     */
    public List<Endpoint> mapService(ProtoService service, String packageName,
                                      Map<String, ProtoMessage> messages) {
        List<Endpoint> endpoints = new ArrayList<>();

        for (ProtoRpcMethod method : service.getMethods()) {
            endpoints.add(mapRpcMethod(method, service, packageName, messages));
        }

        return endpoints;
    }

    /**
     * Map all services to endpoints.
     */
    public List<Endpoint> mapServices(List<ProtoService> services, String packageName,
                                       Map<String, ProtoMessage> messages) {
        List<Endpoint> endpoints = new ArrayList<>();

        for (ProtoService service : services) {
            endpoints.addAll(mapService(service, packageName, messages));
        }

        return endpoints;
    }

    /**
     * Create a Response object for an RPC method.
     */
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

    /**
     * Get the gRPC path format for a service method.
     */
    public String getGrpcPath(String packageName, String serviceName, String methodName) {
        if (packageName == null || packageName.isEmpty()) {
            return "/" + serviceName + "/" + methodName;
        }
        return "/" + packageName + "." + serviceName + "/" + methodName;
    }

    /**
     * Extract service name from a gRPC path.
     */
    public String extractServiceName(String grpcPath) {
        if (grpcPath == null || grpcPath.isEmpty()) {
            return null;
        }

        // Remove leading slash
        String path = grpcPath.startsWith("/") ? grpcPath.substring(1) : grpcPath;

        // Split by last /
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash > 0) {
            String serviceFullName = path.substring(0, lastSlash);
            int lastDot = serviceFullName.lastIndexOf('.');
            return lastDot > 0 ? serviceFullName.substring(lastDot + 1) : serviceFullName;
        }

        return null;
    }

    /**
     * Extract method name from a gRPC path.
     */
    public String extractMethodName(String grpcPath) {
        if (grpcPath == null || grpcPath.isEmpty()) {
            return null;
        }

        int lastSlash = grpcPath.lastIndexOf('/');
        return lastSlash >= 0 ? grpcPath.substring(lastSlash + 1) : grpcPath;
    }

    /**
     * Create endpoint metadata map.
     */
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

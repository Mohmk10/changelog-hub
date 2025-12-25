package io.github.mohmk10.changeloghub.parser.grpc.mapper;

import io.github.mohmk10.changeloghub.core.model.Parameter;
import io.github.mohmk10.changeloghub.core.model.ParameterLocation;
import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoField;
import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoMessage;
import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoRpcMethod;

import java.util.*;

/**
 * Mapper for converting Protocol Buffers fields to core Parameter objects.
 */
public class GrpcParameterMapper {

    /**
     * Map a ProtoField to a Parameter.
     */
    public Parameter mapField(ProtoField field, String messageContext) {
        Parameter param = new Parameter();
        param.setName(field.getName());
        param.setType(mapFieldType(field));
        param.setRequired(field.isRequired());
        param.setLocation(ParameterLocation.BODY); // gRPC always uses body

        // Build description
        StringBuilder description = new StringBuilder();
        description.append("Field ").append(field.getName());
        description.append(" (").append(field.getFullTypeSignature()).append(")");
        description.append(" in ").append(messageContext);

        if (field.isPartOfOneof()) {
            description.append(" [oneof: ").append(field.getOneofName().orElse("?")).append("]");
        }

        if (field.isDeprecated()) {
            description.append(" [DEPRECATED]");
        }

        param.setDescription(description.toString());

        // Add default value if present
        field.getDefaultValue().ifPresent(param::setDefaultValue);

        return param;
    }

    /**
     * Map all fields from a request message to parameters.
     */
    public List<Parameter> mapRequestFields(ProtoMessage requestMessage) {
        List<Parameter> parameters = new ArrayList<>();

        if (requestMessage == null) {
            return parameters;
        }

        for (ProtoField field : requestMessage.getFields()) {
            parameters.add(mapField(field, requestMessage.getFullName()));
        }

        return parameters;
    }

    /**
     * Map RPC method input to parameters.
     */
    public List<Parameter> mapRpcInput(ProtoRpcMethod method, Map<String, ProtoMessage> messages) {
        List<Parameter> parameters = new ArrayList<>();

        String inputType = method.getInputType();
        ProtoMessage inputMessage = messages.get(inputType);

        if (inputMessage != null) {
            // Add each field as a parameter
            parameters.addAll(mapRequestFields(inputMessage));
        } else {
            // Create a single body parameter for the input message
            Parameter param = new Parameter();
            param.setName("request");
            param.setType("object");
            param.setRequired(true);
            param.setLocation(ParameterLocation.BODY);
            param.setDescription("Request message: " + inputType);
            parameters.add(param);
        }

        // Add streaming indicator if client streaming
        if (method.isClientStreaming()) {
            Parameter streamParam = new Parameter();
            streamParam.setName("_streaming");
            streamParam.setType("boolean");
            streamParam.setRequired(false);
            streamParam.setLocation(ParameterLocation.HEADER);
            streamParam.setDescription("Client streaming: stream of " + inputType);
            streamParam.setDefaultValue("true");
            parameters.add(streamParam);
        }

        return parameters;
    }

    /**
     * Map a field type to a string representation.
     */
    private String mapFieldType(ProtoField field) {
        if (field.isMap()) {
            return "map<" + field.getMapKeyType().orElse("?") +
                    ", " + field.getMapValueType().orElse("?") + ">";
        }

        if (field.isRepeated()) {
            return "array[" + field.getTypeName() + "]";
        }

        return field.getType().getApiType();
    }

    /**
     * Create a summary of parameters for an RPC method.
     */
    public Map<String, Object> createParameterSummary(ProtoRpcMethod method, Map<String, ProtoMessage> messages) {
        Map<String, Object> summary = new LinkedHashMap<>();

        summary.put("methodName", method.getName());
        summary.put("inputType", method.getInputType());
        summary.put("outputType", method.getOutputType());
        summary.put("clientStreaming", method.isClientStreaming());
        summary.put("serverStreaming", method.isServerStreaming());

        ProtoMessage inputMessage = messages.get(method.getInputType());
        if (inputMessage != null) {
            List<Map<String, Object>> fieldSummaries = new ArrayList<>();
            for (ProtoField field : inputMessage.getFields()) {
                Map<String, Object> fieldSummary = new LinkedHashMap<>();
                fieldSummary.put("name", field.getName());
                fieldSummary.put("type", field.getFullTypeSignature());
                fieldSummary.put("number", field.getNumber());
                fieldSummary.put("required", field.isRequired());
                fieldSummary.put("deprecated", field.isDeprecated());
                fieldSummaries.add(fieldSummary);
            }
            summary.put("inputFields", fieldSummaries);
        }

        return summary;
    }

    /**
     * Compare parameters between two versions.
     */
    public Map<String, List<Parameter>> compareParameters(
            List<Parameter> oldParams,
            List<Parameter> newParams) {

        Map<String, List<Parameter>> result = new LinkedHashMap<>();
        result.put("added", new ArrayList<>());
        result.put("removed", new ArrayList<>());
        result.put("modified", new ArrayList<>());

        Set<String> oldNames = new HashSet<>();
        Map<String, Parameter> oldMap = new HashMap<>();
        for (Parameter p : oldParams) {
            oldNames.add(p.getName());
            oldMap.put(p.getName(), p);
        }

        Set<String> newNames = new HashSet<>();
        Map<String, Parameter> newMap = new HashMap<>();
        for (Parameter p : newParams) {
            newNames.add(p.getName());
            newMap.put(p.getName(), p);
        }

        // Find added
        for (String name : newNames) {
            if (!oldNames.contains(name)) {
                result.get("added").add(newMap.get(name));
            }
        }

        // Find removed
        for (String name : oldNames) {
            if (!newNames.contains(name)) {
                result.get("removed").add(oldMap.get(name));
            }
        }

        // Find modified
        for (String name : oldNames) {
            if (newNames.contains(name)) {
                Parameter oldParam = oldMap.get(name);
                Parameter newParam = newMap.get(name);

                if (!Objects.equals(oldParam.getType(), newParam.getType()) ||
                        oldParam.isRequired() != newParam.isRequired()) {
                    result.get("modified").add(newParam);
                }
            }
        }

        return result;
    }
}

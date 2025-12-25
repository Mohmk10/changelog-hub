package io.github.mohmk10.changeloghub.parser.asyncapi.mapper;

import io.github.mohmk10.changeloghub.core.model.Parameter;
import io.github.mohmk10.changeloghub.core.model.ParameterLocation;
import io.github.mohmk10.changeloghub.parser.asyncapi.model.AsyncChannel;
import io.github.mohmk10.changeloghub.parser.asyncapi.model.AsyncMessage;
import io.github.mohmk10.changeloghub.parser.asyncapi.model.AsyncSchema;

import java.util.*;

/**
 * Maps AsyncAPI message fields and channel parameters to core Parameter model.
 */
public class AsyncApiParameterMapper {

    private final AsyncApiSchemaMapper schemaMapper;

    public AsyncApiParameterMapper() {
        this.schemaMapper = new AsyncApiSchemaMapper();
    }

    public AsyncApiParameterMapper(AsyncApiSchemaMapper schemaMapper) {
        this.schemaMapper = schemaMapper;
    }

    /**
     * Map channel parameters to core Parameters.
     */
    public List<Parameter> mapChannelParameters(AsyncChannel channel) {
        List<Parameter> parameters = new ArrayList<>();

        if (channel == null || channel.getParameters() == null) {
            return parameters;
        }

        for (Map.Entry<String, AsyncChannel.ChannelParameter> entry : channel.getParameters().entrySet()) {
            Parameter param = mapChannelParameter(entry.getKey(), entry.getValue());
            if (param != null) {
                parameters.add(param);
            }
        }

        return parameters;
    }

    /**
     * Map a single channel parameter.
     */
    public Parameter mapChannelParameter(String name, AsyncChannel.ChannelParameter channelParam) {
        Parameter param = new Parameter();
        param.setName(name);
        param.setLocation(ParameterLocation.PATH);
        param.setRequired(true); // Channel parameters are always required

        if (channelParam != null) {
            if (channelParam.getDescription() != null) {
                param.setDescription(channelParam.getDescription());
            }

            if (channelParam.getSchema() != null) {
                param.setType(schemaMapper.mapToType(channelParam.getSchema()));
            } else {
                // Default to string type for channel parameters
                param.setType("string");
            }
        } else {
            param.setType("string");
        }

        return param;
    }

    /**
     * Map message payload fields to parameters.
     */
    public List<Parameter> mapMessagePayloadFields(AsyncMessage message) {
        List<Parameter> parameters = new ArrayList<>();

        if (message == null || message.getPayload() == null) {
            return parameters;
        }

        AsyncSchema payload = message.getPayload();
        if (payload.getProperties() != null) {
            Set<String> requiredFields = payload.getRequiredFields() != null ?
                    new HashSet<>(payload.getRequiredFields()) : Collections.emptySet();

            for (Map.Entry<String, AsyncSchema> entry : payload.getProperties().entrySet()) {
                Parameter param = mapPayloadField(entry.getKey(), entry.getValue(),
                        requiredFields.contains(entry.getKey()));
                if (param != null) {
                    parameters.add(param);
                }
            }
        }

        return parameters;
    }

    /**
     * Map a payload field to a parameter.
     */
    public Parameter mapPayloadField(String name, AsyncSchema fieldSchema, boolean required) {
        Parameter param = new Parameter();
        param.setName(name);
        param.setLocation(ParameterLocation.BODY);
        param.setRequired(required);

        if (fieldSchema != null) {
            if (fieldSchema.getDescription() != null) {
                param.setDescription(fieldSchema.getDescription());
            }

            param.setType(schemaMapper.mapToType(fieldSchema));

            if (fieldSchema.getDefaultValue() != null) {
                param.setDefaultValue(String.valueOf(fieldSchema.getDefaultValue()));
            }
        }

        return param;
    }

    /**
     * Map message header fields to parameters.
     */
    public List<Parameter> mapMessageHeaders(AsyncMessage message) {
        List<Parameter> parameters = new ArrayList<>();

        if (message == null || message.getHeaders() == null) {
            return parameters;
        }

        AsyncSchema headers = message.getHeaders();
        if (headers.getProperties() != null) {
            Set<String> requiredFields = headers.getRequiredFields() != null ?
                    new HashSet<>(headers.getRequiredFields()) : Collections.emptySet();

            for (Map.Entry<String, AsyncSchema> entry : headers.getProperties().entrySet()) {
                Parameter param = mapHeaderField(entry.getKey(), entry.getValue(),
                        requiredFields.contains(entry.getKey()));
                if (param != null) {
                    parameters.add(param);
                }
            }
        }

        return parameters;
    }

    /**
     * Map a header field to a parameter.
     */
    public Parameter mapHeaderField(String name, AsyncSchema fieldSchema, boolean required) {
        Parameter param = new Parameter();
        param.setName(name);
        param.setLocation(ParameterLocation.HEADER);
        param.setRequired(required);

        if (fieldSchema != null) {
            if (fieldSchema.getDescription() != null) {
                param.setDescription(fieldSchema.getDescription());
            }

            param.setType(schemaMapper.mapToType(fieldSchema));
        }

        return param;
    }

    /**
     * Map all parameters from a message (payload + headers).
     */
    public List<Parameter> mapAllMessageParameters(AsyncMessage message) {
        List<Parameter> parameters = new ArrayList<>();

        // Add payload fields
        parameters.addAll(mapMessagePayloadFields(message));

        // Add header fields
        parameters.addAll(mapMessageHeaders(message));

        return parameters;
    }

    /**
     * Map all parameters for an endpoint (channel + message).
     */
    public List<Parameter> mapEndpointParameters(AsyncChannel channel, AsyncMessage message) {
        List<Parameter> parameters = new ArrayList<>();

        // Channel parameters (path)
        parameters.addAll(mapChannelParameters(channel));

        // Message parameters (body + headers)
        if (message != null) {
            parameters.addAll(mapAllMessageParameters(message));
        }

        return parameters;
    }

    /**
     * Get parameter names by location.
     */
    public List<String> getParameterNamesByLocation(List<Parameter> parameters, ParameterLocation location) {
        List<String> names = new ArrayList<>();
        if (parameters != null) {
            for (Parameter param : parameters) {
                if (param.getLocation() == location) {
                    names.add(param.getName());
                }
            }
        }
        return names;
    }

    /**
     * Find a parameter by name.
     */
    public Parameter findParameter(List<Parameter> parameters, String name) {
        if (parameters == null || name == null) {
            return null;
        }
        for (Parameter param : parameters) {
            if (name.equals(param.getName())) {
                return param;
            }
        }
        return null;
    }

    /**
     * Get required parameters.
     */
    public List<Parameter> getRequiredParameters(List<Parameter> parameters) {
        List<Parameter> required = new ArrayList<>();
        if (parameters != null) {
            for (Parameter param : parameters) {
                if (param.isRequired()) {
                    required.add(param);
                }
            }
        }
        return required;
    }

    /**
     * Get optional parameters.
     */
    public List<Parameter> getOptionalParameters(List<Parameter> parameters) {
        List<Parameter> optional = new ArrayList<>();
        if (parameters != null) {
            for (Parameter param : parameters) {
                if (!param.isRequired()) {
                    optional.add(param);
                }
            }
        }
        return optional;
    }

    /**
     * Create parameter signature for comparison.
     */
    public String createParameterSignature(Parameter param) {
        if (param == null) {
            return "null";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(param.getName());
        sb.append(":");
        sb.append(param.getLocation() != null ? param.getLocation().name() : "UNKNOWN");
        sb.append(":");
        sb.append(param.isRequired() ? "required" : "optional");

        if (param.getType() != null) {
            sb.append(":").append(param.getType());
        }

        return sb.toString();
    }

    /**
     * Create parameters signature for comparison.
     */
    public String createParametersSignature(List<Parameter> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return "[]";
        }

        List<String> signatures = new ArrayList<>();
        for (Parameter param : parameters) {
            signatures.add(createParameterSignature(param));
        }
        Collections.sort(signatures);

        return "[" + String.join(",", signatures) + "]";
    }
}

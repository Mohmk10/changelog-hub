package io.github.mohmk10.changeloghub.parser.asyncapi.mapper;

import io.github.mohmk10.changeloghub.core.model.Parameter;
import io.github.mohmk10.changeloghub.core.model.ParameterLocation;
import io.github.mohmk10.changeloghub.parser.asyncapi.model.AsyncChannel;
import io.github.mohmk10.changeloghub.parser.asyncapi.model.AsyncMessage;
import io.github.mohmk10.changeloghub.parser.asyncapi.model.AsyncSchema;

import java.util.*;

public class AsyncApiParameterMapper {

    private final AsyncApiSchemaMapper schemaMapper;

    public AsyncApiParameterMapper() {
        this.schemaMapper = new AsyncApiSchemaMapper();
    }

    public AsyncApiParameterMapper(AsyncApiSchemaMapper schemaMapper) {
        this.schemaMapper = schemaMapper;
    }

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

    public Parameter mapChannelParameter(String name, AsyncChannel.ChannelParameter channelParam) {
        Parameter param = new Parameter();
        param.setName(name);
        param.setLocation(ParameterLocation.PATH);
        param.setRequired(true); 

        if (channelParam != null) {
            if (channelParam.getDescription() != null) {
                param.setDescription(channelParam.getDescription());
            }

            if (channelParam.getSchema() != null) {
                param.setType(schemaMapper.mapToType(channelParam.getSchema()));
            } else {
                
                param.setType("string");
            }
        } else {
            param.setType("string");
        }

        return param;
    }

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

    public List<Parameter> mapAllMessageParameters(AsyncMessage message) {
        List<Parameter> parameters = new ArrayList<>();

        parameters.addAll(mapMessagePayloadFields(message));

        parameters.addAll(mapMessageHeaders(message));

        return parameters;
    }

    public List<Parameter> mapEndpointParameters(AsyncChannel channel, AsyncMessage message) {
        List<Parameter> parameters = new ArrayList<>();

        parameters.addAll(mapChannelParameters(channel));

        if (message != null) {
            parameters.addAll(mapAllMessageParameters(message));
        }

        return parameters;
    }

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

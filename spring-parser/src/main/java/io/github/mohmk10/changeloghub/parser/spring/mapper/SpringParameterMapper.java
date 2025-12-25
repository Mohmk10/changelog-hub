package io.github.mohmk10.changeloghub.parser.spring.mapper;

import io.github.mohmk10.changeloghub.core.model.Parameter;
import io.github.mohmk10.changeloghub.core.model.ParameterLocation;
import io.github.mohmk10.changeloghub.parser.spring.extractor.TypeExtractor;
import io.github.mohmk10.changeloghub.parser.spring.model.SpringParameter;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps Spring parameter models to core Parameter models.
 */
public class SpringParameterMapper {

    private final TypeExtractor typeExtractor;

    public SpringParameterMapper() {
        this.typeExtractor = new TypeExtractor();
    }

    /**
     * Map a list of Spring parameters to core parameters.
     */
    public List<Parameter> mapParameters(List<SpringParameter> springParameters) {
        List<Parameter> parameters = new ArrayList<>();

        for (SpringParameter springParam : springParameters) {
            // Skip body parameters - they should be handled separately as request body
            if (springParam.getLocation() != SpringParameter.Location.BODY) {
                parameters.add(mapParameter(springParam));
            }
        }

        return parameters;
    }

    /**
     * Map a single Spring parameter to a core parameter.
     */
    public Parameter mapParameter(SpringParameter springParam) {
        Parameter parameter = new Parameter();

        parameter.setName(springParam.getName());
        parameter.setLocation(mapLocation(springParam.getLocation()));
        parameter.setRequired(springParam.isRequired());
        parameter.setType(typeExtractor.javaTypeToApiType(springParam.getJavaType()));

        if (springParam.getDefaultValue() != null) {
            parameter.setDefaultValue(springParam.getDefaultValue());
        }

        if (springParam.getDescription() != null) {
            parameter.setDescription(springParam.getDescription());
        }

        return parameter;
    }

    /**
     * Map Spring parameter location to core ParameterLocation.
     */
    public ParameterLocation mapLocation(SpringParameter.Location location) {
        if (location == null) {
            return ParameterLocation.QUERY;
        }

        switch (location) {
            case PATH:
                return ParameterLocation.PATH;
            case QUERY:
                return ParameterLocation.QUERY;
            case HEADER:
                return ParameterLocation.HEADER;
            case COOKIE:
                return ParameterLocation.COOKIE;
            case BODY:
                // BODY parameters are handled separately as RequestBody
                // This should not happen as BODY params are filtered out
                return ParameterLocation.QUERY;
            default:
                return ParameterLocation.QUERY;
        }
    }

    /**
     * Check if there's a request body in the parameters.
     */
    public boolean hasRequestBody(List<SpringParameter> springParameters) {
        return springParameters.stream()
                .anyMatch(p -> p.getLocation() == SpringParameter.Location.BODY);
    }

    /**
     * Get the request body parameter if present.
     */
    public SpringParameter getRequestBody(List<SpringParameter> springParameters) {
        return springParameters.stream()
                .filter(p -> p.getLocation() == SpringParameter.Location.BODY)
                .findFirst()
                .orElse(null);
    }
}

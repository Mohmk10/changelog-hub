package io.github.mohmk10.changeloghub.parser.spring.mapper;

import io.github.mohmk10.changeloghub.core.model.Parameter;
import io.github.mohmk10.changeloghub.core.model.ParameterLocation;
import io.github.mohmk10.changeloghub.parser.spring.extractor.TypeExtractor;
import io.github.mohmk10.changeloghub.parser.spring.model.SpringParameter;

import java.util.ArrayList;
import java.util.List;

public class SpringParameterMapper {

    private final TypeExtractor typeExtractor;

    public SpringParameterMapper() {
        this.typeExtractor = new TypeExtractor();
    }

    public List<Parameter> mapParameters(List<SpringParameter> springParameters) {
        List<Parameter> parameters = new ArrayList<>();

        for (SpringParameter springParam : springParameters) {
            
            if (springParam.getLocation() != SpringParameter.Location.BODY) {
                parameters.add(mapParameter(springParam));
            }
        }

        return parameters;
    }

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

                return ParameterLocation.QUERY;
            default:
                return ParameterLocation.QUERY;
        }
    }

    public boolean hasRequestBody(List<SpringParameter> springParameters) {
        return springParameters.stream()
                .anyMatch(p -> p.getLocation() == SpringParameter.Location.BODY);
    }

    public SpringParameter getRequestBody(List<SpringParameter> springParameters) {
        return springParameters.stream()
                .filter(p -> p.getLocation() == SpringParameter.Location.BODY)
                .findFirst()
                .orElse(null);
    }
}

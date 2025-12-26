package io.github.mohmk10.changeloghub.parser.graphql.mapper;

import io.github.mohmk10.changeloghub.core.model.Parameter;
import io.github.mohmk10.changeloghub.core.model.ParameterLocation;
import io.github.mohmk10.changeloghub.parser.graphql.model.GraphQLArgument;
import io.github.mohmk10.changeloghub.parser.graphql.model.GraphQLOperation;

import java.util.ArrayList;
import java.util.List;

public class GraphQLParameterMapper {

    private final GraphQLTypeMapper typeMapper = new GraphQLTypeMapper();

    public List<Parameter> mapArguments(List<GraphQLArgument> arguments, GraphQLOperation.OperationType opType) {
        List<Parameter> parameters = new ArrayList<>();
        for (GraphQLArgument arg : arguments) {
            parameters.add(mapArgument(arg, opType));
        }
        return parameters;
    }

    public Parameter mapArgument(GraphQLArgument argument, GraphQLOperation.OperationType opType) {
        Parameter param = new Parameter();
        param.setName(argument.getName());
        param.setRequired(argument.isRequired());
        param.setDescription(argument.getDescription());
        param.setType(typeMapper.toApiType(argument.getType()));

        if (opType == GraphQLOperation.OperationType.QUERY) {
            param.setLocation(ParameterLocation.QUERY);
        } else {
            
            param.setLocation(ParameterLocation.QUERY);
        }

        if (argument.hasDefaultValue()) {
            param.setDefaultValue(argument.getDefaultValue());
        }

        return param;
    }

    public List<Parameter> mapQueryArguments(List<GraphQLArgument> arguments) {
        return mapArguments(arguments, GraphQLOperation.OperationType.QUERY);
    }

    public List<Parameter> mapMutationArguments(List<GraphQLArgument> arguments) {
        return mapArguments(arguments, GraphQLOperation.OperationType.MUTATION);
    }
}

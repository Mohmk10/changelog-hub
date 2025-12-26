package io.github.mohmk10.changeloghub.parser.graphql.mapper;

import io.github.mohmk10.changeloghub.core.model.Endpoint;
import io.github.mohmk10.changeloghub.core.model.HttpMethod;
import io.github.mohmk10.changeloghub.core.model.Response;
import io.github.mohmk10.changeloghub.parser.graphql.model.GraphQLOperation;
import io.github.mohmk10.changeloghub.parser.graphql.util.GraphQLConstants;

import java.util.ArrayList;
import java.util.List;

public class GraphQLEndpointMapper {

    private final GraphQLParameterMapper parameterMapper = new GraphQLParameterMapper();
    private final GraphQLTypeMapper typeMapper = new GraphQLTypeMapper();

    public List<Endpoint> mapOperations(List<GraphQLOperation> operations) {
        List<Endpoint> endpoints = new ArrayList<>();
        for (GraphQLOperation op : operations) {
            endpoints.add(mapOperation(op));
        }
        return endpoints;
    }

    public Endpoint mapOperation(GraphQLOperation operation) {
        Endpoint endpoint = new Endpoint();

        endpoint.setOperationId(operation.getName());

        endpoint.setPath(buildPath(operation));

        endpoint.setMethod(mapHttpMethod(operation.getOperationType()));

        endpoint.setDescription(operation.getDescription());

        endpoint.setDeprecated(operation.isDeprecated());

        endpoint.setParameters(parameterMapper.mapArguments(
                operation.getArguments(),
                operation.getOperationType()));

        Response response = new Response();
        response.setStatusCode("200");
        response.setDescription("Successful response");
        response.setSchemaRef(operation.getReturnType());
        endpoint.addResponse(response);

        endpoint.addTag(operation.getOperationType().name().toLowerCase());
        endpoint.addTag("graphql");

        return endpoint;
    }

    private String buildPath(GraphQLOperation operation) {
        StringBuilder path = new StringBuilder(GraphQLConstants.GRAPHQL_ENDPOINT);

        if (operation.isQuery()) {
            path.append("/").append(operation.getName());
        } else if (operation.isMutation()) {
            path.append("/mutation/").append(operation.getName());
        } else if (operation.isSubscription()) {
            path.append("/subscription/").append(operation.getName());
        }

        return path.toString();
    }

    private HttpMethod mapHttpMethod(GraphQLOperation.OperationType operationType) {
        return switch (operationType) {
            case QUERY -> HttpMethod.GET;
            case MUTATION -> HttpMethod.POST;
            case SUBSCRIPTION -> HttpMethod.GET; 
        };
    }

    public List<Endpoint> mapQueries(List<GraphQLOperation> queries) {
        return mapOperations(queries);
    }

    public List<Endpoint> mapMutations(List<GraphQLOperation> mutations) {
        return mapOperations(mutations);
    }

    public List<Endpoint> mapSubscriptions(List<GraphQLOperation> subscriptions) {
        return mapOperations(subscriptions);
    }
}

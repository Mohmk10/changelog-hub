package io.github.mohmk10.changeloghub.parser.graphql.mapper;

import io.github.mohmk10.changeloghub.core.model.Endpoint;
import io.github.mohmk10.changeloghub.core.model.HttpMethod;
import io.github.mohmk10.changeloghub.core.model.Response;
import io.github.mohmk10.changeloghub.parser.graphql.model.GraphQLOperation;
import io.github.mohmk10.changeloghub.parser.graphql.util.GraphQLConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps GraphQL operations to core Endpoint models.
 */
public class GraphQLEndpointMapper {

    private final GraphQLParameterMapper parameterMapper = new GraphQLParameterMapper();
    private final GraphQLTypeMapper typeMapper = new GraphQLTypeMapper();

    /**
     * Maps a list of GraphQL operations to core Endpoints.
     */
    public List<Endpoint> mapOperations(List<GraphQLOperation> operations) {
        List<Endpoint> endpoints = new ArrayList<>();
        for (GraphQLOperation op : operations) {
            endpoints.add(mapOperation(op));
        }
        return endpoints;
    }

    /**
     * Maps a single GraphQL operation to a core Endpoint.
     */
    public Endpoint mapOperation(GraphQLOperation operation) {
        Endpoint endpoint = new Endpoint();

        // Set operation ID
        endpoint.setOperationId(operation.getName());

        // Set path based on operation type
        endpoint.setPath(buildPath(operation));

        // Set HTTP method based on operation type
        endpoint.setMethod(mapHttpMethod(operation.getOperationType()));

        // Set description
        endpoint.setDescription(operation.getDescription());

        // Set deprecated
        endpoint.setDeprecated(operation.isDeprecated());

        // Map parameters (arguments)
        endpoint.setParameters(parameterMapper.mapArguments(
                operation.getArguments(),
                operation.getOperationType()));

        // Set response
        Response response = new Response();
        response.setStatusCode("200");
        response.setDescription("Successful response");
        response.setSchemaRef(operation.getReturnType());
        endpoint.addResponse(response);

        // Add tags
        endpoint.addTag(operation.getOperationType().name().toLowerCase());
        endpoint.addTag("graphql");

        return endpoint;
    }

    /**
     * Builds the path for a GraphQL operation.
     */
    private String buildPath(GraphQLOperation operation) {
        StringBuilder path = new StringBuilder(GraphQLConstants.GRAPHQL_ENDPOINT);

        // For queries, include the operation name in the path for clarity
        if (operation.isQuery()) {
            path.append("/").append(operation.getName());
        } else if (operation.isMutation()) {
            path.append("/mutation/").append(operation.getName());
        } else if (operation.isSubscription()) {
            path.append("/subscription/").append(operation.getName());
        }

        return path.toString();
    }

    /**
     * Maps GraphQL operation type to HTTP method.
     */
    private HttpMethod mapHttpMethod(GraphQLOperation.OperationType operationType) {
        return switch (operationType) {
            case QUERY -> HttpMethod.GET;
            case MUTATION -> HttpMethod.POST;
            case SUBSCRIPTION -> HttpMethod.GET; // WebSocket but represented as GET
        };
    }

    /**
     * Maps queries to endpoints.
     */
    public List<Endpoint> mapQueries(List<GraphQLOperation> queries) {
        return mapOperations(queries);
    }

    /**
     * Maps mutations to endpoints.
     */
    public List<Endpoint> mapMutations(List<GraphQLOperation> mutations) {
        return mapOperations(mutations);
    }

    /**
     * Maps subscriptions to endpoints.
     */
    public List<Endpoint> mapSubscriptions(List<GraphQLOperation> subscriptions) {
        return mapOperations(subscriptions);
    }
}

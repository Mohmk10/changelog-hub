package io.github.mohmk10.changeloghub.parser.graphql.mapper;

import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.core.model.ApiType;
import io.github.mohmk10.changeloghub.core.model.Endpoint;
import io.github.mohmk10.changeloghub.parser.graphql.model.GraphQLSchema;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GraphQLModelMapper {

    private final GraphQLEndpointMapper endpointMapper = new GraphQLEndpointMapper();

    public ApiSpec mapToApiSpec(GraphQLSchema schema) {
        return mapToApiSpec(schema, schema.getName(), schema.getVersion());
    }

    public ApiSpec mapToApiSpec(GraphQLSchema schema, String name, String version) {
        ApiSpec apiSpec = new ApiSpec();

        apiSpec.setName(name != null ? name : "GraphQL API");
        apiSpec.setVersion(version != null ? version : "1.0.0");
        apiSpec.setType(ApiType.GRAPHQL);
        apiSpec.setParsedAt(LocalDateTime.now());

        List<Endpoint> endpoints = new ArrayList<>();
        endpoints.addAll(endpointMapper.mapQueries(schema.getQueries()));
        endpoints.addAll(endpointMapper.mapMutations(schema.getMutations()));
        endpoints.addAll(endpointMapper.mapSubscriptions(schema.getSubscriptions()));
        apiSpec.setEndpoints(endpoints);

        apiSpec.addMetadata("queryCount", schema.getQueries().size());
        apiSpec.addMetadata("mutationCount", schema.getMutations().size());
        apiSpec.addMetadata("subscriptionCount", schema.getSubscriptions().size());
        apiSpec.addMetadata("typeCount", schema.getTypeCount());
        apiSpec.addMetadata("schemaDescription", schema.getDescription());

        return apiSpec;
    }

    public ApiSpec createEmptyApiSpec(String name, String version) {
        ApiSpec apiSpec = new ApiSpec();
        apiSpec.setName(name != null ? name : "GraphQL API");
        apiSpec.setVersion(version != null ? version : "1.0.0");
        apiSpec.setType(ApiType.GRAPHQL);
        apiSpec.setParsedAt(LocalDateTime.now());
        apiSpec.setEndpoints(new ArrayList<>());
        return apiSpec;
    }
}

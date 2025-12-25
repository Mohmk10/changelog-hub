package io.github.mohmk10.changeloghub.parser.openapi.mapper;

import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.core.model.ApiType;
import io.github.mohmk10.changeloghub.core.model.Endpoint;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;

import java.util.List;
import java.util.Map;

public class OpenApiModelMapper {

    private final EndpointMapper endpointMapper;

    public OpenApiModelMapper() {
        this.endpointMapper = new EndpointMapper();
    }

    public OpenApiModelMapper(EndpointMapper endpointMapper) {
        this.endpointMapper = endpointMapper;
    }

    public ApiSpec map(OpenAPI openApi) {
        if (openApi == null) {
            return null;
        }

        ApiSpec.Builder builder = ApiSpec.builder()
                .type(ApiType.REST);

        mapInfo(builder, openApi.getInfo());
        mapPaths(builder, openApi.getPaths());

        return builder.build();
    }

    private void mapInfo(ApiSpec.Builder builder, Info info) {
        if (info == null) {
            builder.name("Unknown API");
            builder.version("0.0.0");
            return;
        }

        builder.name(info.getTitle() != null ? info.getTitle() : "Unknown API");
        builder.version(info.getVersion() != null ? info.getVersion() : "0.0.0");
        if (info.getDescription() != null) {
            builder.addMetadata("description", info.getDescription());
        }
    }

    private void mapPaths(ApiSpec.Builder builder, Paths paths) {
        if (paths == null || paths.isEmpty()) {
            return;
        }

        for (Map.Entry<String, PathItem> entry : paths.entrySet()) {
            String path = entry.getKey();
            PathItem pathItem = entry.getValue();

            List<Endpoint> endpoints = endpointMapper.map(path, pathItem);
            for (Endpoint endpoint : endpoints) {
                builder.addEndpoint(endpoint);
            }
        }
    }
}

package io.github.mohmk10.changeloghub.parser.spring.mapper;

import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.core.model.ApiType;
import io.github.mohmk10.changeloghub.core.model.Endpoint;
import io.github.mohmk10.changeloghub.parser.spring.model.SpringController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps Spring controller models to core ApiSpec model.
 */
public class SpringModelMapper {

    private final SpringEndpointMapper endpointMapper;

    public SpringModelMapper() {
        this.endpointMapper = new SpringEndpointMapper();
    }

    /**
     * Map a list of Spring controllers to an ApiSpec.
     */
    public ApiSpec mapToApiSpec(List<SpringController> controllers, String apiName, String apiVersion) {
        ApiSpec.Builder builder = ApiSpec.builder();

        builder.name(apiName != null ? apiName : "Spring Boot API");
        builder.version(apiVersion != null ? apiVersion : "1.0.0");
        builder.type(ApiType.REST);
        builder.parsedAt(LocalDateTime.now());

        // Map all endpoints from all controllers
        List<Endpoint> allEndpoints = new ArrayList<>();
        for (SpringController controller : controllers) {
            List<Endpoint> endpoints = endpointMapper.mapEndpoints(controller);
            allEndpoints.addAll(endpoints);
        }
        builder.endpoints(allEndpoints);

        // Add metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "spring-parser");
        metadata.put("controllerCount", controllers.size());
        metadata.put("endpointCount", allEndpoints.size());
        builder.metadata(metadata);

        return builder.build();
    }

    /**
     * Map a single Spring controller to an ApiSpec.
     */
    public ApiSpec mapToApiSpec(SpringController controller) {
        return mapToApiSpec(List.of(controller), controller.getClassName(), "1.0.0");
    }

    /**
     * Map a single Spring controller to endpoints.
     */
    public List<Endpoint> mapToEndpoints(SpringController controller) {
        return endpointMapper.mapEndpoints(controller);
    }
}

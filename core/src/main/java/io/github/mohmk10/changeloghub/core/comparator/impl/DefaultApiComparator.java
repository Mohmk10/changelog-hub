package io.github.mohmk10.changeloghub.core.comparator.impl;

import io.github.mohmk10.changeloghub.core.comparator.ApiComparator;
import io.github.mohmk10.changeloghub.core.comparator.EndpointComparator;
import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.core.model.Change;
import io.github.mohmk10.changeloghub.core.model.ChangeCategory;
import io.github.mohmk10.changeloghub.core.model.ChangeType;
import io.github.mohmk10.changeloghub.core.model.Changelog;
import io.github.mohmk10.changeloghub.core.model.Endpoint;
import io.github.mohmk10.changeloghub.core.model.Severity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DefaultApiComparator implements ApiComparator {

    private final EndpointComparator endpointComparator;

    public DefaultApiComparator() {
        this.endpointComparator = new DefaultEndpointComparator();
    }

    public DefaultApiComparator(EndpointComparator endpointComparator) {
        this.endpointComparator = endpointComparator;
    }

    @Override
    public Changelog compare(ApiSpec oldSpec, ApiSpec newSpec) {
        List<Change> allChanges = new ArrayList<>();

        Map<String, Endpoint> oldEndpointMap = toEndpointMap(oldSpec);
        Map<String, Endpoint> newEndpointMap = toEndpointMap(newSpec);

        allChanges.addAll(detectAddedEndpoints(oldEndpointMap, newEndpointMap));
        allChanges.addAll(detectRemovedEndpoints(oldEndpointMap, newEndpointMap));
        allChanges.addAll(detectModifiedEndpoints(oldEndpointMap, newEndpointMap));

        return Changelog.builder()
                .apiName(newSpec != null ? newSpec.getName() : (oldSpec != null ? oldSpec.getName() : "Unknown"))
                .fromVersion(oldSpec != null ? oldSpec.getVersion() : null)
                .toVersion(newSpec != null ? newSpec.getVersion() : null)
                .changes(allChanges)
                .build();
    }

    private Map<String, Endpoint> toEndpointMap(ApiSpec spec) {
        if (spec == null || spec.getEndpoints() == null) {
            return Map.of();
        }
        return spec.getEndpoints().stream()
                .collect(Collectors.toMap(
                        this::getEndpointKey,
                        Function.identity(),
                        (a, b) -> a
                ));
    }

    private String getEndpointKey(Endpoint endpoint) {
        return endpoint.getMethod() + ":" + endpoint.getPath();
    }

    private List<Change> detectAddedEndpoints(Map<String, Endpoint> oldMap, Map<String, Endpoint> newMap) {
        List<Change> changes = new ArrayList<>();

        for (Map.Entry<String, Endpoint> entry : newMap.entrySet()) {
            if (!oldMap.containsKey(entry.getKey())) {
                Endpoint endpoint = entry.getValue();
                changes.add(Change.builder()
                        .type(ChangeType.ADDED)
                        .category(ChangeCategory.ENDPOINT)
                        .severity(Severity.INFO)
                        .path(endpoint.getPath())
                        .description("New endpoint added: " + endpoint.getMethod() + " " + endpoint.getPath())
                        .oldValue(null)
                        .newValue(endpoint)
                        .build());
            }
        }

        return changes;
    }

    private List<Change> detectRemovedEndpoints(Map<String, Endpoint> oldMap, Map<String, Endpoint> newMap) {
        List<Change> changes = new ArrayList<>();

        for (Map.Entry<String, Endpoint> entry : oldMap.entrySet()) {
            if (!newMap.containsKey(entry.getKey())) {
                Endpoint endpoint = entry.getValue();
                changes.add(Change.builder()
                        .type(ChangeType.REMOVED)
                        .category(ChangeCategory.ENDPOINT)
                        .severity(Severity.BREAKING)
                        .path(endpoint.getPath())
                        .description("Endpoint removed: " + endpoint.getMethod() + " " + endpoint.getPath())
                        .oldValue(endpoint)
                        .newValue(null)
                        .build());
            }
        }

        return changes;
    }

    private List<Change> detectModifiedEndpoints(Map<String, Endpoint> oldMap, Map<String, Endpoint> newMap) {
        List<Change> changes = new ArrayList<>();

        for (Map.Entry<String, Endpoint> entry : newMap.entrySet()) {
            String key = entry.getKey();
            if (oldMap.containsKey(key)) {
                Endpoint oldEndpoint = oldMap.get(key);
                Endpoint newEndpoint = entry.getValue();
                List<Change> endpointChanges = endpointComparator.compare(oldEndpoint, newEndpoint);
                changes.addAll(endpointChanges);
            }
        }

        return changes;
    }
}

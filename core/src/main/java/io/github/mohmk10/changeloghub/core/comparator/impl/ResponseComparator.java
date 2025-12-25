package io.github.mohmk10.changeloghub.core.comparator.impl;

import io.github.mohmk10.changeloghub.core.model.Change;
import io.github.mohmk10.changeloghub.core.model.ChangeCategory;
import io.github.mohmk10.changeloghub.core.model.ChangeType;
import io.github.mohmk10.changeloghub.core.model.Response;
import io.github.mohmk10.changeloghub.core.model.Severity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ResponseComparator {

    public List<Change> compare(List<Response> oldResponses, List<Response> newResponses) {
        List<Change> changes = new ArrayList<>();

        Map<String, Response> oldResponseMap = toMap(oldResponses);
        Map<String, Response> newResponseMap = toMap(newResponses);

        for (Response newResponse : newResponses) {
            Response oldResponse = oldResponseMap.get(newResponse.getStatusCode());
            if (oldResponse == null) {
                changes.add(createAddedResponseChange(newResponse));
            } else {
                changes.addAll(compareResponses(oldResponse, newResponse));
            }
        }

        for (Response oldResponse : oldResponses) {
            if (!newResponseMap.containsKey(oldResponse.getStatusCode())) {
                changes.add(createRemovedResponseChange(oldResponse));
            }
        }

        return changes;
    }

    private Map<String, Response> toMap(List<Response> responses) {
        if (responses == null) {
            return Map.of();
        }
        return responses.stream()
                .collect(Collectors.toMap(Response::getStatusCode, Function.identity(), (a, b) -> a));
    }

    private Change createAddedResponseChange(Response response) {
        return Change.builder()
                .type(ChangeType.ADDED)
                .category(ChangeCategory.RESPONSE)
                .severity(Severity.INFO)
                .path("response:" + response.getStatusCode())
                .description("Response '" + response.getStatusCode() + "' added")
                .oldValue(null)
                .newValue(response)
                .build();
    }

    private Change createRemovedResponseChange(Response response) {
        return Change.builder()
                .type(ChangeType.REMOVED)
                .category(ChangeCategory.RESPONSE)
                .severity(Severity.DANGEROUS)
                .path("response:" + response.getStatusCode())
                .description("Response '" + response.getStatusCode() + "' removed")
                .oldValue(response)
                .newValue(null)
                .build();
    }

    private List<Change> compareResponses(Response oldResponse, Response newResponse) {
        List<Change> changes = new ArrayList<>();

        if (!Objects.equals(oldResponse.getSchemaRef(), newResponse.getSchemaRef())) {
            changes.add(Change.builder()
                    .type(ChangeType.MODIFIED)
                    .category(ChangeCategory.RESPONSE)
                    .severity(Severity.DANGEROUS)
                    .path("response:" + newResponse.getStatusCode() + ".schema")
                    .description("Response '" + newResponse.getStatusCode() + "' schema changed")
                    .oldValue(oldResponse.getSchemaRef())
                    .newValue(newResponse.getSchemaRef())
                    .build());
        }

        if (!Objects.equals(oldResponse.getContentType(), newResponse.getContentType())) {
            changes.add(Change.builder()
                    .type(ChangeType.MODIFIED)
                    .category(ChangeCategory.RESPONSE)
                    .severity(Severity.WARNING)
                    .path("response:" + newResponse.getStatusCode() + ".contentType")
                    .description("Response '" + newResponse.getStatusCode() + "' content type changed from '"
                            + oldResponse.getContentType() + "' to '" + newResponse.getContentType() + "'")
                    .oldValue(oldResponse.getContentType())
                    .newValue(newResponse.getContentType())
                    .build());
        }

        return changes;
    }
}

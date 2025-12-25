package io.github.mohmk10.changeloghub.core.comparator.impl;

import io.github.mohmk10.changeloghub.core.comparator.EndpointComparator;
import io.github.mohmk10.changeloghub.core.model.Change;
import io.github.mohmk10.changeloghub.core.model.ChangeCategory;
import io.github.mohmk10.changeloghub.core.model.ChangeType;
import io.github.mohmk10.changeloghub.core.model.Endpoint;
import io.github.mohmk10.changeloghub.core.model.RequestBody;
import io.github.mohmk10.changeloghub.core.model.Severity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DefaultEndpointComparator implements EndpointComparator {

    private final ParameterComparator parameterComparator;
    private final ResponseComparator responseComparator;

    public DefaultEndpointComparator() {
        this.parameterComparator = new ParameterComparator();
        this.responseComparator = new ResponseComparator();
    }

    public DefaultEndpointComparator(ParameterComparator parameterComparator,
                                      ResponseComparator responseComparator) {
        this.parameterComparator = parameterComparator;
        this.responseComparator = responseComparator;
    }

    @Override
    public List<Change> compare(Endpoint oldEndpoint, Endpoint newEndpoint) {
        List<Change> changes = new ArrayList<>();

        if (oldEndpoint == null || newEndpoint == null) {
            return changes;
        }

        changes.addAll(comparePath(oldEndpoint, newEndpoint));
        changes.addAll(compareMethod(oldEndpoint, newEndpoint));
        changes.addAll(compareDeprecation(oldEndpoint, newEndpoint));
        changes.addAll(compareParameters(oldEndpoint, newEndpoint));
        changes.addAll(compareRequestBody(oldEndpoint, newEndpoint));
        changes.addAll(compareResponses(oldEndpoint, newEndpoint));

        return changes;
    }

    private List<Change> comparePath(Endpoint oldEndpoint, Endpoint newEndpoint) {
        List<Change> changes = new ArrayList<>();

        if (!Objects.equals(oldEndpoint.getPath(), newEndpoint.getPath())) {
            changes.add(Change.builder()
                    .type(ChangeType.MODIFIED)
                    .category(ChangeCategory.ENDPOINT)
                    .severity(Severity.BREAKING)
                    .path(newEndpoint.getPath())
                    .description("Endpoint path changed from '" + oldEndpoint.getPath()
                            + "' to '" + newEndpoint.getPath() + "'")
                    .oldValue(oldEndpoint.getPath())
                    .newValue(newEndpoint.getPath())
                    .build());
        }

        return changes;
    }

    private List<Change> compareMethod(Endpoint oldEndpoint, Endpoint newEndpoint) {
        List<Change> changes = new ArrayList<>();

        if (oldEndpoint.getMethod() != newEndpoint.getMethod()) {
            changes.add(Change.builder()
                    .type(ChangeType.MODIFIED)
                    .category(ChangeCategory.ENDPOINT)
                    .severity(Severity.BREAKING)
                    .path(newEndpoint.getPath())
                    .description("HTTP method changed from " + oldEndpoint.getMethod()
                            + " to " + newEndpoint.getMethod())
                    .oldValue(oldEndpoint.getMethod())
                    .newValue(newEndpoint.getMethod())
                    .build());
        }

        return changes;
    }

    private List<Change> compareDeprecation(Endpoint oldEndpoint, Endpoint newEndpoint) {
        List<Change> changes = new ArrayList<>();

        if (!oldEndpoint.isDeprecated() && newEndpoint.isDeprecated()) {
            changes.add(Change.builder()
                    .type(ChangeType.DEPRECATED)
                    .category(ChangeCategory.ENDPOINT)
                    .severity(Severity.WARNING)
                    .path(newEndpoint.getPath())
                    .description("Endpoint '" + newEndpoint.getPath() + "' marked as deprecated")
                    .oldValue(false)
                    .newValue(true)
                    .build());
        } else if (oldEndpoint.isDeprecated() && !newEndpoint.isDeprecated()) {
            changes.add(Change.builder()
                    .type(ChangeType.MODIFIED)
                    .category(ChangeCategory.ENDPOINT)
                    .severity(Severity.INFO)
                    .path(newEndpoint.getPath())
                    .description("Endpoint '" + newEndpoint.getPath() + "' deprecation removed")
                    .oldValue(true)
                    .newValue(false)
                    .build());
        }

        return changes;
    }

    private List<Change> compareParameters(Endpoint oldEndpoint, Endpoint newEndpoint) {
        return parameterComparator.compare(
                oldEndpoint.getParameters(),
                newEndpoint.getParameters()
        );
    }

    private List<Change> compareRequestBody(Endpoint oldEndpoint, Endpoint newEndpoint) {
        List<Change> changes = new ArrayList<>();
        RequestBody oldBody = oldEndpoint.getRequestBody();
        RequestBody newBody = newEndpoint.getRequestBody();

        if (oldBody == null && newBody != null) {
            Severity severity = newBody.isRequired() ? Severity.BREAKING : Severity.INFO;
            changes.add(Change.builder()
                    .type(ChangeType.ADDED)
                    .category(ChangeCategory.REQUEST_BODY)
                    .severity(severity)
                    .path(newEndpoint.getPath() + ".requestBody")
                    .description(newBody.isRequired()
                            ? "Required request body added"
                            : "Optional request body added")
                    .oldValue(null)
                    .newValue(newBody)
                    .build());
        } else if (oldBody != null && newBody == null) {
            changes.add(Change.builder()
                    .type(ChangeType.REMOVED)
                    .category(ChangeCategory.REQUEST_BODY)
                    .severity(Severity.DANGEROUS)
                    .path(oldEndpoint.getPath() + ".requestBody")
                    .description("Request body removed")
                    .oldValue(oldBody)
                    .newValue(null)
                    .build());
        } else if (oldBody != null && newBody != null) {
            changes.addAll(compareRequestBodies(oldBody, newBody, newEndpoint.getPath()));
        }

        return changes;
    }

    private List<Change> compareRequestBodies(RequestBody oldBody, RequestBody newBody, String path) {
        List<Change> changes = new ArrayList<>();

        if (!oldBody.isRequired() && newBody.isRequired()) {
            changes.add(Change.builder()
                    .type(ChangeType.MODIFIED)
                    .category(ChangeCategory.REQUEST_BODY)
                    .severity(Severity.BREAKING)
                    .path(path + ".requestBody.required")
                    .description("Request body changed from optional to required")
                    .oldValue(false)
                    .newValue(true)
                    .build());
        }

        if (!Objects.equals(oldBody.getSchemaRef(), newBody.getSchemaRef())) {
            changes.add(Change.builder()
                    .type(ChangeType.MODIFIED)
                    .category(ChangeCategory.REQUEST_BODY)
                    .severity(Severity.DANGEROUS)
                    .path(path + ".requestBody.schema")
                    .description("Request body schema changed")
                    .oldValue(oldBody.getSchemaRef())
                    .newValue(newBody.getSchemaRef())
                    .build());
        }

        if (!Objects.equals(oldBody.getContentType(), newBody.getContentType())) {
            changes.add(Change.builder()
                    .type(ChangeType.MODIFIED)
                    .category(ChangeCategory.REQUEST_BODY)
                    .severity(Severity.WARNING)
                    .path(path + ".requestBody.contentType")
                    .description("Request body content type changed from '"
                            + oldBody.getContentType() + "' to '" + newBody.getContentType() + "'")
                    .oldValue(oldBody.getContentType())
                    .newValue(newBody.getContentType())
                    .build());
        }

        return changes;
    }

    private List<Change> compareResponses(Endpoint oldEndpoint, Endpoint newEndpoint) {
        return responseComparator.compare(
                oldEndpoint.getResponses(),
                newEndpoint.getResponses()
        );
    }
}

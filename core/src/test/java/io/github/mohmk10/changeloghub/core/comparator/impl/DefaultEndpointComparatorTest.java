package io.github.mohmk10.changeloghub.core.comparator.impl;

import io.github.mohmk10.changeloghub.core.model.Change;
import io.github.mohmk10.changeloghub.core.model.ChangeCategory;
import io.github.mohmk10.changeloghub.core.model.ChangeType;
import io.github.mohmk10.changeloghub.core.model.Endpoint;
import io.github.mohmk10.changeloghub.core.model.HttpMethod;
import io.github.mohmk10.changeloghub.core.model.Parameter;
import io.github.mohmk10.changeloghub.core.model.ParameterLocation;
import io.github.mohmk10.changeloghub.core.model.RequestBody;
import io.github.mohmk10.changeloghub.core.model.Response;
import io.github.mohmk10.changeloghub.core.model.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultEndpointComparatorTest {

    private DefaultEndpointComparator comparator;

    @BeforeEach
    void setUp() {
        comparator = new DefaultEndpointComparator();
    }

    @Test
    void testCompareIdenticalEndpoints() {
        Endpoint endpoint = Endpoint.builder()
                .path("/api/users")
                .method(HttpMethod.GET)
                .build();

        List<Change> changes = comparator.compare(endpoint, endpoint);

        assertThat(changes).isEmpty();
    }

    @Test
    void testComparePathChanged() {
        Endpoint oldEndpoint = Endpoint.builder()
                .path("/api/users")
                .method(HttpMethod.GET)
                .build();

        Endpoint newEndpoint = Endpoint.builder()
                .path("/api/v2/users")
                .method(HttpMethod.GET)
                .build();

        List<Change> changes = comparator.compare(oldEndpoint, newEndpoint);

        assertThat(changes).hasSize(1);
        assertThat(changes.get(0).getType()).isEqualTo(ChangeType.MODIFIED);
        assertThat(changes.get(0).getCategory()).isEqualTo(ChangeCategory.ENDPOINT);
        assertThat(changes.get(0).getSeverity()).isEqualTo(Severity.BREAKING);
    }

    @Test
    void testCompareMethodChanged() {
        Endpoint oldEndpoint = Endpoint.builder()
                .path("/api/users")
                .method(HttpMethod.GET)
                .build();

        Endpoint newEndpoint = Endpoint.builder()
                .path("/api/users")
                .method(HttpMethod.POST)
                .build();

        List<Change> changes = comparator.compare(oldEndpoint, newEndpoint);

        assertThat(changes).hasSize(1);
        assertThat(changes.get(0).getSeverity()).isEqualTo(Severity.BREAKING);
        assertThat(changes.get(0).getDescription()).contains("HTTP method changed");
    }

    @Test
    void testCompareParameterAdded() {
        Endpoint oldEndpoint = Endpoint.builder()
                .path("/api/users")
                .method(HttpMethod.GET)
                .build();

        Parameter newParam = new Parameter("filter", ParameterLocation.QUERY, "string", true, null, "Filter");
        Endpoint newEndpoint = Endpoint.builder()
                .path("/api/users")
                .method(HttpMethod.GET)
                .addParameter(newParam)
                .build();

        List<Change> changes = comparator.compare(oldEndpoint, newEndpoint);

        assertThat(changes).hasSize(1);
        assertThat(changes.get(0).getType()).isEqualTo(ChangeType.ADDED);
        assertThat(changes.get(0).getCategory()).isEqualTo(ChangeCategory.PARAMETER);
    }

    @Test
    void testCompareParameterRemoved() {
        Parameter param = new Parameter("filter", ParameterLocation.QUERY, "string", false, null, "Filter");
        Endpoint oldEndpoint = Endpoint.builder()
                .path("/api/users")
                .method(HttpMethod.GET)
                .addParameter(param)
                .build();

        Endpoint newEndpoint = Endpoint.builder()
                .path("/api/users")
                .method(HttpMethod.GET)
                .build();

        List<Change> changes = comparator.compare(oldEndpoint, newEndpoint);

        assertThat(changes).hasSize(1);
        assertThat(changes.get(0).getType()).isEqualTo(ChangeType.REMOVED);
        assertThat(changes.get(0).getCategory()).isEqualTo(ChangeCategory.PARAMETER);
    }

    @Test
    void testCompareDeprecationAdded() {
        Endpoint oldEndpoint = Endpoint.builder()
                .path("/api/users")
                .method(HttpMethod.GET)
                .deprecated(false)
                .build();

        Endpoint newEndpoint = Endpoint.builder()
                .path("/api/users")
                .method(HttpMethod.GET)
                .deprecated(true)
                .build();

        List<Change> changes = comparator.compare(oldEndpoint, newEndpoint);

        assertThat(changes).hasSize(1);
        assertThat(changes.get(0).getType()).isEqualTo(ChangeType.DEPRECATED);
        assertThat(changes.get(0).getSeverity()).isEqualTo(Severity.WARNING);
    }

    @Test
    void testCompareDeprecationRemoved() {
        Endpoint oldEndpoint = Endpoint.builder()
                .path("/api/users")
                .method(HttpMethod.GET)
                .deprecated(true)
                .build();

        Endpoint newEndpoint = Endpoint.builder()
                .path("/api/users")
                .method(HttpMethod.GET)
                .deprecated(false)
                .build();

        List<Change> changes = comparator.compare(oldEndpoint, newEndpoint);

        assertThat(changes).hasSize(1);
        assertThat(changes.get(0).getType()).isEqualTo(ChangeType.MODIFIED);
        assertThat(changes.get(0).getSeverity()).isEqualTo(Severity.INFO);
    }

    @Test
    void testCompareRequestBodyAdded() {
        Endpoint oldEndpoint = Endpoint.builder()
                .path("/api/users")
                .method(HttpMethod.POST)
                .build();

        RequestBody body = new RequestBody("application/json", "#/components/schemas/User", true);
        Endpoint newEndpoint = Endpoint.builder()
                .path("/api/users")
                .method(HttpMethod.POST)
                .requestBody(body)
                .build();

        List<Change> changes = comparator.compare(oldEndpoint, newEndpoint);

        assertThat(changes).hasSize(1);
        assertThat(changes.get(0).getType()).isEqualTo(ChangeType.ADDED);
        assertThat(changes.get(0).getCategory()).isEqualTo(ChangeCategory.REQUEST_BODY);
        assertThat(changes.get(0).getSeverity()).isEqualTo(Severity.BREAKING);
    }

    @Test
    void testCompareResponseAdded() {
        Endpoint oldEndpoint = Endpoint.builder()
                .path("/api/users")
                .method(HttpMethod.GET)
                .build();

        Response response = new Response("200", "Success", "application/json", "#/components/schemas/User");
        Endpoint newEndpoint = Endpoint.builder()
                .path("/api/users")
                .method(HttpMethod.GET)
                .addResponse(response)
                .build();

        List<Change> changes = comparator.compare(oldEndpoint, newEndpoint);

        assertThat(changes).hasSize(1);
        assertThat(changes.get(0).getType()).isEqualTo(ChangeType.ADDED);
        assertThat(changes.get(0).getCategory()).isEqualTo(ChangeCategory.RESPONSE);
    }

    @Test
    void testCompareMultipleChanges() {
        Parameter oldParam = new Parameter("id", ParameterLocation.PATH, "string", true, null, "ID");
        Endpoint oldEndpoint = Endpoint.builder()
                .path("/api/users/{id}")
                .method(HttpMethod.GET)
                .addParameter(oldParam)
                .deprecated(false)
                .build();

        Parameter newParam = new Parameter("id", ParameterLocation.PATH, "integer", true, null, "ID");
        Endpoint newEndpoint = Endpoint.builder()
                .path("/api/users/{id}")
                .method(HttpMethod.GET)
                .addParameter(newParam)
                .deprecated(true)
                .build();

        List<Change> changes = comparator.compare(oldEndpoint, newEndpoint);

        assertThat(changes).hasSize(2);
    }
}

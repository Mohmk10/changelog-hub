package io.github.mohmk10.changeloghub.core.comparator.impl;

import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.core.model.ApiType;
import io.github.mohmk10.changeloghub.core.model.Change;
import io.github.mohmk10.changeloghub.core.model.ChangeCategory;
import io.github.mohmk10.changeloghub.core.model.ChangeType;
import io.github.mohmk10.changeloghub.core.model.Changelog;
import io.github.mohmk10.changeloghub.core.model.Endpoint;
import io.github.mohmk10.changeloghub.core.model.HttpMethod;
import io.github.mohmk10.changeloghub.core.model.Parameter;
import io.github.mohmk10.changeloghub.core.model.ParameterLocation;
import io.github.mohmk10.changeloghub.core.model.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultApiComparatorTest {

    private DefaultApiComparator comparator;

    @BeforeEach
    void setUp() {
        comparator = new DefaultApiComparator();
    }

    @Test
    void testCompareIdenticalSpecs() {
        Endpoint endpoint = Endpoint.builder()
                .path("/api/users")
                .method(HttpMethod.GET)
                .build();

        ApiSpec spec = ApiSpec.builder()
                .name("Test API")
                .version("1.0.0")
                .type(ApiType.REST)
                .addEndpoint(endpoint)
                .build();

        Changelog changelog = comparator.compare(spec, spec);

        assertThat(changelog).isNotNull();
        assertThat(changelog.getChanges()).isEmpty();
        assertThat(changelog.getFromVersion()).isEqualTo("1.0.0");
        assertThat(changelog.getToVersion()).isEqualTo("1.0.0");
    }

    @Test
    void testCompareWithAddedEndpoint() {
        ApiSpec oldSpec = ApiSpec.builder()
                .name("Test API")
                .version("1.0.0")
                .type(ApiType.REST)
                .build();

        Endpoint newEndpoint = Endpoint.builder()
                .path("/api/users")
                .method(HttpMethod.GET)
                .build();

        ApiSpec newSpec = ApiSpec.builder()
                .name("Test API")
                .version("1.1.0")
                .type(ApiType.REST)
                .addEndpoint(newEndpoint)
                .build();

        Changelog changelog = comparator.compare(oldSpec, newSpec);

        assertThat(changelog.getChanges()).hasSize(1);
        Change change = changelog.getChanges().get(0);
        assertThat(change.getType()).isEqualTo(ChangeType.ADDED);
        assertThat(change.getCategory()).isEqualTo(ChangeCategory.ENDPOINT);
        assertThat(change.getSeverity()).isEqualTo(Severity.INFO);
    }

    @Test
    void testCompareWithRemovedEndpoint() {
        Endpoint endpoint = Endpoint.builder()
                .path("/api/users")
                .method(HttpMethod.GET)
                .build();

        ApiSpec oldSpec = ApiSpec.builder()
                .name("Test API")
                .version("1.0.0")
                .type(ApiType.REST)
                .addEndpoint(endpoint)
                .build();

        ApiSpec newSpec = ApiSpec.builder()
                .name("Test API")
                .version("2.0.0")
                .type(ApiType.REST)
                .build();

        Changelog changelog = comparator.compare(oldSpec, newSpec);

        assertThat(changelog.getChanges()).hasSize(1);
        Change change = changelog.getChanges().get(0);
        assertThat(change.getType()).isEqualTo(ChangeType.REMOVED);
        assertThat(change.getCategory()).isEqualTo(ChangeCategory.ENDPOINT);
        assertThat(change.getSeverity()).isEqualTo(Severity.BREAKING);
    }

    @Test
    void testCompareWithModifiedEndpoint() {
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

        ApiSpec oldSpec = ApiSpec.builder()
                .name("Test API")
                .version("1.0.0")
                .addEndpoint(oldEndpoint)
                .build();

        ApiSpec newSpec = ApiSpec.builder()
                .name("Test API")
                .version("1.1.0")
                .addEndpoint(newEndpoint)
                .build();

        Changelog changelog = comparator.compare(oldSpec, newSpec);

        assertThat(changelog.getChanges()).hasSize(1);
        assertThat(changelog.getChanges().get(0).getType()).isEqualTo(ChangeType.DEPRECATED);
    }

    @Test
    void testCompareComplexChanges() {
        Endpoint endpoint1 = Endpoint.builder()
                .path("/api/users")
                .method(HttpMethod.GET)
                .build();
        Endpoint endpoint2 = Endpoint.builder()
                .path("/api/orders")
                .method(HttpMethod.GET)
                .build();

        ApiSpec oldSpec = ApiSpec.builder()
                .name("Test API")
                .version("1.0.0")
                .addEndpoint(endpoint1)
                .addEndpoint(endpoint2)
                .build();

        Endpoint modifiedEndpoint1 = Endpoint.builder()
                .path("/api/users")
                .method(HttpMethod.GET)
                .deprecated(true)
                .build();
        Endpoint newEndpoint = Endpoint.builder()
                .path("/api/products")
                .method(HttpMethod.GET)
                .build();

        ApiSpec newSpec = ApiSpec.builder()
                .name("Test API")
                .version("2.0.0")
                .addEndpoint(modifiedEndpoint1)
                .addEndpoint(newEndpoint)
                .build();

        Changelog changelog = comparator.compare(oldSpec, newSpec);

        assertThat(changelog.getChanges()).hasSizeGreaterThanOrEqualTo(3);

        List<Change> addedChanges = changelog.getChanges().stream()
                .filter(c -> c.getType() == ChangeType.ADDED)
                .toList();
        List<Change> removedChanges = changelog.getChanges().stream()
                .filter(c -> c.getType() == ChangeType.REMOVED)
                .toList();
        List<Change> deprecatedChanges = changelog.getChanges().stream()
                .filter(c -> c.getType() == ChangeType.DEPRECATED)
                .toList();

        assertThat(addedChanges).hasSize(1);
        assertThat(removedChanges).hasSize(1);
        assertThat(deprecatedChanges).hasSize(1);
    }

    @Test
    void testCompareWithParameterChanges() {
        Parameter param = new Parameter("filter", ParameterLocation.QUERY, "string", true, null, null);

        Endpoint oldEndpoint = Endpoint.builder()
                .path("/api/users")
                .method(HttpMethod.GET)
                .build();

        Endpoint newEndpoint = Endpoint.builder()
                .path("/api/users")
                .method(HttpMethod.GET)
                .addParameter(param)
                .build();

        ApiSpec oldSpec = ApiSpec.builder()
                .name("Test API")
                .version("1.0.0")
                .addEndpoint(oldEndpoint)
                .build();

        ApiSpec newSpec = ApiSpec.builder()
                .name("Test API")
                .version("1.1.0")
                .addEndpoint(newEndpoint)
                .build();

        Changelog changelog = comparator.compare(oldSpec, newSpec);

        assertThat(changelog.getChanges()).hasSize(1);
        assertThat(changelog.getChanges().get(0).getCategory()).isEqualTo(ChangeCategory.PARAMETER);
    }

    @Test
    void testVersionsAreCorrectlySet() {
        ApiSpec oldSpec = ApiSpec.builder()
                .name("Test API")
                .version("1.0.0")
                .build();

        ApiSpec newSpec = ApiSpec.builder()
                .name("Test API")
                .version("2.0.0")
                .build();

        Changelog changelog = comparator.compare(oldSpec, newSpec);

        assertThat(changelog.getFromVersion()).isEqualTo("1.0.0");
        assertThat(changelog.getToVersion()).isEqualTo("2.0.0");
        assertThat(changelog.getApiName()).isEqualTo("Test API");
    }
}

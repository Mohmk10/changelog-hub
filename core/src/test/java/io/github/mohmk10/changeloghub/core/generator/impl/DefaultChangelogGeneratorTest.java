package io.github.mohmk10.changeloghub.core.generator.impl;

import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.core.model.ApiType;
import io.github.mohmk10.changeloghub.core.model.Changelog;
import io.github.mohmk10.changeloghub.core.model.Endpoint;
import io.github.mohmk10.changeloghub.core.model.HttpMethod;
import io.github.mohmk10.changeloghub.core.model.Parameter;
import io.github.mohmk10.changeloghub.core.model.ParameterLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultChangelogGeneratorTest {

    private DefaultChangelogGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new DefaultChangelogGenerator();
    }

    @Test
    void testGenerateWithNoChanges() {
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

        Changelog changelog = generator.generate(spec, spec);

        assertThat(changelog).isNotNull();
        assertThat(changelog.getChanges()).isEmpty();
        assertThat(changelog.getBreakingChanges()).isEmpty();
        assertThat(changelog.getRiskAssessment()).isNotNull();
        assertThat(changelog.getRiskAssessment().getTotalChangesCount()).isZero();
    }

    @Test
    void testGenerateWithBreakingChanges() {
        Endpoint oldEndpoint = Endpoint.builder()
                .path("/api/users")
                .method(HttpMethod.GET)
                .build();

        ApiSpec oldSpec = ApiSpec.builder()
                .name("Test API")
                .version("1.0.0")
                .type(ApiType.REST)
                .addEndpoint(oldEndpoint)
                .build();

        ApiSpec newSpec = ApiSpec.builder()
                .name("Test API")
                .version("2.0.0")
                .type(ApiType.REST)
                .build();

        Changelog changelog = generator.generate(oldSpec, newSpec);

        assertThat(changelog).isNotNull();
        assertThat(changelog.getChanges()).isNotEmpty();
        assertThat(changelog.getBreakingChanges()).isNotEmpty();
        assertThat(changelog.getRiskAssessment().getSemverRecommendation()).isEqualTo("MAJOR");
    }

    @Test
    void testGenerateWithMixedChanges() {
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

        Endpoint modifiedEndpoint = Endpoint.builder()
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
                .version("1.1.0")
                .addEndpoint(modifiedEndpoint)
                .addEndpoint(newEndpoint)
                .build();

        Changelog changelog = generator.generate(oldSpec, newSpec);

        assertThat(changelog.getChanges()).hasSizeGreaterThanOrEqualTo(3);
        assertThat(changelog.getRiskAssessment()).isNotNull();
    }

    @Test
    void testGeneratePopulatesAllFields() {
        Parameter param = new Parameter("id", ParameterLocation.PATH, "string", true, null, null);

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

        Changelog changelog = generator.generate(oldSpec, newSpec);

        assertThat(changelog.getId()).isNotNull();
        assertThat(changelog.getApiName()).isEqualTo("Test API");
        assertThat(changelog.getFromVersion()).isEqualTo("1.0.0");
        assertThat(changelog.getToVersion()).isEqualTo("1.1.0");
        assertThat(changelog.getGeneratedAt()).isNotNull();
        assertThat(changelog.getChanges()).isNotNull();
        assertThat(changelog.getBreakingChanges()).isNotNull();
        assertThat(changelog.getRiskAssessment()).isNotNull();
        assertThat(changelog.getRiskAssessment().getOverallScore()).isGreaterThanOrEqualTo(0);
        assertThat(changelog.getRiskAssessment().getLevel()).isNotNull();
        assertThat(changelog.getRiskAssessment().getSemverRecommendation()).isNotNull();
    }

    @Test
    void testGenerateClassifiesSeverities() {
        Endpoint endpoint = Endpoint.builder()
                .path("/api/users")
                .method(HttpMethod.GET)
                .build();

        ApiSpec oldSpec = ApiSpec.builder()
                .name("Test API")
                .version("1.0.0")
                .addEndpoint(endpoint)
                .build();

        ApiSpec newSpec = ApiSpec.builder()
                .name("Test API")
                .version("2.0.0")
                .build();

        Changelog changelog = generator.generate(oldSpec, newSpec);

        assertThat(changelog.getChanges()).allMatch(c -> c.getSeverity() != null);
    }
}

package io.github.mohmk10.changeloghub.core.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ApiSpecTest {

    @Test
    void shouldCreateApiSpecWithDefaultConstructor() {
        ApiSpec apiSpec = new ApiSpec();

        assertThat(apiSpec.getName()).isNull();
        assertThat(apiSpec.getVersion()).isNull();
        assertThat(apiSpec.getType()).isNull();
        assertThat(apiSpec.getEndpoints()).isNotNull().isEmpty();
        assertThat(apiSpec.getMetadata()).isNotNull().isEmpty();
        assertThat(apiSpec.getParsedAt()).isNull();
    }

    @Test
    void shouldCreateApiSpecWithAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        Endpoint endpoint = Endpoint.builder()
                .path("/api/users")
                .method(HttpMethod.GET)
                .build();
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("key", "value");

        ApiSpec apiSpec = new ApiSpec(
                "Test API",
                "1.0.0",
                ApiType.REST,
                Arrays.asList(endpoint),
                metadata,
                now
        );

        assertThat(apiSpec.getName()).isEqualTo("Test API");
        assertThat(apiSpec.getVersion()).isEqualTo("1.0.0");
        assertThat(apiSpec.getType()).isEqualTo(ApiType.REST);
        assertThat(apiSpec.getEndpoints()).hasSize(1);
        assertThat(apiSpec.getMetadata()).containsEntry("key", "value");
        assertThat(apiSpec.getParsedAt()).isEqualTo(now);
    }

    @Test
    void shouldCreateApiSpecWithBuilder() {
        LocalDateTime now = LocalDateTime.now();
        Endpoint endpoint = Endpoint.builder()
                .path("/api/users")
                .method(HttpMethod.GET)
                .build();

        ApiSpec apiSpec = ApiSpec.builder()
                .name("Test API")
                .version("1.0.0")
                .type(ApiType.REST)
                .addEndpoint(endpoint)
                .addMetadata("key", "value")
                .parsedAt(now)
                .build();

        assertThat(apiSpec.getName()).isEqualTo("Test API");
        assertThat(apiSpec.getVersion()).isEqualTo("1.0.0");
        assertThat(apiSpec.getType()).isEqualTo(ApiType.REST);
        assertThat(apiSpec.getEndpoints()).hasSize(1);
        assertThat(apiSpec.getMetadata()).containsEntry("key", "value");
        assertThat(apiSpec.getParsedAt()).isEqualTo(now);
    }

    @Test
    void shouldHaveCorrectEqualsAndHashCode() {
        LocalDateTime now = LocalDateTime.now();

        ApiSpec apiSpec1 = ApiSpec.builder()
                .name("Test API")
                .version("1.0.0")
                .type(ApiType.REST)
                .parsedAt(now)
                .build();

        ApiSpec apiSpec2 = ApiSpec.builder()
                .name("Test API")
                .version("1.0.0")
                .type(ApiType.REST)
                .parsedAt(now)
                .build();

        ApiSpec apiSpec3 = ApiSpec.builder()
                .name("Different API")
                .version("1.0.0")
                .type(ApiType.REST)
                .parsedAt(now)
                .build();

        assertThat(apiSpec1).isEqualTo(apiSpec2);
        assertThat(apiSpec1.hashCode()).isEqualTo(apiSpec2.hashCode());
        assertThat(apiSpec1).isNotEqualTo(apiSpec3);
    }

    @Test
    void shouldHaveCorrectToString() {
        ApiSpec apiSpec = ApiSpec.builder()
                .name("Test API")
                .version("1.0.0")
                .type(ApiType.REST)
                .build();

        String toString = apiSpec.toString();

        assertThat(toString).contains("Test API");
        assertThat(toString).contains("1.0.0");
        assertThat(toString).contains("REST");
    }

    @Test
    void shouldSupportAllApiTypes() {
        for (ApiType type : ApiType.values()) {
            ApiSpec apiSpec = ApiSpec.builder()
                    .type(type)
                    .build();

            assertThat(apiSpec.getType()).isEqualTo(type);
        }
    }
}

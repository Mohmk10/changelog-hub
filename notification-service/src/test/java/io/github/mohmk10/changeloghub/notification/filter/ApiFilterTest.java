package io.github.mohmk10.changeloghub.notification.filter;

import io.github.mohmk10.changeloghub.notification.model.Notification;
import io.github.mohmk10.changeloghub.notification.model.NotificationEvent;
import io.github.mohmk10.changeloghub.notification.util.EventType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiFilterTest {

    @Test
    void shouldIncludeSpecificApis() {
        ApiFilter filter = ApiFilter.include("api-1", "api-2");

        assertThat(filter.shouldSend(createNotification("api-1"))).isTrue();
        assertThat(filter.shouldSend(createNotification("api-2"))).isTrue();
        assertThat(filter.shouldSend(createNotification("api-3"))).isFalse();
    }

    @Test
    void shouldExcludeSpecificApis() {
        ApiFilter filter = ApiFilter.exclude("internal-api", "deprecated-api");

        assertThat(filter.shouldSend(createNotification("public-api"))).isTrue();
        assertThat(filter.shouldSend(createNotification("internal-api"))).isFalse();
        assertThat(filter.shouldSend(createNotification("deprecated-api"))).isFalse();
    }

    @Test
    void shouldIncludeByPattern() {
        ApiFilter filter = ApiFilter.builder()
            .includePattern("public-.*")
            .build();

        assertThat(filter.shouldSend(createNotification("public-api"))).isTrue();
        assertThat(filter.shouldSend(createNotification("public-service"))).isTrue();
        assertThat(filter.shouldSend(createNotification("internal-api"))).isFalse();
    }

    @Test
    void shouldExcludeByPattern() {
        ApiFilter filter = ApiFilter.builder()
            .excludePattern("test-.*")
            .excludePattern("mock-.*")
            .build();

        assertThat(filter.shouldSend(createNotification("prod-api"))).isTrue();
        assertThat(filter.shouldSend(createNotification("test-api"))).isFalse();
        assertThat(filter.shouldSend(createNotification("mock-service"))).isFalse();
    }

    @Test
    void shouldCombineIncludeAndExclude() {
        ApiFilter filter = ApiFilter.builder()
            .includePattern("api-.*")
            .exclude("api-internal")
            .build();

        assertThat(filter.shouldSend(createNotification("api-public"))).isTrue();
        assertThat(filter.shouldSend(createNotification("api-internal"))).isFalse();
        assertThat(filter.shouldSend(createNotification("service"))).isFalse();
    }

    @Test
    void shouldAllowAllWhenNoFilters() {
        ApiFilter filter = ApiFilter.builder().build();

        assertThat(filter.shouldSend(createNotification("any-api"))).isTrue();
        assertThat(filter.shouldSend(createNotification("another-api"))).isTrue();
    }

    @Test
    void shouldHandleNullApiName() {
        ApiFilter includeFilter = ApiFilter.include("api-1");
        ApiFilter excludeFilter = ApiFilter.exclude("api-1");
        ApiFilter noFilter = ApiFilter.builder().build();

        Notification noApi = Notification.builder().title("Test").build();

        assertThat(includeFilter.shouldSend(noApi)).isFalse();
        assertThat(excludeFilter.shouldSend(noApi)).isTrue();
        assertThat(noFilter.shouldSend(noApi)).isTrue();
    }

    @Test
    void shouldProvideFilterReason() {
        ApiFilter filter = ApiFilter.exclude("internal-api");
        Notification notification = createNotification("internal-api");

        String reason = filter.getFilterReason(notification);

        assertThat(reason).contains("internal-api").contains("excluded");
    }

    private Notification createNotification(String apiName) {
        NotificationEvent event = NotificationEvent.builder()
            .eventType(EventType.API_VERSION_RELEASED)
            .apiName(apiName)
            .build();

        return Notification.builder()
            .title("Test")
            .event(event)
            .build();
    }
}

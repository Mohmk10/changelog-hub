package io.github.mohmk10.changeloghub.notification.filter;

import io.github.mohmk10.changeloghub.notification.model.Notification;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitFilterTest {

    @Test
    void shouldAllowWithinLimit() {
        RateLimitFilter filter = new RateLimitFilter(5, Duration.ofMinutes(1));

        for (int i = 0; i < 5; i++) {
            Notification notification = createNotification();
            assertThat(filter.shouldSend(notification)).isTrue();
        }
    }

    @Test
    void shouldBlockWhenLimitExceeded() {
        RateLimitFilter filter = new RateLimitFilter(3, Duration.ofMinutes(1));

        for (int i = 0; i < 3; i++) {
            assertThat(filter.shouldSend(createNotification())).isTrue();
        }

        assertThat(filter.shouldSend(createNotification())).isFalse();
    }

    @Test
    void shouldCreatePerMinuteFilter() {
        RateLimitFilter filter = RateLimitFilter.perMinute(10);

        assertThat(filter.getMaxNotificationsPerWindow()).isEqualTo(10);
        assertThat(filter.getWindowDuration()).isEqualTo(Duration.ofMinutes(1));
    }

    @Test
    void shouldCreatePerHourFilter() {
        RateLimitFilter filter = RateLimitFilter.perHour(100);

        assertThat(filter.getMaxNotificationsPerWindow()).isEqualTo(100);
        assertThat(filter.getWindowDuration()).isEqualTo(Duration.ofHours(1));
    }

    @Test
    void shouldReset() {
        RateLimitFilter filter = new RateLimitFilter(2, Duration.ofMinutes(1));

        filter.shouldSend(createNotification());
        filter.shouldSend(createNotification());
        assertThat(filter.shouldSend(createNotification())).isFalse();

        filter.reset();

        assertThat(filter.shouldSend(createNotification())).isTrue();
    }

    @Test
    void shouldGetRemainingCapacity() {
        RateLimitFilter filter = new RateLimitFilter(5, Duration.ofMinutes(1));

        assertThat(filter.getRemainingCapacity()).isEqualTo(5);

        filter.shouldSend(createNotification());
        filter.shouldSend(createNotification());

        assertThat(filter.getRemainingCapacity()).isEqualTo(3);
    }

    @Test
    void shouldProvideFilterReason() {
        RateLimitFilter filter = new RateLimitFilter(1, Duration.ofMinutes(1));
        filter.shouldSend(createNotification());

        Notification blocked = createNotification();
        filter.shouldSend(blocked);

        String reason = filter.getFilterReason(blocked);

        assertThat(reason).contains("Rate limit");
    }

    @Test
    void shouldHaveVeryEarlyPriority() {
        RateLimitFilter filter = RateLimitFilter.perMinute(10);

        assertThat(filter.getPriority()).isLessThan(10);
    }

    private Notification createNotification() {
        return Notification.builder()
            .title("Test")
            .build();
    }
}

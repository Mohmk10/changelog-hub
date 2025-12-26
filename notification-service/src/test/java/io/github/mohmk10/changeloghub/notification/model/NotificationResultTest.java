package io.github.mohmk10.changeloghub.notification.model;

import io.github.mohmk10.changeloghub.notification.util.ChannelType;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationResultTest {

    @Test
    void shouldCreateSuccessResult() {
        NotificationResult result = NotificationResult.success("notif-1", ChannelType.SLACK);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isFailure()).isFalse();
        assertThat(result.getNotificationId()).isEqualTo("notif-1");
        assertThat(result.getChannelType()).isEqualTo(ChannelType.SLACK);
    }

    @Test
    void shouldCreateSuccessResultWithMessageId() {
        NotificationResult result = NotificationResult.success("notif-1", ChannelType.SLACK, "msg-123");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessageId()).contains("msg-123");
    }

    @Test
    void shouldCreateFailureResult() {
        NotificationResult result = NotificationResult.failure("notif-1", ChannelType.DISCORD, "Error message");

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getErrorMessage()).contains("Error message");
    }

    @Test
    void shouldCreateFailureResultWithException() {
        Exception ex = new RuntimeException("Test exception");
        NotificationResult result = NotificationResult.failure("notif-1", ChannelType.EMAIL, ex);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).contains(ex);
        assertThat(result.getErrorMessage()).contains("Test exception");
    }

    @Test
    void shouldCreateHttpFailureResult() {
        NotificationResult result = NotificationResult.httpFailure(
            "notif-1", ChannelType.TEAMS, 429, "Rate limited");

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getHttpStatusCode()).isEqualTo(429);
        assertThat(result.getErrorMessage()).contains("Rate limited");
    }

    @Test
    void shouldBuildWithDuration() {
        NotificationResult result = NotificationResult.builder()
            .notificationId("notif-1")
            .channelType(ChannelType.WEBHOOK)
            .success(true)
            .duration(Duration.ofMillis(150))
            .build();

        assertThat(result.getDuration()).contains(Duration.ofMillis(150));
    }

    @Test
    void shouldBuildWithRetryCount() {
        NotificationResult result = NotificationResult.builder()
            .notificationId("notif-1")
            .channelType(ChannelType.WEBHOOK)
            .success(true)
            .retryCount(2)
            .build();

        assertThat(result.getRetryCount()).isEqualTo(2);
        assertThat(result.hasRetried()).isTrue();
    }

    @Test
    void shouldHaveSentAtTimestamp() {
        NotificationResult result = NotificationResult.success("notif-1", ChannelType.SLACK);

        assertThat(result.getSentAt()).isNotNull();
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        NotificationResult r1 = NotificationResult.success("notif-1", ChannelType.SLACK);
        NotificationResult r2 = NotificationResult.success("notif-1", ChannelType.SLACK);
        NotificationResult r3 = NotificationResult.success("notif-2", ChannelType.SLACK);

        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
        assertThat(r1).isNotEqualTo(r3);
    }

    @Test
    void shouldHaveDescriptiveToString() {
        NotificationResult success = NotificationResult.success("n1", ChannelType.SLACK, "msg-1");
        NotificationResult failure = NotificationResult.failure("n2", ChannelType.DISCORD, "Error");

        assertThat(success.toString()).contains("success=true", "msg-1");
        assertThat(failure.toString()).contains("success=false", "Error");
    }
}

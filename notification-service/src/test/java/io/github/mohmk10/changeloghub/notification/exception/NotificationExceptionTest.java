package io.github.mohmk10.changeloghub.notification.exception;

import io.github.mohmk10.changeloghub.notification.util.ChannelType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationExceptionTest {

    @Test
    void shouldCreateWithMessage() {
        NotificationException ex = new NotificationException("Test error");

        assertThat(ex.getMessage()).isEqualTo("Test error");
        assertThat(ex.getErrorCode()).isEqualTo(NotificationException.ErrorCode.UNKNOWN);
    }

    @Test
    void shouldCreateWithMessageAndCause() {
        RuntimeException cause = new RuntimeException("cause");
        NotificationException ex = new NotificationException("Test error", cause);

        assertThat(ex.getMessage()).isEqualTo("Test error");
        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    void shouldCreateChannelNotConfigured() {
        NotificationException ex = NotificationException.channelNotConfigured(ChannelType.SLACK);

        assertThat(ex.getErrorCode()).isEqualTo(NotificationException.ErrorCode.CHANNEL_NOT_CONFIGURED);
        assertThat(ex.getChannelType()).isEqualTo(ChannelType.SLACK);
        assertThat(ex.getMessage()).contains("Slack");
    }

    @Test
    void shouldCreateInvalidConfiguration() {
        NotificationException ex = NotificationException.invalidConfiguration("Missing webhook");

        assertThat(ex.getErrorCode()).isEqualTo(NotificationException.ErrorCode.INVALID_CONFIGURATION);
        assertThat(ex.getMessage()).isEqualTo("Missing webhook");
    }

    @Test
    void shouldCreateInvalidConfigurationWithChannel() {
        NotificationException ex = NotificationException.invalidConfiguration(
            ChannelType.DISCORD, "Missing URL");

        assertThat(ex.getErrorCode()).isEqualTo(NotificationException.ErrorCode.INVALID_CONFIGURATION);
        assertThat(ex.getChannelType()).isEqualTo(ChannelType.DISCORD);
        assertThat(ex.getMessage()).contains("Discord").contains("Missing URL");
    }

    @Test
    void shouldCreateSendFailed() {
        NotificationException ex = NotificationException.sendFailed(ChannelType.TEAMS, "Connection refused");

        assertThat(ex.getErrorCode()).isEqualTo(NotificationException.ErrorCode.SEND_FAILED);
        assertThat(ex.getChannelType()).isEqualTo(ChannelType.TEAMS);
        assertThat(ex.getMessage()).contains("Teams").contains("Connection refused");
    }

    @Test
    void shouldCreateSendFailedWithCause() {
        RuntimeException cause = new RuntimeException("Network error");
        NotificationException ex = NotificationException.sendFailed(ChannelType.WEBHOOK, cause);

        assertThat(ex.getErrorCode()).isEqualTo(NotificationException.ErrorCode.SEND_FAILED);
        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    void shouldCreateHttpError() {
        NotificationException ex = NotificationException.httpError(
            ChannelType.SLACK, 500, "Internal Server Error");

        assertThat(ex.getErrorCode()).isEqualTo(NotificationException.ErrorCode.HTTP_ERROR);
        assertThat(ex.getHttpStatusCode()).isEqualTo(500);
        assertThat(ex.getMessage()).contains("500").contains("Slack");
    }

    @Test
    void shouldCreateTimeout() {
        NotificationException ex = NotificationException.timeout(ChannelType.DISCORD);

        assertThat(ex.getErrorCode()).isEqualTo(NotificationException.ErrorCode.TIMEOUT);
        assertThat(ex.getMessage()).contains("Timeout").contains("Discord");
    }

    @Test
    void shouldCreateRateLimited() {
        NotificationException ex = NotificationException.rateLimited(ChannelType.TEAMS);

        assertThat(ex.getErrorCode()).isEqualTo(NotificationException.ErrorCode.RATE_LIMITED);
        assertThat(ex.getMessage()).contains("Rate limited").contains("Teams");
    }

    @Test
    void shouldCreateAuthenticationFailed() {
        NotificationException ex = NotificationException.authenticationFailed(ChannelType.WEBHOOK);

        assertThat(ex.getErrorCode()).isEqualTo(NotificationException.ErrorCode.AUTHENTICATION_FAILED);
        assertThat(ex.getMessage()).contains("Authentication failed");
    }

    @Test
    void shouldCreateFormatError() {
        RuntimeException cause = new RuntimeException("JSON error");
        NotificationException ex = NotificationException.formatError("Invalid format", cause);

        assertThat(ex.getErrorCode()).isEqualTo(NotificationException.ErrorCode.FORMAT_ERROR);
        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    void shouldCreateTemplateError() {
        NotificationException ex = NotificationException.templateError("Missing variable");

        assertThat(ex.getErrorCode()).isEqualTo(NotificationException.ErrorCode.TEMPLATE_ERROR);
        assertThat(ex.getMessage()).isEqualTo("Missing variable");
    }

    @Test
    void shouldCreateFiltered() {
        NotificationException ex = NotificationException.filtered("Rate limit exceeded");

        assertThat(ex.getErrorCode()).isEqualTo(NotificationException.ErrorCode.FILTERED);
        assertThat(ex.getMessage()).contains("filtered").contains("Rate limit");
    }

    @Test
    void shouldIdentifyRetryableErrors() {
        assertThat(NotificationException.ErrorCode.SEND_FAILED.isRetryable()).isTrue();
        assertThat(NotificationException.ErrorCode.HTTP_ERROR.isRetryable()).isTrue();
        assertThat(NotificationException.ErrorCode.TIMEOUT.isRetryable()).isTrue();
        assertThat(NotificationException.ErrorCode.RATE_LIMITED.isRetryable()).isTrue();
        assertThat(NotificationException.ErrorCode.CONNECTION_ERROR.isRetryable()).isTrue();

        assertThat(NotificationException.ErrorCode.AUTHENTICATION_FAILED.isRetryable()).isFalse();
        assertThat(NotificationException.ErrorCode.INVALID_CONFIGURATION.isRetryable()).isFalse();
        assertThat(NotificationException.ErrorCode.FILTERED.isRetryable()).isFalse();
    }

    @Test
    void shouldHaveErrorCodeDescriptions() {
        assertThat(NotificationException.ErrorCode.UNKNOWN.getDescription()).isEqualTo("Unknown error");
        assertThat(NotificationException.ErrorCode.CHANNEL_NOT_CONFIGURED.getDescription())
            .isEqualTo("Channel not configured");
        assertThat(NotificationException.ErrorCode.SEND_FAILED.getDescription())
            .isEqualTo("Failed to send notification");
    }

    @Test
    void shouldBuildWithBuilder() {
        NotificationException ex = NotificationException.builder()
            .errorCode(NotificationException.ErrorCode.HTTP_ERROR)
            .channelType(ChannelType.SLACK)
            .httpStatusCode(429)
            .notificationId("notif-123")
            .message("Rate limited")
            .build();

        assertThat(ex.getErrorCode()).isEqualTo(NotificationException.ErrorCode.HTTP_ERROR);
        assertThat(ex.getChannelType()).isEqualTo(ChannelType.SLACK);
        assertThat(ex.getHttpStatusCode()).isEqualTo(429);
        assertThat(ex.getNotificationId()).isEqualTo("notif-123");
        assertThat(ex.isRetryable()).isTrue();
    }
}

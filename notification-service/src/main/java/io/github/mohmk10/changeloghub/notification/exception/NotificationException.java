package io.github.mohmk10.changeloghub.notification.exception;

import io.github.mohmk10.changeloghub.notification.util.ChannelType;

public class NotificationException extends RuntimeException {

    private final ErrorCode errorCode;
    private final ChannelType channelType;
    private final String notificationId;
    private final int httpStatusCode;

    public NotificationException(String message) {
        super(message);
        this.errorCode = ErrorCode.UNKNOWN;
        this.channelType = null;
        this.notificationId = null;
        this.httpStatusCode = 0;
    }

    public NotificationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.UNKNOWN;
        this.channelType = null;
        this.notificationId = null;
        this.httpStatusCode = 0;
    }

    public NotificationException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.channelType = null;
        this.notificationId = null;
        this.httpStatusCode = 0;
    }

    public NotificationException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.channelType = null;
        this.notificationId = null;
        this.httpStatusCode = 0;
    }

    public NotificationException(ErrorCode errorCode, ChannelType channelType, String message) {
        super(message);
        this.errorCode = errorCode;
        this.channelType = channelType;
        this.notificationId = null;
        this.httpStatusCode = 0;
    }

    public NotificationException(ErrorCode errorCode, ChannelType channelType, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.channelType = channelType;
        this.notificationId = null;
        this.httpStatusCode = 0;
    }

    private NotificationException(Builder builder) {
        super(builder.message, builder.cause);
        this.errorCode = builder.errorCode;
        this.channelType = builder.channelType;
        this.notificationId = builder.notificationId;
        this.httpStatusCode = builder.httpStatusCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public ChannelType getChannelType() {
        return channelType;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public boolean isRetryable() {
        return errorCode.isRetryable();
    }

    public static NotificationException channelNotConfigured(ChannelType channelType) {
        return new NotificationException(
            ErrorCode.CHANNEL_NOT_CONFIGURED,
            channelType,
            String.format("Channel %s is not configured", channelType.getDisplayName())
        );
    }

    public static NotificationException invalidConfiguration(String message) {
        return new NotificationException(ErrorCode.INVALID_CONFIGURATION, message);
    }

    public static NotificationException invalidConfiguration(ChannelType channelType, String message) {
        return new NotificationException(
            ErrorCode.INVALID_CONFIGURATION,
            channelType,
            String.format("Invalid configuration for %s: %s", channelType.getDisplayName(), message)
        );
    }

    public static NotificationException sendFailed(ChannelType channelType, String message) {
        return new NotificationException(
            ErrorCode.SEND_FAILED,
            channelType,
            String.format("Failed to send notification via %s: %s", channelType.getDisplayName(), message)
        );
    }

    public static NotificationException sendFailed(ChannelType channelType, Throwable cause) {
        return new NotificationException(
            ErrorCode.SEND_FAILED,
            channelType,
            String.format("Failed to send notification via %s: %s",
                channelType.getDisplayName(), cause.getMessage()),
            cause
        );
    }

    public static NotificationException httpError(ChannelType channelType, int statusCode, String response) {
        return builder()
            .errorCode(ErrorCode.HTTP_ERROR)
            .channelType(channelType)
            .httpStatusCode(statusCode)
            .message(String.format("HTTP error %d from %s: %s",
                statusCode, channelType.getDisplayName(), response))
            .build();
    }

    public static NotificationException timeout(ChannelType channelType) {
        return new NotificationException(
            ErrorCode.TIMEOUT,
            channelType,
            String.format("Timeout sending notification via %s", channelType.getDisplayName())
        );
    }

    public static NotificationException rateLimited(ChannelType channelType) {
        return new NotificationException(
            ErrorCode.RATE_LIMITED,
            channelType,
            String.format("Rate limited by %s", channelType.getDisplayName())
        );
    }

    public static NotificationException authenticationFailed(ChannelType channelType) {
        return new NotificationException(
            ErrorCode.AUTHENTICATION_FAILED,
            channelType,
            String.format("Authentication failed for %s", channelType.getDisplayName())
        );
    }

    public static NotificationException formatError(String message, Throwable cause) {
        return new NotificationException(ErrorCode.FORMAT_ERROR, message, cause);
    }

    public static NotificationException templateError(String message) {
        return new NotificationException(ErrorCode.TEMPLATE_ERROR, message);
    }

    public static NotificationException filtered(String reason) {
        return new NotificationException(ErrorCode.FILTERED, "Notification filtered: " + reason);
    }

    public static Builder builder() {
        return new Builder();
    }

    public enum ErrorCode {
        UNKNOWN("Unknown error", false),
        CHANNEL_NOT_CONFIGURED("Channel not configured", false),
        INVALID_CONFIGURATION("Invalid configuration", false),
        SEND_FAILED("Failed to send notification", true),
        HTTP_ERROR("HTTP error", true),
        TIMEOUT("Request timeout", true),
        RATE_LIMITED("Rate limited", true),
        AUTHENTICATION_FAILED("Authentication failed", false),
        FORMAT_ERROR("Message formatting error", false),
        TEMPLATE_ERROR("Template processing error", false),
        FILTERED("Notification was filtered", false),
        CONNECTION_ERROR("Connection error", true),
        SSL_ERROR("SSL/TLS error", false);

        private final String description;
        private final boolean retryable;

        ErrorCode(String description, boolean retryable) {
            this.description = description;
            this.retryable = retryable;
        }

        public String getDescription() {
            return description;
        }

        public boolean isRetryable() {
            return retryable;
        }
    }

    public static class Builder {
        private ErrorCode errorCode = ErrorCode.UNKNOWN;
        private ChannelType channelType;
        private String notificationId;
        private int httpStatusCode;
        private String message;
        private Throwable cause;

        public Builder errorCode(ErrorCode errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public Builder channelType(ChannelType channelType) {
            this.channelType = channelType;
            return this;
        }

        public Builder notificationId(String notificationId) {
            this.notificationId = notificationId;
            return this;
        }

        public Builder httpStatusCode(int httpStatusCode) {
            this.httpStatusCode = httpStatusCode;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder cause(Throwable cause) {
            this.cause = cause;
            return this;
        }

        public NotificationException build() {
            return new NotificationException(this);
        }
    }
}

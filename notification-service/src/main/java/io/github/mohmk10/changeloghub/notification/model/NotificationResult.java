package io.github.mohmk10.changeloghub.notification.model;

import io.github.mohmk10.changeloghub.notification.util.ChannelType;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Result of sending a notification.
 */
public class NotificationResult {

    private final String notificationId;
    private final ChannelType channelType;
    private final boolean success;
    private final String messageId;
    private final String errorMessage;
    private final Throwable error;
    private final Instant sentAt;
    private final Duration duration;
    private final int retryCount;
    private final int httpStatusCode;

    private NotificationResult(Builder builder) {
        this.notificationId = builder.notificationId;
        this.channelType = builder.channelType;
        this.success = builder.success;
        this.messageId = builder.messageId;
        this.errorMessage = builder.errorMessage;
        this.error = builder.error;
        this.sentAt = builder.sentAt != null ? builder.sentAt : Instant.now();
        this.duration = builder.duration;
        this.retryCount = builder.retryCount;
        this.httpStatusCode = builder.httpStatusCode;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public ChannelType getChannelType() {
        return channelType;
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isFailure() {
        return !success;
    }

    public Optional<String> getMessageId() {
        return Optional.ofNullable(messageId);
    }

    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessage);
    }

    public Optional<Throwable> getError() {
        return Optional.ofNullable(error);
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public Optional<Duration> getDuration() {
        return Optional.ofNullable(duration);
    }

    public int getRetryCount() {
        return retryCount;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public boolean hasRetried() {
        return retryCount > 0;
    }

    /**
     * Create a success result.
     */
    public static NotificationResult success(String notificationId, ChannelType channelType) {
        return builder()
            .notificationId(notificationId)
            .channelType(channelType)
            .success(true)
            .build();
    }

    /**
     * Create a success result with message ID.
     */
    public static NotificationResult success(String notificationId, ChannelType channelType, String messageId) {
        return builder()
            .notificationId(notificationId)
            .channelType(channelType)
            .success(true)
            .messageId(messageId)
            .build();
    }

    /**
     * Create a failure result.
     */
    public static NotificationResult failure(String notificationId, ChannelType channelType, String errorMessage) {
        return builder()
            .notificationId(notificationId)
            .channelType(channelType)
            .success(false)
            .errorMessage(errorMessage)
            .build();
    }

    /**
     * Create a failure result with exception.
     */
    public static NotificationResult failure(String notificationId, ChannelType channelType, Throwable error) {
        return builder()
            .notificationId(notificationId)
            .channelType(channelType)
            .success(false)
            .error(error)
            .errorMessage(error.getMessage())
            .build();
    }

    /**
     * Create a failure result with HTTP status.
     */
    public static NotificationResult httpFailure(String notificationId, ChannelType channelType,
                                                  int statusCode, String errorMessage) {
        return builder()
            .notificationId(notificationId)
            .channelType(channelType)
            .success(false)
            .httpStatusCode(statusCode)
            .errorMessage(errorMessage)
            .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationResult that = (NotificationResult) o;
        return Objects.equals(notificationId, that.notificationId) &&
               channelType == that.channelType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(notificationId, channelType);
    }

    @Override
    public String toString() {
        if (success) {
            return String.format("NotificationResult{id='%s', channel=%s, success=true, messageId='%s'}",
                notificationId, channelType, messageId);
        } else {
            return String.format("NotificationResult{id='%s', channel=%s, success=false, error='%s'}",
                notificationId, channelType, errorMessage);
        }
    }

    public static class Builder {
        private String notificationId;
        private ChannelType channelType;
        private boolean success;
        private String messageId;
        private String errorMessage;
        private Throwable error;
        private Instant sentAt;
        private Duration duration;
        private int retryCount;
        private int httpStatusCode;

        public Builder notificationId(String notificationId) {
            this.notificationId = notificationId;
            return this;
        }

        public Builder channelType(ChannelType channelType) {
            this.channelType = channelType;
            return this;
        }

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder messageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder error(Throwable error) {
            this.error = error;
            return this;
        }

        public Builder sentAt(Instant sentAt) {
            this.sentAt = sentAt;
            return this;
        }

        public Builder duration(Duration duration) {
            this.duration = duration;
            return this;
        }

        public Builder retryCount(int retryCount) {
            this.retryCount = retryCount;
            return this;
        }

        public Builder httpStatusCode(int httpStatusCode) {
            this.httpStatusCode = httpStatusCode;
            return this;
        }

        public NotificationResult build() {
            return new NotificationResult(this);
        }
    }
}

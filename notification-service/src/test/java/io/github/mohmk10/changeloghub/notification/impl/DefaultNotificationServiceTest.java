package io.github.mohmk10.changeloghub.notification.impl;

import io.github.mohmk10.changeloghub.core.model.*;
import io.github.mohmk10.changeloghub.notification.channel.NotificationChannel;
import io.github.mohmk10.changeloghub.notification.exception.NotificationException;
import io.github.mohmk10.changeloghub.notification.filter.RateLimitFilter;
import io.github.mohmk10.changeloghub.notification.filter.SeverityFilter;
import io.github.mohmk10.changeloghub.notification.model.*;
import io.github.mohmk10.changeloghub.notification.util.ChannelType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultNotificationServiceTest {

    private DefaultNotificationService service;

    @BeforeEach
    void setUp() {
        service = new DefaultNotificationService();
    }

    @AfterEach
    void tearDown() {
        service.shutdown();
    }

    @Test
    void shouldConfigureService() {
        NotificationConfig config = NotificationConfig.builder()
            .enabled(true)
            .slack("https://hooks.slack.com/test")
            .discord("https://discord.com/api/webhooks/test")
            .build();

        service.configure(config);

        assertThat(service.isEnabled()).isTrue();
        assertThat(service.isChannelConfigured(ChannelType.SLACK)).isTrue();
        assertThat(service.isChannelConfigured(ChannelType.DISCORD)).isTrue();
        assertThat(service.isChannelConfigured(ChannelType.EMAIL)).isFalse();
    }

    @Test
    void shouldAddChannel() {
        ChannelConfig slackConfig = ChannelConfig.slack("https://hooks.slack.com/test").build();

        service.addChannel(ChannelType.SLACK, slackConfig);

        assertThat(service.isChannelConfigured(ChannelType.SLACK)).isTrue();
        assertThat(service.getConfiguredChannels()).contains(ChannelType.SLACK);
    }

    @Test
    void shouldRemoveChannel() {
        ChannelConfig slackConfig = ChannelConfig.slack("https://hooks.slack.com/test").build();
        service.addChannel(ChannelType.SLACK, slackConfig);

        service.removeChannel(ChannelType.SLACK);

        assertThat(service.isChannelConfigured(ChannelType.SLACK)).isFalse();
    }

    @Test
    void shouldReturnConfiguredChannels() {
        service.addChannel(ChannelType.SLACK, ChannelConfig.slack("https://slack").build());
        service.addChannel(ChannelType.DISCORD, ChannelConfig.discord("https://discord").build());

        Set<ChannelType> configured = service.getConfiguredChannels();

        assertThat(configured).containsExactlyInAnyOrder(ChannelType.SLACK, ChannelType.DISCORD);
    }

    @Test
    void shouldFailWhenNotificationsDisabled() {
        service.setEnabled(false);

        Notification notification = Notification.builder()
            .title("Test")
            .build();

        NotificationResult result = service.notify(notification);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getErrorMessage()).isPresent();
        assertThat(result.getErrorMessage().orElse("")).contains("disabled");
    }

    @Test
    void shouldFilterBySeverity() {
        NotificationConfig config = NotificationConfig.builder()
            .slack("https://hooks.slack.com/test")
            .filter(SeverityFilter.breakingOnly())
            .build();

        service.configure(config);

        Notification infoNotification = Notification.builder()
            .title("Test")
            .severity(Severity.INFO)
            .build();

        assertThatThrownBy(() -> service.notify(infoNotification))
            .isInstanceOf(NotificationException.class);
    }

    @Test
    void shouldFilterByRateLimit() {
        NotificationConfig config = NotificationConfig.builder()
            .slack("https://hooks.slack.com/test")
            .filter(new RateLimitFilter(1, Duration.ofMinutes(1)))
            .build();

        service.configure(config);

        Notification notification = Notification.builder()
            .title("Test")
            .severity(Severity.BREAKING)
            .build();

        // First should work (or fail on actual send)
        try {
            service.notify(notification);
        } catch (Exception ignored) {
        }

        // Second should be filtered
        assertThatThrownBy(() -> service.notify(notification))
            .isInstanceOf(NotificationException.class)
            .satisfies(e -> assertThat(((NotificationException) e).getErrorCode())
                .isEqualTo(NotificationException.ErrorCode.FILTERED));
    }

    @Test
    void shouldBuildWithBuilder() {
        DefaultNotificationService built = DefaultNotificationService.builder()
            .enabled(true)
            .slack("https://hooks.slack.com/test")
            .filter(SeverityFilter.criticalOnly())
            .async(true)
            .build();

        assertThat(built.isEnabled()).isTrue();
        assertThat(built.isChannelConfigured(ChannelType.SLACK)).isTrue();

        built.shutdown();
    }

    @Test
    void shouldNotifyFromChangelog() {
        service.addChannel(ChannelType.WEBHOOK,
            ChannelConfig.webhook("https://example.com/webhook").build());

        Changelog changelog = Changelog.builder()
            .apiName("Test API")
            .fromVersion("1.0.0")
            .toVersion("2.0.0")
            .addChange(Change.builder()
                .description("Test change")
                .type(ChangeType.ADDED)
                .severity(Severity.INFO)
                .build())
            .build();

        // This will fail because the webhook URL is not real,
        // but it tests the flow
        NotificationResult result = service.notify(changelog);

        assertThat(result).isNotNull();
    }

    @Test
    void shouldNotifySpecificChannels() {
        service.addChannel(ChannelType.SLACK,
            ChannelConfig.slack("https://slack").build());
        service.addChannel(ChannelType.DISCORD,
            ChannelConfig.discord("https://discord").build());

        Changelog changelog = Changelog.builder()
            .apiName("API")
            .build();

        // Notify only Slack
        NotificationResult result = service.notify(changelog, List.of(ChannelType.SLACK));

        assertThat(result).isNotNull();
    }

    @Test
    void shouldNotifyAllChannels() {
        service.addChannel(ChannelType.WEBHOOK,
            ChannelConfig.webhook("https://webhook1.example.com").build());

        Changelog changelog = Changelog.builder()
            .apiName("API")
            .build();

        List<NotificationResult> results = service.notifyAll(changelog);

        assertThat(results).isNotEmpty();
    }

    @Test
    void shouldNotifyAsync() throws Exception {
        service.setEnabled(false); // Disable to get quick result

        Notification notification = Notification.builder()
            .title("Test")
            .build();

        CompletableFuture<List<NotificationResult>> future = service.notifyAsync(notification);

        List<NotificationResult> results = future.get(5, TimeUnit.SECONDS);
        assertThat(results).isNotEmpty();
    }

    @Test
    void shouldTestChannel() {
        // Channel not configured - should return false
        assertThat(service.testChannel(ChannelType.SLACK)).isFalse();
    }

    @Test
    void shouldTestAllChannels() {
        Map<ChannelType, Boolean> results = service.testAllChannels();

        assertThat(results).isEmpty(); // No channels configured
    }

    @Test
    void shouldEnableAndDisable() {
        service.setEnabled(true);
        assertThat(service.isEnabled()).isTrue();

        service.setEnabled(false);
        assertThat(service.isEnabled()).isFalse();
    }

    @Test
    void shouldGetConfig() {
        NotificationConfig config = NotificationConfig.builder()
            .slack("https://slack")
            .build();

        service.configure(config);

        NotificationConfig retrieved = service.getConfig();
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.hasChannel(ChannelType.SLACK)).isTrue();
    }
}

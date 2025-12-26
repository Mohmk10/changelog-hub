package io.github.mohmk10.changeloghub.notification.model;

import io.github.mohmk10.changeloghub.notification.util.ChannelType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChannelConfigTest {

    @Test
    void shouldBuildSlackConfig() {
        ChannelConfig config = ChannelConfig.slack("https://hooks.slack.com/test").build();

        assertThat(config.getChannelType()).isEqualTo(ChannelType.SLACK);
        assertThat(config.getWebhookUrl()).isEqualTo("https://hooks.slack.com/test");
        assertThat(config.isEnabled()).isTrue();
        assertThat(config.isValid()).isTrue();
    }

    @Test
    void shouldBuildDiscordConfig() {
        ChannelConfig config = ChannelConfig.discord("https://discord.com/api/webhooks/123").build();

        assertThat(config.getChannelType()).isEqualTo(ChannelType.DISCORD);
        assertThat(config.hasWebhookUrl()).isTrue();
        assertThat(config.isValid()).isTrue();
    }

    @Test
    void shouldBuildTeamsConfig() {
        ChannelConfig config = ChannelConfig.teams("https://outlook.office.com/webhook/123").build();

        assertThat(config.getChannelType()).isEqualTo(ChannelType.TEAMS);
        assertThat(config.isValid()).isTrue();
    }

    @Test
    void shouldBuildWebhookConfig() {
        ChannelConfig config = ChannelConfig.webhook("https://api.example.com/webhook")
            .bearerAuth("token123")
            .header("X-Custom", "value")
            .build();

        assertThat(config.getChannelType()).isEqualTo(ChannelType.WEBHOOK);
        assertThat(config.hasAuth()).isTrue();
        assertThat(config.getAuthType()).isEqualTo("Bearer");
        assertThat(config.getHeaders()).containsEntry("X-Custom", "value");
    }

    @Test
    void shouldBuildEmailConfig() {
        ChannelConfig.SmtpConfig smtp = ChannelConfig.SmtpConfig.builder()
            .host("smtp.example.com")
            .port(587)
            .username("user")
            .password("pass")
            .fromAddress("noreply@example.com")
            .fromName("ChangelogHub")
            .startTls(true)
            .build();

        ChannelConfig config = ChannelConfig.email(smtp).build();

        assertThat(config.getChannelType()).isEqualTo(ChannelType.EMAIL);
        assertThat(config.hasSmtpConfig()).isTrue();
        assertThat(config.getSmtpConfig().getHost()).isEqualTo("smtp.example.com");
        assertThat(config.getSmtpConfig().hasAuth()).isTrue();
        assertThat(config.isValid()).isTrue();
    }

    @Test
    void shouldValidateSmtpConfig() {
        ChannelConfig.SmtpConfig validSmtp = ChannelConfig.SmtpConfig.builder()
            .host("smtp.example.com")
            .fromAddress("test@example.com")
            .build();

        ChannelConfig.SmtpConfig invalidSmtp = ChannelConfig.SmtpConfig.builder()
            .port(587)
            .build();

        assertThat(validSmtp.isValid()).isTrue();
        assertThat(invalidSmtp.isValid()).isFalse();
    }

    @Test
    void shouldConfigureBasicAuth() {
        ChannelConfig config = ChannelConfig.webhook("https://api.example.com")
            .basicAuth("user", "pass")
            .build();

        assertThat(config.hasAuth()).isTrue();
        assertThat(config.getAuthType()).isEqualTo("Basic");
        assertThat(config.getAuthValue()).isNotEmpty();
    }

    @Test
    void shouldSetTimeoutAndRetry() {
        ChannelConfig config = ChannelConfig.webhook("https://api.example.com")
            .timeoutMs(5000)
            .retryCount(5)
            .build();

        assertThat(config.getTimeoutMs()).isEqualTo(5000);
        assertThat(config.getRetryCount()).isEqualTo(5);
    }

    @Test
    void shouldHaveDefaultTimeoutAndRetry() {
        ChannelConfig config = ChannelConfig.slack("https://hooks.slack.com/test").build();

        assertThat(config.getTimeoutMs()).isEqualTo(30000);
        assertThat(config.getRetryCount()).isEqualTo(3);
    }

    @Test
    void shouldSetChannel() {
        ChannelConfig config = ChannelConfig.slack("https://hooks.slack.com/test")
            .channel("#alerts")
            .build();

        assertThat(config.getChannel()).isEqualTo("#alerts");
    }

    @Test
    void shouldBeInvalidWhenDisabled() {
        ChannelConfig config = ChannelConfig.slack("https://hooks.slack.com/test")
            .enabled(false)
            .build();

        assertThat(config.isEnabled()).isFalse();
        assertThat(config.isValid()).isTrue(); 
    }

    @Test
    void shouldBeInvalidWithMissingWebhook() {
        ChannelConfig config = ChannelConfig.builder()
            .channelType(ChannelType.SLACK)
            .enabled(true)
            .build();

        assertThat(config.isValid()).isFalse();
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        ChannelConfig c1 = ChannelConfig.slack("https://hooks.slack.com/test").build();
        ChannelConfig c2 = ChannelConfig.slack("https://hooks.slack.com/test").build();
        ChannelConfig c3 = ChannelConfig.slack("https://hooks.slack.com/other").build();

        assertThat(c1).isEqualTo(c2);
        assertThat(c1.hashCode()).isEqualTo(c2.hashCode());
        assertThat(c1).isNotEqualTo(c3);
    }
}

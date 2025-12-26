package io.github.mohmk10.changeloghub.notification.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChannelTypeTest {

    @Test
    void shouldHaveCorrectDisplayNames() {
        assertThat(ChannelType.SLACK.getDisplayName()).isEqualTo("Slack");
        assertThat(ChannelType.DISCORD.getDisplayName()).isEqualTo("Discord");
        assertThat(ChannelType.EMAIL.getDisplayName()).isEqualTo("Email");
        assertThat(ChannelType.TEAMS.getDisplayName()).isEqualTo("Microsoft Teams");
        assertThat(ChannelType.WEBHOOK.getDisplayName()).isEqualTo("Webhook");
    }

    @Test
    void shouldHaveCorrectConfigKeys() {
        assertThat(ChannelType.SLACK.getConfigKey()).isEqualTo("slack");
        assertThat(ChannelType.DISCORD.getConfigKey()).isEqualTo("discord");
        assertThat(ChannelType.EMAIL.getConfigKey()).isEqualTo("email");
        assertThat(ChannelType.TEAMS.getConfigKey()).isEqualTo("teams");
        assertThat(ChannelType.WEBHOOK.getConfigKey()).isEqualTo("webhook");
    }

    @Test
    void shouldParseFromConfigKey() {
        assertThat(ChannelType.fromConfigKey("slack")).isEqualTo(ChannelType.SLACK);
        assertThat(ChannelType.fromConfigKey("DISCORD")).isEqualTo(ChannelType.DISCORD);
        assertThat(ChannelType.fromConfigKey("Email")).isEqualTo(ChannelType.EMAIL);
        assertThat(ChannelType.fromConfigKey("unknown")).isNull();
        assertThat(ChannelType.fromConfigKey(null)).isNull();
    }

    @Test
    void shouldIdentifyWebhookBasedChannels() {
        assertThat(ChannelType.SLACK.isWebhookBased()).isTrue();
        assertThat(ChannelType.DISCORD.isWebhookBased()).isTrue();
        assertThat(ChannelType.TEAMS.isWebhookBased()).isTrue();
        assertThat(ChannelType.WEBHOOK.isWebhookBased()).isTrue();
        assertThat(ChannelType.EMAIL.isWebhookBased()).isFalse();
    }

    @Test
    void shouldIdentifyRichFormattingSupport() {
        assertThat(ChannelType.SLACK.supportsRichFormatting()).isTrue();
        assertThat(ChannelType.DISCORD.supportsRichFormatting()).isTrue();
        assertThat(ChannelType.TEAMS.supportsRichFormatting()).isTrue();
        assertThat(ChannelType.EMAIL.supportsRichFormatting()).isTrue();
        assertThat(ChannelType.WEBHOOK.supportsRichFormatting()).isFalse();
    }
}

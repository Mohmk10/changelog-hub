package io.github.mohmk10.changeloghub.notification.channel;

import com.slack.api.Slack;
import com.slack.api.webhook.Payload;
import com.slack.api.webhook.WebhookResponse;
import io.github.mohmk10.changeloghub.notification.exception.NotificationException;
import io.github.mohmk10.changeloghub.notification.formatter.SlackMessageFormatter;
import io.github.mohmk10.changeloghub.notification.model.ChannelConfig;
import io.github.mohmk10.changeloghub.notification.model.Notification;
import io.github.mohmk10.changeloghub.notification.model.NotificationResult;
import io.github.mohmk10.changeloghub.notification.util.ChannelType;

import java.io.IOException;

public class SlackNotifier extends AbstractNotificationChannel {

    private final Slack slack;

    public SlackNotifier() {
        this.slack = Slack.getInstance();
        this.formatter = new SlackMessageFormatter();
    }

    public SlackNotifier(ChannelConfig config) {
        super(config);
        this.slack = Slack.getInstance();
        this.formatter = new SlackMessageFormatter();
    }

    @Override
    public ChannelType getType() {
        return ChannelType.SLACK;
    }

    @Override
    protected NotificationResult doSend(Notification notification, String formattedMessage) {
        String webhookUrl = config.getWebhookUrl();

        try {
            Payload.PayloadBuilder payloadBuilder = Payload.builder()
                .text(formattedMessage);

            if (config.getChannel() != null && !config.getChannel().isEmpty()) {
                payloadBuilder.channel(config.getChannel());
            }

            if (!notification.getMentions().isEmpty()) {
                String mentionsText = String.join(" ", notification.getMentions());
                payloadBuilder.text(mentionsText + "\n" + formattedMessage);
            }

            Payload payload = payloadBuilder.build();

            WebhookResponse response = slack.send(webhookUrl, payload);

            if (response.getCode() == 200) {
                logger.info("Slack notification sent successfully");
                return NotificationResult.success(notification.getId(), getType());
            } else {
                logger.warn("Slack API returned error: {} - {}",
                    response.getCode(), response.getMessage());
                return NotificationResult.httpFailure(
                    notification.getId(),
                    getType(),
                    response.getCode(),
                    response.getMessage()
                );
            }

        } catch (IOException e) {
            logger.error("Failed to send Slack notification: {}", e.getMessage());
            throw NotificationException.sendFailed(getType(), e);
        }
    }

    @Override
    protected void validateConfiguration() {
        super.validateConfiguration();
        if (!config.hasWebhookUrl() && !config.hasApiToken()) {
            throw NotificationException.invalidConfiguration(getType(),
                "Either webhook URL or API token must be configured");
        }
    }

    @Override
    public boolean testConnection() {
        if (!isConfigured()) {
            return false;
        }

        try {
            
            Payload testPayload = Payload.builder()
                .text("ChangelogHub connection test")
                .build();

            WebhookResponse response = slack.send(config.getWebhookUrl(), testPayload);
            return response.getCode() == 200;

        } catch (Exception e) {
            logger.warn("Slack connection test failed: {}", e.getMessage());
            return false;
        }
    }

    public NotificationResult sendWithBlocks(Notification notification, String blocksJson) {
        try {
            WebhookResponse response = slack.send(config.getWebhookUrl(), blocksJson);

            if (response.getCode() == 200) {
                return NotificationResult.success(notification.getId(), getType());
            } else {
                return NotificationResult.httpFailure(
                    notification.getId(),
                    getType(),
                    response.getCode(),
                    response.getMessage()
                );
            }

        } catch (IOException e) {
            throw NotificationException.sendFailed(getType(), e);
        }
    }

    public static SlackNotifier create(String webhookUrl) {
        ChannelConfig config = ChannelConfig.slack(webhookUrl).build();
        return new SlackNotifier(config);
    }

    public static SlackNotifier create(String webhookUrl, String channel) {
        ChannelConfig config = ChannelConfig.slack(webhookUrl)
            .channel(channel)
            .build();
        return new SlackNotifier(config);
    }
}

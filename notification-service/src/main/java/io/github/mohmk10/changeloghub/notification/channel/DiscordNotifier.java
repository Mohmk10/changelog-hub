package io.github.mohmk10.changeloghub.notification.channel;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mohmk10.changeloghub.notification.exception.NotificationException;
import io.github.mohmk10.changeloghub.notification.formatter.DiscordMessageFormatter;
import io.github.mohmk10.changeloghub.notification.model.ChannelConfig;
import io.github.mohmk10.changeloghub.notification.model.Notification;
import io.github.mohmk10.changeloghub.notification.model.NotificationResult;
import io.github.mohmk10.changeloghub.notification.util.ChannelType;
import io.github.mohmk10.changeloghub.notification.util.NotificationConstants;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.Timeout;

import java.util.HashMap;
import java.util.Map;

public class DiscordNotifier extends AbstractNotificationChannel {

    private final ObjectMapper objectMapper;
    private CloseableHttpClient httpClient;

    public DiscordNotifier() {
        this.objectMapper = new ObjectMapper();
        this.formatter = new DiscordMessageFormatter();
        initHttpClient();
    }

    public DiscordNotifier(ChannelConfig config) {
        super(config);
        this.objectMapper = new ObjectMapper();
        this.formatter = new DiscordMessageFormatter();
        initHttpClient();
    }

    private void initHttpClient() {
        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(Timeout.ofMilliseconds(getTimeout()))
            .setResponseTimeout(Timeout.ofMilliseconds(getTimeout()))
            .build();

        this.httpClient = HttpClients.custom()
            .setDefaultRequestConfig(requestConfig)
            .build();
    }

    @Override
    public ChannelType getType() {
        return ChannelType.DISCORD;
    }

    @Override
    protected NotificationResult doSend(Notification notification, String formattedMessage) {
        String webhookUrl = config.getWebhookUrl();

        try {
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("content", null); 

            if (formatter instanceof DiscordMessageFormatter) {
                String embedsJson = formattedMessage;
                
                Map<String, Object> formattedPayload = objectMapper.readValue(embedsJson, Map.class);
                payload.putAll(formattedPayload);
            } else {
                payload.put("content", formattedMessage);
            }

            String jsonPayload = objectMapper.writeValueAsString(payload);

            HttpPost httpPost = new HttpPost(webhookUrl);
            httpPost.setHeader(NotificationConstants.HEADER_CONTENT_TYPE,
                NotificationConstants.CONTENT_TYPE_JSON);
            httpPost.setHeader(NotificationConstants.HEADER_USER_AGENT,
                NotificationConstants.USER_AGENT_VALUE);
            httpPost.setEntity(new StringEntity(jsonPayload, ContentType.APPLICATION_JSON));

            return httpClient.execute(httpPost, response -> {
                int statusCode = response.getCode();
                String responseBody = response.getEntity() != null
                    ? EntityUtils.toString(response.getEntity())
                    : "";

                if (statusCode >= 200 && statusCode < 300) {
                    logger.info("Discord notification sent successfully");
                    return NotificationResult.success(notification.getId(), getType());
                } else if (statusCode == 429) {
                    logger.warn("Discord rate limit hit");
                    throw NotificationException.rateLimited(getType());
                } else {
                    logger.warn("Discord API error: {} - {}", statusCode, responseBody);
                    return NotificationResult.httpFailure(
                        notification.getId(),
                        getType(),
                        statusCode,
                        responseBody
                    );
                }
            });

        } catch (NotificationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to send Discord notification: {}", e.getMessage());
            throw NotificationException.sendFailed(getType(), e);
        }
    }

    @Override
    protected void validateConfiguration() {
        super.validateConfiguration();
        if (!config.hasWebhookUrl()) {
            throw NotificationException.invalidConfiguration(getType(),
                "Webhook URL is required");
        }
    }

    @Override
    public boolean testConnection() {
        if (!isConfigured()) {
            return false;
        }

        try {
            Map<String, Object> testPayload = new HashMap<>();
            testPayload.put("content", "ChangelogHub connection test");

            HttpPost httpPost = new HttpPost(config.getWebhookUrl());
            httpPost.setHeader(NotificationConstants.HEADER_CONTENT_TYPE,
                NotificationConstants.CONTENT_TYPE_JSON);
            httpPost.setEntity(new StringEntity(
                objectMapper.writeValueAsString(testPayload),
                ContentType.APPLICATION_JSON));

            return httpClient.execute(httpPost, response -> response.getCode() == 204);

        } catch (Exception e) {
            logger.warn("Discord connection test failed: {}", e.getMessage());
            return false;
        }
    }

    public NotificationResult sendWithEmbeds(Notification notification, String embedsJson) {
        try {
            HttpPost httpPost = new HttpPost(config.getWebhookUrl());
            httpPost.setHeader(NotificationConstants.HEADER_CONTENT_TYPE,
                NotificationConstants.CONTENT_TYPE_JSON);
            httpPost.setEntity(new StringEntity(embedsJson, ContentType.APPLICATION_JSON));

            return httpClient.execute(httpPost, response -> {
                int statusCode = response.getCode();

                if (statusCode >= 200 && statusCode < 300) {
                    return NotificationResult.success(notification.getId(), getType());
                } else {
                    String responseBody = response.getEntity() != null
                        ? EntityUtils.toString(response.getEntity())
                        : "";
                    return NotificationResult.httpFailure(
                        notification.getId(),
                        getType(),
                        statusCode,
                        responseBody
                    );
                }
            });

        } catch (Exception e) {
            throw NotificationException.sendFailed(getType(), e);
        }
    }

    public static DiscordNotifier create(String webhookUrl) {
        ChannelConfig config = ChannelConfig.discord(webhookUrl).build();
        return new DiscordNotifier(config);
    }

    void setHttpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }
}

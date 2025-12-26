package io.github.mohmk10.changeloghub.notification.channel;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mohmk10.changeloghub.notification.exception.NotificationException;
import io.github.mohmk10.changeloghub.notification.formatter.TeamsMessageFormatter;
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

public class TeamsNotifier extends AbstractNotificationChannel {

    private final ObjectMapper objectMapper;
    private CloseableHttpClient httpClient;

    public TeamsNotifier() {
        this.objectMapper = new ObjectMapper();
        this.formatter = new TeamsMessageFormatter();
        initHttpClient();
    }

    public TeamsNotifier(ChannelConfig config) {
        super(config);
        this.objectMapper = new ObjectMapper();
        this.formatter = new TeamsMessageFormatter();
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
        return ChannelType.TEAMS;
    }

    @Override
    protected NotificationResult doSend(Notification notification, String formattedMessage) {
        String webhookUrl = config.getWebhookUrl();

        try {
            HttpPost httpPost = new HttpPost(webhookUrl);
            httpPost.setHeader(NotificationConstants.HEADER_CONTENT_TYPE,
                NotificationConstants.CONTENT_TYPE_JSON);
            httpPost.setHeader(NotificationConstants.HEADER_USER_AGENT,
                NotificationConstants.USER_AGENT_VALUE);
            httpPost.setEntity(new StringEntity(formattedMessage, ContentType.APPLICATION_JSON));

            return httpClient.execute(httpPost, response -> {
                int statusCode = response.getCode();
                String responseBody = response.getEntity() != null
                    ? EntityUtils.toString(response.getEntity())
                    : "";

                if (statusCode == 200) {
                    logger.info("Teams notification sent successfully");
                    return NotificationResult.success(notification.getId(), getType());
                } else if (statusCode == 429) {
                    logger.warn("Teams rate limit hit");
                    throw NotificationException.rateLimited(getType());
                } else {
                    logger.warn("Teams API error: {} - {}", statusCode, responseBody);
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
            logger.error("Failed to send Teams notification: {}", e.getMessage());
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
            
            String testPayload = """
                {
                    "@type": "MessageCard",
                    "@context": "http:
                    "summary": "ChangelogHub Connection Test",
                    "text": "This is a connection test from ChangelogHub"
                }
                """;

            HttpPost httpPost = new HttpPost(config.getWebhookUrl());
            httpPost.setHeader(NotificationConstants.HEADER_CONTENT_TYPE,
                NotificationConstants.CONTENT_TYPE_JSON);
            httpPost.setEntity(new StringEntity(testPayload, ContentType.APPLICATION_JSON));

            return httpClient.execute(httpPost, response -> response.getCode() == 200);

        } catch (Exception e) {
            logger.warn("Teams connection test failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Send with Adaptive Card.
     */
    public NotificationResult sendWithAdaptiveCard(Notification notification, String cardJson) {
        try {
            HttpPost httpPost = new HttpPost(config.getWebhookUrl());
            httpPost.setHeader(NotificationConstants.HEADER_CONTENT_TYPE,
                NotificationConstants.CONTENT_TYPE_JSON);
            httpPost.setEntity(new StringEntity(cardJson, ContentType.APPLICATION_JSON));

            return httpClient.execute(httpPost, response -> {
                int statusCode = response.getCode();

                if (statusCode == 200) {
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

    /**
     * Create a configured TeamsNotifier.
     */
    public static TeamsNotifier create(String webhookUrl) {
        ChannelConfig config = ChannelConfig.teams(webhookUrl).build();
        return new TeamsNotifier(config);
    }

    // Allow setting a custom HTTP client for testing
    void setHttpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }
}

package io.github.mohmk10.changeloghub.notification.channel;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mohmk10.changeloghub.notification.exception.NotificationException;
import io.github.mohmk10.changeloghub.notification.formatter.WebhookMessageFormatter;
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

import java.util.Map;

public class WebhookNotifier extends AbstractNotificationChannel {

    private final ObjectMapper objectMapper;
    private CloseableHttpClient httpClient;

    public WebhookNotifier() {
        this.objectMapper = new ObjectMapper();
        this.formatter = new WebhookMessageFormatter();
        initHttpClient();
    }

    public WebhookNotifier(ChannelConfig config) {
        super(config);
        this.objectMapper = new ObjectMapper();
        this.formatter = new WebhookMessageFormatter();
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
        return ChannelType.WEBHOOK;
    }

    @Override
    protected NotificationResult doSend(Notification notification, String formattedMessage) {
        String webhookUrl = config.getWebhookUrl();
        int retryCount = 0;
        int maxRetries = getRetryCount();
        Exception lastException = null;

        while (retryCount <= maxRetries) {
            try {
                NotificationResult result = sendRequest(notification, webhookUrl, formattedMessage);

                if (result.isSuccess()) {
                    return NotificationResult.builder()
                        .notificationId(result.getNotificationId())
                        .channelType(result.getChannelType())
                        .success(true)
                        .messageId(result.getMessageId().orElse(null))
                        .retryCount(retryCount)
                        .build();
                }

                int statusCode = result.getHttpStatusCode();
                if (statusCode == 429 || statusCode >= 500) {
                    retryCount++;
                    if (retryCount <= maxRetries) {
                        logger.warn("Webhook request failed with {}, retrying ({}/{})",
                            statusCode, retryCount, maxRetries);
                        Thread.sleep(NotificationConstants.DEFAULT_RETRY_DELAY_MS * retryCount);
                        continue;
                    }
                }

                return result;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw NotificationException.sendFailed(getType(), e);
            } catch (NotificationException e) {
                if (e.isRetryable() && retryCount < maxRetries) {
                    retryCount++;
                    lastException = e;
                    try {
                        Thread.sleep(NotificationConstants.DEFAULT_RETRY_DELAY_MS * retryCount);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw e;
                    }
                } else {
                    throw e;
                }
            }
        }

        throw NotificationException.sendFailed(getType(),
            lastException != null ? lastException : new Exception("Max retries exceeded"));
    }

    private NotificationResult sendRequest(Notification notification, String webhookUrl,
                                           String payload) {
        try {
            HttpPost httpPost = new HttpPost(webhookUrl);

            httpPost.setHeader(NotificationConstants.HEADER_CONTENT_TYPE,
                NotificationConstants.CONTENT_TYPE_JSON);
            httpPost.setHeader(NotificationConstants.HEADER_USER_AGENT,
                NotificationConstants.USER_AGENT_VALUE);

            for (Map.Entry<String, String> header : config.getHeaders().entrySet()) {
                httpPost.setHeader(header.getKey(), header.getValue());
            }

            if (config.hasAuth()) {
                String authHeader = config.getAuthType() + " " + config.getAuthValue();
                httpPost.setHeader(NotificationConstants.HEADER_AUTHORIZATION, authHeader);
            }

            httpPost.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));

            return httpClient.execute(httpPost, response -> {
                int statusCode = response.getCode();
                String responseBody = response.getEntity() != null
                    ? EntityUtils.toString(response.getEntity())
                    : "";

                if (statusCode >= 200 && statusCode < 300) {
                    logger.info("Webhook notification sent successfully to {}",
                        maskUrl(webhookUrl));
                    return NotificationResult.builder()
                        .notificationId(notification.getId())
                        .channelType(getType())
                        .success(true)
                        .httpStatusCode(statusCode)
                        .build();
                } else if (statusCode == 401 || statusCode == 403) {
                    throw NotificationException.authenticationFailed(getType());
                } else if (statusCode == 429) {
                    throw NotificationException.rateLimited(getType());
                } else {
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
            throw NotificationException.sendFailed(getType(), e);
        }
    }

    private String maskUrl(String url) {
        if (url == null || url.length() < 20) {
            return "***";
        }
        return url.substring(0, 20) + "...";
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
            HttpPost httpPost = new HttpPost(config.getWebhookUrl());
            httpPost.setHeader(NotificationConstants.HEADER_CONTENT_TYPE,
                NotificationConstants.CONTENT_TYPE_JSON);

            if (config.hasAuth()) {
                String authHeader = config.getAuthType() + " " + config.getAuthValue();
                httpPost.setHeader(NotificationConstants.HEADER_AUTHORIZATION, authHeader);
            }

            httpPost.setEntity(new StringEntity("{}", ContentType.APPLICATION_JSON));

            return httpClient.execute(httpPost, response -> {
                int code = response.getCode();
                
                return code >= 200 && code < 500;
            });

        } catch (Exception e) {
            logger.warn("Webhook connection test failed: {}", e.getMessage());
            return false;
        }
    }

    public static WebhookNotifier create(String webhookUrl) {
        ChannelConfig config = ChannelConfig.webhook(webhookUrl).build();
        return new WebhookNotifier(config);
    }

    public static WebhookNotifier createWithBearerAuth(String webhookUrl, String token) {
        ChannelConfig config = ChannelConfig.webhook(webhookUrl)
            .bearerAuth(token)
            .build();
        return new WebhookNotifier(config);
    }

    public static WebhookNotifier createWithBasicAuth(String webhookUrl, String username, String password) {
        ChannelConfig config = ChannelConfig.webhook(webhookUrl)
            .basicAuth(username, password)
            .build();
        return new WebhookNotifier(config);
    }

    void setHttpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }
}

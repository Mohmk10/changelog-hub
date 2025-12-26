package io.github.mohmk10.changeloghub.notification.model;

import io.github.mohmk10.changeloghub.notification.util.ChannelType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ChannelConfig {

    private final ChannelType channelType;
    private final boolean enabled;
    private final String webhookUrl;
    private final String apiToken;
    private final SmtpConfig smtpConfig;
    private final Map<String, String> headers;
    private final String authType;
    private final String authValue;
    private final int timeoutMs;
    private final int retryCount;
    private final String channel; 

    private ChannelConfig(Builder builder) {
        this.channelType = builder.channelType;
        this.enabled = builder.enabled;
        this.webhookUrl = builder.webhookUrl;
        this.apiToken = builder.apiToken;
        this.smtpConfig = builder.smtpConfig;
        this.headers = builder.headers != null
            ? new HashMap<>(builder.headers)
            : new HashMap<>();
        this.authType = builder.authType;
        this.authValue = builder.authValue;
        this.timeoutMs = builder.timeoutMs > 0 ? builder.timeoutMs : 30000;
        this.retryCount = builder.retryCount >= 0 ? builder.retryCount : 3;
        this.channel = builder.channel;
    }

    public ChannelType getChannelType() {
        return channelType;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public String getApiToken() {
        return apiToken;
    }

    public SmtpConfig getSmtpConfig() {
        return smtpConfig;
    }

    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    public String getAuthType() {
        return authType;
    }

    public String getAuthValue() {
        return authValue;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public String getChannel() {
        return channel;
    }

    public boolean hasWebhookUrl() {
        return webhookUrl != null && !webhookUrl.isEmpty();
    }

    public boolean hasApiToken() {
        return apiToken != null && !apiToken.isEmpty();
    }

    public boolean hasSmtpConfig() {
        return smtpConfig != null && smtpConfig.isValid();
    }

    public boolean hasAuth() {
        return authType != null && authValue != null;
    }

    public boolean isValid() {
        if (!enabled) return true;

        return switch (channelType) {
            case SLACK -> hasWebhookUrl() || hasApiToken();
            case DISCORD, TEAMS, WEBHOOK -> hasWebhookUrl();
            case EMAIL -> hasSmtpConfig();
            case null -> false;
        };
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder slack(String webhookUrl) {
        return builder()
            .channelType(ChannelType.SLACK)
            .webhookUrl(webhookUrl)
            .enabled(true);
    }

    public static Builder discord(String webhookUrl) {
        return builder()
            .channelType(ChannelType.DISCORD)
            .webhookUrl(webhookUrl)
            .enabled(true);
    }

    public static Builder teams(String webhookUrl) {
        return builder()
            .channelType(ChannelType.TEAMS)
            .webhookUrl(webhookUrl)
            .enabled(true);
    }

    public static Builder webhook(String webhookUrl) {
        return builder()
            .channelType(ChannelType.WEBHOOK)
            .webhookUrl(webhookUrl)
            .enabled(true);
    }

    public static Builder email(SmtpConfig smtpConfig) {
        return builder()
            .channelType(ChannelType.EMAIL)
            .smtpConfig(smtpConfig)
            .enabled(true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChannelConfig that = (ChannelConfig) o;
        return channelType == that.channelType &&
               Objects.equals(webhookUrl, that.webhookUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channelType, webhookUrl);
    }

    @Override
    public String toString() {
        return "ChannelConfig{" +
               "channelType=" + channelType +
               ", enabled=" + enabled +
               ", hasWebhook=" + hasWebhookUrl() +
               ", hasToken=" + hasApiToken() +
               '}';
    }

    public static class SmtpConfig {
        private final String host;
        private final int port;
        private final String username;
        private final String password;
        private final boolean ssl;
        private final boolean startTls;
        private final String fromAddress;
        private final String fromName;

        private SmtpConfig(SmtpBuilder builder) {
            this.host = builder.host;
            this.port = builder.port > 0 ? builder.port : 587;
            this.username = builder.username;
            this.password = builder.password;
            this.ssl = builder.ssl;
            this.startTls = builder.startTls;
            this.fromAddress = builder.fromAddress;
            this.fromName = builder.fromName;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public boolean isSsl() {
            return ssl;
        }

        public boolean isStartTls() {
            return startTls;
        }

        public String getFromAddress() {
            return fromAddress;
        }

        public String getFromName() {
            return fromName;
        }

        public boolean isValid() {
            return host != null && !host.isEmpty() &&
                   fromAddress != null && !fromAddress.isEmpty();
        }

        public boolean hasAuth() {
            return username != null && !username.isEmpty() &&
                   password != null && !password.isEmpty();
        }

        public static SmtpBuilder builder() {
            return new SmtpBuilder();
        }

        public static class SmtpBuilder {
            private String host;
            private int port = 587;
            private String username;
            private String password;
            private boolean ssl;
            private boolean startTls = true;
            private String fromAddress;
            private String fromName;

            public SmtpBuilder host(String host) {
                this.host = host;
                return this;
            }

            public SmtpBuilder port(int port) {
                this.port = port;
                return this;
            }

            public SmtpBuilder username(String username) {
                this.username = username;
                return this;
            }

            public SmtpBuilder password(String password) {
                this.password = password;
                return this;
            }

            public SmtpBuilder ssl(boolean ssl) {
                this.ssl = ssl;
                return this;
            }

            public SmtpBuilder startTls(boolean startTls) {
                this.startTls = startTls;
                return this;
            }

            public SmtpBuilder fromAddress(String fromAddress) {
                this.fromAddress = fromAddress;
                return this;
            }

            public SmtpBuilder fromName(String fromName) {
                this.fromName = fromName;
                return this;
            }

            public SmtpConfig build() {
                return new SmtpConfig(this);
            }
        }
    }

    public static class Builder {
        private ChannelType channelType;
        private boolean enabled = true;
        private String webhookUrl;
        private String apiToken;
        private SmtpConfig smtpConfig;
        private Map<String, String> headers;
        private String authType;
        private String authValue;
        private int timeoutMs = 30000;
        private int retryCount = 3;
        private String channel;

        public Builder channelType(ChannelType channelType) {
            this.channelType = channelType;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder webhookUrl(String webhookUrl) {
            this.webhookUrl = webhookUrl;
            return this;
        }

        public Builder apiToken(String apiToken) {
            this.apiToken = apiToken;
            return this;
        }

        public Builder smtpConfig(SmtpConfig smtpConfig) {
            this.smtpConfig = smtpConfig;
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder header(String name, String value) {
            if (this.headers == null) {
                this.headers = new HashMap<>();
            }
            this.headers.put(name, value);
            return this;
        }

        public Builder authType(String authType) {
            this.authType = authType;
            return this;
        }

        public Builder authValue(String authValue) {
            this.authValue = authValue;
            return this;
        }

        public Builder bearerAuth(String token) {
            this.authType = "Bearer";
            this.authValue = token;
            return this;
        }

        public Builder basicAuth(String username, String password) {
            this.authType = "Basic";
            this.authValue = java.util.Base64.getEncoder()
                .encodeToString((username + ":" + password).getBytes());
            return this;
        }

        public Builder timeoutMs(int timeoutMs) {
            this.timeoutMs = timeoutMs;
            return this;
        }

        public Builder retryCount(int retryCount) {
            this.retryCount = retryCount;
            return this;
        }

        public Builder channel(String channel) {
            this.channel = channel;
            return this;
        }

        public ChannelConfig build() {
            return new ChannelConfig(this);
        }
    }
}

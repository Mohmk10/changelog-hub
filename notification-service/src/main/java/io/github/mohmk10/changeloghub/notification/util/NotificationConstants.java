package io.github.mohmk10.changeloghub.notification.util;

public final class NotificationConstants {

    private NotificationConstants() {
        
    }

    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_HTML = "text/html";
    public static final String CHARSET_UTF8 = "UTF-8";
    public static final int DEFAULT_TIMEOUT_MS = 30000;
    public static final int DEFAULT_RETRY_COUNT = 3;
    public static final int DEFAULT_RETRY_DELAY_MS = 1000;

    public static final String SLACK_API_URL = "https://slack.com/api/";
    public static final String SLACK_WEBHOOK_PATH = "/services/";
    public static final int SLACK_MAX_BLOCKS = 50;
    public static final int SLACK_MAX_TEXT_LENGTH = 3000;

    public static final String DISCORD_WEBHOOK_URL_PREFIX = "https://discord.com/api/webhooks/";
    public static final int DISCORD_MAX_EMBEDS = 10;
    public static final int DISCORD_MAX_EMBED_FIELDS = 25;
    public static final int DISCORD_MAX_FIELD_VALUE_LENGTH = 1024;

    public static final String TEAMS_WEBHOOK_URL_PATTERN = "https://.*\\.webhook\\.office\\.com/.*";
    public static final int TEAMS_MAX_CARD_SIZE = 28000;

    public static final String EMAIL_MIME_TYPE_HTML = "text/html; charset=UTF-8";
    public static final int EMAIL_MAX_RECIPIENTS = 100;
    public static final String EMAIL_DEFAULT_FROM = "changelog-hub@noreply.local";

    public static final int DEFAULT_RATE_LIMIT_PER_MINUTE = 10;
    public static final int DEFAULT_RATE_LIMIT_PER_HOUR = 100;
    public static final long RATE_LIMIT_WINDOW_MS = 60000;

    public static final String COLOR_BREAKING = "#dc3545";
    public static final String COLOR_DANGEROUS = "#fd7e14";
    public static final String COLOR_WARNING = "#ffc107";
    public static final String COLOR_INFO = "#28a745";
    public static final String COLOR_SUCCESS = "#198754";

    public static final int DISCORD_COLOR_BREAKING = 14423829; 
    public static final int DISCORD_COLOR_DANGEROUS = 16613908; 
    public static final int DISCORD_COLOR_WARNING = 16760071; 
    public static final int DISCORD_COLOR_INFO = 2664261; 

    public static final String PLACEHOLDER_API_NAME = "${apiName}";
    public static final String PLACEHOLDER_FROM_VERSION = "${fromVersion}";
    public static final String PLACEHOLDER_TO_VERSION = "${toVersion}";
    public static final String PLACEHOLDER_BREAKING_COUNT = "${breakingCount}";
    public static final String PLACEHOLDER_TOTAL_CHANGES = "${totalChanges}";
    public static final String PLACEHOLDER_RISK_SCORE = "${riskScore}";
    public static final String PLACEHOLDER_TIMESTAMP = "${timestamp}";
    public static final String PLACEHOLDER_EVENT_TYPE = "${eventType}";
    public static final String PLACEHOLDER_CHANGES_LIST = "${changesList}";
    public static final String PLACEHOLDER_REPORT_URL = "${reportUrl}";

    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_USER_AGENT = "User-Agent";
    public static final String USER_AGENT_VALUE = "ChangelogHub-NotificationService/1.0";

    public static final String AUTH_BEARER = "Bearer ";
    public static final String AUTH_BASIC = "Basic ";

    public static final String JSON_TEXT = "text";
    public static final String JSON_BLOCKS = "blocks";
    public static final String JSON_ATTACHMENTS = "attachments";
    public static final String JSON_EMBEDS = "embeds";
    public static final String JSON_CONTENT = "content";
    public static final String JSON_TYPE = "type";
    public static final String JSON_TITLE = "title";
    public static final String JSON_DESCRIPTION = "description";
    public static final String JSON_COLOR = "color";
    public static final String JSON_FIELDS = "fields";
}

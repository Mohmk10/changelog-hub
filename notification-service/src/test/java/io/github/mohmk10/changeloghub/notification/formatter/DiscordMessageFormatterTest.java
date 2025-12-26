package io.github.mohmk10.changeloghub.notification.formatter;

import io.github.mohmk10.changeloghub.core.model.*;
import io.github.mohmk10.changeloghub.notification.model.Notification;
import io.github.mohmk10.changeloghub.notification.util.NotificationConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DiscordMessageFormatterTest {

    private DiscordMessageFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new DiscordMessageFormatter();
    }

    @Test
    void shouldFormatNotification() {
        Notification notification = Notification.builder()
            .title("Test Title")
            .message("Test Message")
            .severity(Severity.WARNING)
            .build();

        String result = formatter.format(notification);

        assertThat(result).contains("Test Title");
        assertThat(result).contains("Test Message");
        assertThat(result).contains("\"embeds\"");
    }

    @Test
    void shouldFormatChangelog() {
        Changelog changelog = Changelog.builder()
            .apiName("Test API")
            .fromVersion("1.0.0")
            .toVersion("2.0.0")
            .build();

        String result = formatter.format(changelog);

        assertThat(result).contains("Test API");
        assertThat(result).contains("1.0.0");
        assertThat(result).contains("2.0.0");
        assertThat(result).contains("\"embeds\"");
    }

    @Test
    void shouldIncludeBreakingChangesAsFields() {
        Changelog changelog = Changelog.builder()
            .apiName("API")
            .addBreakingChange(BreakingChange.breakingChangeBuilder()
                .description("Removed endpoint /api/v1/users")
                .build())
            .build();

        String result = formatter.format(changelog);

        assertThat(result).contains("fields");
        assertThat(result).contains("Breaking");
        assertThat(result).contains("Removed endpoint");
    }

    @Test
    void shouldUseBreakingColorForBreakingChanges() {
        Changelog changelog = Changelog.builder()
            .apiName("API")
            .addBreakingChange(BreakingChange.breakingChangeBuilder()
                .description("Breaking")
                .build())
            .build();

        String result = formatter.format(changelog);

        assertThat(result).contains(String.valueOf(NotificationConstants.DISCORD_COLOR_BREAKING));
    }

    @Test
    void shouldUseInfoColorForNonBreakingChanges() {
        Changelog changelog = Changelog.builder()
            .apiName("API")
            .addChange(Change.builder()
                .description("Info change")
                .severity(Severity.INFO)
                .type(ChangeType.ADDED)
                .build())
            .build();

        String result = formatter.format(changelog);

        assertThat(result).contains(String.valueOf(NotificationConstants.DISCORD_COLOR_INFO));
    }

    @Test
    void shouldIncludeTimestamp() {
        Changelog changelog = Changelog.builder()
            .apiName("API")
            .build();

        String result = formatter.format(changelog);

        assertThat(result).contains("\"timestamp\"");
    }

    @Test
    void shouldIncludeFooter() {
        Notification notification = Notification.builder()
            .title("Test")
            .build();

        String result = formatter.format(notification);

        assertThat(result).contains("\"footer\"");
        assertThat(result).contains("ChangelogHub");
    }

    @Test
    void shouldReturnJsonContentType() {
        assertThat(formatter.getContentType()).isEqualTo(NotificationConstants.CONTENT_TYPE_JSON);
    }

    @Test
    void shouldTruncateLongFieldValues() {
        String longDescription = "A".repeat(2000);
        Changelog changelog = Changelog.builder()
            .apiName("API")
            .addBreakingChange(BreakingChange.breakingChangeBuilder()
                .description(longDescription)
                .build())
            .build();

        String result = formatter.format(changelog);

        // Should be truncated to max field value length
        assertThat(result.length()).isLessThan(longDescription.length() + 500);
    }
}

package io.github.mohmk10.changeloghub.notification.formatter;

import io.github.mohmk10.changeloghub.core.model.*;
import io.github.mohmk10.changeloghub.notification.model.Notification;
import io.github.mohmk10.changeloghub.notification.util.NotificationConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SlackMessageFormatterTest {

    private SlackMessageFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new SlackMessageFormatter();
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
        assertThat(result).contains("\"blocks\"");
        assertThat(result).contains("header");
    }

    @Test
    void shouldFormatChangelog() {
        Changelog changelog = createSampleChangelog();

        String result = formatter.format(changelog);

        assertThat(result).contains("Test API");
        assertThat(result).contains("1.0.0");
        assertThat(result).contains("2.0.0");
        assertThat(result).contains("\"blocks\"");
    }

    @Test
    void shouldIncludeBreakingChangesInChangelog() {
        Changelog changelog = Changelog.builder()
            .apiName("API")
            .addBreakingChange(BreakingChange.breakingChangeBuilder()
                .description("Removed endpoint")
                .migrationSuggestion("Use new endpoint")
                .build())
            .build();

        String result = formatter.format(changelog);

        assertThat(result).contains("Removed endpoint");
        assertThat(result).contains("Breaking");
    }

    @Test
    void shouldUseCorrectColorForBreakingChanges() {
        Changelog changelog = Changelog.builder()
            .apiName("API")
            .addBreakingChange(BreakingChange.breakingChangeBuilder()
                .description("Breaking change")
                .build())
            .build();

        String result = formatter.format(changelog);

        assertThat(result).contains(NotificationConstants.COLOR_BREAKING);
    }

    @Test
    void shouldUseCorrectColorForInfoChanges() {
        Changelog changelog = Changelog.builder()
            .apiName("API")
            .addChange(Change.builder()
                .description("Info change")
                .severity(Severity.INFO)
                .type(ChangeType.ADDED)
                .build())
            .build();

        String result = formatter.format(changelog);

        assertThat(result).contains(NotificationConstants.COLOR_INFO);
    }

    @Test
    void shouldReturnJsonContentType() {
        assertThat(formatter.getContentType()).isEqualTo(NotificationConstants.CONTENT_TYPE_JSON);
    }

    @Test
    void shouldEscapeJsonCharacters() {
        Notification notification = Notification.builder()
            .title("Test with \"quotes\" and\nnewline")
            .message("Tab\there")
            .build();

        String result = formatter.format(notification);

        assertThat(result).contains("\\\"quotes\\\"");
        assertThat(result).contains("\\n");
        assertThat(result).contains("\\t");
    }

    @Test
    void shouldLimitBreakingChangesDisplay() {
        Changelog.Builder builder = Changelog.builder().apiName("API");
        for (int i = 0; i < 10; i++) {
            builder.addBreakingChange(BreakingChange.breakingChangeBuilder()
                .description("Breaking change " + i)
                .build());
        }
        Changelog changelog = builder.build();

        String result = formatter.format(changelog);

        assertThat(result).contains("more");
    }

    private Changelog createSampleChangelog() {
        return Changelog.builder()
            .apiName("Test API")
            .fromVersion("1.0.0")
            .toVersion("2.0.0")
            .addChange(Change.builder()
                .description("Added feature")
                .type(ChangeType.ADDED)
                .severity(Severity.INFO)
                .build())
            .addBreakingChange(BreakingChange.breakingChangeBuilder()
                .description("Removed endpoint")
                .build())
            .build();
    }
}

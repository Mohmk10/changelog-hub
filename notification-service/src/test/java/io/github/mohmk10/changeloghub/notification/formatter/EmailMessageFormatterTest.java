package io.github.mohmk10.changeloghub.notification.formatter;

import io.github.mohmk10.changeloghub.core.model.*;
import io.github.mohmk10.changeloghub.notification.model.Notification;
import io.github.mohmk10.changeloghub.notification.util.NotificationConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailMessageFormatterTest {

    private EmailMessageFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new EmailMessageFormatter();
    }

    @Test
    void shouldFormatAsHtml() {
        Notification notification = Notification.builder()
            .title("Test Title")
            .message("Test Message")
            .build();

        String result = formatter.format(notification);

        assertThat(result).contains("<!DOCTYPE html>");
        assertThat(result).contains("<html>");
        assertThat(result).contains("</html>");
    }

    @Test
    void shouldIncludeTitleInHtml() {
        Notification notification = Notification.builder()
            .title("Important Notification")
            .message("Details here")
            .build();

        String result = formatter.format(notification);

        assertThat(result).contains("Important Notification");
    }

    @Test
    void shouldFormatChangelogWithVersions() {
        Changelog changelog = Changelog.builder()
            .apiName("Test API")
            .fromVersion("1.0.0")
            .toVersion("2.0.0")
            .build();

        String result = formatter.format(changelog);

        assertThat(result).contains("Test API");
        assertThat(result).contains("1.0.0");
        assertThat(result).contains("2.0.0");
    }

    @Test
    void shouldIncludeBreakingChangesSection() {
        Changelog changelog = Changelog.builder()
            .apiName("API")
            .addBreakingChange(BreakingChange.breakingChangeBuilder()
                .description("Removed endpoint")
                .migrationSuggestion("Use new endpoint")
                .build())
            .build();

        String result = formatter.format(changelog);

        assertThat(result).contains("Breaking Changes");
        assertThat(result).contains("Removed endpoint");
        assertThat(result).contains("Use new endpoint");
    }

    @Test
    void shouldIncludeDangerousChangesSection() {
        Changelog changelog = Changelog.builder()
            .apiName("API")
            .addChange(Change.builder()
                .description("Dangerous change")
                .type(ChangeType.MODIFIED)
                .severity(Severity.DANGEROUS)
                .build())
            .build();

        String result = formatter.format(changelog);

        assertThat(result).contains("Dangerous Changes");
        assertThat(result).contains("Dangerous change");
    }

    @Test
    void shouldIncludeWarningsSection() {
        Changelog changelog = Changelog.builder()
            .apiName("API")
            .addChange(Change.builder()
                .description("Warning change")
                .type(ChangeType.DEPRECATED)
                .severity(Severity.WARNING)
                .build())
            .build();

        String result = formatter.format(changelog);

        assertThat(result).contains("Warning");
    }

    @Test
    void shouldIncludeStyles() {
        Notification notification = Notification.builder()
            .title("Test")
            .build();

        String result = formatter.format(notification);

        assertThat(result).contains("<style>");
        assertThat(result).contains("</style>");
    }

    @Test
    void shouldIncludeSummaryStats() {
        Changelog changelog = Changelog.builder()
            .apiName("API")
            .addChange(Change.builder()
                .description("Change 1")
                .severity(Severity.INFO)
                .type(ChangeType.ADDED)
                .build())
            .addBreakingChange(BreakingChange.breakingChangeBuilder()
                .description("Breaking 1")
                .build())
            .build();

        String result = formatter.format(changelog);

        assertThat(result).contains("Total Changes");
        assertThat(result).contains("Breaking Changes");
    }

    @Test
    void shouldEscapeHtmlCharacters() {
        Notification notification = Notification.builder()
            .title("<script>alert('xss')</script>")
            .message("Test & verify < > \"quotes\"")
            .build();

        String result = formatter.format(notification);

        assertThat(result).contains("&lt;script&gt;");
        assertThat(result).contains("&amp;");
        assertThat(result).doesNotContain("<script>alert");
    }

    @Test
    void shouldReturnHtmlContentType() {
        assertThat(formatter.getContentType()).isEqualTo(NotificationConstants.CONTENT_TYPE_HTML);
    }

    @Test
    void shouldIncludeFooter() {
        Notification notification = Notification.builder()
            .title("Test")
            .build();

        String result = formatter.format(notification);

        assertThat(result).contains("ChangelogHub");
    }
}

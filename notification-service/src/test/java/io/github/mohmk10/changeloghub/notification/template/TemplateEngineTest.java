package io.github.mohmk10.changeloghub.notification.template;

import io.github.mohmk10.changeloghub.core.model.*;
import io.github.mohmk10.changeloghub.notification.model.Notification;
import io.github.mohmk10.changeloghub.notification.model.NotificationEvent;
import io.github.mohmk10.changeloghub.notification.util.EventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateEngineTest {

    private TemplateEngine engine;

    @BeforeEach
    void setUp() {
        engine = new TemplateEngine();
    }

    @Test
    void shouldReplaceVariables() {
        String template = "Hello ${name}, welcome to ${app}!";
        Map<String, Object> vars = new HashMap<>();
        vars.put("name", "User");
        vars.put("app", "ChangelogHub");

        String result = engine.process(template, vars);

        assertThat(result).isEqualTo("Hello User, welcome to ChangelogHub!");
    }

    @Test
    void shouldHandleMissingVariables() {
        String template = "Hello ${name}, your id is ${id}";
        Map<String, Object> vars = new HashMap<>();
        vars.put("name", "User");

        String result = engine.process(template, vars);

        assertThat(result).isEqualTo("Hello User, your id is ");
    }

    @Test
    void shouldProcessConditionalWhenTrue() {
        String template = "Changes: {{#if hasBreakingChanges}}BREAKING{{/if}}";
        Map<String, Object> vars = new HashMap<>();
        vars.put("hasBreakingChanges", true);

        String result = engine.process(template, vars);

        assertThat(result).isEqualTo("Changes: BREAKING");
    }

    @Test
    void shouldProcessConditionalWhenFalse() {
        String template = "Changes: {{#if hasBreakingChanges}}BREAKING{{/if}}";
        Map<String, Object> vars = new HashMap<>();
        vars.put("hasBreakingChanges", false);

        String result = engine.process(template, vars);

        assertThat(result).isEqualTo("Changes: ");
    }

    @Test
    void shouldProcessConditionalWithPositiveNumber() {
        String template = "{{#if count}}Count: ${count}{{/if}}";
        Map<String, Object> vars = new HashMap<>();
        vars.put("count", 5);

        String result = engine.process(template, vars);

        assertThat(result).isEqualTo("Count: 5");
    }

    @Test
    void shouldProcessConditionalWithZero() {
        String template = "{{#if count}}Count: ${count}{{/if}}";
        Map<String, Object> vars = new HashMap<>();
        vars.put("count", 0);

        String result = engine.process(template, vars);

        assertThat(result).isEqualTo("");
    }

    @Test
    void shouldProcessConditionalWithNonEmptyString() {
        String template = "{{#if name}}Name: ${name}{{/if}}";
        Map<String, Object> vars = new HashMap<>();
        vars.put("name", "Test");

        String result = engine.process(template, vars);

        assertThat(result).isEqualTo("Name: Test");
    }

    @Test
    void shouldProcessConditionalWithEmptyString() {
        String template = "{{#if name}}Name: ${name}{{/if}}";
        Map<String, Object> vars = new HashMap<>();
        vars.put("name", "");

        String result = engine.process(template, vars);

        assertThat(result).isEqualTo("");
    }

    @Test
    void shouldProcessNotification() {
        String template = "Event: ${eventType} for ${apiName} (v${fromVersion} -> v${toVersion})";

        NotificationEvent event = NotificationEvent.builder()
            .eventType(EventType.BREAKING_CHANGE_DETECTED)
            .apiName("Test API")
            .fromVersion("1.0.0")
            .toVersion("2.0.0")
            .build();

        Notification notification = Notification.builder()
            .title("Test")
            .event(event)
            .build();

        String result = engine.process(template, notification);

        assertThat(result).contains("Breaking Change Detected");
        assertThat(result).contains("Test API");
        assertThat(result).contains("1.0.0");
        assertThat(result).contains("2.0.0");
    }

    @Test
    void shouldProcessChangelog() {
        String template = "API: ${apiName}, Breaking: ${breakingCount}, Total: ${totalChanges}";

        Changelog changelog = Changelog.builder()
            .apiName("My API")
            .addBreakingChange(BreakingChange.breakingChangeBuilder()
                .description("Breaking 1")
                .build())
            .addChange(Change.builder()
                .description("Change 1")
                .type(ChangeType.ADDED)
                .severity(Severity.INFO)
                .build())
            .build();

        String result = engine.process(template, changelog);

        assertThat(result).isEqualTo("API: My API, Breaking: 1, Total: 1");
    }

    @Test
    void shouldBuildVariablesFromNotification() {
        NotificationEvent event = NotificationEvent.builder()
            .eventType(EventType.API_VERSION_RELEASED)
            .apiName("Test API")
            .fromVersion("1.0.0")
            .toVersion("2.0.0")
            .build();

        Notification notification = Notification.builder()
            .title("Test Title")
            .message("Test Message")
            .severity(Severity.WARNING)
            .event(event)
            .reportUrl("https://example.com/report")
            .build();

        Map<String, Object> vars = engine.buildVariables(notification);

        assertThat(vars).containsEntry("title", "Test Title");
        assertThat(vars).containsEntry("message", "Test Message");
        assertThat(vars).containsEntry("severity", "WARNING");
        assertThat(vars).containsEntry("apiName", "Test API");
        assertThat(vars).containsEntry("reportUrl", "https://example.com/report");
    }

    @Test
    void shouldBuildVariablesFromChangelog() {
        Changelog changelog = Changelog.builder()
            .apiName("Test API")
            .fromVersion("1.0")
            .toVersion("2.0")
            .addBreakingChange(BreakingChange.breakingChangeBuilder()
                .description("Breaking change 1")
                .build())
            .build();

        Map<String, Object> vars = engine.buildVariables(changelog);

        assertThat(vars).containsEntry("apiName", "Test API");
        assertThat(vars).containsEntry("fromVersion", "1.0");
        assertThat(vars).containsEntry("toVersion", "2.0");
        assertThat(vars).containsEntry("breakingCount", 1);
        assertThat(vars).containsEntry("hasBreakingChanges", true);
        assertThat((String) vars.get("changesList")).contains("Breaking change 1");
    }

    @Test
    void shouldHandleNullTemplate() {
        String result = engine.process(null, new HashMap<>());

        assertThat(result).isEmpty();
    }
}

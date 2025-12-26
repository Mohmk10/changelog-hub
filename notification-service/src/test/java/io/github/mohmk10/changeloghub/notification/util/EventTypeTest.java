package io.github.mohmk10.changeloghub.notification.util;

import io.github.mohmk10.changeloghub.core.model.Severity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EventTypeTest {

    @Test
    void shouldHaveCorrectDescriptions() {
        assertThat(EventType.BREAKING_CHANGE_DETECTED.getDescription())
            .isEqualTo("Breaking Change Detected");
        assertThat(EventType.DANGEROUS_CHANGE_DETECTED.getDescription())
            .isEqualTo("Dangerous Change Detected");
        assertThat(EventType.API_VERSION_RELEASED.getDescription())
            .isEqualTo("API Version Released");
    }

    @Test
    void shouldHaveCorrectDefaultSeverities() {
        assertThat(EventType.BREAKING_CHANGE_DETECTED.getDefaultSeverity())
            .isEqualTo(Severity.BREAKING);
        assertThat(EventType.DANGEROUS_CHANGE_DETECTED.getDefaultSeverity())
            .isEqualTo(Severity.DANGEROUS);
        assertThat(EventType.DEPRECATION_ADDED.getDefaultSeverity())
            .isEqualTo(Severity.WARNING);
        assertThat(EventType.API_VERSION_RELEASED.getDefaultSeverity())
            .isEqualTo(Severity.INFO);
    }

    @Test
    void shouldIdentifyCriticalEvents() {
        assertThat(EventType.BREAKING_CHANGE_DETECTED.isCritical()).isTrue();
        assertThat(EventType.DANGEROUS_CHANGE_DETECTED.isCritical()).isTrue();
        assertThat(EventType.HIGH_RISK_RELEASE.isCritical()).isTrue();
        assertThat(EventType.CONSUMER_IMPACTED.isCritical()).isTrue();
        assertThat(EventType.ENDPOINT_REMOVED.isCritical()).isTrue();

        assertThat(EventType.DEPRECATION_ADDED.isCritical()).isFalse();
        assertThat(EventType.API_VERSION_RELEASED.isCritical()).isFalse();
    }

    @Test
    void shouldRequireImmediateNotificationForCriticalEvents() {
        assertThat(EventType.BREAKING_CHANGE_DETECTED.requiresImmediateNotification()).isTrue();
        assertThat(EventType.API_VERSION_RELEASED.requiresImmediateNotification()).isFalse();
    }

    @Test
    void shouldHaveColorCodes() {
        assertThat(EventType.BREAKING_CHANGE_DETECTED.getColorCode()).isEqualTo("#dc3545");
        assertThat(EventType.DANGEROUS_CHANGE_DETECTED.getColorCode()).isEqualTo("#fd7e14");
        assertThat(EventType.DEPRECATION_ADDED.getColorCode()).isEqualTo("#ffc107");
        assertThat(EventType.API_VERSION_RELEASED.getColorCode()).isEqualTo("#28a745");
    }

    @Test
    void shouldHaveEmojis() {
        assertThat(EventType.BREAKING_CHANGE_DETECTED.getEmoji()).isNotEmpty();
        assertThat(EventType.DANGEROUS_CHANGE_DETECTED.getEmoji()).isNotEmpty();
        assertThat(EventType.API_VERSION_RELEASED.getEmoji()).isNotEmpty();
    }
}

package io.github.mohmk10.changeloghub.notification.filter;

import io.github.mohmk10.changeloghub.core.model.Severity;
import io.github.mohmk10.changeloghub.notification.model.Notification;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;

class SeverityFilterTest {

    @Test
    void shouldFilterByMinimumSeverity() {
        SeverityFilter filter = SeverityFilter.minimum(Severity.WARNING);

        Notification breaking = createNotification(Severity.BREAKING);
        Notification dangerous = createNotification(Severity.DANGEROUS);
        Notification warning = createNotification(Severity.WARNING);
        Notification info = createNotification(Severity.INFO);

        assertThat(filter.shouldSend(breaking)).isTrue();
        assertThat(filter.shouldSend(dangerous)).isTrue();
        assertThat(filter.shouldSend(warning)).isTrue();
        assertThat(filter.shouldSend(info)).isFalse();
    }

    @Test
    void shouldFilterBreakingOnly() {
        SeverityFilter filter = SeverityFilter.breakingOnly();

        assertThat(filter.shouldSend(createNotification(Severity.BREAKING))).isTrue();
        assertThat(filter.shouldSend(createNotification(Severity.DANGEROUS))).isFalse();
        assertThat(filter.shouldSend(createNotification(Severity.WARNING))).isFalse();
        assertThat(filter.shouldSend(createNotification(Severity.INFO))).isFalse();
    }

    @Test
    void shouldFilterCriticalOnly() {
        SeverityFilter filter = SeverityFilter.criticalOnly();

        assertThat(filter.shouldSend(createNotification(Severity.BREAKING))).isTrue();
        assertThat(filter.shouldSend(createNotification(Severity.DANGEROUS))).isTrue();
        assertThat(filter.shouldSend(createNotification(Severity.WARNING))).isFalse();
        assertThat(filter.shouldSend(createNotification(Severity.INFO))).isFalse();
    }

    @Test
    void shouldAllowAllSeverities() {
        SeverityFilter filter = SeverityFilter.allowAll();

        for (Severity severity : Severity.values()) {
            assertThat(filter.shouldSend(createNotification(severity))).isTrue();
        }
    }

    @Test
    void shouldFilterByAllowedSet() {
        SeverityFilter filter = new SeverityFilter(
            EnumSet.of(Severity.BREAKING, Severity.WARNING));

        assertThat(filter.shouldSend(createNotification(Severity.BREAKING))).isTrue();
        assertThat(filter.shouldSend(createNotification(Severity.WARNING))).isTrue();
        assertThat(filter.shouldSend(createNotification(Severity.DANGEROUS))).isFalse();
        assertThat(filter.shouldSend(createNotification(Severity.INFO))).isFalse();
    }

    @Test
    void shouldProvideFilterReason() {
        SeverityFilter filter = SeverityFilter.minimum(Severity.WARNING);
        Notification notification = createNotification(Severity.INFO);

        String reason = filter.getFilterReason(notification);

        assertThat(reason).contains("INFO").contains("WARNING");
    }

    @Test
    void shouldHaveEarlyPriority() {
        SeverityFilter filter = SeverityFilter.breakingOnly();

        assertThat(filter.getPriority()).isLessThan(100);
    }

    @Test
    void shouldGetMinimumSeverity() {
        SeverityFilter filter = SeverityFilter.minimum(Severity.DANGEROUS);

        assertThat(filter.getMinimumSeverity()).isEqualTo(Severity.DANGEROUS);
    }

    @Test
    void shouldGetAllowedSeverities() {
        SeverityFilter filter = SeverityFilter.criticalOnly();

        assertThat(filter.getAllowedSeverities())
            .containsExactlyInAnyOrder(Severity.BREAKING, Severity.DANGEROUS);
    }

    private Notification createNotification(Severity severity) {
        return Notification.builder()
            .title("Test")
            .severity(severity)
            .build();
    }
}

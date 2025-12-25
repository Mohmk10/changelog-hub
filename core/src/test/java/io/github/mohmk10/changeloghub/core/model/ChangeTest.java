package io.github.mohmk10.changeloghub.core.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ChangeTest {

    @Test
    void shouldCreateChangeWithDefaultConstructor() {
        Change change = new Change();

        assertThat(change.getId()).isNotNull();
        assertThat(change.getDetectedAt()).isNotNull();
        assertThat(change.getType()).isNull();
        assertThat(change.getCategory()).isNull();
        assertThat(change.getSeverity()).isNull();
    }

    @Test
    void shouldCreateChangeWithAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();

        Change change = new Change(
                "test-id",
                ChangeType.ADDED,
                ChangeCategory.ENDPOINT,
                Severity.INFO,
                "/api/users",
                "New endpoint added",
                null,
                "/api/users",
                now
        );

        assertThat(change.getId()).isEqualTo("test-id");
        assertThat(change.getType()).isEqualTo(ChangeType.ADDED);
        assertThat(change.getCategory()).isEqualTo(ChangeCategory.ENDPOINT);
        assertThat(change.getSeverity()).isEqualTo(Severity.INFO);
        assertThat(change.getPath()).isEqualTo("/api/users");
        assertThat(change.getDescription()).isEqualTo("New endpoint added");
        assertThat(change.getDetectedAt()).isEqualTo(now);
    }

    @Test
    void shouldCreateChangeWithBuilder() {
        Change change = Change.builder()
                .type(ChangeType.REMOVED)
                .category(ChangeCategory.ENDPOINT)
                .severity(Severity.BREAKING)
                .path("/api/legacy")
                .description("Endpoint removed")
                .oldValue("/api/legacy")
                .newValue(null)
                .build();

        assertThat(change.getId()).isNotNull();
        assertThat(change.getType()).isEqualTo(ChangeType.REMOVED);
        assertThat(change.getCategory()).isEqualTo(ChangeCategory.ENDPOINT);
        assertThat(change.getSeverity()).isEqualTo(Severity.BREAKING);
        assertThat(change.getPath()).isEqualTo("/api/legacy");
        assertThat(change.getOldValue()).isEqualTo("/api/legacy");
        assertThat(change.getNewValue()).isNull();
    }

    @Test
    void shouldClassifyAllSeverityLevels() {
        assertThat(Severity.BREAKING.ordinal()).isLessThan(Severity.DANGEROUS.ordinal());
        assertThat(Severity.DANGEROUS.ordinal()).isLessThan(Severity.WARNING.ordinal());
        assertThat(Severity.WARNING.ordinal()).isLessThan(Severity.INFO.ordinal());
    }

    @Test
    void shouldSupportAllChangeTypes() {
        for (ChangeType type : ChangeType.values()) {
            Change change = Change.builder()
                    .type(type)
                    .build();

            assertThat(change.getType()).isEqualTo(type);
        }
    }

    @Test
    void shouldSupportAllChangeCategories() {
        for (ChangeCategory category : ChangeCategory.values()) {
            Change change = Change.builder()
                    .category(category)
                    .build();

            assertThat(change.getCategory()).isEqualTo(category);
        }
    }

    @Test
    void shouldHaveCorrectEqualsAndHashCode() {
        LocalDateTime now = LocalDateTime.now();

        Change change1 = new Change("id-1", ChangeType.ADDED, ChangeCategory.ENDPOINT,
                Severity.INFO, "/path", "desc", null, "new", now);

        Change change2 = new Change("id-1", ChangeType.ADDED, ChangeCategory.ENDPOINT,
                Severity.INFO, "/path", "desc", null, "new", now);

        Change change3 = new Change("id-2", ChangeType.ADDED, ChangeCategory.ENDPOINT,
                Severity.INFO, "/path", "desc", null, "new", now);

        assertThat(change1).isEqualTo(change2);
        assertThat(change1.hashCode()).isEqualTo(change2.hashCode());
        assertThat(change1).isNotEqualTo(change3);
    }

    @Test
    void shouldHaveCorrectToString() {
        Change change = Change.builder()
                .type(ChangeType.MODIFIED)
                .category(ChangeCategory.PARAMETER)
                .severity(Severity.WARNING)
                .path("/api/users/{id}")
                .build();

        String toString = change.toString();

        assertThat(toString).contains("MODIFIED");
        assertThat(toString).contains("PARAMETER");
        assertThat(toString).contains("WARNING");
        assertThat(toString).contains("/api/users/{id}");
    }
}

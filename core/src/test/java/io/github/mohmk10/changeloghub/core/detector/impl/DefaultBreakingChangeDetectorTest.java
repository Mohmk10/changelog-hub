package io.github.mohmk10.changeloghub.core.detector.impl;

import io.github.mohmk10.changeloghub.core.model.BreakingChange;
import io.github.mohmk10.changeloghub.core.model.Change;
import io.github.mohmk10.changeloghub.core.model.ChangeCategory;
import io.github.mohmk10.changeloghub.core.model.ChangeType;
import io.github.mohmk10.changeloghub.core.model.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultBreakingChangeDetectorTest {

    private DefaultBreakingChangeDetector detector;

    @BeforeEach
    void setUp() {
        detector = new DefaultBreakingChangeDetector();
    }

    @Test
    void testDetectBreakingChanges() {
        Change breakingChange = Change.builder()
                .type(ChangeType.REMOVED)
                .category(ChangeCategory.ENDPOINT)
                .severity(Severity.BREAKING)
                .path("/api/users")
                .description("Endpoint removed")
                .build();

        Change infoChange = Change.builder()
                .type(ChangeType.ADDED)
                .category(ChangeCategory.ENDPOINT)
                .severity(Severity.INFO)
                .path("/api/products")
                .description("Endpoint added")
                .build();

        List<BreakingChange> breakingChanges = detector.detect(Arrays.asList(breakingChange, infoChange));

        assertThat(breakingChanges).hasSize(1);
        assertThat(breakingChanges.get(0).getPath()).isEqualTo("/api/users");
    }

    @Test
    void testDetectDangerousChanges() {
        Change dangerousChange = Change.builder()
                .type(ChangeType.REMOVED)
                .category(ChangeCategory.PARAMETER)
                .severity(Severity.DANGEROUS)
                .path("parameter:filter")
                .description("Parameter removed")
                .build();

        List<BreakingChange> breakingChanges = detector.detect(Arrays.asList(dangerousChange));

        assertThat(breakingChanges).hasSize(1);
    }

    @Test
    void testIsBreakingWithBreakingSeverity() {
        Change change = Change.builder()
                .type(ChangeType.REMOVED)
                .category(ChangeCategory.ENDPOINT)
                .severity(Severity.BREAKING)
                .build();

        boolean result = detector.isBreaking(change);

        assertThat(result).isTrue();
    }

    @Test
    void testIsBreakingWithDangerousSeverity() {
        Change change = Change.builder()
                .type(ChangeType.REMOVED)
                .category(ChangeCategory.RESPONSE)
                .severity(Severity.DANGEROUS)
                .build();

        boolean result = detector.isBreaking(change);

        assertThat(result).isTrue();
    }

    @Test
    void testIsBreakingWithWarningSeverity() {
        Change change = Change.builder()
                .type(ChangeType.DEPRECATED)
                .category(ChangeCategory.ENDPOINT)
                .severity(Severity.WARNING)
                .build();

        boolean result = detector.isBreaking(change);

        assertThat(result).isFalse();
    }

    @Test
    void testIsBreakingWithInfoSeverity() {
        Change change = Change.builder()
                .type(ChangeType.ADDED)
                .category(ChangeCategory.ENDPOINT)
                .severity(Severity.INFO)
                .build();

        boolean result = detector.isBreaking(change);

        assertThat(result).isFalse();
    }

    @Test
    void testImpactScoreCalculationForRemovedEndpoint() {
        Change change = Change.builder()
                .type(ChangeType.REMOVED)
                .category(ChangeCategory.ENDPOINT)
                .severity(Severity.BREAKING)
                .path("/api/users")
                .build();

        List<BreakingChange> breakingChanges = detector.detect(Arrays.asList(change));

        assertThat(breakingChanges).hasSize(1);
        assertThat(breakingChanges.get(0).getImpactScore()).isEqualTo(100);
    }

    @Test
    void testImpactScoreCalculationForRemovedParameter() {
        Change change = Change.builder()
                .type(ChangeType.REMOVED)
                .category(ChangeCategory.PARAMETER)
                .severity(Severity.DANGEROUS)
                .path("parameter:userId")
                .build();

        List<BreakingChange> breakingChanges = detector.detect(Arrays.asList(change));

        assertThat(breakingChanges).hasSize(1);
        assertThat(breakingChanges.get(0).getImpactScore()).isLessThanOrEqualTo(80);
    }

    @Test
    void testMigrationSuggestionGenerationForRemovedEndpoint() {
        Change change = Change.builder()
                .type(ChangeType.REMOVED)
                .category(ChangeCategory.ENDPOINT)
                .severity(Severity.BREAKING)
                .path("/api/users")
                .build();

        List<BreakingChange> breakingChanges = detector.detect(Arrays.asList(change));

        assertThat(breakingChanges.get(0).getMigrationSuggestion())
                .contains("stop using the removed endpoint");
    }

    @Test
    void testMigrationSuggestionGenerationForAddedParameter() {
        Change change = Change.builder()
                .type(ChangeType.ADDED)
                .category(ChangeCategory.PARAMETER)
                .severity(Severity.BREAKING)
                .path("parameter:userId")
                .description("Required parameter added")
                .build();

        List<BreakingChange> breakingChanges = detector.detect(Arrays.asList(change));

        assertThat(breakingChanges.get(0).getMigrationSuggestion())
                .contains("Add the new required parameter");
    }

    @Test
    void testDetectEmptyList() {
        List<BreakingChange> breakingChanges = detector.detect(Collections.emptyList());

        assertThat(breakingChanges).isEmpty();
    }

    @Test
    void testDetectNullList() {
        List<BreakingChange> breakingChanges = detector.detect(null);

        assertThat(breakingChanges).isEmpty();
    }

    @Test
    void testIsBreakingNull() {
        boolean result = detector.isBreaking(null);

        assertThat(result).isFalse();
    }
}

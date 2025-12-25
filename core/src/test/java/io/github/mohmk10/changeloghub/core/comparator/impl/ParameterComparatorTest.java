package io.github.mohmk10.changeloghub.core.comparator.impl;

import io.github.mohmk10.changeloghub.core.model.Change;
import io.github.mohmk10.changeloghub.core.model.ChangeType;
import io.github.mohmk10.changeloghub.core.model.Parameter;
import io.github.mohmk10.changeloghub.core.model.ParameterLocation;
import io.github.mohmk10.changeloghub.core.model.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ParameterComparatorTest {

    private ParameterComparator comparator;

    @BeforeEach
    void setUp() {
        comparator = new ParameterComparator();
    }

    @Test
    void testRequiredParameterAdded() {
        Parameter newParam = new Parameter("userId", ParameterLocation.PATH, "string", true, null, "User ID");

        List<Change> changes = comparator.compare(
                Collections.emptyList(),
                Arrays.asList(newParam)
        );

        assertThat(changes).hasSize(1);
        assertThat(changes.get(0).getType()).isEqualTo(ChangeType.ADDED);
        assertThat(changes.get(0).getSeverity()).isEqualTo(Severity.BREAKING);
        assertThat(changes.get(0).getDescription()).contains("Required parameter");
    }

    @Test
    void testOptionalParameterAdded() {
        Parameter newParam = new Parameter("filter", ParameterLocation.QUERY, "string", false, null, "Filter");

        List<Change> changes = comparator.compare(
                Collections.emptyList(),
                Arrays.asList(newParam)
        );

        assertThat(changes).hasSize(1);
        assertThat(changes.get(0).getType()).isEqualTo(ChangeType.ADDED);
        assertThat(changes.get(0).getSeverity()).isEqualTo(Severity.INFO);
        assertThat(changes.get(0).getDescription()).contains("Optional parameter");
    }

    @Test
    void testParameterRemoved() {
        Parameter oldParam = new Parameter("userId", ParameterLocation.PATH, "string", true, null, "User ID");

        List<Change> changes = comparator.compare(
                Arrays.asList(oldParam),
                Collections.emptyList()
        );

        assertThat(changes).hasSize(1);
        assertThat(changes.get(0).getType()).isEqualTo(ChangeType.REMOVED);
        assertThat(changes.get(0).getSeverity()).isEqualTo(Severity.DANGEROUS);
    }

    @Test
    void testParameterTypeChanged() {
        Parameter oldParam = new Parameter("userId", ParameterLocation.PATH, "string", true, null, "User ID");
        Parameter newParam = new Parameter("userId", ParameterLocation.PATH, "integer", true, null, "User ID");

        List<Change> changes = comparator.compare(
                Arrays.asList(oldParam),
                Arrays.asList(newParam)
        );

        assertThat(changes).hasSize(1);
        assertThat(changes.get(0).getType()).isEqualTo(ChangeType.MODIFIED);
        assertThat(changes.get(0).getSeverity()).isEqualTo(Severity.BREAKING);
        assertThat(changes.get(0).getPath()).contains(".type");
    }

    @Test
    void testRequiredChangedFromFalseToTrue() {
        Parameter oldParam = new Parameter("filter", ParameterLocation.QUERY, "string", false, null, "Filter");
        Parameter newParam = new Parameter("filter", ParameterLocation.QUERY, "string", true, null, "Filter");

        List<Change> changes = comparator.compare(
                Arrays.asList(oldParam),
                Arrays.asList(newParam)
        );

        assertThat(changes).hasSize(1);
        assertThat(changes.get(0).getType()).isEqualTo(ChangeType.MODIFIED);
        assertThat(changes.get(0).getSeverity()).isEqualTo(Severity.BREAKING);
        assertThat(changes.get(0).getDescription()).contains("optional to required");
    }

    @Test
    void testRequiredChangedFromTrueToFalse() {
        Parameter oldParam = new Parameter("filter", ParameterLocation.QUERY, "string", true, null, "Filter");
        Parameter newParam = new Parameter("filter", ParameterLocation.QUERY, "string", false, null, "Filter");

        List<Change> changes = comparator.compare(
                Arrays.asList(oldParam),
                Arrays.asList(newParam)
        );

        assertThat(changes).hasSize(1);
        assertThat(changes.get(0).getSeverity()).isEqualTo(Severity.INFO);
        assertThat(changes.get(0).getDescription()).contains("required to optional");
    }

    @Test
    void testLocationChanged() {
        Parameter oldParam = new Parameter("id", ParameterLocation.QUERY, "string", false, null, "ID");
        Parameter newParam = new Parameter("id", ParameterLocation.PATH, "string", false, null, "ID");

        List<Change> changes = comparator.compare(
                Arrays.asList(oldParam),
                Arrays.asList(newParam)
        );

        assertThat(changes).hasSize(1);
        assertThat(changes.get(0).getSeverity()).isEqualTo(Severity.BREAKING);
        assertThat(changes.get(0).getPath()).contains(".location");
    }

    @Test
    void testNoChangesWhenIdentical() {
        Parameter param = new Parameter("userId", ParameterLocation.PATH, "string", true, null, "User ID");

        List<Change> changes = comparator.compare(
                Arrays.asList(param),
                Arrays.asList(param)
        );

        assertThat(changes).isEmpty();
    }
}

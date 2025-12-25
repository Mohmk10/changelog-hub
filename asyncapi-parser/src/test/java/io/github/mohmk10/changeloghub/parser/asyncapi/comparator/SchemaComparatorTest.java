package io.github.mohmk10.changeloghub.parser.asyncapi.comparator;

import io.github.mohmk10.changeloghub.core.model.Change;
import io.github.mohmk10.changeloghub.core.model.ChangeCategory;
import io.github.mohmk10.changeloghub.core.model.Severity;
import io.github.mohmk10.changeloghub.parser.asyncapi.model.AsyncSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SchemaComparator.
 */
class SchemaComparatorTest {

    private SchemaComparator comparator;

    @BeforeEach
    void setUp() {
        comparator = new SchemaComparator();
    }

    @Test
    @DisplayName("Should detect schema addition")
    void testSchemaAdded() {
        AsyncSchema newSchema = AsyncSchema.builder()
                .type("object")
                .build();

        List<Change> changes = comparator.compare("TestSchema", null, newSchema);

        assertEquals(1, changes.size());
        assertEquals(Severity.INFO, changes.get(0).getSeverity());
        assertTrue(changes.get(0).getDescription().contains("added"));
    }

    @Test
    @DisplayName("Should detect schema removal as breaking")
    void testSchemaRemoved() {
        AsyncSchema oldSchema = AsyncSchema.builder()
                .type("object")
                .build();

        List<Change> changes = comparator.compare("TestSchema", oldSchema, null);

        assertEquals(1, changes.size());
        assertEquals(Severity.BREAKING, changes.get(0).getSeverity());
        assertTrue(changes.get(0).getDescription().contains("removed"));
    }

    @Test
    @DisplayName("Should detect type change as breaking")
    void testTypeChangeBreaking() {
        AsyncSchema oldSchema = AsyncSchema.builder()
                .type("string")
                .build();
        AsyncSchema newSchema = AsyncSchema.builder()
                .type("integer")
                .build();

        List<Change> changes = comparator.compare("TestSchema", oldSchema, newSchema);

        assertFalse(changes.isEmpty());
        boolean hasBreaking = changes.stream()
                .anyMatch(c -> c.getSeverity() == Severity.BREAKING &&
                              c.getDescription().contains("type"));
        assertTrue(hasBreaking);
    }

    @Test
    @DisplayName("Should detect required field removed as breaking")
    void testRequiredFieldRemoved() {
        Map<String, AsyncSchema> oldProps = new HashMap<>();
        oldProps.put("id", AsyncSchema.builder().type("string").build());
        oldProps.put("name", AsyncSchema.builder().type("string").build());

        Map<String, AsyncSchema> newProps = new HashMap<>();
        newProps.put("id", AsyncSchema.builder().type("string").build());
        // name is removed

        AsyncSchema oldSchema = AsyncSchema.builder()
                .type("object")
                .properties(oldProps)
                .requiredFields(Arrays.asList("id", "name"))
                .build();

        AsyncSchema newSchema = AsyncSchema.builder()
                .type("object")
                .properties(newProps)
                .requiredFields(Arrays.asList("id"))
                .build();

        List<Change> changes = comparator.compare("TestSchema", oldSchema, newSchema);

        boolean hasBreaking = changes.stream()
                .anyMatch(c -> c.getSeverity() == Severity.BREAKING &&
                              c.getDescription().contains("Required property") &&
                              c.getDescription().contains("name") &&
                              c.getDescription().contains("removed"));
        assertTrue(hasBreaking);
    }

    @Test
    @DisplayName("Should detect new required field as breaking")
    void testNewRequiredFieldAdded() {
        Map<String, AsyncSchema> oldProps = new HashMap<>();
        oldProps.put("id", AsyncSchema.builder().type("string").build());

        Map<String, AsyncSchema> newProps = new HashMap<>();
        newProps.put("id", AsyncSchema.builder().type("string").build());
        newProps.put("name", AsyncSchema.builder().type("string").build());

        AsyncSchema oldSchema = AsyncSchema.builder()
                .type("object")
                .properties(oldProps)
                .requiredFields(Arrays.asList("id"))
                .build();

        AsyncSchema newSchema = AsyncSchema.builder()
                .type("object")
                .properties(newProps)
                .requiredFields(Arrays.asList("id", "name"))
                .build();

        List<Change> changes = comparator.compare("TestSchema", oldSchema, newSchema);

        boolean hasBreaking = changes.stream()
                .anyMatch(c -> c.getSeverity() == Severity.BREAKING &&
                              c.getDescription().contains("required") &&
                              c.getDescription().contains("name") &&
                              c.getDescription().contains("added"));
        assertTrue(hasBreaking);
    }

    @Test
    @DisplayName("Should detect optional field removed as dangerous")
    void testOptionalFieldRemoved() {
        Map<String, AsyncSchema> oldProps = new HashMap<>();
        oldProps.put("id", AsyncSchema.builder().type("string").build());
        oldProps.put("description", AsyncSchema.builder().type("string").build());

        Map<String, AsyncSchema> newProps = new HashMap<>();
        newProps.put("id", AsyncSchema.builder().type("string").build());
        // description (optional) is removed

        AsyncSchema oldSchema = AsyncSchema.builder()
                .type("object")
                .properties(oldProps)
                .requiredFields(Arrays.asList("id"))
                .build();

        AsyncSchema newSchema = AsyncSchema.builder()
                .type("object")
                .properties(newProps)
                .requiredFields(Arrays.asList("id"))
                .build();

        List<Change> changes = comparator.compare("TestSchema", oldSchema, newSchema);

        boolean hasDangerous = changes.stream()
                .anyMatch(c -> c.getSeverity() == Severity.DANGEROUS &&
                              c.getDescription().contains("Optional property") &&
                              c.getDescription().contains("description") &&
                              c.getDescription().contains("removed"));
        assertTrue(hasDangerous);
    }

    @Test
    @DisplayName("Should detect new optional field as info")
    void testNewOptionalFieldAdded() {
        Map<String, AsyncSchema> oldProps = new HashMap<>();
        oldProps.put("id", AsyncSchema.builder().type("string").build());

        Map<String, AsyncSchema> newProps = new HashMap<>();
        newProps.put("id", AsyncSchema.builder().type("string").build());
        newProps.put("description", AsyncSchema.builder().type("string").build());

        AsyncSchema oldSchema = AsyncSchema.builder()
                .type("object")
                .properties(oldProps)
                .requiredFields(Arrays.asList("id"))
                .build();

        AsyncSchema newSchema = AsyncSchema.builder()
                .type("object")
                .properties(newProps)
                .requiredFields(Arrays.asList("id"))
                .build();

        List<Change> changes = comparator.compare("TestSchema", oldSchema, newSchema);

        boolean hasInfo = changes.stream()
                .anyMatch(c -> c.getSeverity() == Severity.INFO &&
                              c.getDescription().contains("optional") &&
                              c.getDescription().contains("description") &&
                              c.getDescription().contains("added"));
        assertTrue(hasInfo);
    }

    @Test
    @DisplayName("Should detect enum value removed as breaking")
    void testEnumValueRemoved() {
        AsyncSchema oldSchema = AsyncSchema.builder()
                .type("string")
                .enumValues(Arrays.asList("A", "B", "C"))
                .build();

        AsyncSchema newSchema = AsyncSchema.builder()
                .type("string")
                .enumValues(Arrays.asList("A", "B"))
                .build();

        List<Change> changes = comparator.compare("TestSchema", oldSchema, newSchema);

        boolean hasBreaking = changes.stream()
                .anyMatch(c -> c.getSeverity() == Severity.BREAKING &&
                              c.getDescription().contains("Enum value") &&
                              c.getDescription().contains("C") &&
                              c.getDescription().contains("removed"));
        assertTrue(hasBreaking);
    }

    @Test
    @DisplayName("Should detect enum value added as info")
    void testEnumValueAdded() {
        AsyncSchema oldSchema = AsyncSchema.builder()
                .type("string")
                .enumValues(Arrays.asList("A", "B"))
                .build();

        AsyncSchema newSchema = AsyncSchema.builder()
                .type("string")
                .enumValues(Arrays.asList("A", "B", "C"))
                .build();

        List<Change> changes = comparator.compare("TestSchema", oldSchema, newSchema);

        boolean hasInfo = changes.stream()
                .anyMatch(c -> c.getSeverity() == Severity.INFO &&
                              c.getDescription().contains("Enum value") &&
                              c.getDescription().contains("C") &&
                              c.getDescription().contains("added"));
        assertTrue(hasInfo);
    }

    @Test
    @DisplayName("Should detect format change as warning")
    void testFormatChange() {
        AsyncSchema oldSchema = AsyncSchema.builder()
                .type("string")
                .format("date")
                .build();

        AsyncSchema newSchema = AsyncSchema.builder()
                .type("string")
                .format("date-time")
                .build();

        List<Change> changes = comparator.compare("TestSchema", oldSchema, newSchema);

        boolean hasWarning = changes.stream()
                .anyMatch(c -> c.getSeverity() == Severity.WARNING &&
                              c.getDescription().contains("format"));
        assertTrue(hasWarning);
    }

    @Test
    @DisplayName("Should detect deprecated as warning")
    void testSchemaDeprecated() {
        AsyncSchema oldSchema = AsyncSchema.builder()
                .type("string")
                .deprecated(false)
                .build();

        AsyncSchema newSchema = AsyncSchema.builder()
                .type("string")
                .deprecated(true)
                .build();

        List<Change> changes = comparator.compare("TestSchema", oldSchema, newSchema);

        boolean hasWarning = changes.stream()
                .anyMatch(c -> c.getSeverity() == Severity.WARNING &&
                              c.getDescription().contains("deprecated"));
        assertTrue(hasWarning);
    }

    @Test
    @DisplayName("Should return empty list for null schemas")
    void testNullSchemas() {
        List<Change> changes = comparator.compare("TestSchema", null, null);

        assertNotNull(changes);
        assertTrue(changes.isEmpty());
    }

    @Test
    @DisplayName("Should create schema signature")
    void testCreateSignature() {
        AsyncSchema stringSchema = AsyncSchema.builder()
                .type("string")
                .format("email")
                .build();

        String signature = comparator.createSignature(stringSchema);

        assertNotNull(signature);
        assertTrue(signature.contains("string"));
        assertTrue(signature.contains("email"));
    }

    @Test
    @DisplayName("Should detect reference change as dangerous")
    void testReferenceChange() {
        AsyncSchema oldSchema = AsyncSchema.builder()
                .ref("#/components/schemas/OldType")
                .build();

        AsyncSchema newSchema = AsyncSchema.builder()
                .ref("#/components/schemas/NewType")
                .build();

        List<Change> changes = comparator.compare("TestSchema", oldSchema, newSchema);

        boolean hasDangerous = changes.stream()
                .anyMatch(c -> c.getSeverity() == Severity.DANGEROUS &&
                              c.getDescription().contains("reference"));
        assertTrue(hasDangerous);
    }

    @Test
    @DisplayName("Should use correct category for schema changes")
    void testChangeCategoryIsSchema() {
        AsyncSchema oldSchema = AsyncSchema.builder()
                .type("object")
                .build();

        List<Change> changes = comparator.compare("TestSchema", oldSchema, null);

        assertEquals(1, changes.size());
        assertEquals(ChangeCategory.SCHEMA, changes.get(0).getCategory());
    }
}

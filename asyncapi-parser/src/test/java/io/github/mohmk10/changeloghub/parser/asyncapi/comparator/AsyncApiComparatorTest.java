package io.github.mohmk10.changeloghub.parser.asyncapi.comparator;

import io.github.mohmk10.changeloghub.core.model.Change;
import io.github.mohmk10.changeloghub.core.model.ChangeCategory;
import io.github.mohmk10.changeloghub.core.model.Severity;
import io.github.mohmk10.changeloghub.parser.asyncapi.DefaultAsyncApiParser;
import io.github.mohmk10.changeloghub.parser.asyncapi.model.AsyncApiSpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AsyncApiComparator.
 */
class AsyncApiComparatorTest {

    private AsyncApiComparator comparator;
    private DefaultAsyncApiParser parser;

    @BeforeEach
    void setUp() {
        comparator = new AsyncApiComparator();
        parser = new DefaultAsyncApiParser();
    }

    @Test
    @DisplayName("Should detect breaking changes between v1 and v2-breaking")
    void testDetectBreakingChanges() throws Exception {
        InputStream v1Stream = getClass().getClassLoader().getResourceAsStream("user-events-v1.yaml");
        InputStream v2Stream = getClass().getClassLoader().getResourceAsStream("user-events-v2-breaking.yaml");

        AsyncApiSpec v1 = parser.parse(v1Stream);
        AsyncApiSpec v2 = parser.parse(v2Stream);

        List<Change> changes = comparator.compare(v1, v2);

        assertNotNull(changes);
        assertFalse(changes.isEmpty());

        // Should have breaking changes
        assertTrue(comparator.hasBreakingChanges(changes));

        // Get breaking changes
        List<Change> breakingChanges = comparator.filterBySeverity(changes, Severity.BREAKING);
        assertFalse(breakingChanges.isEmpty());

        // Print changes for debugging
        System.out.println("=== Breaking Changes ===");
        for (Change change : breakingChanges) {
            System.out.println(change.getSeverity() + ": " + change.getDescription() + " @ " + change.getPath());
        }
    }

    @Test
    @DisplayName("Should detect minor changes between v1 and v2-minor")
    void testDetectMinorChanges() throws Exception {
        InputStream v1Stream = getClass().getClassLoader().getResourceAsStream("user-events-v1.yaml");
        InputStream v2Stream = getClass().getClassLoader().getResourceAsStream("user-events-v2-minor.yaml");

        AsyncApiSpec v1 = parser.parse(v1Stream);
        AsyncApiSpec v2 = parser.parse(v2Stream);

        List<Change> changes = comparator.compare(v1, v2);

        assertNotNull(changes);
        assertFalse(changes.isEmpty());

        // Should have INFO level changes (additions)
        List<Change> infoChanges = comparator.filterBySeverity(changes, Severity.INFO);
        assertFalse(infoChanges.isEmpty());

        // Check for new server added
        boolean hasNewServer = infoChanges.stream()
                .anyMatch(c -> c.getDescription().contains("Server") && c.getDescription().contains("added"));
        assertTrue(hasNewServer, "Should detect new server added");

        // Check for new channel added
        boolean hasNewChannel = infoChanges.stream()
                .anyMatch(c -> c.getDescription().contains("Channel") && c.getDescription().contains("added"));
        assertTrue(hasNewChannel, "Should detect new channel added");

        System.out.println("=== Minor Changes ===");
        for (Change change : changes) {
            System.out.println(change.getSeverity() + ": " + change.getDescription());
        }
    }

    @Test
    @DisplayName("Should detect channel removal as breaking")
    void testChannelRemoval() throws Exception {
        InputStream v1Stream = getClass().getClassLoader().getResourceAsStream("user-events-v1.yaml");
        InputStream v2Stream = getClass().getClassLoader().getResourceAsStream("user-events-v2-breaking.yaml");

        AsyncApiSpec v1 = parser.parse(v1Stream);
        AsyncApiSpec v2 = parser.parse(v2Stream);

        List<Change> changes = comparator.compare(v1, v2);

        // user/created was renamed to users/created (effectively removed)
        List<Change> breakingChanges = comparator.filterBySeverity(changes, Severity.BREAKING);

        boolean hasChannelRemoved = breakingChanges.stream()
                .anyMatch(c -> c.getDescription().contains("Channel") &&
                              c.getDescription().contains("removed"));
        assertTrue(hasChannelRemoved, "Should detect channel removal as breaking");
    }

    @Test
    @DisplayName("Should detect server removal as breaking")
    void testServerRemoval() throws Exception {
        InputStream v1Stream = getClass().getClassLoader().getResourceAsStream("user-events-v1.yaml");
        InputStream v2Stream = getClass().getClassLoader().getResourceAsStream("user-events-v2-breaking.yaml");

        AsyncApiSpec v1 = parser.parse(v1Stream);
        AsyncApiSpec v2 = parser.parse(v2Stream);

        List<Change> changes = comparator.compare(v1, v2);

        List<Change> breakingChanges = comparator.filterBySeverity(changes, Severity.BREAKING);

        boolean hasServerRemoved = breakingChanges.stream()
                .anyMatch(c -> c.getDescription().contains("Server") &&
                              c.getDescription().contains("staging") &&
                              c.getDescription().contains("removed"));
        assertTrue(hasServerRemoved, "Should detect server removal as breaking");
    }

    @Test
    @DisplayName("Should detect required field added as breaking")
    void testRequiredFieldAdded() throws Exception {
        InputStream v1Stream = getClass().getClassLoader().getResourceAsStream("user-events-v1.yaml");
        InputStream v2Stream = getClass().getClassLoader().getResourceAsStream("user-events-v2-breaking.yaml");

        AsyncApiSpec v1 = parser.parse(v1Stream);
        AsyncApiSpec v2 = parser.parse(v2Stream);

        List<Change> changes = comparator.compare(v1, v2);

        List<Change> breakingChanges = comparator.filterBySeverity(changes, Severity.BREAKING);

        boolean hasNewRequired = breakingChanges.stream()
                .anyMatch(c -> c.getDescription().contains("required") &&
                              c.getDescription().toLowerCase().contains("added"));
        assertTrue(hasNewRequired, "Should detect new required field as breaking");
    }

    @Test
    @DisplayName("Should detect enum value removal as breaking")
    void testEnumValueRemoval() throws Exception {
        InputStream v1Stream = getClass().getClassLoader().getResourceAsStream("user-events-v1.yaml");
        InputStream v2Stream = getClass().getClassLoader().getResourceAsStream("user-events-v2-breaking.yaml");

        AsyncApiSpec v1 = parser.parse(v1Stream);
        AsyncApiSpec v2 = parser.parse(v2Stream);

        List<Change> changes = comparator.compare(v1, v2);

        List<Change> breakingChanges = comparator.filterBySeverity(changes, Severity.BREAKING);

        // 'error' enum value was removed from NotificationPayload.type
        boolean hasEnumRemoved = breakingChanges.stream()
                .anyMatch(c -> c.getDescription().contains("Enum value") &&
                              c.getDescription().contains("removed"));
        assertTrue(hasEnumRemoved, "Should detect enum value removal as breaking");
    }

    @Test
    @DisplayName("Should get changes summary")
    void testGetChangesSummary() throws Exception {
        InputStream v1Stream = getClass().getClassLoader().getResourceAsStream("user-events-v1.yaml");
        InputStream v2Stream = getClass().getClassLoader().getResourceAsStream("user-events-v2-breaking.yaml");

        AsyncApiSpec v1 = parser.parse(v1Stream);
        AsyncApiSpec v2 = parser.parse(v2Stream);

        List<Change> changes = comparator.compare(v1, v2);
        Map<Severity, Integer> summary = comparator.getChangesSummary(changes);

        assertNotNull(summary);
        assertTrue(summary.get(Severity.BREAKING) > 0);

        System.out.println("=== Changes Summary ===");
        for (Map.Entry<Severity, Integer> entry : summary.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    @Test
    @DisplayName("Should detect optional field added as info")
    void testOptionalFieldAdded() throws Exception {
        InputStream v1Stream = getClass().getClassLoader().getResourceAsStream("user-events-v1.yaml");
        InputStream v2Stream = getClass().getClassLoader().getResourceAsStream("user-events-v2-minor.yaml");

        AsyncApiSpec v1 = parser.parse(v1Stream);
        AsyncApiSpec v2 = parser.parse(v2Stream);

        List<Change> changes = comparator.compare(v1, v2);

        List<Change> infoChanges = comparator.filterBySeverity(changes, Severity.INFO);

        // Check for new optional properties
        boolean hasOptionalAdded = infoChanges.stream()
                .anyMatch(c -> c.getDescription().contains("optional") &&
                              c.getDescription().contains("property") &&
                              c.getDescription().contains("added"));
        assertTrue(hasOptionalAdded, "Should detect optional field added as INFO");
    }

    @Test
    @DisplayName("Should detect new enum value as info")
    void testEnumValueAdded() throws Exception {
        InputStream v1Stream = getClass().getClassLoader().getResourceAsStream("user-events-v1.yaml");
        InputStream v2Stream = getClass().getClassLoader().getResourceAsStream("user-events-v2-minor.yaml");

        AsyncApiSpec v1 = parser.parse(v1Stream);
        AsyncApiSpec v2 = parser.parse(v2Stream);

        List<Change> changes = comparator.compare(v1, v2);

        List<Change> infoChanges = comparator.filterBySeverity(changes, Severity.INFO);

        // 'success' enum value added to NotificationPayload.type
        boolean hasEnumAdded = infoChanges.stream()
                .anyMatch(c -> c.getDescription().contains("Enum value") &&
                              c.getDescription().contains("added"));
        assertTrue(hasEnumAdded, "Should detect enum value added as INFO");
    }

    @Test
    @DisplayName("Should handle null specs")
    void testNullSpecs() {
        List<Change> changes1 = comparator.compare(null, null);
        assertTrue(changes1.isEmpty());

        AsyncApiSpec spec = AsyncApiSpec.builder()
                .title("Test")
                .apiVersion("1.0.0")
                .build();

        List<Change> changes2 = comparator.compare(null, spec);
        assertEquals(1, changes2.size());
        assertEquals(Severity.INFO, changes2.get(0).getSeverity());

        List<Change> changes3 = comparator.compare(spec, null);
        assertEquals(1, changes3.size());
        assertEquals(Severity.BREAKING, changes3.get(0).getSeverity());
    }

    @Test
    @DisplayName("Should detect no changes for identical specs")
    void testIdenticalSpecs() throws Exception {
        InputStream stream1 = getClass().getClassLoader().getResourceAsStream("user-events-v1.yaml");
        InputStream stream2 = getClass().getClassLoader().getResourceAsStream("user-events-v1.yaml");

        AsyncApiSpec v1 = parser.parse(stream1);
        AsyncApiSpec v1Copy = parser.parse(stream2);

        List<Change> changes = comparator.compare(v1, v1Copy);

        // Should have no breaking or dangerous changes
        List<Change> breakingChanges = comparator.filterBySeverity(changes, Severity.BREAKING);
        assertTrue(breakingChanges.isEmpty(), "Identical specs should have no breaking changes");
    }

    @Test
    @DisplayName("Should filter by category correctly")
    void testFilterByCategory() throws Exception {
        InputStream v1Stream = getClass().getClassLoader().getResourceAsStream("user-events-v1.yaml");
        InputStream v2Stream = getClass().getClassLoader().getResourceAsStream("user-events-v2-breaking.yaml");

        AsyncApiSpec v1 = parser.parse(v1Stream);
        AsyncApiSpec v2 = parser.parse(v2Stream);

        List<Change> changes = comparator.compare(v1, v2);

        // Filter by CHANNEL category
        List<Change> channelChanges = comparator.filterByCategory(changes, ChangeCategory.CHANNEL);
        assertNotNull(channelChanges);

        // Filter by SERVER category
        List<Change> serverChanges = comparator.filterByCategory(changes, ChangeCategory.SERVER);
        assertNotNull(serverChanges);
    }
}

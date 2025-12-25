package io.github.mohmk10.changeloghub.parser.grpc.integration;

import io.github.mohmk10.changeloghub.core.comparator.ApiComparator;
import io.github.mohmk10.changeloghub.core.comparator.impl.DefaultApiComparator;
import io.github.mohmk10.changeloghub.core.detector.BreakingChangeDetector;
import io.github.mohmk10.changeloghub.core.detector.impl.DefaultBreakingChangeDetector;
import io.github.mohmk10.changeloghub.core.model.*;
import io.github.mohmk10.changeloghub.core.reporter.Reporter;
import io.github.mohmk10.changeloghub.core.reporter.ReporterFactory;
import io.github.mohmk10.changeloghub.core.reporter.ReportFormat;
import io.github.mohmk10.changeloghub.parser.grpc.DefaultGrpcParser;
import io.github.mohmk10.changeloghub.parser.grpc.comparator.ProtoComparator;
import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("gRPC Parser Integration Tests")
class GrpcIntegrationTest {

    private DefaultGrpcParser parser;
    private ProtoComparator protoComparator;
    private ApiComparator apiComparator;
    private BreakingChangeDetector breakingChangeDetector;

    @BeforeEach
    void setUp() {
        parser = new DefaultGrpcParser();
        protoComparator = new ProtoComparator();
        apiComparator = new DefaultApiComparator();
        breakingChangeDetector = new DefaultBreakingChangeDetector();
    }

    private ProtoFile loadProtoFile(String resourceName) {
        InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName);
        assertNotNull(is, "Resource not found: " + resourceName);
        return parser.parseStream(is, resourceName);
    }

    @Nested
    @DisplayName("Parse Test Proto Files")
    class ParseTestProtoFiles {

        @Test
        @DisplayName("Should parse user_service_v1.proto successfully")
        void shouldParseUserServiceV1() {
            ProtoFile protoFile = loadProtoFile("user_service_v1.proto");

            assertNotNull(protoFile);
            assertEquals("proto3", protoFile.getSyntax());
            assertEquals("com.example.user.v1", protoFile.getPackageName());

            // Check services
            assertEquals(2, protoFile.getServices().size());
            assertTrue(protoFile.hasService("UserService"));
            assertTrue(protoFile.hasService("BulkUserService"));

            // Check UserService methods
            var userService = protoFile.getService("UserService").orElseThrow();
            assertEquals(6, userService.getMethodCount());
            assertTrue(userService.hasMethod("GetUser"));
            assertTrue(userService.hasMethod("CreateUser"));
            assertTrue(userService.hasMethod("UpdateUser"));
            assertTrue(userService.hasMethod("DeleteUser"));
            assertTrue(userService.hasMethod("ListUsers"));
            assertTrue(userService.hasMethod("WatchUsers"));

            // Check streaming methods
            var watchUsers = userService.getMethod("WatchUsers").orElseThrow();
            assertTrue(watchUsers.isServerStreaming());
            assertFalse(watchUsers.isClientStreaming());

            // Check BulkUserService methods
            var bulkService = protoFile.getService("BulkUserService").orElseThrow();
            assertEquals(2, bulkService.getMethodCount());

            var importUsers = bulkService.getMethod("ImportUsers").orElseThrow();
            assertTrue(importUsers.isClientStreaming());
            assertFalse(importUsers.isServerStreaming());

            var syncUsers = bulkService.getMethod("SyncUsers").orElseThrow();
            assertTrue(syncUsers.isClientStreaming());
            assertTrue(syncUsers.isServerStreaming());

            // Check messages
            assertTrue(protoFile.getMessages().size() >= 10);
            assertTrue(protoFile.hasMessage("User"));
            assertTrue(protoFile.hasMessage("Address"));
            assertTrue(protoFile.hasMessage("GetUserRequest"));
            assertTrue(protoFile.hasMessage("CreateUserRequest"));

            // Check enums
            assertEquals(2, protoFile.getEnums().size());
            assertTrue(protoFile.hasEnum("UserStatus"));
            assertTrue(protoFile.hasEnum("Role"));

            // Check User message fields
            var userMessage = protoFile.getMessage("User").orElseThrow();
            assertEquals(12, userMessage.getFieldCount());
            assertTrue(userMessage.hasField("id"));
            assertTrue(userMessage.hasField("email"));
            assertTrue(userMessage.hasField("metadata"));
            assertTrue(userMessage.getField("roles").orElseThrow().isRepeated());
            assertTrue(userMessage.getField("metadata").orElseThrow().isMap());
        }

        @Test
        @DisplayName("Should parse user_service_v2_breaking.proto successfully")
        void shouldParseUserServiceV2Breaking() {
            ProtoFile protoFile = loadProtoFile("user_service_v2_breaking.proto");

            assertNotNull(protoFile);
            assertEquals("proto3", protoFile.getSyntax());
            assertEquals("com.example.user.v2", protoFile.getPackageName());

            // BulkUserService should be removed
            assertFalse(protoFile.hasService("BulkUserService"));

            // AdminService should be added
            assertTrue(protoFile.hasService("AdminService"));

            // Check UserStatus enum - SUSPENDED should be removed
            var userStatus = protoFile.getEnum("UserStatus").orElseThrow();
            assertFalse(userStatus.hasValue("USER_STATUS_SUSPENDED"));
            assertTrue(userStatus.hasValue("USER_STATUS_BANNED"));
        }

        @Test
        @DisplayName("Should parse user_service_v2_minor.proto successfully")
        void shouldParseUserServiceV2Minor() {
            ProtoFile protoFile = loadProtoFile("user_service_v2_minor.proto");

            assertNotNull(protoFile);
            assertEquals("proto3", protoFile.getSyntax());
            assertEquals("com.example.user.v1", protoFile.getPackageName());

            // Should have NotificationService added
            assertTrue(protoFile.hasService("NotificationService"));

            // UserService should have new methods
            var userService = protoFile.getService("UserService").orElseThrow();
            assertTrue(userService.hasMethod("GetUserByEmail"));
            assertTrue(userService.hasMethod("VerifyEmail"));
        }
    }

    @Nested
    @DisplayName("Compare Breaking Changes")
    class CompareBreakingChanges {

        @Test
        @DisplayName("Should detect breaking changes between v1 and v2_breaking")
        void shouldDetectBreakingChangesBetweenV1AndV2Breaking() {
            ProtoFile v1 = loadProtoFile("user_service_v1.proto");
            ProtoFile v2 = loadProtoFile("user_service_v2_breaking.proto");

            List<BreakingChange> changes = protoComparator.compare(v1, v2);

            assertFalse(changes.isEmpty());
            assertTrue(protoComparator.hasBreakingChanges(changes));

            // Count changes by severity
            Map<Severity, List<BreakingChange>> bySeverity = protoComparator.groupBySeverity(changes);

            // Should have breaking changes
            assertFalse(bySeverity.get(Severity.BREAKING).isEmpty(),
                    "Should have BREAKING changes");

            // Verify specific breaking changes
            assertTrue(changes.stream().anyMatch(c ->
                            c.getCategory() == ChangeCategory.PACKAGE &&
                                    c.getSeverity() == Severity.BREAKING),
                    "Should detect package change");

            assertTrue(changes.stream().anyMatch(c ->
                            c.getCategory() == ChangeCategory.SERVICE &&
                                    c.getType() == ChangeType.REMOVED),
                    "Should detect service removed (BulkUserService)");

            assertTrue(changes.stream().anyMatch(c ->
                            c.getCategory() == ChangeCategory.ENUM_VALUE &&
                                    c.getType() == ChangeType.REMOVED),
                    "Should detect enum value removed");

            // Print summary
            System.out.println("=== Breaking Changes Summary (v1 -> v2_breaking) ===");
            System.out.println("Total changes: " + changes.size());
            System.out.println("BREAKING: " + bySeverity.get(Severity.BREAKING).size());
            System.out.println("DANGEROUS: " + bySeverity.get(Severity.DANGEROUS).size());
            System.out.println("WARNING: " + bySeverity.get(Severity.WARNING).size());
            System.out.println("INFO: " + bySeverity.get(Severity.INFO).size());
        }

        @Test
        @DisplayName("Should detect minor changes between v1 and v2_minor")
        void shouldDetectMinorChangesBetweenV1AndV2Minor() {
            ProtoFile v1 = loadProtoFile("user_service_v1.proto");
            ProtoFile v2 = loadProtoFile("user_service_v2_minor.proto");

            List<BreakingChange> changes = protoComparator.compare(v1, v2);

            assertFalse(changes.isEmpty());

            // Should not have breaking changes for package (same package)
            assertFalse(changes.stream().anyMatch(c ->
                            c.getCategory() == ChangeCategory.PACKAGE),
                    "Should not have package changes");

            // Count changes by severity
            Map<Severity, List<BreakingChange>> bySeverity = protoComparator.groupBySeverity(changes);

            // Should mostly be INFO (additions)
            assertFalse(bySeverity.get(Severity.INFO).isEmpty(),
                    "Should have INFO changes (additions)");

            // Verify specific additions
            assertTrue(changes.stream().anyMatch(c ->
                            c.getCategory() == ChangeCategory.SERVICE &&
                                    c.getType() == ChangeType.ADDED &&
                                    c.getPath().contains("NotificationService")),
                    "Should detect NotificationService added");

            assertTrue(changes.stream().anyMatch(c ->
                            c.getCategory() == ChangeCategory.RPC_METHOD &&
                                    c.getType() == ChangeType.ADDED),
                    "Should detect new RPC methods added");

            assertTrue(changes.stream().anyMatch(c ->
                            c.getCategory() == ChangeCategory.ENUM_VALUE &&
                                    c.getType() == ChangeType.ADDED),
                    "Should detect new enum values added");

            // Print summary
            System.out.println("=== Minor Changes Summary (v1 -> v2_minor) ===");
            System.out.println("Total changes: " + changes.size());
            System.out.println("BREAKING: " + bySeverity.get(Severity.BREAKING).size());
            System.out.println("DANGEROUS: " + bySeverity.get(Severity.DANGEROUS).size());
            System.out.println("WARNING: " + bySeverity.get(Severity.WARNING).size());
            System.out.println("INFO: " + bySeverity.get(Severity.INFO).size());
        }
    }

    @Nested
    @DisplayName("ApiSpec Conversion")
    class ApiSpecConversion {

        @Test
        @DisplayName("Should convert ProtoFile to ApiSpec")
        void shouldConvertProtoFileToApiSpec() {
            ProtoFile protoFile = loadProtoFile("user_service_v1.proto");

            ApiSpec apiSpec = parser.toApiSpec(protoFile);

            assertNotNull(apiSpec);
            assertNotNull(apiSpec.getName());
            assertTrue(apiSpec.getName().contains("com.example.user.v1"));

            // Check endpoints (one per RPC method)
            assertFalse(apiSpec.getEndpoints().isEmpty());
            assertTrue(apiSpec.getEndpoints().size() >= 8); // 6 UserService + 2 BulkUserService

            // Check metadata contains schemas
            assertNotNull(apiSpec.getMetadata());
            assertTrue(apiSpec.getMetadata().containsKey("schemas"));

            // Print API info
            System.out.println("=== ApiSpec Info ===");
            System.out.println("Name: " + apiSpec.getName());
            System.out.println("Version: " + apiSpec.getVersion());
            System.out.println("Endpoints: " + apiSpec.getEndpoints().size());
            System.out.println("Type: " + apiSpec.getType());
        }

        @Test
        @DisplayName("Should compare ApiSpecs using core comparator")
        void shouldCompareApiSpecsUsingCoreComparator() {
            ProtoFile v1Proto = loadProtoFile("user_service_v1.proto");
            ProtoFile v2Proto = loadProtoFile("user_service_v2_minor.proto");

            ApiSpec v1 = parser.toApiSpec(v1Proto);
            ApiSpec v2 = parser.toApiSpec(v2Proto);

            Changelog changelog = apiComparator.compare(v1, v2);

            assertNotNull(changelog);
            assertFalse(changelog.getChanges().isEmpty());

            System.out.println("=== Core ApiComparator Changes ===");
            System.out.println("Total changes: " + changelog.getChanges().size());
        }
    }

    @Nested
    @DisplayName("Report Generation")
    class ReportGeneration {

        @Test
        @DisplayName("Should generate Markdown report for breaking changes")
        void shouldGenerateMarkdownReportForBreakingChanges() {
            ProtoFile v1 = loadProtoFile("user_service_v1.proto");
            ProtoFile v2 = loadProtoFile("user_service_v2_breaking.proto");

            List<BreakingChange> changes = protoComparator.compare(v1, v2);

            // Create changelog
            Changelog changelog = new Changelog();
            changelog.setFromVersion("1.0.0");
            changelog.setToVersion("2.0.0");
            changelog.getBreakingChanges().addAll(changes);

            Reporter reporter = ReporterFactory.create(ReportFormat.MARKDOWN);
            String report = reporter.report(changelog);

            assertNotNull(report);
            assertFalse(report.isEmpty());
            assertTrue(report.contains("Breaking Changes") || report.contains("breaking"));
            assertTrue(report.contains("2.0.0"));

            System.out.println("=== Markdown Report (excerpt) ===");
            System.out.println(report.substring(0, Math.min(2000, report.length())));
        }

        @Test
        @DisplayName("Should generate JSON report for breaking changes")
        void shouldGenerateJsonReportForBreakingChanges() {
            ProtoFile v1 = loadProtoFile("user_service_v1.proto");
            ProtoFile v2 = loadProtoFile("user_service_v2_breaking.proto");

            List<BreakingChange> changes = protoComparator.compare(v1, v2);

            Changelog changelog = new Changelog();
            changelog.setFromVersion("1.0.0");
            changelog.setToVersion("2.0.0");
            changelog.getBreakingChanges().addAll(changes);

            Reporter reporter = ReporterFactory.create(ReportFormat.JSON);
            String report = reporter.report(changelog);

            assertNotNull(report);
            assertFalse(report.isEmpty());
            assertTrue(report.startsWith("{"));
            assertTrue(report.contains("\"severity\""));

            System.out.println("=== JSON Report (excerpt) ===");
            System.out.println(report.substring(0, Math.min(1500, report.length())));
        }

        @Test
        @DisplayName("Should generate HTML report for breaking changes")
        void shouldGenerateHtmlReportForBreakingChanges() {
            ProtoFile v1 = loadProtoFile("user_service_v1.proto");
            ProtoFile v2 = loadProtoFile("user_service_v2_breaking.proto");

            List<BreakingChange> changes = protoComparator.compare(v1, v2);

            Changelog changelog = new Changelog();
            changelog.setFromVersion("1.0.0");
            changelog.setToVersion("2.0.0");
            changelog.getBreakingChanges().addAll(changes);

            Reporter reporter = ReporterFactory.create(ReportFormat.HTML);
            String report = reporter.report(changelog);

            assertNotNull(report);
            assertFalse(report.isEmpty());
            assertTrue(report.contains("<!DOCTYPE html>") || report.contains("<html"));
            assertTrue(report.contains("BREAKING") || report.contains("breaking"));
        }
    }

    @Nested
    @DisplayName("Complete Flow Tests")
    class CompleteFlowTests {

        @Test
        @DisplayName("Should execute complete changelog generation flow")
        void shouldExecuteCompleteChangelogGenerationFlow() {
            // 1. Parse proto files
            ProtoFile oldProto = loadProtoFile("user_service_v1.proto");
            ProtoFile newProto = loadProtoFile("user_service_v2_breaking.proto");

            assertNotNull(oldProto);
            assertNotNull(newProto);

            // 2. Compare and detect changes
            List<BreakingChange> changes = protoComparator.compare(oldProto, newProto);
            assertFalse(changes.isEmpty());

            // 3. Verify breaking changes exist
            assertTrue(protoComparator.hasBreakingChanges(changes));

            // 4. Get statistics
            Map<String, Object> stats = protoComparator.getStatistics(changes);
            assertTrue((int) stats.get("totalChanges") > 0);

            // 5. Create changelog
            Changelog changelog = new Changelog();
            changelog.setFromVersion("1.0.0");
            changelog.setToVersion("2.0.0");
            changelog.getBreakingChanges().addAll(changes);

            // 6. Generate reports in all formats
            for (ReportFormat format : ReportFormat.values()) {
                Reporter reporter = ReporterFactory.create(format);
                String report = reporter.report(changelog);
                assertNotNull(report);
                assertFalse(report.isEmpty());
            }

            // Print final summary
            System.out.println("=== Complete Flow Test Summary ===");
            System.out.println("Old proto: " + oldProto.getPackageName());
            System.out.println("New proto: " + newProto.getPackageName());
            System.out.println("Total changes detected: " + changes.size());
            System.out.println("Breaking changes: " + changes.stream()
                    .filter(c -> c.getSeverity() == Severity.BREAKING).count());
            System.out.println("Reports generated for all formats: MARKDOWN, JSON, HTML, CONSOLE");
        }
    }

    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {

        @Test
        @DisplayName("Should calculate proto file statistics")
        void shouldCalculateProtoFileStatistics() {
            ProtoFile protoFile = loadProtoFile("user_service_v1.proto");

            Map<String, Integer> stats = protoFile.getStatistics();

            assertEquals(2, stats.get("services"));
            assertTrue(stats.get("messages") >= 10);
            assertEquals(2, stats.get("enums"));
            assertTrue(stats.get("rpcMethods") >= 8);
            assertTrue(stats.get("fields") > 0);

            System.out.println("=== Proto File Statistics ===");
            stats.forEach((key, value) -> System.out.println(key + ": " + value));
        }

        @Test
        @DisplayName("Should get all gRPC paths")
        void shouldGetAllGrpcPaths() {
            ProtoFile protoFile = loadProtoFile("user_service_v1.proto");

            List<String> paths = protoFile.getAllGrpcPaths();

            assertFalse(paths.isEmpty());
            assertTrue(paths.stream().allMatch(p -> p.startsWith("/")));
            assertTrue(paths.stream().anyMatch(p -> p.contains("UserService")));
            assertTrue(paths.stream().anyMatch(p -> p.contains("BulkUserService")));

            System.out.println("=== gRPC Paths ===");
            paths.forEach(System.out::println);
        }
    }
}

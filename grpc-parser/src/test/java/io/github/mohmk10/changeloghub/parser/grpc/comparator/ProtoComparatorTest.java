package io.github.mohmk10.changeloghub.parser.grpc.comparator;

import io.github.mohmk10.changeloghub.core.model.BreakingChange;
import io.github.mohmk10.changeloghub.core.model.ChangeCategory;
import io.github.mohmk10.changeloghub.core.model.ChangeType;
import io.github.mohmk10.changeloghub.core.model.Severity;
import io.github.mohmk10.changeloghub.parser.grpc.DefaultGrpcParser;
import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProtoComparator Tests")
class ProtoComparatorTest {

    private ProtoComparator comparator;
    private DefaultGrpcParser parser;

    @BeforeEach
    void setUp() {
        comparator = new ProtoComparator();
        parser = new DefaultGrpcParser();
    }

    @Nested
    @DisplayName("Package Change Detection")
    class PackageChangeDetection {

        @Test
        @DisplayName("Should detect package change as breaking")
        void shouldDetectPackageChangeAsBreaking() {
            String oldProto = """
                syntax = "proto3";
                package com.example.api.v1;
                message User { string id = 1; }
                """;

            String newProto = """
                syntax = "proto3";
                package com.example.api.v2;
                message User { string id = 1; }
                """;

            ProtoFile oldFile = parser.parse(oldProto);
            ProtoFile newFile = parser.parse(newProto);

            List<BreakingChange> changes = comparator.compare(oldFile, newFile);

            assertTrue(changes.stream().anyMatch(c ->
                    c.getCategory() == ChangeCategory.PACKAGE &&
                            c.getSeverity() == Severity.BREAKING));
        }
    }

    @Nested
    @DisplayName("Service Change Detection")
    class ServiceChangeDetection {

        @Test
        @DisplayName("Should detect service removed as breaking")
        void shouldDetectServiceRemovedAsBreaking() {
            String oldProto = """
                syntax = "proto3";
                package test;
                message Request { string id = 1; }
                message Response { string data = 1; }
                service UserService {
                    rpc GetUser(Request) returns (Response);
                }
                service OrderService {
                    rpc GetOrder(Request) returns (Response);
                }
                """;

            String newProto = """
                syntax = "proto3";
                package test;
                message Request { string id = 1; }
                message Response { string data = 1; }
                service UserService {
                    rpc GetUser(Request) returns (Response);
                }
                """;

            ProtoFile oldFile = parser.parse(oldProto);
            ProtoFile newFile = parser.parse(newProto);

            List<BreakingChange> changes = comparator.compare(oldFile, newFile);

            assertTrue(changes.stream().anyMatch(c ->
                    c.getCategory() == ChangeCategory.SERVICE &&
                            c.getType() == ChangeType.REMOVED &&
                            c.getSeverity() == Severity.BREAKING));
        }

        @Test
        @DisplayName("Should detect service added as info")
        void shouldDetectServiceAddedAsInfo() {
            String oldProto = """
                syntax = "proto3";
                package test;
                message Request { string id = 1; }
                message Response { string data = 1; }
                service UserService {
                    rpc GetUser(Request) returns (Response);
                }
                """;

            String newProto = """
                syntax = "proto3";
                package test;
                message Request { string id = 1; }
                message Response { string data = 1; }
                service UserService {
                    rpc GetUser(Request) returns (Response);
                }
                service OrderService {
                    rpc GetOrder(Request) returns (Response);
                }
                """;

            ProtoFile oldFile = parser.parse(oldProto);
            ProtoFile newFile = parser.parse(newProto);

            List<BreakingChange> changes = comparator.compare(oldFile, newFile);

            assertTrue(changes.stream().anyMatch(c ->
                    c.getCategory() == ChangeCategory.SERVICE &&
                            c.getType() == ChangeType.ADDED &&
                            c.getSeverity() == Severity.INFO));
        }
    }

    @Nested
    @DisplayName("RPC Method Change Detection")
    class RpcMethodChangeDetection {

        @Test
        @DisplayName("Should detect RPC method removed as breaking")
        void shouldDetectRpcMethodRemovedAsBreaking() {
            String oldProto = """
                syntax = "proto3";
                package test;
                message Request { string id = 1; }
                message Response { string data = 1; }
                service UserService {
                    rpc GetUser(Request) returns (Response);
                    rpc DeleteUser(Request) returns (Response);
                }
                """;

            String newProto = """
                syntax = "proto3";
                package test;
                message Request { string id = 1; }
                message Response { string data = 1; }
                service UserService {
                    rpc GetUser(Request) returns (Response);
                }
                """;

            ProtoFile oldFile = parser.parse(oldProto);
            ProtoFile newFile = parser.parse(newProto);

            List<BreakingChange> changes = comparator.compare(oldFile, newFile);

            assertTrue(changes.stream().anyMatch(c ->
                    c.getCategory() == ChangeCategory.RPC_METHOD &&
                            c.getType() == ChangeType.REMOVED &&
                            c.getSeverity() == Severity.BREAKING));
        }

        @Test
        @DisplayName("Should detect input type change as breaking")
        void shouldDetectInputTypeChangeAsBreaking() {
            String oldProto = """
                syntax = "proto3";
                package test;
                message OldRequest { string id = 1; }
                message NewRequest { string id = 1; }
                message Response { string data = 1; }
                service UserService {
                    rpc GetUser(OldRequest) returns (Response);
                }
                """;

            String newProto = """
                syntax = "proto3";
                package test;
                message OldRequest { string id = 1; }
                message NewRequest { string id = 1; }
                message Response { string data = 1; }
                service UserService {
                    rpc GetUser(NewRequest) returns (Response);
                }
                """;

            ProtoFile oldFile = parser.parse(oldProto);
            ProtoFile newFile = parser.parse(newProto);

            List<BreakingChange> changes = comparator.compare(oldFile, newFile);

            assertTrue(changes.stream().anyMatch(c ->
                    c.getCategory() == ChangeCategory.RPC_METHOD &&
                            c.getSeverity() == Severity.BREAKING &&
                            c.getDescription().contains("input type")));
        }

        @Test
        @DisplayName("Should detect streaming type change as breaking")
        void shouldDetectStreamingTypeChangeAsBreaking() {
            String oldProto = """
                syntax = "proto3";
                package test;
                message Request { string id = 1; }
                message Response { string data = 1; }
                service UserService {
                    rpc WatchUsers(Request) returns (stream Response);
                }
                """;

            String newProto = """
                syntax = "proto3";
                package test;
                message Request { string id = 1; }
                message Response { string data = 1; }
                service UserService {
                    rpc WatchUsers(stream Request) returns (stream Response);
                }
                """;

            ProtoFile oldFile = parser.parse(oldProto);
            ProtoFile newFile = parser.parse(newProto);

            List<BreakingChange> changes = comparator.compare(oldFile, newFile);

            assertTrue(changes.stream().anyMatch(c ->
                    c.getCategory() == ChangeCategory.STREAMING_TYPE &&
                            c.getSeverity() == Severity.BREAKING));
        }
    }

    @Nested
    @DisplayName("Message Change Detection")
    class MessageChangeDetection {

        @Test
        @DisplayName("Should detect message removed as breaking")
        void shouldDetectMessageRemovedAsBreaking() {
            String oldProto = """
                syntax = "proto3";
                package test;
                message User { string id = 1; }
                message Order { string id = 1; }
                """;

            String newProto = """
                syntax = "proto3";
                package test;
                message User { string id = 1; }
                """;

            ProtoFile oldFile = parser.parse(oldProto);
            ProtoFile newFile = parser.parse(newProto);

            List<BreakingChange> changes = comparator.compare(oldFile, newFile);

            assertTrue(changes.stream().anyMatch(c ->
                    c.getCategory() == ChangeCategory.MESSAGE &&
                            c.getType() == ChangeType.REMOVED &&
                            c.getSeverity() == Severity.BREAKING));
        }

        @Test
        @DisplayName("Should detect message added as info")
        void shouldDetectMessageAddedAsInfo() {
            String oldProto = """
                syntax = "proto3";
                package test;
                message User { string id = 1; }
                """;

            String newProto = """
                syntax = "proto3";
                package test;
                message User { string id = 1; }
                message Order { string id = 1; }
                """;

            ProtoFile oldFile = parser.parse(oldProto);
            ProtoFile newFile = parser.parse(newProto);

            List<BreakingChange> changes = comparator.compare(oldFile, newFile);

            assertTrue(changes.stream().anyMatch(c ->
                    c.getCategory() == ChangeCategory.MESSAGE &&
                            c.getType() == ChangeType.ADDED &&
                            c.getSeverity() == Severity.INFO));
        }
    }

    @Nested
    @DisplayName("Field Change Detection")
    class FieldChangeDetection {

        @Test
        @DisplayName("Should detect field number change as breaking")
        void shouldDetectFieldNumberChangeAsBreaking() {
            String oldProto = """
                syntax = "proto3";
                package test;
                message User {
                    string id = 1;
                    string name = 2;
                }
                """;

            String newProto = """
                syntax = "proto3";
                package test;
                message User {
                    string id = 1;
                    string name = 3;
                }
                """;

            ProtoFile oldFile = parser.parse(oldProto);
            ProtoFile newFile = parser.parse(newProto);

            List<BreakingChange> changes = comparator.compare(oldFile, newFile);

            assertTrue(changes.stream().anyMatch(c ->
                    c.getCategory() == ChangeCategory.FIELD_NUMBER &&
                            c.getSeverity() == Severity.BREAKING));
        }

        @Test
        @DisplayName("Should detect incompatible type change as breaking")
        void shouldDetectIncompatibleTypeChangeAsBreaking() {
            String oldProto = """
                syntax = "proto3";
                package test;
                message User {
                    string name = 1;
                }
                """;

            String newProto = """
                syntax = "proto3";
                package test;
                message User {
                    int32 name = 1;
                }
                """;

            ProtoFile oldFile = parser.parse(oldProto);
            ProtoFile newFile = parser.parse(newProto);

            List<BreakingChange> changes = comparator.compare(oldFile, newFile);

            assertTrue(changes.stream().anyMatch(c ->
                    c.getCategory() == ChangeCategory.FIELD &&
                            c.getSeverity() == Severity.BREAKING));
        }

        @Test
        @DisplayName("Should detect field added as info")
        void shouldDetectFieldAddedAsInfo() {
            String oldProto = """
                syntax = "proto3";
                package test;
                message User {
                    string id = 1;
                }
                """;

            String newProto = """
                syntax = "proto3";
                package test;
                message User {
                    string id = 1;
                    string name = 2;
                }
                """;

            ProtoFile oldFile = parser.parse(oldProto);
            ProtoFile newFile = parser.parse(newProto);

            List<BreakingChange> changes = comparator.compare(oldFile, newFile);

            assertTrue(changes.stream().anyMatch(c ->
                    c.getCategory() == ChangeCategory.FIELD &&
                            c.getType() == ChangeType.ADDED &&
                            c.getSeverity() == Severity.INFO));
        }

        @Test
        @DisplayName("Should detect field deprecation as warning")
        void shouldDetectFieldDeprecationAsWarning() {
            String oldProto = """
                syntax = "proto3";
                package test;
                message User {
                    string name = 1;
                }
                """;

            String newProto = """
                syntax = "proto3";
                package test;
                message User {
                    string name = 1 [deprecated = true];
                }
                """;

            ProtoFile oldFile = parser.parse(oldProto);
            ProtoFile newFile = parser.parse(newProto);

            List<BreakingChange> changes = comparator.compare(oldFile, newFile);

            assertTrue(changes.stream().anyMatch(c ->
                    c.getCategory() == ChangeCategory.FIELD &&
                            c.getType() == ChangeType.DEPRECATED &&
                            c.getSeverity() == Severity.WARNING));
        }
    }

    @Nested
    @DisplayName("Enum Change Detection")
    class EnumChangeDetection {

        @Test
        @DisplayName("Should detect enum value removed as breaking")
        void shouldDetectEnumValueRemovedAsBreaking() {
            String oldProto = """
                syntax = "proto3";
                package test;
                enum Status {
                    UNKNOWN = 0;
                    ACTIVE = 1;
                    INACTIVE = 2;
                }
                """;

            String newProto = """
                syntax = "proto3";
                package test;
                enum Status {
                    UNKNOWN = 0;
                    ACTIVE = 1;
                }
                """;

            ProtoFile oldFile = parser.parse(oldProto);
            ProtoFile newFile = parser.parse(newProto);

            List<BreakingChange> changes = comparator.compare(oldFile, newFile);

            assertTrue(changes.stream().anyMatch(c ->
                    c.getCategory() == ChangeCategory.ENUM_VALUE &&
                            c.getType() == ChangeType.REMOVED &&
                            c.getSeverity() == Severity.BREAKING));
        }

        @Test
        @DisplayName("Should detect enum value added as info")
        void shouldDetectEnumValueAddedAsInfo() {
            String oldProto = """
                syntax = "proto3";
                package test;
                enum Status {
                    UNKNOWN = 0;
                    ACTIVE = 1;
                }
                """;

            String newProto = """
                syntax = "proto3";
                package test;
                enum Status {
                    UNKNOWN = 0;
                    ACTIVE = 1;
                    INACTIVE = 2;
                }
                """;

            ProtoFile oldFile = parser.parse(oldProto);
            ProtoFile newFile = parser.parse(newProto);

            List<BreakingChange> changes = comparator.compare(oldFile, newFile);

            assertTrue(changes.stream().anyMatch(c ->
                    c.getCategory() == ChangeCategory.ENUM_VALUE &&
                            c.getType() == ChangeType.ADDED &&
                            c.getSeverity() == Severity.INFO));
        }
    }

    @Nested
    @DisplayName("Change Grouping and Filtering")
    class ChangeGroupingAndFiltering {

        @Test
        @DisplayName("Should group changes by severity")
        void shouldGroupChangesBySeverity() {
            String oldProto = """
                syntax = "proto3";
                package test;
                message User {
                    string id = 1;
                    string name = 2;
                }
                """;

            String newProto = """
                syntax = "proto3";
                package test;
                message User {
                    string id = 1;
                    string name = 2 [deprecated = true];
                    string email = 3;
                }
                """;

            ProtoFile oldFile = parser.parse(oldProto);
            ProtoFile newFile = parser.parse(newProto);

            List<BreakingChange> changes = comparator.compare(oldFile, newFile);
            Map<Severity, List<BreakingChange>> grouped = comparator.groupBySeverity(changes);

            assertFalse(grouped.get(Severity.INFO).isEmpty()); // field added
            assertFalse(grouped.get(Severity.WARNING).isEmpty()); // deprecation
        }

        @Test
        @DisplayName("Should check for breaking changes")
        void shouldCheckForBreakingChanges() {
            String oldProto = """
                syntax = "proto3";
                package test;
                message User { string id = 1; }
                """;

            String newProto = """
                syntax = "proto3";
                package test;
                message User { int32 id = 1; }
                """;

            ProtoFile oldFile = parser.parse(oldProto);
            ProtoFile newFile = parser.parse(newProto);

            List<BreakingChange> changes = comparator.compare(oldFile, newFile);

            assertTrue(comparator.hasBreakingChanges(changes));
        }

        @Test
        @DisplayName("Should get statistics")
        void shouldGetStatistics() {
            String oldProto = """
                syntax = "proto3";
                package test;
                message User {
                    string id = 1;
                    string name = 2;
                }
                """;

            String newProto = """
                syntax = "proto3";
                package test;
                message User {
                    string id = 1;
                    string name = 3;
                    string email = 4;
                }
                """;

            ProtoFile oldFile = parser.parse(oldProto);
            ProtoFile newFile = parser.parse(newProto);

            List<BreakingChange> changes = comparator.compare(oldFile, newFile);
            Map<String, Object> stats = comparator.getStatistics(changes);

            assertTrue((int) stats.get("totalChanges") > 0);
            assertNotNull(stats.get("bySeverity"));
            assertNotNull(stats.get("byType"));
        }

        @Test
        @DisplayName("Should filter by minimum severity")
        void shouldFilterByMinimumSeverity() {
            String oldProto = """
                syntax = "proto3";
                package test;
                message User {
                    string id = 1;
                    string name = 2;
                }
                """;

            String newProto = """
                syntax = "proto3";
                package test;
                message User {
                    string id = 1;
                    string name = 2 [deprecated = true];
                    string email = 3;
                }
                """;

            ProtoFile oldFile = parser.parse(oldProto);
            ProtoFile newFile = parser.parse(newProto);

            List<BreakingChange> allChanges = comparator.compare(oldFile, newFile);
            List<BreakingChange> warningAndAbove = comparator.filterByMinSeverity(allChanges, Severity.WARNING);

            assertTrue(warningAndAbove.stream().noneMatch(c -> c.getSeverity() == Severity.INFO));
        }
    }

    @Nested
    @DisplayName("No Changes Detection")
    class NoChangesDetection {

        @Test
        @DisplayName("Should return empty list when no changes")
        void shouldReturnEmptyListWhenNoChanges() {
            String proto = """
                syntax = "proto3";
                package test;
                message User { string id = 1; }
                service UserService {
                    rpc GetUser(User) returns (User);
                }
                """;

            ProtoFile oldFile = parser.parse(proto);
            ProtoFile newFile = parser.parse(proto);

            List<BreakingChange> changes = comparator.compare(oldFile, newFile);

            assertTrue(changes.isEmpty());
        }
    }
}

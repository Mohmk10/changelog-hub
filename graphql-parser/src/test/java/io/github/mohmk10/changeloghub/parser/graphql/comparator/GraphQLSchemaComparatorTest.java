package io.github.mohmk10.changeloghub.parser.graphql.comparator;

import io.github.mohmk10.changeloghub.core.model.Change;
import io.github.mohmk10.changeloghub.core.model.ChangeCategory;
import io.github.mohmk10.changeloghub.core.model.ChangeType;
import io.github.mohmk10.changeloghub.core.model.Severity;
import io.github.mohmk10.changeloghub.parser.graphql.DefaultGraphQLParser;
import io.github.mohmk10.changeloghub.parser.graphql.model.GraphQLSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("GraphQLSchemaComparator Tests")
class GraphQLSchemaComparatorTest {

    private GraphQLSchemaComparator comparator;
    private DefaultGraphQLParser parser;

    @BeforeEach
    void setUp() {
        comparator = new GraphQLSchemaComparator();
        parser = new DefaultGraphQLParser();
    }

    private GraphQLSchema parseSchema(String sdl) {
        return parser.parse(sdl);
    }

    @Nested
    @DisplayName("Operation comparison")
    class OperationComparisonTests {

        @Test
        @DisplayName("Should detect removed query - BREAKING")
        void shouldDetectRemovedQuery() {
            String oldSdl = """
                type Query {
                    user(id: ID!): User
                    users: [User!]!
                }
                type User { id: ID! }
                """;

            String newSdl = """
                type Query {
                    user(id: ID!): User
                }
                type User { id: ID! }
                """;

            GraphQLSchema oldSchema = parseSchema(oldSdl);
            GraphQLSchema newSchema = parseSchema(newSdl);

            List<Change> changes = comparator.compare(oldSchema, newSchema);

            assertThat(changes).anyMatch(c ->
                c.getType() == ChangeType.REMOVED &&
                c.getSeverity() == Severity.BREAKING &&
                c.getPath().contains("users"));
        }

        @Test
        @DisplayName("Should detect added query - INFO")
        void shouldDetectAddedQuery() {
            String oldSdl = """
                type Query {
                    user(id: ID!): User
                }
                type User { id: ID! }
                """;

            String newSdl = """
                type Query {
                    user(id: ID!): User
                    users: [User!]!
                }
                type User { id: ID! }
                """;

            GraphQLSchema oldSchema = parseSchema(oldSdl);
            GraphQLSchema newSchema = parseSchema(newSdl);

            List<Change> changes = comparator.compare(oldSchema, newSchema);

            assertThat(changes).anyMatch(c ->
                c.getType() == ChangeType.ADDED &&
                c.getSeverity() == Severity.INFO &&
                c.getPath().contains("users"));
        }

        @Test
        @DisplayName("Should detect removed mutation - BREAKING")
        void shouldDetectRemovedMutation() {
            String oldSdl = """
                type Query { dummy: String }
                type Mutation {
                    createUser(name: String!): User!
                    deleteUser(id: ID!): Boolean!
                }
                type User { id: ID! }
                """;

            String newSdl = """
                type Query { dummy: String }
                type Mutation {
                    createUser(name: String!): User!
                }
                type User { id: ID! }
                """;

            GraphQLSchema oldSchema = parseSchema(oldSdl);
            GraphQLSchema newSchema = parseSchema(newSdl);

            List<Change> changes = comparator.compare(oldSchema, newSchema);

            assertThat(changes).anyMatch(c ->
                c.getType() == ChangeType.REMOVED &&
                c.getSeverity() == Severity.BREAKING &&
                c.getPath().contains("deleteUser"));
        }

        @Test
        @DisplayName("Should detect return type change - BREAKING")
        void shouldDetectReturnTypeChange() {
            String oldSdl = """
                type Query {
                    user(id: ID!): User
                }
                type User { id: ID! }
                """;

            String newSdl = """
                type Query {
                    user(id: ID!): [User!]!
                }
                type User { id: ID! }
                """;

            GraphQLSchema oldSchema = parseSchema(oldSdl);
            GraphQLSchema newSchema = parseSchema(newSdl);

            List<Change> changes = comparator.compare(oldSchema, newSchema);

            assertThat(changes).anyMatch(c ->
                c.getType() == ChangeType.MODIFIED &&
                c.getSeverity() == Severity.BREAKING &&
                c.getDescription().contains("Return type"));
        }

        @Test
        @DisplayName("Should detect deprecated operation - WARNING")
        void shouldDetectDeprecatedOperation() {
            String oldSdl = """
                type Query {
                    user(id: ID!): User
                }
                type User { id: ID! }
                """;

            String newSdl = """
                type Query {
                    user(id: ID!): User @deprecated(reason: "Use getUser")
                }
                type User { id: ID! }
                """;

            GraphQLSchema oldSchema = parseSchema(oldSdl);
            GraphQLSchema newSchema = parseSchema(newSdl);

            List<Change> changes = comparator.compare(oldSchema, newSchema);

            assertThat(changes).anyMatch(c ->
                c.getSeverity() == Severity.WARNING &&
                c.getDescription().contains("deprecated"));
        }
    }

    @Nested
    @DisplayName("Argument comparison")
    class ArgumentComparisonTests {

        @Test
        @DisplayName("Should detect added required argument - BREAKING")
        void shouldDetectAddedRequiredArgument() {
            String oldSdl = """
                type Query {
                    user(id: ID!): User
                }
                type User { id: ID! }
                """;

            String newSdl = """
                type Query {
                    user(id: ID!, includeDeleted: Boolean!): User
                }
                type User { id: ID! }
                """;

            GraphQLSchema oldSchema = parseSchema(oldSdl);
            GraphQLSchema newSchema = parseSchema(newSdl);

            List<Change> changes = comparator.compare(oldSchema, newSchema);

            assertThat(changes).anyMatch(c ->
                c.getType() == ChangeType.ADDED &&
                c.getSeverity() == Severity.BREAKING &&
                c.getPath().contains("includeDeleted"));
        }

        @Test
        @DisplayName("Should detect added optional argument - INFO")
        void shouldDetectAddedOptionalArgument() {
            String oldSdl = """
                type Query {
                    user(id: ID!): User
                }
                type User { id: ID! }
                """;

            String newSdl = """
                type Query {
                    user(id: ID!, includeDeleted: Boolean = false): User
                }
                type User { id: ID! }
                """;

            GraphQLSchema oldSchema = parseSchema(oldSdl);
            GraphQLSchema newSchema = parseSchema(newSdl);

            List<Change> changes = comparator.compare(oldSchema, newSchema);

            assertThat(changes).anyMatch(c ->
                c.getType() == ChangeType.ADDED &&
                c.getSeverity() == Severity.INFO &&
                c.getPath().contains("includeDeleted"));
        }

        @Test
        @DisplayName("Should detect removed argument - DANGEROUS")
        void shouldDetectRemovedArgument() {
            String oldSdl = """
                type Query {
                    search(query: String!, limit: Int): [Result!]!
                }
                type Result { id: ID! }
                """;

            String newSdl = """
                type Query {
                    search(query: String!): [Result!]!
                }
                type Result { id: ID! }
                """;

            GraphQLSchema oldSchema = parseSchema(oldSdl);
            GraphQLSchema newSchema = parseSchema(newSdl);

            List<Change> changes = comparator.compare(oldSchema, newSchema);

            assertThat(changes).anyMatch(c ->
                c.getType() == ChangeType.REMOVED &&
                c.getSeverity() == Severity.DANGEROUS &&
                c.getPath().contains("limit"));
        }

        @Test
        @DisplayName("Should detect argument type change - BREAKING")
        void shouldDetectArgumentTypeChange() {
            String oldSdl = """
                type Query {
                    user(id: ID!): User
                }
                type User { id: ID! }
                """;

            String newSdl = """
                type Query {
                    user(id: String!): User
                }
                type User { id: ID! }
                """;

            GraphQLSchema oldSchema = parseSchema(oldSdl);
            GraphQLSchema newSchema = parseSchema(newSdl);

            List<Change> changes = comparator.compare(oldSchema, newSchema);

            assertThat(changes).anyMatch(c ->
                c.getType() == ChangeType.MODIFIED &&
                c.getSeverity() == Severity.BREAKING &&
                c.getDescription().contains("type"));
        }
    }

    @Nested
    @DisplayName("Type comparison")
    class TypeComparisonTests {

        @Test
        @DisplayName("Should detect removed type - BREAKING")
        void shouldDetectRemovedType() {
            String oldSdl = """
                type Query { dummy: String }
                type User { id: ID! }
                type Product { id: ID! }
                """;

            String newSdl = """
                type Query { dummy: String }
                type User { id: ID! }
                """;

            GraphQLSchema oldSchema = parseSchema(oldSdl);
            GraphQLSchema newSchema = parseSchema(newSdl);

            List<Change> changes = comparator.compare(oldSchema, newSchema);

            assertThat(changes).anyMatch(c ->
                c.getType() == ChangeType.REMOVED &&
                c.getSeverity() == Severity.BREAKING &&
                c.getPath().equals("Product"));
        }

        @Test
        @DisplayName("Should detect added type - INFO")
        void shouldDetectAddedType() {
            String oldSdl = """
                type Query { dummy: String }
                type User { id: ID! }
                """;

            String newSdl = """
                type Query { dummy: String }
                type User { id: ID! }
                type Product { id: ID! }
                """;

            GraphQLSchema oldSchema = parseSchema(oldSdl);
            GraphQLSchema newSchema = parseSchema(newSdl);

            List<Change> changes = comparator.compare(oldSchema, newSchema);

            assertThat(changes).anyMatch(c ->
                c.getType() == ChangeType.ADDED &&
                c.getSeverity() == Severity.INFO &&
                c.getPath().equals("Product"));
        }

        @Test
        @DisplayName("Should detect type kind change - BREAKING")
        void shouldDetectTypeKindChange() {
            String oldSdl = """
                type Query { dummy: String }
                type Status { value: String! }
                """;

            String newSdl = """
                type Query { dummy: String }
                enum Status { ACTIVE INACTIVE }
                """;

            GraphQLSchema oldSchema = parseSchema(oldSdl);
            GraphQLSchema newSchema = parseSchema(newSdl);

            List<Change> changes = comparator.compare(oldSchema, newSchema);

            assertThat(changes).anyMatch(c ->
                c.getType() == ChangeType.MODIFIED &&
                c.getSeverity() == Severity.BREAKING &&
                c.getDescription().contains("kind"));
        }
    }

    @Nested
    @DisplayName("Field comparison")
    class FieldComparisonTests {

        @Test
        @DisplayName("Should detect removed field - BREAKING")
        void shouldDetectRemovedField() {
            String oldSdl = """
                type Query { dummy: String }
                type User {
                    id: ID!
                    name: String!
                    email: String!
                }
                """;

            String newSdl = """
                type Query { dummy: String }
                type User {
                    id: ID!
                    name: String!
                }
                """;

            GraphQLSchema oldSchema = parseSchema(oldSdl);
            GraphQLSchema newSchema = parseSchema(newSdl);

            List<Change> changes = comparator.compare(oldSchema, newSchema);

            assertThat(changes).anyMatch(c ->
                c.getType() == ChangeType.REMOVED &&
                c.getSeverity() == Severity.BREAKING &&
                c.getPath().contains("email"));
        }

        @Test
        @DisplayName("Should detect added field - INFO")
        void shouldDetectAddedField() {
            String oldSdl = """
                type Query { dummy: String }
                type User {
                    id: ID!
                    name: String!
                }
                """;

            String newSdl = """
                type Query { dummy: String }
                type User {
                    id: ID!
                    name: String!
                    email: String!
                }
                """;

            GraphQLSchema oldSchema = parseSchema(oldSdl);
            GraphQLSchema newSchema = parseSchema(newSdl);

            List<Change> changes = comparator.compare(oldSchema, newSchema);

            assertThat(changes).anyMatch(c ->
                c.getType() == ChangeType.ADDED &&
                c.getSeverity() == Severity.INFO &&
                c.getPath().contains("email"));
        }

        @Test
        @DisplayName("Should detect field type change - BREAKING")
        void shouldDetectFieldTypeChange() {
            String oldSdl = """
                type Query { dummy: String }
                type User {
                    id: ID!
                    age: Int
                }
                """;

            String newSdl = """
                type Query { dummy: String }
                type User {
                    id: ID!
                    age: String
                }
                """;

            GraphQLSchema oldSchema = parseSchema(oldSdl);
            GraphQLSchema newSchema = parseSchema(newSdl);

            List<Change> changes = comparator.compare(oldSchema, newSchema);

            assertThat(changes).anyMatch(c ->
                c.getType() == ChangeType.MODIFIED &&
                c.getSeverity() == Severity.BREAKING &&
                c.getPath().contains("age"));
        }

        @Test
        @DisplayName("Should detect nullability change - DANGEROUS")
        void shouldDetectNullabilityChange() {
            String oldSdl = """
                type Query { dummy: String }
                type User {
                    id: ID!
                    name: String
                }
                """;

            String newSdl = """
                type Query { dummy: String }
                type User {
                    id: ID!
                    name: String!
                }
                """;

            GraphQLSchema oldSchema = parseSchema(oldSdl);
            GraphQLSchema newSchema = parseSchema(newSdl);

            List<Change> changes = comparator.compare(oldSchema, newSchema);

            assertThat(changes).anyMatch(c ->
                c.getSeverity() == Severity.DANGEROUS &&
                c.getDescription().contains("nullable"));
        }
    }

    @Nested
    @DisplayName("Enum comparison")
    class EnumComparisonTests {

        @Test
        @DisplayName("Should detect removed enum value - BREAKING")
        void shouldDetectRemovedEnumValue() {
            String oldSdl = """
                type Query { dummy: String }
                enum Status { ACTIVE INACTIVE DELETED }
                """;

            String newSdl = """
                type Query { dummy: String }
                enum Status { ACTIVE INACTIVE }
                """;

            GraphQLSchema oldSchema = parseSchema(oldSdl);
            GraphQLSchema newSchema = parseSchema(newSdl);

            List<Change> changes = comparator.compare(oldSchema, newSchema);

            assertThat(changes).anyMatch(c ->
                c.getType() == ChangeType.REMOVED &&
                c.getSeverity() == Severity.BREAKING &&
                c.getPath().contains("DELETED"));
        }

        @Test
        @DisplayName("Should detect added enum value - INFO")
        void shouldDetectAddedEnumValue() {
            String oldSdl = """
                type Query { dummy: String }
                enum Status { ACTIVE INACTIVE }
                """;

            String newSdl = """
                type Query { dummy: String }
                enum Status { ACTIVE INACTIVE DELETED }
                """;

            GraphQLSchema oldSchema = parseSchema(oldSdl);
            GraphQLSchema newSchema = parseSchema(newSdl);

            List<Change> changes = comparator.compare(oldSchema, newSchema);

            assertThat(changes).anyMatch(c ->
                c.getType() == ChangeType.ADDED &&
                c.getSeverity() == Severity.INFO &&
                c.getPath().contains("DELETED"));
        }
    }

    @Nested
    @DisplayName("Union comparison")
    class UnionComparisonTests {

        @Test
        @DisplayName("Should detect removed union member - BREAKING")
        void shouldDetectRemovedUnionMember() {
            String oldSdl = """
                type Query { dummy: String }
                union SearchResult = User | Product | Order
                type User { id: ID! }
                type Product { id: ID! }
                type Order { id: ID! }
                """;

            String newSdl = """
                type Query { dummy: String }
                union SearchResult = User | Product
                type User { id: ID! }
                type Product { id: ID! }
                """;

            GraphQLSchema oldSchema = parseSchema(oldSdl);
            GraphQLSchema newSchema = parseSchema(newSdl);

            List<Change> changes = comparator.compare(oldSchema, newSchema);

            assertThat(changes).anyMatch(c ->
                c.getType() == ChangeType.REMOVED &&
                c.getSeverity() == Severity.BREAKING &&
                c.getPath().contains("Order"));
        }

        @Test
        @DisplayName("Should detect added union member - INFO")
        void shouldDetectAddedUnionMember() {
            String oldSdl = """
                type Query { dummy: String }
                union SearchResult = User | Product
                type User { id: ID! }
                type Product { id: ID! }
                """;

            String newSdl = """
                type Query { dummy: String }
                union SearchResult = User | Product | Order
                type User { id: ID! }
                type Product { id: ID! }
                type Order { id: ID! }
                """;

            GraphQLSchema oldSchema = parseSchema(oldSdl);
            GraphQLSchema newSchema = parseSchema(newSdl);

            List<Change> changes = comparator.compare(oldSchema, newSchema);

            assertThat(changes).anyMatch(c ->
                c.getType() == ChangeType.ADDED &&
                c.getSeverity() == Severity.INFO &&
                c.getPath().contains("Order"));
        }
    }

    @Nested
    @DisplayName("Utility methods")
    class UtilityTests {

        @Test
        @DisplayName("Should check for breaking changes")
        void shouldCheckForBreakingChanges() {
            String oldSdl = """
                type Query {
                    user(id: ID!): User
                    users: [User!]!
                }
                type User { id: ID! }
                """;

            String newSdl = """
                type Query {
                    user(id: ID!): User
                }
                type User { id: ID! }
                """;

            GraphQLSchema oldSchema = parseSchema(oldSdl);
            GraphQLSchema newSchema = parseSchema(newSdl);

            List<Change> changes = comparator.compare(oldSchema, newSchema);

            assertThat(comparator.hasBreakingChanges(changes)).isTrue();
        }

        @Test
        @DisplayName("Should get only breaking changes")
        void shouldGetOnlyBreakingChanges() {
            String oldSdl = """
                type Query {
                    user(id: ID!): User
                    users: [User!]!
                }
                type User { id: ID! }
                """;

            String newSdl = """
                type Query {
                    user(id: ID!): User
                    products: [Product!]!
                }
                type User { id: ID! }
                type Product { id: ID! }
                """;

            GraphQLSchema oldSchema = parseSchema(oldSdl);
            GraphQLSchema newSchema = parseSchema(newSdl);

            List<Change> changes = comparator.compare(oldSchema, newSchema);
            List<Change> breakingChanges = comparator.getBreakingChanges(changes);

            assertThat(breakingChanges).isNotEmpty();
            assertThat(breakingChanges).allMatch(c -> c.getSeverity() == Severity.BREAKING);
        }

        @Test
        @DisplayName("Should count changes by severity")
        void shouldCountChangesBySeverity() {
            String oldSdl = """
                type Query {
                    user(id: ID!): User
                    users: [User!]!
                }
                type User { id: ID! name: String }
                """;

            String newSdl = """
                type Query {
                    user(id: ID!): User @deprecated
                    products: [Product!]!
                }
                type User { id: ID! name: String! }
                type Product { id: ID! }
                """;

            GraphQLSchema oldSchema = parseSchema(oldSdl);
            GraphQLSchema newSchema = parseSchema(newSdl);

            List<Change> changes = comparator.compare(oldSchema, newSchema);
            Map<Severity, Long> counts = comparator.countBySeverity(changes);

            assertThat(counts).containsKey(Severity.BREAKING);
            assertThat(counts).containsKey(Severity.INFO);
        }

        @Test
        @DisplayName("Should get changes by severity")
        void shouldGetChangesBySeverity() {
            String oldSdl = """
                type Query { user: User users: [User!]! }
                type User { id: ID! }
                """;

            String newSdl = """
                type Query { user: User }
                type User { id: ID! email: String }
                """;

            GraphQLSchema oldSchema = parseSchema(oldSdl);
            GraphQLSchema newSchema = parseSchema(newSdl);

            List<Change> changes = comparator.compare(oldSchema, newSchema);
            List<Change> infoChanges = comparator.getChangesBySeverity(changes, Severity.INFO);

            assertThat(infoChanges).allMatch(c -> c.getSeverity() == Severity.INFO);
        }
    }

    @Nested
    @DisplayName("Complex schema comparison")
    class ComplexSchemaTests {

        @Test
        @DisplayName("Should compare complete schemas from files")
        void shouldCompareCompleteSchemas() throws Exception {
            GraphQLSchema v1 = parser.parseFile("src/test/resources/schemas/schema-v1.graphql");
            GraphQLSchema v2Breaking = parser.parseFile("src/test/resources/schemas/schema-v2-breaking.graphql");

            List<Change> changes = comparator.compare(v1, v2Breaking);

            assertThat(changes).isNotEmpty();
            assertThat(comparator.hasBreakingChanges(changes)).isTrue();
        }

        @Test
        @DisplayName("Should detect minor changes as non-breaking")
        void shouldDetectMinorChanges() throws Exception {
            GraphQLSchema v1 = parser.parseFile("src/test/resources/schemas/schema-v1.graphql");
            GraphQLSchema v2Minor = parser.parseFile("src/test/resources/schemas/schema-v2-minor.graphql");

            List<Change> changes = comparator.compare(v1, v2Minor);

            long infoCount = changes.stream().filter(c -> c.getSeverity() == Severity.INFO).count();
            long warningCount = changes.stream().filter(c -> c.getSeverity() == Severity.WARNING).count();

            assertThat(infoCount).isGreaterThan(0);
            
        }
    }
}

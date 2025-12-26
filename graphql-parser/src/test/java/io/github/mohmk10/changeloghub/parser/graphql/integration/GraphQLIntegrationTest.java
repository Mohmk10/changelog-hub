package io.github.mohmk10.changeloghub.parser.graphql.integration;

import io.github.mohmk10.changeloghub.core.model.*;
import io.github.mohmk10.changeloghub.parser.graphql.DefaultGraphQLParser;
import io.github.mohmk10.changeloghub.parser.graphql.comparator.GraphQLSchemaComparator;
import io.github.mohmk10.changeloghub.parser.graphql.mapper.GraphQLModelMapper;
import io.github.mohmk10.changeloghub.parser.graphql.model.GraphQLSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("GraphQL Parser Integration Tests")
class GraphQLIntegrationTest {

    private DefaultGraphQLParser parser;
    private GraphQLSchemaComparator comparator;
    private GraphQLModelMapper mapper;

    @BeforeEach
    void setUp() {
        parser = new DefaultGraphQLParser();
        comparator = new GraphQLSchemaComparator();
        mapper = new GraphQLModelMapper();
    }

    @Nested
    @DisplayName("Complete parsing flow")
    class ParsingFlowTests {

        @Test
        @DisplayName("Should parse v1 schema completely")
        void shouldParseV1Schema() throws Exception {
            GraphQLSchema schema = parser.parseFile("src/test/resources/schemas/schema-v1.graphql");

            assertThat(schema.getTypes()).isNotEmpty();
            assertThat(schema.getTypes()).containsKeys("User", "Product", "Order", "Review");
            assertThat(schema.getTypes()).containsKeys("UserRole", "OrderStatus", "ProductCategory");
            assertThat(schema.getTypes()).containsKeys("Node", "Timestamped");
            assertThat(schema.getTypes()).containsKey("SearchResult");
            assertThat(schema.getTypes()).containsKeys("CreateUserInput", "ProductFilter");

            assertThat(schema.getQueries()).isNotEmpty();
            assertThat(schema.getMutations()).isNotEmpty();
            assertThat(schema.getSubscriptions()).isNotEmpty();
        }

        @Test
        @DisplayName("Should convert schema to ApiSpec")
        void shouldConvertSchemaToApiSpec() throws Exception {
            GraphQLSchema schema = parser.parseFile("src/test/resources/schemas/schema-v1.graphql");
            ApiSpec apiSpec = mapper.mapToApiSpec(schema);

            assertThat(apiSpec.getType()).isEqualTo(ApiType.GRAPHQL);
            assertThat(apiSpec.getEndpoints()).isNotEmpty();

            assertThat(apiSpec.getEndpoints())
                    .anyMatch(e -> e.getMethod() == HttpMethod.GET && e.getTags().contains("query"));
            assertThat(apiSpec.getEndpoints())
                    .anyMatch(e -> e.getMethod() == HttpMethod.POST && e.getTags().contains("mutation"));
        }
    }

    @Nested
    @DisplayName("Breaking change detection flow")
    class BreakingChangeFlowTests {

        @Test
        @DisplayName("Should detect breaking changes between v1 and v2-breaking")
        void shouldDetectBreakingChanges() throws Exception {
            GraphQLSchema v1 = parser.parseFile("src/test/resources/schemas/schema-v1.graphql");
            GraphQLSchema v2 = parser.parseFile("src/test/resources/schemas/schema-v2-breaking.graphql");

            List<Change> changes = comparator.compare(v1, v2);

            assertThat(changes).isNotEmpty();
            assertThat(comparator.hasBreakingChanges(changes)).isTrue();

            List<Change> breakingChanges = comparator.getBreakingChanges(changes);
            assertThat(breakingChanges).isNotEmpty();

            assertThat(breakingChanges).anyMatch(c ->
                c.getType() == ChangeType.REMOVED &&
                c.getCategory() == ChangeCategory.ENDPOINT);

            assertThat(breakingChanges).anyMatch(c ->
                c.getCategory() == ChangeCategory.TYPE ||
                c.getCategory() == ChangeCategory.FIELD);
        }

        @Test
        @DisplayName("Should detect removed query 'me'")
        void shouldDetectRemovedMeQuery() throws Exception {
            GraphQLSchema v1 = parser.parseFile("src/test/resources/schemas/schema-v1.graphql");
            GraphQLSchema v2 = parser.parseFile("src/test/resources/schemas/schema-v2-breaking.graphql");

            List<Change> changes = comparator.compare(v1, v2);

            assertThat(changes).anyMatch(c ->
                c.getPath().contains("me") &&
                c.getType() == ChangeType.REMOVED &&
                c.getSeverity() == Severity.BREAKING);
        }

        @Test
        @DisplayName("Should detect removed mutation 'deleteUser'")
        void shouldDetectRemovedDeleteUserMutation() throws Exception {
            GraphQLSchema v1 = parser.parseFile("src/test/resources/schemas/schema-v1.graphql");
            GraphQLSchema v2 = parser.parseFile("src/test/resources/schemas/schema-v2-breaking.graphql");

            List<Change> changes = comparator.compare(v1, v2);

            assertThat(changes).anyMatch(c ->
                c.getPath().contains("deleteUser") &&
                c.getType() == ChangeType.REMOVED &&
                c.getSeverity() == Severity.BREAKING);
        }

        @Test
        @DisplayName("Should detect removed enum value")
        void shouldDetectRemovedEnumValue() throws Exception {
            GraphQLSchema v1 = parser.parseFile("src/test/resources/schemas/schema-v1.graphql");
            GraphQLSchema v2 = parser.parseFile("src/test/resources/schemas/schema-v2-breaking.graphql");

            List<Change> changes = comparator.compare(v1, v2);

            assertThat(changes).anyMatch(c ->
                c.getPath().contains("GUEST") &&
                c.getType() == ChangeType.REMOVED &&
                c.getSeverity() == Severity.BREAKING);
        }

        @Test
        @DisplayName("Should detect removed type 'ProductCategory'")
        void shouldDetectRemovedType() throws Exception {
            GraphQLSchema v1 = parser.parseFile("src/test/resources/schemas/schema-v1.graphql");
            GraphQLSchema v2 = parser.parseFile("src/test/resources/schemas/schema-v2-breaking.graphql");

            List<Change> changes = comparator.compare(v1, v2);

            assertThat(changes).anyMatch(c ->
                c.getPath().equals("ProductCategory") &&
                c.getType() == ChangeType.REMOVED &&
                c.getSeverity() == Severity.BREAKING);
        }

        @Test
        @DisplayName("Should detect added required argument")
        void shouldDetectAddedRequiredArgument() throws Exception {
            GraphQLSchema v1 = parser.parseFile("src/test/resources/schemas/schema-v1.graphql");
            GraphQLSchema v2 = parser.parseFile("src/test/resources/schemas/schema-v2-breaking.graphql");

            List<Change> changes = comparator.compare(v1, v2);

            assertThat(changes).anyMatch(c ->
                c.getType() == ChangeType.MODIFIED &&
                c.getSeverity() == Severity.BREAKING &&
                (c.getPath().contains("orders") || c.getPath().contains("product")));
        }
    }

    @Nested
    @DisplayName("Minor change detection flow")
    class MinorChangeFlowTests {

        @Test
        @DisplayName("Should detect minor changes between v1 and v2-minor")
        void shouldDetectMinorChanges() throws Exception {
            GraphQLSchema v1 = parser.parseFile("src/test/resources/schemas/schema-v1.graphql");
            GraphQLSchema v2 = parser.parseFile("src/test/resources/schemas/schema-v2-minor.graphql");

            List<Change> changes = comparator.compare(v1, v2);

            assertThat(changes).isNotEmpty();

            List<Change> infoChanges = comparator.getChangesBySeverity(changes, Severity.INFO);
            assertThat(infoChanges).isNotEmpty();

            List<Change> warningChanges = comparator.getChangesBySeverity(changes, Severity.WARNING);
            assertThat(warningChanges).isNotEmpty();
        }

        @Test
        @DisplayName("Should detect new queries added")
        void shouldDetectNewQueries() throws Exception {
            GraphQLSchema v1 = parser.parseFile("src/test/resources/schemas/schema-v1.graphql");
            GraphQLSchema v2 = parser.parseFile("src/test/resources/schemas/schema-v2-minor.graphql");

            List<Change> changes = comparator.compare(v1, v2);

            assertThat(changes).anyMatch(c ->
                c.getType() == ChangeType.ADDED &&
                c.getSeverity() == Severity.INFO &&
                c.getCategory() == ChangeCategory.ENDPOINT);
        }

        @Test
        @DisplayName("Should detect new enum values added")
        void shouldDetectNewEnumValues() throws Exception {
            GraphQLSchema v1 = parser.parseFile("src/test/resources/schemas/schema-v1.graphql");
            GraphQLSchema v2 = parser.parseFile("src/test/resources/schemas/schema-v2-minor.graphql");

            List<Change> changes = comparator.compare(v1, v2);

            assertThat(changes).anyMatch(c ->
                c.getType() == ChangeType.ADDED &&
                c.getCategory() == ChangeCategory.ENUM_VALUE &&
                c.getSeverity() == Severity.INFO);
        }

        @Test
        @DisplayName("Should detect deprecations as warnings")
        void shouldDetectDeprecations() throws Exception {
            GraphQLSchema v1 = parser.parseFile("src/test/resources/schemas/schema-v1.graphql");
            GraphQLSchema v2 = parser.parseFile("src/test/resources/schemas/schema-v2-minor.graphql");

            List<Change> changes = comparator.compare(v1, v2);

            assertThat(changes).anyMatch(c ->
                c.getSeverity() == Severity.WARNING &&
                c.getDescription().contains("deprecated"));
        }

        @Test
        @DisplayName("Should detect new fields added to types")
        void shouldDetectNewFields() throws Exception {
            GraphQLSchema v1 = parser.parseFile("src/test/resources/schemas/schema-v1.graphql");
            GraphQLSchema v2 = parser.parseFile("src/test/resources/schemas/schema-v2-minor.graphql");

            List<Change> changes = comparator.compare(v1, v2);

            assertThat(changes).anyMatch(c ->
                c.getType() == ChangeType.ADDED &&
                c.getCategory() == ChangeCategory.FIELD &&
                c.getSeverity() == Severity.INFO);
        }

        @Test
        @DisplayName("Should detect new union member added")
        void shouldDetectNewUnionMember() throws Exception {
            GraphQLSchema v1 = parser.parseFile("src/test/resources/schemas/schema-v1.graphql");
            GraphQLSchema v2 = parser.parseFile("src/test/resources/schemas/schema-v2-minor.graphql");

            List<Change> changes = comparator.compare(v1, v2);

            assertThat(changes).anyMatch(c ->
                c.getType() == ChangeType.ADDED &&
                c.getCategory() == ChangeCategory.UNION_MEMBER &&
                c.getPath().contains("Review"));
        }
    }

    @Nested
    @DisplayName("Change statistics")
    class ChangeStatisticsTests {

        @Test
        @DisplayName("Should count changes by severity for breaking version")
        void shouldCountBreakingVersionChanges() throws Exception {
            GraphQLSchema v1 = parser.parseFile("src/test/resources/schemas/schema-v1.graphql");
            GraphQLSchema v2 = parser.parseFile("src/test/resources/schemas/schema-v2-breaking.graphql");

            List<Change> changes = comparator.compare(v1, v2);
            Map<Severity, Long> counts = comparator.countBySeverity(changes);

            assertThat(counts.get(Severity.BREAKING)).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should count changes by severity for minor version")
        void shouldCountMinorVersionChanges() throws Exception {
            GraphQLSchema v1 = parser.parseFile("src/test/resources/schemas/schema-v1.graphql");
            GraphQLSchema v2 = parser.parseFile("src/test/resources/schemas/schema-v2-minor.graphql");

            List<Change> changes = comparator.compare(v1, v2);
            Map<Severity, Long> counts = comparator.countBySeverity(changes);

            assertThat(counts.get(Severity.INFO)).isGreaterThan(0);
            assertThat(counts.get(Severity.WARNING)).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("End-to-end workflow")
    class EndToEndWorkflowTests {

        @Test
        @DisplayName("Should complete full workflow: parse -> compare -> report")
        void shouldCompleteFullWorkflow() throws Exception {
            
            GraphQLSchema v1 = parser.parseFile("src/test/resources/schemas/schema-v1.graphql");
            GraphQLSchema v2 = parser.parseFile("src/test/resources/schemas/schema-v2-breaking.graphql");

            assertThat(v1).isNotNull();
            assertThat(v2).isNotNull();

            ApiSpec apiSpec1 = mapper.mapToApiSpec(v1);
            ApiSpec apiSpec2 = mapper.mapToApiSpec(v2);

            assertThat(apiSpec1.getType()).isEqualTo(ApiType.GRAPHQL);
            assertThat(apiSpec2.getType()).isEqualTo(ApiType.GRAPHQL);

            List<Change> changes = comparator.compare(v1, v2);

            assertThat(changes).isNotEmpty();

            boolean hasBreaking = comparator.hasBreakingChanges(changes);
            List<Change> breakingChanges = comparator.getBreakingChanges(changes);
            Map<Severity, Long> counts = comparator.countBySeverity(changes);

            assertThat(hasBreaking).isTrue();
            assertThat(breakingChanges).isNotEmpty();
            assertThat(counts).isNotEmpty();

            for (Change change : breakingChanges) {
                assertThat(change.getPath()).isNotBlank();
                assertThat(change.getDescription()).isNotBlank();
                assertThat(change.getSeverity()).isEqualTo(Severity.BREAKING);
            }
        }

        @Test
        @DisplayName("Should parse and validate schema")
        void shouldParseAndValidateSchema() throws Exception {
            
            boolean isValid = parser.validateFile(
                    new java.io.File("src/test/resources/schemas/schema-v1.graphql"));

            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("Should directly convert file to ApiSpec")
        void shouldDirectlyConvertFileToApiSpec() throws Exception {
            ApiSpec apiSpec = parser.parseFileToApiSpec("src/test/resources/schemas/schema-v1.graphql");

            assertThat(apiSpec).isNotNull();
            assertThat(apiSpec.getType()).isEqualTo(ApiType.GRAPHQL);
            assertThat(apiSpec.getEndpoints()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Type-specific analysis")
    class TypeSpecificAnalysisTests {

        @Test
        @DisplayName("Should identify all object types")
        void shouldIdentifyObjectTypes() throws Exception {
            GraphQLSchema schema = parser.parseFile("src/test/resources/schemas/schema-v1.graphql");

            long objectTypeCount = schema.getTypes().values().stream()
                    .filter(t -> t.isObjectType())
                    .count();

            assertThat(objectTypeCount).isGreaterThanOrEqualTo(5);
        }

        @Test
        @DisplayName("Should identify all enum types")
        void shouldIdentifyEnumTypes() throws Exception {
            GraphQLSchema schema = parser.parseFile("src/test/resources/schemas/schema-v1.graphql");

            long enumCount = schema.getTypes().values().stream()
                    .filter(t -> t.isEnum())
                    .count();

            assertThat(enumCount).isGreaterThanOrEqualTo(3);
        }

        @Test
        @DisplayName("Should identify all interface types")
        void shouldIdentifyInterfaceTypes() throws Exception {
            GraphQLSchema schema = parser.parseFile("src/test/resources/schemas/schema-v1.graphql");

            long interfaceCount = schema.getTypes().values().stream()
                    .filter(t -> t.isInterface())
                    .count();

            assertThat(interfaceCount).isGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("Should identify union types")
        void shouldIdentifyUnionTypes() throws Exception {
            GraphQLSchema schema = parser.parseFile("src/test/resources/schemas/schema-v1.graphql");

            long unionCount = schema.getTypes().values().stream()
                    .filter(t -> t.isUnion())
                    .count();

            assertThat(unionCount).isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("Should identify input types")
        void shouldIdentifyInputTypes() throws Exception {
            GraphQLSchema schema = parser.parseFile("src/test/resources/schemas/schema-v1.graphql");

            long inputCount = schema.getTypes().values().stream()
                    .filter(t -> t.isInputType())
                    .count();

            assertThat(inputCount).isGreaterThanOrEqualTo(4);
        }
    }
}

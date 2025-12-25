package io.github.mohmk10.changeloghub.parser.graphql.analyzer;

import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.github.mohmk10.changeloghub.parser.graphql.model.GraphQLType;
import io.github.mohmk10.changeloghub.parser.graphql.util.GraphQLTypeKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TypeAnalyzer Tests")
class TypeAnalyzerTest {

    private TypeAnalyzer analyzer;
    private SchemaParser schemaParser;

    @BeforeEach
    void setUp() {
        analyzer = new TypeAnalyzer();
        schemaParser = new SchemaParser();
    }

    private TypeDefinitionRegistry parse(String sdl) {
        return schemaParser.parse(sdl);
    }

    private Map<String, GraphQLType> analyzeToMap(String sdl) {
        List<GraphQLType> types = analyzer.analyzeTypes(parse(sdl));
        return types.stream().collect(Collectors.toMap(GraphQLType::getName, t -> t));
    }

    @Nested
    @DisplayName("Object type analysis")
    class ObjectTypeTests {

        @Test
        @DisplayName("Should analyze simple object type")
        void shouldAnalyzeSimpleObjectType() {
            String sdl = """
                type User {
                    id: ID!
                    name: String!
                    email: String
                }
                """;

            Map<String, GraphQLType> types = analyzeToMap(sdl);

            assertThat(types).containsKey("User");
            GraphQLType user = types.get("User");
            assertThat(user.getKind()).isEqualTo(GraphQLTypeKind.OBJECT);
            assertThat(user.isObjectType()).isTrue();
            assertThat(user.getFields()).hasSize(3);
        }

        @Test
        @DisplayName("Should analyze object with interfaces")
        void shouldAnalyzeObjectWithInterfaces() {
            String sdl = """
                interface Node {
                    id: ID!
                }

                interface Timestamped {
                    createdAt: String!
                }

                type User implements Node & Timestamped {
                    id: ID!
                    name: String!
                    createdAt: String!
                }
                """;

            Map<String, GraphQLType> types = analyzeToMap(sdl);

            GraphQLType user = types.get("User");
            assertThat(user.getInterfaces()).containsExactlyInAnyOrder("Node", "Timestamped");
        }

        @Test
        @DisplayName("Should analyze nested types")
        void shouldAnalyzeNestedTypes() {
            String sdl = """
                type User {
                    id: ID!
                    address: Address!
                    orders: [Order!]!
                }

                type Address {
                    street: String!
                    city: String!
                }

                type Order {
                    id: ID!
                    total: Float!
                }
                """;

            Map<String, GraphQLType> types = analyzeToMap(sdl);

            assertThat(types).containsKeys("User", "Address", "Order");
        }
    }

    @Nested
    @DisplayName("Input type analysis")
    class InputTypeTests {

        @Test
        @DisplayName("Should analyze input type")
        void shouldAnalyzeInputType() {
            String sdl = """
                input CreateUserInput {
                    name: String!
                    email: String!
                    age: Int
                }
                """;

            Map<String, GraphQLType> types = analyzeToMap(sdl);

            assertThat(types).containsKey("CreateUserInput");
            GraphQLType input = types.get("CreateUserInput");
            assertThat(input.getKind()).isEqualTo(GraphQLTypeKind.INPUT_OBJECT);
            assertThat(input.isInputType()).isTrue();
            assertThat(input.getFields()).hasSize(3);
        }

        @Test
        @DisplayName("Should identify required input fields")
        void shouldIdentifyRequiredInputFields() {
            String sdl = """
                input UserInput {
                    required: String!
                    optional: String
                }
                """;

            Map<String, GraphQLType> types = analyzeToMap(sdl);
            GraphQLType input = types.get("UserInput");

            assertThat(input.getFields()).anyMatch(f ->
                f.getName().equals("required") && f.isRequired());
            assertThat(input.getFields()).anyMatch(f ->
                f.getName().equals("optional") && !f.isRequired());
        }
    }

    @Nested
    @DisplayName("Enum type analysis")
    class EnumTypeTests {

        @Test
        @DisplayName("Should analyze enum type")
        void shouldAnalyzeEnumType() {
            String sdl = """
                enum UserRole {
                    ADMIN
                    USER
                    GUEST
                }
                """;

            Map<String, GraphQLType> types = analyzeToMap(sdl);

            assertThat(types).containsKey("UserRole");
            GraphQLType enumType = types.get("UserRole");
            assertThat(enumType.getKind()).isEqualTo(GraphQLTypeKind.ENUM);
            assertThat(enumType.isEnum()).isTrue();
            assertThat(enumType.getEnumValues()).containsExactlyInAnyOrder("ADMIN", "USER", "GUEST");
        }

        @Test
        @DisplayName("Should handle deprecated enum values")
        void shouldHandleDeprecatedEnumValues() {
            String sdl = """
                enum Status {
                    ACTIVE
                    INACTIVE @deprecated(reason: "Use DISABLED instead")
                    DISABLED
                }
                """;

            Map<String, GraphQLType> types = analyzeToMap(sdl);

            GraphQLType enumType = types.get("Status");
            assertThat(enumType.getEnumValues()).contains("INACTIVE");
        }
    }

    @Nested
    @DisplayName("Interface type analysis")
    class InterfaceTypeTests {

        @Test
        @DisplayName("Should analyze interface type")
        void shouldAnalyzeInterfaceType() {
            String sdl = """
                interface Node {
                    id: ID!
                }
                """;

            Map<String, GraphQLType> types = analyzeToMap(sdl);

            assertThat(types).containsKey("Node");
            GraphQLType iface = types.get("Node");
            assertThat(iface.getKind()).isEqualTo(GraphQLTypeKind.INTERFACE);
            assertThat(iface.isInterface()).isTrue();
            assertThat(iface.getFields()).hasSize(1);
        }

        @Test
        @DisplayName("Should analyze interface with multiple fields")
        void shouldAnalyzeInterfaceWithFields() {
            String sdl = """
                interface Timestamped {
                    createdAt: String!
                    updatedAt: String
                    deletedAt: String
                }
                """;

            Map<String, GraphQLType> types = analyzeToMap(sdl);

            GraphQLType iface = types.get("Timestamped");
            assertThat(iface.getFields()).hasSize(3);
        }
    }

    @Nested
    @DisplayName("Union type analysis")
    class UnionTypeTests {

        @Test
        @DisplayName("Should analyze union type")
        void shouldAnalyzeUnionType() {
            String sdl = """
                union SearchResult = User | Product | Order

                type User { id: ID! }
                type Product { id: ID! }
                type Order { id: ID! }
                """;

            Map<String, GraphQLType> types = analyzeToMap(sdl);

            assertThat(types).containsKey("SearchResult");
            GraphQLType union = types.get("SearchResult");
            assertThat(union.getKind()).isEqualTo(GraphQLTypeKind.UNION);
            assertThat(union.isUnion()).isTrue();
            assertThat(union.getPossibleTypes()).containsExactlyInAnyOrder("User", "Product", "Order");
        }

        @Test
        @DisplayName("Should analyze simple union")
        void shouldAnalyzeSimpleUnion() {
            String sdl = """
                union Result = Success | Error

                type Success { data: String }
                type Error { message: String }
                """;

            Map<String, GraphQLType> types = analyzeToMap(sdl);

            GraphQLType union = types.get("Result");
            assertThat(union.getPossibleTypes()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Scalar type analysis")
    class ScalarTypeTests {

        @Test
        @DisplayName("Should analyze custom scalar type")
        void shouldAnalyzeCustomScalar() {
            String sdl = """
                scalar DateTime
                scalar JSON

                type Event {
                    timestamp: DateTime!
                    data: JSON
                }
                """;

            Map<String, GraphQLType> types = analyzeToMap(sdl);

            assertThat(types).containsKeys("DateTime", "JSON");
            GraphQLType dateTime = types.get("DateTime");
            assertThat(dateTime.getKind()).isEqualTo(GraphQLTypeKind.SCALAR);
            assertThat(dateTime.isScalar()).isTrue();
        }
    }

    @Nested
    @DisplayName("Complex type analysis")
    class ComplexTypeTests {

        @Test
        @DisplayName("Should filter built-in types")
        void shouldFilterBuiltInTypes() {
            String sdl = """
                type User {
                    id: ID!
                    name: String!
                    age: Int
                    active: Boolean
                    score: Float
                }
                """;

            Map<String, GraphQLType> types = analyzeToMap(sdl);

            // Built-in types should not be included
            assertThat(types).doesNotContainKeys("String", "Int", "Boolean", "Float", "ID");
            assertThat(types).containsKey("User");
        }

        @Test
        @DisplayName("Should analyze type with deprecated fields")
        void shouldAnalyzeDeprecatedFields() {
            String sdl = """
                type User {
                    id: ID!
                    name: String! @deprecated(reason: "Use fullName")
                    fullName: String!
                }
                """;

            Map<String, GraphQLType> types = analyzeToMap(sdl);

            GraphQLType user = types.get("User");
            assertThat(user.getFields()).anyMatch(f ->
                f.getName().equals("name") && f.isDeprecated());
        }

        @Test
        @DisplayName("Should get type count")
        void shouldGetTypeCount() {
            String sdl = """
                type User { id: ID! }
                type Product { id: ID! }
                type Order { id: ID! }
                enum Status { ACTIVE }
                input UserInput { name: String }
                """;

            List<GraphQLType> types = analyzer.analyzeTypes(parse(sdl));

            assertThat(types).hasSize(5);
        }
    }
}

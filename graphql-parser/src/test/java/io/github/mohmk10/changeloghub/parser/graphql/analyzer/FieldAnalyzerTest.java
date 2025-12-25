package io.github.mohmk10.changeloghub.parser.graphql.analyzer;

import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.github.mohmk10.changeloghub.parser.graphql.model.GraphQLArgument;
import io.github.mohmk10.changeloghub.parser.graphql.model.GraphQLField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@DisplayName("FieldAnalyzer Tests")
class FieldAnalyzerTest {

    private FieldAnalyzer analyzer;
    private SchemaParser schemaParser;

    @BeforeEach
    void setUp() {
        analyzer = new FieldAnalyzer();
        schemaParser = new SchemaParser();
    }

    private TypeDefinitionRegistry parse(String sdl) {
        return schemaParser.parse(sdl);
    }

    private ObjectTypeDefinition getObjectType(String sdl, String typeName) {
        TypeDefinitionRegistry registry = parse(sdl);
        return (ObjectTypeDefinition) registry.getType(typeName).orElseThrow();
    }

    @Nested
    @DisplayName("analyzeFields() method tests")
    class AnalyzeFieldsTests {

        @Test
        @DisplayName("Should analyze all fields of a type")
        void shouldAnalyzeAllFields() {
            String sdl = """
                type User {
                    id: ID!
                    name: String!
                    email: String
                    age: Int
                }
                """;

            ObjectTypeDefinition userType = getObjectType(sdl, "User");
            List<GraphQLField> fields = analyzer.analyzeFields(userType);

            assertThat(fields).hasSize(4);
            Map<String, GraphQLField> fieldMap = fields.stream()
                    .collect(Collectors.toMap(GraphQLField::getName, f -> f));

            assertThat(fieldMap).containsKeys("id", "name", "email", "age");
        }

        @Test
        @DisplayName("Should detect required fields")
        void shouldDetectRequiredFields() {
            String sdl = """
                type User {
                    required: String!
                    optional: String
                }
                """;

            ObjectTypeDefinition type = getObjectType(sdl, "User");
            List<GraphQLField> fields = analyzer.analyzeFields(type);

            assertThat(fields).anyMatch(f -> f.getName().equals("required") && f.isRequired());
            assertThat(fields).anyMatch(f -> f.getName().equals("optional") && !f.isRequired());
        }

        @Test
        @DisplayName("Should analyze field types")
        void shouldAnalyzeFieldTypes() {
            String sdl = """
                type Data {
                    stringField: String
                    intField: Int
                    floatField: Float
                    boolField: Boolean
                    idField: ID
                }
                """;

            ObjectTypeDefinition type = getObjectType(sdl, "Data");
            List<GraphQLField> fields = analyzer.analyzeFields(type);

            Map<String, GraphQLField> fieldMap = fields.stream()
                    .collect(Collectors.toMap(GraphQLField::getName, f -> f));

            assertThat(fieldMap.get("stringField").getType()).isEqualTo("String");
            assertThat(fieldMap.get("intField").getType()).isEqualTo("Int");
            assertThat(fieldMap.get("floatField").getType()).isEqualTo("Float");
            assertThat(fieldMap.get("boolField").getType()).isEqualTo("Boolean");
            assertThat(fieldMap.get("idField").getType()).isEqualTo("ID");
        }
    }

    @Nested
    @DisplayName("analyzeField() method tests")
    class AnalyzeFieldTests {

        @Test
        @DisplayName("Should analyze simple field")
        void shouldAnalyzeSimpleField() {
            String sdl = """
                type User {
                    name: String!
                }
                """;

            ObjectTypeDefinition type = getObjectType(sdl, "User");
            FieldDefinition fieldDef = type.getFieldDefinitions().get(0);

            GraphQLField field = analyzer.analyzeField(fieldDef);

            assertThat(field.getName()).isEqualTo("name");
            assertThat(field.getType()).isEqualTo("String");
            assertThat(field.isRequired()).isTrue();
            assertThat(field.isList()).isFalse();
        }

        @Test
        @DisplayName("Should analyze deprecated field")
        void shouldAnalyzeDeprecatedField() {
            String sdl = """
                type User {
                    oldName: String @deprecated(reason: "Use name instead")
                    name: String!
                }
                """;

            ObjectTypeDefinition type = getObjectType(sdl, "User");
            FieldDefinition fieldDef = type.getFieldDefinitions().get(0);

            GraphQLField field = analyzer.analyzeField(fieldDef);

            assertThat(field.isDeprecated()).isTrue();
            assertThat(field.getDeprecationReason()).contains("Use name instead");
        }

        @Test
        @DisplayName("Should analyze field with description")
        void shouldAnalyzeFieldWithDescription() {
            String sdl = """
                type User {
                    "The user's display name"
                    name: String!
                }
                """;

            ObjectTypeDefinition type = getObjectType(sdl, "User");
            FieldDefinition fieldDef = type.getFieldDefinitions().get(0);

            GraphQLField field = analyzer.analyzeField(fieldDef);

            assertThat(field.getDescription()).contains("display name");
        }
    }

    @Nested
    @DisplayName("List type handling")
    class ListTypeTests {

        @Test
        @DisplayName("Should detect list type")
        void shouldDetectListType() {
            String sdl = """
                type User {
                    tags: [String]
                }
                """;

            ObjectTypeDefinition type = getObjectType(sdl, "User");
            GraphQLField field = analyzer.analyzeField(type.getFieldDefinitions().get(0));

            assertThat(field.isList()).isTrue();
            assertThat(field.isRequired()).isFalse();
        }

        @Test
        @DisplayName("Should detect required list")
        void shouldDetectRequiredList() {
            String sdl = """
                type User {
                    items: [String]!
                }
                """;

            ObjectTypeDefinition type = getObjectType(sdl, "User");
            GraphQLField field = analyzer.analyzeField(type.getFieldDefinitions().get(0));

            assertThat(field.isList()).isTrue();
            assertThat(field.isRequired()).isTrue();
        }

        @Test
        @DisplayName("Should detect list with required items")
        void shouldDetectListWithRequiredItems() {
            String sdl = """
                type User {
                    items: [String!]!
                }
                """;

            ObjectTypeDefinition type = getObjectType(sdl, "User");
            GraphQLField field = analyzer.analyzeField(type.getFieldDefinitions().get(0));

            assertThat(field.isList()).isTrue();
            assertThat(field.isRequired()).isTrue();
            assertThat(field.isListItemRequired()).isTrue();
        }
    }

    @Nested
    @DisplayName("Argument handling")
    class ArgumentTests {

        @Test
        @DisplayName("Should analyze field with arguments")
        void shouldAnalyzeFieldWithArguments() {
            String sdl = """
                type Query {
                    user(id: ID!): User
                }

                type User {
                    id: ID!
                }
                """;

            ObjectTypeDefinition type = getObjectType(sdl, "Query");
            GraphQLField field = analyzer.analyzeField(type.getFieldDefinitions().get(0));

            assertThat(field.getArguments()).hasSize(1);
            assertThat(field.getArguments().get(0).getName()).isEqualTo("id");
            assertThat(field.getArguments().get(0).isRequired()).isTrue();
        }

        @Test
        @DisplayName("Should analyze multiple arguments")
        void shouldAnalyzeMultipleArguments() {
            String sdl = """
                type Query {
                    search(query: String!, limit: Int = 10, offset: Int): [Result]
                }

                type Result { id: ID! }
                """;

            ObjectTypeDefinition type = getObjectType(sdl, "Query");
            GraphQLField field = analyzer.analyzeField(type.getFieldDefinitions().get(0));

            assertThat(field.getArguments()).hasSize(3);

            Map<String, GraphQLArgument> args = field.getArguments().stream()
                    .collect(Collectors.toMap(GraphQLArgument::getName, a -> a));

            assertThat(args.get("query").isRequired()).isTrue();
            assertThat(args.get("limit").getDefaultValue()).isEqualTo("10");
            assertThat(args.get("offset").isRequired()).isFalse();
        }

        @Test
        @DisplayName("Should detect argument with default value")
        void shouldDetectArgumentWithDefault() {
            String sdl = """
                type Query {
                    items(first: Int = 10, after: String): [Item]
                }

                type Item { id: ID! }
                """;

            ObjectTypeDefinition type = getObjectType(sdl, "Query");
            GraphQLField field = analyzer.analyzeField(type.getFieldDefinitions().get(0));

            GraphQLArgument firstArg = field.getArguments().stream()
                    .filter(a -> a.getName().equals("first"))
                    .findFirst().orElseThrow();

            assertThat(firstArg.hasDefaultValue()).isTrue();
            assertThat(firstArg.getDefaultValue()).isEqualTo("10");
        }
    }

    @Nested
    @DisplayName("Type parsing")
    class TypeParsingTests {

        @Test
        @DisplayName("Should get base type name from NonNull wrapper")
        void shouldGetBaseTypeFromNonNull() {
            String sdl = """
                type Data {
                    value: String!
                }
                """;

            ObjectTypeDefinition type = getObjectType(sdl, "Data");
            GraphQLField field = analyzer.analyzeField(type.getFieldDefinitions().get(0));

            assertThat(field.getType()).isEqualTo("String");
        }

        @Test
        @DisplayName("Should get base type name from List wrapper")
        void shouldGetBaseTypeFromList() {
            String sdl = """
                type Data {
                    values: [String]
                }
                """;

            ObjectTypeDefinition type = getObjectType(sdl, "Data");
            GraphQLField field = analyzer.analyzeField(type.getFieldDefinitions().get(0));

            assertThat(field.getType()).isEqualTo("String");
        }

        @Test
        @DisplayName("Should get base type from nested wrappers")
        void shouldGetBaseTypeFromNestedWrappers() {
            String sdl = """
                type Data {
                    values: [String!]!
                }
                """;

            ObjectTypeDefinition type = getObjectType(sdl, "Data");
            GraphQLField field = analyzer.analyzeField(type.getFieldDefinitions().get(0));

            assertThat(field.getType()).isEqualTo("String");
        }

        @Test
        @DisplayName("Should handle custom type references")
        void shouldHandleCustomTypeReferences() {
            String sdl = """
                type User {
                    address: Address!
                    orders: [Order!]!
                }

                type Address { city: String! }
                type Order { id: ID! }
                """;

            ObjectTypeDefinition type = getObjectType(sdl, "User");
            List<GraphQLField> fields = analyzer.analyzeFields(type);

            Map<String, GraphQLField> fieldMap = fields.stream()
                    .collect(Collectors.toMap(GraphQLField::getName, f -> f));

            assertThat(fieldMap.get("address").getType()).isEqualTo("Address");
            assertThat(fieldMap.get("orders").getType()).isEqualTo("Order");
        }
    }
}

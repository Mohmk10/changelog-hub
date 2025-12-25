package io.github.mohmk10.changeloghub.parser.graphql;

import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.core.model.ApiType;
import io.github.mohmk10.changeloghub.parser.graphql.exception.GraphQLParseException;
import io.github.mohmk10.changeloghub.parser.graphql.model.GraphQLOperation;
import io.github.mohmk10.changeloghub.parser.graphql.model.GraphQLSchema;
import io.github.mohmk10.changeloghub.parser.graphql.model.GraphQLType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DefaultGraphQLParser Tests")
class DefaultGraphQLParserTest {

    private DefaultGraphQLParser parser;

    @BeforeEach
    void setUp() {
        parser = new DefaultGraphQLParser();
    }

    @Nested
    @DisplayName("parse() method tests")
    class ParseTests {

        @Test
        @DisplayName("Should parse simple schema with type")
        void shouldParseSimpleSchema() throws GraphQLParseException {
            String sdl = """
                type Query {
                    hello: String
                }
                """;

            GraphQLSchema schema = parser.parse(sdl);

            assertThat(schema).isNotNull();
            assertThat(schema.getQueries()).hasSize(1);
            assertThat(schema.getQueries().get(0).getName()).isEqualTo("hello");
        }

        @Test
        @DisplayName("Should parse schema with multiple queries")
        void shouldParseMultipleQueries() throws GraphQLParseException {
            String sdl = """
                type Query {
                    user(id: ID!): User
                    users: [User!]!
                    me: User
                }

                type User {
                    id: ID!
                    name: String!
                    email: String!
                }
                """;

            GraphQLSchema schema = parser.parse(sdl);

            assertThat(schema.getQueries()).hasSize(3);
            assertThat(schema.getTypes()).containsKey("User");
        }

        @Test
        @DisplayName("Should parse schema with mutations")
        void shouldParseMutations() throws GraphQLParseException {
            String sdl = """
                type Query {
                    user(id: ID!): User
                }

                type Mutation {
                    createUser(name: String!, email: String!): User!
                    deleteUser(id: ID!): Boolean!
                }

                type User {
                    id: ID!
                    name: String!
                }
                """;

            GraphQLSchema schema = parser.parse(sdl);

            assertThat(schema.getMutations()).hasSize(2);
            assertThat(schema.getMutations().get(0).getName()).isEqualTo("createUser");
            assertThat(schema.getMutations().get(0).isMutation()).isTrue();
        }

        @Test
        @DisplayName("Should parse schema with subscriptions")
        void shouldParseSubscriptions() throws GraphQLParseException {
            String sdl = """
                type Query {
                    message(id: ID!): Message
                }

                type Subscription {
                    messageAdded(channelId: ID!): Message!
                    userJoined: User!
                }

                type Message {
                    id: ID!
                    content: String!
                }

                type User {
                    id: ID!
                    name: String!
                }
                """;

            GraphQLSchema schema = parser.parse(sdl);

            assertThat(schema.getSubscriptions()).hasSize(2);
            assertThat(schema.getSubscriptions().get(0).isSubscription()).isTrue();
        }

        @Test
        @DisplayName("Should parse schema with enums")
        void shouldParseEnums() throws GraphQLParseException {
            String sdl = """
                type Query {
                    user(id: ID!): User
                }

                enum UserRole {
                    ADMIN
                    USER
                    GUEST
                }

                type User {
                    id: ID!
                    role: UserRole!
                }
                """;

            GraphQLSchema schema = parser.parse(sdl);

            assertThat(schema.getTypes()).containsKey("UserRole");
            GraphQLType roleEnum = schema.getTypes().get("UserRole");
            assertThat(roleEnum.isEnum()).isTrue();
            assertThat(roleEnum.getEnumValues()).containsExactlyInAnyOrder("ADMIN", "USER", "GUEST");
        }

        @Test
        @DisplayName("Should parse schema with interfaces")
        void shouldParseInterfaces() throws GraphQLParseException {
            String sdl = """
                type Query {
                    node(id: ID!): Node
                }

                interface Node {
                    id: ID!
                }

                type User implements Node {
                    id: ID!
                    name: String!
                }
                """;

            GraphQLSchema schema = parser.parse(sdl);

            assertThat(schema.getTypes()).containsKey("Node");
            GraphQLType nodeInterface = schema.getTypes().get("Node");
            assertThat(nodeInterface.isInterface()).isTrue();

            GraphQLType user = schema.getTypes().get("User");
            assertThat(user.getInterfaces()).contains("Node");
        }

        @Test
        @DisplayName("Should parse schema with union types")
        void shouldParseUnionTypes() throws GraphQLParseException {
            String sdl = """
                type Query {
                    search(query: String!): [SearchResult!]!
                }

                union SearchResult = User | Product

                type User {
                    id: ID!
                    name: String!
                }

                type Product {
                    id: ID!
                    title: String!
                }
                """;

            GraphQLSchema schema = parser.parse(sdl);

            assertThat(schema.getTypes()).containsKey("SearchResult");
            GraphQLType union = schema.getTypes().get("SearchResult");
            assertThat(union.isUnion()).isTrue();
            assertThat(union.getPossibleTypes()).containsExactlyInAnyOrder("User", "Product");
        }

        @Test
        @DisplayName("Should parse schema with input types")
        void shouldParseInputTypes() throws GraphQLParseException {
            String sdl = """
                type Query {
                    user(id: ID!): User
                }

                type Mutation {
                    createUser(input: CreateUserInput!): User!
                }

                input CreateUserInput {
                    name: String!
                    email: String!
                    age: Int
                }

                type User {
                    id: ID!
                    name: String!
                }
                """;

            GraphQLSchema schema = parser.parse(sdl);

            assertThat(schema.getTypes()).containsKey("CreateUserInput");
            GraphQLType input = schema.getTypes().get("CreateUserInput");
            assertThat(input.isInputType()).isTrue();
            assertThat(input.getFields()).hasSize(3);
        }

        @Test
        @DisplayName("Should parse deprecated fields")
        void shouldParseDeprecatedFields() throws GraphQLParseException {
            String sdl = """
                type Query {
                    user(id: ID!): User
                }

                type User {
                    id: ID!
                    name: String! @deprecated(reason: "Use fullName")
                    fullName: String!
                }
                """;

            GraphQLSchema schema = parser.parse(sdl);

            GraphQLType user = schema.getTypes().get("User");
            assertThat(user.getFields()).anyMatch(f ->
                f.getName().equals("name") && f.isDeprecated());
        }

        @Test
        @DisplayName("Should throw exception for null content")
        void shouldThrowForNullContent() {
            assertThatThrownBy(() -> parser.parse(null))
                    .isInstanceOf(GraphQLParseException.class)
                    .hasMessageContaining("empty");
        }

        @Test
        @DisplayName("Should throw exception for blank content")
        void shouldThrowForBlankContent() {
            assertThatThrownBy(() -> parser.parse("   "))
                    .isInstanceOf(GraphQLParseException.class)
                    .hasMessageContaining("empty");
        }

        @Test
        @DisplayName("Should throw exception for invalid SDL")
        void shouldThrowForInvalidSdl() {
            String invalidSdl = "this is not valid GraphQL";

            assertThatThrownBy(() -> parser.parse(invalidSdl))
                    .isInstanceOf(GraphQLParseException.class);
        }
    }

    @Nested
    @DisplayName("parseFile() method tests")
    class ParseFileTests {

        @Test
        @DisplayName("Should parse schema file")
        void shouldParseSchemaFile() throws GraphQLParseException {
            File file = new File("src/test/resources/schemas/schema-v1.graphql");

            GraphQLSchema schema = parser.parseFile(file);

            assertThat(schema).isNotNull();
            assertThat(schema.getSourceFile()).contains("schema-v1.graphql");
            assertThat(schema.getQueries()).isNotEmpty();
            assertThat(schema.getMutations()).isNotEmpty();
            assertThat(schema.getSubscriptions()).isNotEmpty();
        }

        @Test
        @DisplayName("Should parse schema from path string")
        void shouldParseSchemaFromPath() throws GraphQLParseException {
            GraphQLSchema schema = parser.parseFile("src/test/resources/schemas/schema-v1.graphql");

            assertThat(schema).isNotNull();
            assertThat(schema.getQueries()).isNotEmpty();
        }

        @Test
        @DisplayName("Should throw exception for non-existent file")
        void shouldThrowForNonExistentFile() {
            assertThatThrownBy(() -> parser.parseFile("non-existent.graphql"))
                    .isInstanceOf(GraphQLParseException.class)
                    .hasMessageContaining("not found");
        }
    }

    @Nested
    @DisplayName("parseStream() method tests")
    class ParseStreamTests {

        @Test
        @DisplayName("Should parse from input stream")
        void shouldParseFromInputStream() throws GraphQLParseException {
            String sdl = """
                type Query {
                    hello: String
                }
                """;
            InputStream stream = new ByteArrayInputStream(sdl.getBytes(StandardCharsets.UTF_8));

            GraphQLSchema schema = parser.parseStream(stream);

            assertThat(schema).isNotNull();
            assertThat(schema.getQueries()).hasSize(1);
        }

        @Test
        @DisplayName("Should throw exception for null stream")
        void shouldThrowForNullStream() {
            assertThatThrownBy(() -> parser.parseStream(null))
                    .isInstanceOf(GraphQLParseException.class)
                    .hasMessageContaining("null");
        }
    }

    @Nested
    @DisplayName("parseToApiSpec() method tests")
    class ParseToApiSpecTests {

        @Test
        @DisplayName("Should convert schema to ApiSpec")
        void shouldConvertToApiSpec() throws GraphQLParseException {
            String sdl = """
                type Query {
                    user(id: ID!): User
                    users: [User!]!
                }

                type Mutation {
                    createUser(name: String!): User!
                }

                type User {
                    id: ID!
                    name: String!
                }
                """;

            ApiSpec apiSpec = parser.parseToApiSpec(sdl);

            assertThat(apiSpec).isNotNull();
            assertThat(apiSpec.getType()).isEqualTo(ApiType.GRAPHQL);
            assertThat(apiSpec.getEndpoints()).hasSize(3); // 2 queries + 1 mutation
        }

        @Test
        @DisplayName("Should set metadata on ApiSpec")
        void shouldSetMetadataOnApiSpec() throws GraphQLParseException {
            String sdl = """
                type Query {
                    user: User
                    product: Product
                }

                type Mutation {
                    update: Boolean
                }

                type Subscription {
                    notify: String
                }

                type User { id: ID! }
                type Product { id: ID! }
                """;

            ApiSpec apiSpec = parser.parseToApiSpec(sdl);

            assertThat(apiSpec.getMetadata()).containsKey("queryCount");
            assertThat(apiSpec.getMetadata().get("queryCount")).isEqualTo(2);
            assertThat(apiSpec.getMetadata().get("mutationCount")).isEqualTo(1);
            assertThat(apiSpec.getMetadata().get("subscriptionCount")).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("validate() method tests")
    class ValidateTests {

        @Test
        @DisplayName("Should return true for valid schema")
        void shouldReturnTrueForValidSchema() throws GraphQLParseException {
            String sdl = """
                type Query {
                    hello: String
                }
                """;

            boolean result = parser.validate(sdl);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should throw exception for invalid schema")
        void shouldThrowForInvalidSchema() {
            String invalidSdl = "type { broken";

            assertThatThrownBy(() -> parser.validate(invalidSdl))
                    .isInstanceOf(GraphQLParseException.class);
        }
    }

    @Nested
    @DisplayName("Complex schema tests")
    class ComplexSchemaTests {

        @Test
        @DisplayName("Should parse complete e-commerce schema")
        void shouldParseCompleteSchema() throws GraphQLParseException {
            GraphQLSchema schema = parser.parseFile("src/test/resources/schemas/schema-v1.graphql");

            // Verify types
            assertThat(schema.getTypes()).containsKeys("User", "Product", "Order", "Review");

            // Verify enums
            assertThat(schema.getTypes()).containsKeys("UserRole", "OrderStatus", "ProductCategory");

            // Verify interfaces
            assertThat(schema.getTypes()).containsKeys("Node", "Timestamped");

            // Verify union
            assertThat(schema.getTypes()).containsKey("SearchResult");

            // Verify input types
            assertThat(schema.getTypes()).containsKeys("CreateUserInput", "ProductFilter");

            // Verify operations
            assertThat(schema.getQueries()).hasSizeGreaterThan(5);
            assertThat(schema.getMutations()).hasSizeGreaterThan(5);
            assertThat(schema.getSubscriptions()).hasSizeGreaterThan(0);
        }

        @Test
        @DisplayName("Should parse arguments correctly")
        void shouldParseArgumentsCorrectly() throws GraphQLParseException {
            String sdl = """
                type Query {
                    search(query: String!, limit: Int = 10, offset: Int): [Result!]!
                }

                type Result {
                    id: ID!
                }
                """;

            GraphQLSchema schema = parser.parse(sdl);
            GraphQLOperation search = schema.getQueries().get(0);

            assertThat(search.getArguments()).hasSize(3);
            assertThat(search.getArguments().get(0).getName()).isEqualTo("query");
            assertThat(search.getArguments().get(0).isRequired()).isTrue();
            assertThat(search.getArguments().get(1).getName()).isEqualTo("limit");
            assertThat(search.getArguments().get(1).getDefaultValue()).isEqualTo("10");
        }

        @Test
        @DisplayName("Should handle list types")
        void shouldHandleListTypes() throws GraphQLParseException {
            String sdl = """
                type Query {
                    users: [User!]!
                    tags: [String]
                    ids: [ID!]
                }

                type User {
                    id: ID!
                }
                """;

            GraphQLSchema schema = parser.parse(sdl);

            GraphQLOperation users = schema.getQueries().stream()
                    .filter(q -> q.getName().equals("users"))
                    .findFirst().orElseThrow();

            assertThat(users.getReturnType()).isEqualTo("[User!]!");
        }
    }

    @Nested
    @DisplayName("Builder tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create parser with builder")
        void shouldCreateParserWithBuilder() {
            DefaultGraphQLParser customParser = DefaultGraphQLParser.builder()
                    .lenientParsing(true)
                    .build();

            assertThat(customParser).isNotNull();
        }
    }
}

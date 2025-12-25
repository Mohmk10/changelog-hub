package io.github.mohmk10.changeloghub.parser.graphql.mapper;

import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.core.model.ApiType;
import io.github.mohmk10.changeloghub.core.model.Endpoint;
import io.github.mohmk10.changeloghub.core.model.HttpMethod;
import io.github.mohmk10.changeloghub.parser.graphql.DefaultGraphQLParser;
import io.github.mohmk10.changeloghub.parser.graphql.model.GraphQLSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("GraphQLModelMapper Tests")
class GraphQLModelMapperTest {

    private GraphQLModelMapper mapper;
    private DefaultGraphQLParser parser;

    @BeforeEach
    void setUp() {
        mapper = new GraphQLModelMapper();
        parser = new DefaultGraphQLParser();
    }

    @Nested
    @DisplayName("mapToApiSpec() method tests")
    class MapToApiSpecTests {

        @Test
        @DisplayName("Should map schema to ApiSpec with correct type")
        void shouldMapSchemaToApiSpec() {
            String sdl = """
                type Query {
                    hello: String
                }
                """;

            GraphQLSchema schema = parser.parse(sdl);
            ApiSpec apiSpec = mapper.mapToApiSpec(schema);

            assertThat(apiSpec).isNotNull();
            assertThat(apiSpec.getType()).isEqualTo(ApiType.GRAPHQL);
        }

        @Test
        @DisplayName("Should map queries to endpoints")
        void shouldMapQueriesToEndpoints() {
            String sdl = """
                type Query {
                    user(id: ID!): User
                    users: [User!]!
                }
                type User { id: ID! }
                """;

            GraphQLSchema schema = parser.parse(sdl);
            ApiSpec apiSpec = mapper.mapToApiSpec(schema);

            assertThat(apiSpec.getEndpoints()).hasSize(2);
        }

        @Test
        @DisplayName("Should map mutations to endpoints")
        void shouldMapMutationsToEndpoints() {
            String sdl = """
                type Query { dummy: String }
                type Mutation {
                    createUser(name: String!): User!
                    deleteUser(id: ID!): Boolean!
                }
                type User { id: ID! }
                """;

            GraphQLSchema schema = parser.parse(sdl);
            ApiSpec apiSpec = mapper.mapToApiSpec(schema);

            // 1 query + 2 mutations = 3 endpoints
            assertThat(apiSpec.getEndpoints()).hasSize(3);
        }

        @Test
        @DisplayName("Should map subscriptions to endpoints")
        void shouldMapSubscriptionsToEndpoints() {
            String sdl = """
                type Query { dummy: String }
                type Subscription {
                    messageAdded: Message!
                }
                type Message { id: ID! content: String! }
                """;

            GraphQLSchema schema = parser.parse(sdl);
            ApiSpec apiSpec = mapper.mapToApiSpec(schema);

            // 1 query + 1 subscription = 2 endpoints
            assertThat(apiSpec.getEndpoints()).hasSize(2);
        }

        @Test
        @DisplayName("Should set correct metadata")
        void shouldSetCorrectMetadata() {
            String sdl = """
                type Query {
                    user: User
                    product: Product
                }
                type Mutation {
                    create: Boolean
                }
                type Subscription {
                    notify: String
                }
                type User { id: ID! }
                type Product { id: ID! }
                """;

            GraphQLSchema schema = parser.parse(sdl);
            ApiSpec apiSpec = mapper.mapToApiSpec(schema);

            assertThat(apiSpec.getMetadata()).containsEntry("queryCount", 2);
            assertThat(apiSpec.getMetadata()).containsEntry("mutationCount", 1);
            assertThat(apiSpec.getMetadata()).containsEntry("subscriptionCount", 1);
        }

        @Test
        @DisplayName("Should use custom name and version")
        void shouldUseCustomNameAndVersion() {
            String sdl = """
                type Query { hello: String }
                """;

            GraphQLSchema schema = parser.parse(sdl);
            ApiSpec apiSpec = mapper.mapToApiSpec(schema, "Custom API", "2.0.0");

            assertThat(apiSpec.getName()).isEqualTo("Custom API");
            assertThat(apiSpec.getVersion()).isEqualTo("2.0.0");
        }

        @Test
        @DisplayName("Should use defaults for null name and version")
        void shouldUseDefaultsForNull() {
            String sdl = """
                type Query { hello: String }
                """;

            GraphQLSchema schema = parser.parse(sdl);
            ApiSpec apiSpec = mapper.mapToApiSpec(schema, null, null);

            assertThat(apiSpec.getName()).isEqualTo("GraphQL API");
            assertThat(apiSpec.getVersion()).isEqualTo("1.0.0");
        }

        @Test
        @DisplayName("Should set parsedAt timestamp")
        void shouldSetParsedAtTimestamp() {
            String sdl = """
                type Query { hello: String }
                """;

            GraphQLSchema schema = parser.parse(sdl);
            ApiSpec apiSpec = mapper.mapToApiSpec(schema);

            assertThat(apiSpec.getParsedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Endpoint mapping")
    class EndpointMappingTests {

        @Test
        @DisplayName("Should map query to GET method")
        void shouldMapQueryToGetMethod() {
            String sdl = """
                type Query {
                    user(id: ID!): User
                }
                type User { id: ID! }
                """;

            GraphQLSchema schema = parser.parse(sdl);
            ApiSpec apiSpec = mapper.mapToApiSpec(schema);

            Endpoint endpoint = apiSpec.getEndpoints().get(0);
            assertThat(endpoint.getMethod()).isEqualTo(HttpMethod.GET);
        }

        @Test
        @DisplayName("Should map mutation to POST method")
        void shouldMapMutationToPostMethod() {
            String sdl = """
                type Query { dummy: String }
                type Mutation {
                    createUser(name: String!): User!
                }
                type User { id: ID! }
                """;

            GraphQLSchema schema = parser.parse(sdl);
            ApiSpec apiSpec = mapper.mapToApiSpec(schema);

            Endpoint mutation = apiSpec.getEndpoints().stream()
                    .filter(e -> e.getOperationId().equals("createUser"))
                    .findFirst().orElseThrow();

            assertThat(mutation.getMethod()).isEqualTo(HttpMethod.POST);
        }

        @Test
        @DisplayName("Should set operation ID")
        void shouldSetOperationId() {
            String sdl = """
                type Query {
                    getUser(id: ID!): User
                }
                type User { id: ID! }
                """;

            GraphQLSchema schema = parser.parse(sdl);
            ApiSpec apiSpec = mapper.mapToApiSpec(schema);

            Endpoint endpoint = apiSpec.getEndpoints().get(0);
            assertThat(endpoint.getOperationId()).isEqualTo("getUser");
        }

        @Test
        @DisplayName("Should set path for query")
        void shouldSetPathForQuery() {
            String sdl = """
                type Query {
                    user(id: ID!): User
                }
                type User { id: ID! }
                """;

            GraphQLSchema schema = parser.parse(sdl);
            ApiSpec apiSpec = mapper.mapToApiSpec(schema);

            Endpoint endpoint = apiSpec.getEndpoints().get(0);
            assertThat(endpoint.getPath()).contains("/graphql");
            assertThat(endpoint.getPath()).contains("user");
        }

        @Test
        @DisplayName("Should set path for mutation")
        void shouldSetPathForMutation() {
            String sdl = """
                type Query { dummy: String }
                type Mutation {
                    createUser(name: String!): User!
                }
                type User { id: ID! }
                """;

            GraphQLSchema schema = parser.parse(sdl);
            ApiSpec apiSpec = mapper.mapToApiSpec(schema);

            Endpoint mutation = apiSpec.getEndpoints().stream()
                    .filter(e -> e.getOperationId().equals("createUser"))
                    .findFirst().orElseThrow();

            assertThat(mutation.getPath()).contains("mutation");
        }

        @Test
        @DisplayName("Should add graphql tag")
        void shouldAddGraphqlTag() {
            String sdl = """
                type Query {
                    user(id: ID!): User
                }
                type User { id: ID! }
                """;

            GraphQLSchema schema = parser.parse(sdl);
            ApiSpec apiSpec = mapper.mapToApiSpec(schema);

            Endpoint endpoint = apiSpec.getEndpoints().get(0);
            assertThat(endpoint.getTags()).contains("graphql");
        }

        @Test
        @DisplayName("Should add operation type tag")
        void shouldAddOperationTypeTag() {
            String sdl = """
                type Query {
                    user: User
                }
                type Mutation {
                    create: Boolean
                }
                type User { id: ID! }
                """;

            GraphQLSchema schema = parser.parse(sdl);
            ApiSpec apiSpec = mapper.mapToApiSpec(schema);

            Endpoint query = apiSpec.getEndpoints().stream()
                    .filter(e -> e.getOperationId().equals("user"))
                    .findFirst().orElseThrow();
            assertThat(query.getTags()).contains("query");

            Endpoint mutation = apiSpec.getEndpoints().stream()
                    .filter(e -> e.getOperationId().equals("create"))
                    .findFirst().orElseThrow();
            assertThat(mutation.getTags()).contains("mutation");
        }

        @Test
        @DisplayName("Should map deprecated operation")
        void shouldMapDeprecatedOperation() {
            String sdl = """
                type Query {
                    oldUser(id: ID!): User @deprecated(reason: "Use user")
                }
                type User { id: ID! }
                """;

            GraphQLSchema schema = parser.parse(sdl);
            ApiSpec apiSpec = mapper.mapToApiSpec(schema);

            Endpoint endpoint = apiSpec.getEndpoints().get(0);
            assertThat(endpoint.isDeprecated()).isTrue();
        }
    }

    @Nested
    @DisplayName("Parameter mapping")
    class ParameterMappingTests {

        @Test
        @DisplayName("Should map arguments to parameters")
        void shouldMapArgumentsToParameters() {
            String sdl = """
                type Query {
                    search(query: String!, limit: Int): [Result!]!
                }
                type Result { id: ID! }
                """;

            GraphQLSchema schema = parser.parse(sdl);
            ApiSpec apiSpec = mapper.mapToApiSpec(schema);

            Endpoint endpoint = apiSpec.getEndpoints().get(0);
            assertThat(endpoint.getParameters()).hasSize(2);
        }

        @Test
        @DisplayName("Should set parameter required flag")
        void shouldSetParameterRequiredFlag() {
            String sdl = """
                type Query {
                    search(required: String!, optional: String): [Result!]!
                }
                type Result { id: ID! }
                """;

            GraphQLSchema schema = parser.parse(sdl);
            ApiSpec apiSpec = mapper.mapToApiSpec(schema);

            Endpoint endpoint = apiSpec.getEndpoints().get(0);

            assertThat(endpoint.getParameters()).anyMatch(p ->
                p.getName().equals("required") && p.isRequired());
            assertThat(endpoint.getParameters()).anyMatch(p ->
                p.getName().equals("optional") && !p.isRequired());
        }
    }

    @Nested
    @DisplayName("Response mapping")
    class ResponseMappingTests {

        @Test
        @DisplayName("Should add 200 response")
        void shouldAdd200Response() {
            String sdl = """
                type Query {
                    user(id: ID!): User
                }
                type User { id: ID! }
                """;

            GraphQLSchema schema = parser.parse(sdl);
            ApiSpec apiSpec = mapper.mapToApiSpec(schema);

            Endpoint endpoint = apiSpec.getEndpoints().get(0);
            assertThat(endpoint.getResponses()).isNotEmpty();
            assertThat(endpoint.getResponses().get(0).getStatusCode()).isEqualTo("200");
        }

        @Test
        @DisplayName("Should set response schema reference")
        void shouldSetResponseSchemaRef() {
            String sdl = """
                type Query {
                    user(id: ID!): User
                }
                type User { id: ID! }
                """;

            GraphQLSchema schema = parser.parse(sdl);
            ApiSpec apiSpec = mapper.mapToApiSpec(schema);

            Endpoint endpoint = apiSpec.getEndpoints().get(0);
            assertThat(endpoint.getResponses().get(0).getSchemaRef()).contains("User");
        }
    }

    @Nested
    @DisplayName("createEmptyApiSpec() method tests")
    class CreateEmptyApiSpecTests {

        @Test
        @DisplayName("Should create empty ApiSpec with name and version")
        void shouldCreateEmptyApiSpec() {
            ApiSpec apiSpec = mapper.createEmptyApiSpec("Test API", "1.0.0");

            assertThat(apiSpec).isNotNull();
            assertThat(apiSpec.getName()).isEqualTo("Test API");
            assertThat(apiSpec.getVersion()).isEqualTo("1.0.0");
            assertThat(apiSpec.getType()).isEqualTo(ApiType.GRAPHQL);
            assertThat(apiSpec.getEndpoints()).isEmpty();
        }

        @Test
        @DisplayName("Should use defaults for null values")
        void shouldUseDefaultsForNullValues() {
            ApiSpec apiSpec = mapper.createEmptyApiSpec(null, null);

            assertThat(apiSpec.getName()).isEqualTo("GraphQL API");
            assertThat(apiSpec.getVersion()).isEqualTo("1.0.0");
        }
    }

    @Nested
    @DisplayName("Complex schema mapping")
    class ComplexSchemaMappingTests {

        @Test
        @DisplayName("Should map complete schema")
        void shouldMapCompleteSchema() throws Exception {
            GraphQLSchema schema = parser.parseFile("src/test/resources/schemas/schema-v1.graphql");
            ApiSpec apiSpec = mapper.mapToApiSpec(schema);

            // Verify all operations are mapped
            assertThat(apiSpec.getEndpoints()).hasSizeGreaterThan(10);

            // Verify metadata
            assertThat((Integer) apiSpec.getMetadata().get("queryCount")).isGreaterThan(5);
            assertThat((Integer) apiSpec.getMetadata().get("mutationCount")).isGreaterThan(5);
            assertThat((Integer) apiSpec.getMetadata().get("subscriptionCount")).isGreaterThan(0);
        }
    }
}

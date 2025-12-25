package io.github.mohmk10.changeloghub.parser.graphql.analyzer;

import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.github.mohmk10.changeloghub.parser.graphql.model.GraphQLOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OperationAnalyzer Tests")
class OperationAnalyzerTest {

    private OperationAnalyzer analyzer;
    private SchemaParser schemaParser;

    @BeforeEach
    void setUp() {
        analyzer = new OperationAnalyzer();
        schemaParser = new SchemaParser();
    }

    private TypeDefinitionRegistry parse(String sdl) {
        return schemaParser.parse(sdl);
    }

    @Nested
    @DisplayName("Query analysis")
    class QueryTests {

        @Test
        @DisplayName("Should analyze queries from Query type")
        void shouldAnalyzeQueries() {
            String sdl = """
                type Query {
                    user(id: ID!): User
                    users: [User!]!
                    me: User
                }

                type User {
                    id: ID!
                    name: String!
                }
                """;

            List<GraphQLOperation> operations = analyzer.analyzeOperations(parse(sdl));
            List<GraphQLOperation> queries = operations.stream()
                    .filter(GraphQLOperation::isQuery)
                    .toList();

            assertThat(queries).hasSize(3);
            assertThat(queries).allMatch(q -> q.getOperationType() == GraphQLOperation.OperationType.QUERY);
        }

        @Test
        @DisplayName("Should extract query arguments")
        void shouldExtractQueryArguments() {
            String sdl = """
                type Query {
                    search(query: String!, limit: Int = 10): [Result!]!
                }

                type Result { id: ID! }
                """;

            List<GraphQLOperation> operations = analyzer.analyzeOperations(parse(sdl));
            GraphQLOperation search = operations.get(0);

            assertThat(search.getArguments()).hasSize(2);
            assertThat(search.getArguments().get(0).getName()).isEqualTo("query");
            assertThat(search.getArguments().get(0).isRequired()).isTrue();
        }

        @Test
        @DisplayName("Should extract query return type")
        void shouldExtractQueryReturnType() {
            String sdl = """
                type Query {
                    user(id: ID!): User
                    users: [User!]!
                    count: Int!
                }

                type User { id: ID! }
                """;

            List<GraphQLOperation> operations = analyzer.analyzeOperations(parse(sdl));
            Map<String, GraphQLOperation> opMap = operations.stream()
                    .collect(Collectors.toMap(GraphQLOperation::getName, o -> o));

            assertThat(opMap.get("user").getReturnType()).isEqualTo("User");
            assertThat(opMap.get("users").getReturnType()).isEqualTo("[User!]!");
            assertThat(opMap.get("count").getReturnType()).isEqualTo("Int!");
        }
    }

    @Nested
    @DisplayName("Mutation analysis")
    class MutationTests {

        @Test
        @DisplayName("Should analyze mutations from Mutation type")
        void shouldAnalyzeMutations() {
            String sdl = """
                type Query {
                    dummy: String
                }

                type Mutation {
                    createUser(name: String!, email: String!): User!
                    deleteUser(id: ID!): Boolean!
                    updateUser(id: ID!, name: String): User
                }

                type User {
                    id: ID!
                    name: String!
                }
                """;

            List<GraphQLOperation> operations = analyzer.analyzeOperations(parse(sdl));
            List<GraphQLOperation> mutations = operations.stream()
                    .filter(GraphQLOperation::isMutation)
                    .toList();

            assertThat(mutations).hasSize(3);
            assertThat(mutations).allMatch(m -> m.getOperationType() == GraphQLOperation.OperationType.MUTATION);
        }

        @Test
        @DisplayName("Should extract mutation with input type")
        void shouldExtractMutationWithInputType() {
            String sdl = """
                type Query { dummy: String }

                type Mutation {
                    createUser(input: CreateUserInput!): User!
                }

                input CreateUserInput {
                    name: String!
                    email: String!
                }

                type User { id: ID! }
                """;

            List<GraphQLOperation> mutations = analyzer.analyzeOperations(parse(sdl)).stream()
                    .filter(GraphQLOperation::isMutation)
                    .toList();

            assertThat(mutations).hasSize(1);
            assertThat(mutations.get(0).getArguments()).hasSize(1);
            assertThat(mutations.get(0).getArguments().get(0).getType()).isEqualTo("CreateUserInput!");
        }
    }

    @Nested
    @DisplayName("Subscription analysis")
    class SubscriptionTests {

        @Test
        @DisplayName("Should analyze subscriptions from Subscription type")
        void shouldAnalyzeSubscriptions() {
            String sdl = """
                type Query { dummy: String }

                type Subscription {
                    messageAdded(channelId: ID!): Message!
                    userJoined: User!
                }

                type Message { id: ID! content: String! }
                type User { id: ID! name: String! }
                """;

            List<GraphQLOperation> operations = analyzer.analyzeOperations(parse(sdl));
            List<GraphQLOperation> subscriptions = operations.stream()
                    .filter(GraphQLOperation::isSubscription)
                    .toList();

            assertThat(subscriptions).hasSize(2);
            assertThat(subscriptions).allMatch(s -> s.getOperationType() == GraphQLOperation.OperationType.SUBSCRIPTION);
        }
    }

    @Nested
    @DisplayName("Combined operations")
    class CombinedTests {

        @Test
        @DisplayName("Should analyze all operation types together")
        void shouldAnalyzeAllOperationTypes() {
            String sdl = """
                type Query {
                    user(id: ID!): User
                    users: [User!]!
                }

                type Mutation {
                    createUser(name: String!): User!
                    deleteUser(id: ID!): Boolean!
                }

                type Subscription {
                    userCreated: User!
                }

                type User {
                    id: ID!
                    name: String!
                }
                """;

            List<GraphQLOperation> operations = analyzer.analyzeOperations(parse(sdl));

            long queryCount = operations.stream().filter(GraphQLOperation::isQuery).count();
            long mutationCount = operations.stream().filter(GraphQLOperation::isMutation).count();
            long subscriptionCount = operations.stream().filter(GraphQLOperation::isSubscription).count();

            assertThat(queryCount).isEqualTo(2);
            assertThat(mutationCount).isEqualTo(2);
            assertThat(subscriptionCount).isEqualTo(1);
        }

        @Test
        @DisplayName("Should handle schema with only Query")
        void shouldHandleSchemaWithOnlyQuery() {
            String sdl = """
                type Query {
                    hello: String
                }
                """;

            List<GraphQLOperation> operations = analyzer.analyzeOperations(parse(sdl));

            assertThat(operations).hasSize(1);
            assertThat(operations.get(0).isQuery()).isTrue();
        }
    }

    @Nested
    @DisplayName("Deprecated operations")
    class DeprecatedTests {

        @Test
        @DisplayName("Should detect deprecated query")
        void shouldDetectDeprecatedQuery() {
            String sdl = """
                type Query {
                    oldUser(id: ID!): User @deprecated(reason: "Use user instead")
                    user(id: ID!): User
                }

                type User { id: ID! }
                """;

            List<GraphQLOperation> operations = analyzer.analyzeOperations(parse(sdl));
            GraphQLOperation oldUser = operations.stream()
                    .filter(o -> o.getName().equals("oldUser"))
                    .findFirst().orElseThrow();

            assertThat(oldUser.isDeprecated()).isTrue();
            assertThat(oldUser.getDeprecationReason()).contains("Use user instead");
        }

        @Test
        @DisplayName("Should detect deprecated mutation")
        void shouldDetectDeprecatedMutation() {
            String sdl = """
                type Query { dummy: String }

                type Mutation {
                    deleteUser(id: ID!): Boolean! @deprecated
                }
                """;

            List<GraphQLOperation> mutations = analyzer.analyzeOperations(parse(sdl)).stream()
                    .filter(GraphQLOperation::isMutation)
                    .toList();

            assertThat(mutations.get(0).isDeprecated()).isTrue();
        }
    }

    @Nested
    @DisplayName("Operation descriptions")
    class DescriptionTests {

        @Test
        @DisplayName("Should extract operation description")
        void shouldExtractDescription() {
            String sdl = """
                type Query {
                    "Get a user by ID"
                    user(id: ID!): User
                }

                type User { id: ID! }
                """;

            List<GraphQLOperation> operations = analyzer.analyzeOperations(parse(sdl));
            GraphQLOperation user = operations.get(0);

            assertThat(user.getDescription()).contains("Get a user by ID");
        }

        @Test
        @DisplayName("Should handle block description")
        void shouldHandleBlockDescription() {
            String sdl = """
                type Query {
                    \"\"\"
                    Search for users.
                    Returns a list of matching users.
                    \"\"\"
                    search(query: String!): [User!]!
                }

                type User { id: ID! }
                """;

            List<GraphQLOperation> operations = analyzer.analyzeOperations(parse(sdl));
            GraphQLOperation search = operations.get(0);

            assertThat(search.getDescription()).contains("Search for users");
        }
    }

    @Nested
    @DisplayName("Complex argument types")
    class ComplexArgumentTests {

        @Test
        @DisplayName("Should handle enum argument")
        void shouldHandleEnumArgument() {
            String sdl = """
                enum UserRole { ADMIN USER GUEST }

                type Query {
                    usersByRole(role: UserRole!): [User!]!
                }

                type User { id: ID! }
                """;

            List<GraphQLOperation> operations = analyzer.analyzeOperations(parse(sdl));
            GraphQLOperation query = operations.get(0);

            assertThat(query.getArguments()).hasSize(1);
            assertThat(query.getArguments().get(0).getType()).isEqualTo("UserRole!");
        }

        @Test
        @DisplayName("Should handle list argument")
        void shouldHandleListArgument() {
            String sdl = """
                type Query {
                    usersByIds(ids: [ID!]!): [User!]!
                }

                type User { id: ID! }
                """;

            List<GraphQLOperation> operations = analyzer.analyzeOperations(parse(sdl));
            GraphQLOperation query = operations.get(0);

            assertThat(query.getArguments()).hasSize(1);
            assertThat(query.getArguments().get(0).getType()).contains("[");
        }
    }
}

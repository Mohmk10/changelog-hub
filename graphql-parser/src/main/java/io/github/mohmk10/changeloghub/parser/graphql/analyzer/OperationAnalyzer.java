package io.github.mohmk10.changeloghub.parser.graphql.analyzer;

import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.github.mohmk10.changeloghub.parser.graphql.model.GraphQLOperation;
import io.github.mohmk10.changeloghub.parser.graphql.model.GraphQLOperation.OperationType;
import io.github.mohmk10.changeloghub.parser.graphql.util.GraphQLConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Analyzes GraphQL operations (Query, Mutation, Subscription).
 */
public class OperationAnalyzer {

    private final FieldAnalyzer fieldAnalyzer = new FieldAnalyzer();
    private final DirectiveAnalyzer directiveAnalyzer = new DirectiveAnalyzer();
    private final TypeAnalyzer typeAnalyzer = new TypeAnalyzer();

    /**
     * Analyzes all operations from a TypeDefinitionRegistry.
     */
    public List<GraphQLOperation> analyzeOperations(TypeDefinitionRegistry registry) {
        List<GraphQLOperation> operations = new ArrayList<>();

        // Analyze queries
        typeAnalyzer.getQueryType(registry)
                .ifPresent(queryType -> operations.addAll(analyzeQueries(queryType)));

        // Analyze mutations
        typeAnalyzer.getMutationType(registry)
                .ifPresent(mutationType -> operations.addAll(analyzeMutations(mutationType)));

        // Analyze subscriptions
        typeAnalyzer.getSubscriptionType(registry)
                .ifPresent(subType -> operations.addAll(analyzeSubscriptions(subType)));

        return operations;
    }

    /**
     * Analyzes all queries from the Query type.
     */
    public List<GraphQLOperation> analyzeQueries(ObjectTypeDefinition queryType) {
        return analyzeOperationsOfType(queryType, OperationType.QUERY);
    }

    /**
     * Analyzes all mutations from the Mutation type.
     */
    public List<GraphQLOperation> analyzeMutations(ObjectTypeDefinition mutationType) {
        return analyzeOperationsOfType(mutationType, OperationType.MUTATION);
    }

    /**
     * Analyzes all subscriptions from the Subscription type.
     */
    public List<GraphQLOperation> analyzeSubscriptions(ObjectTypeDefinition subscriptionType) {
        return analyzeOperationsOfType(subscriptionType, OperationType.SUBSCRIPTION);
    }

    /**
     * Analyzes operations from a root type definition.
     */
    private List<GraphQLOperation> analyzeOperationsOfType(ObjectTypeDefinition typeDef, OperationType opType) {
        List<GraphQLOperation> operations = new ArrayList<>();

        for (FieldDefinition fieldDef : typeDef.getFieldDefinitions()) {
            operations.add(analyzeOperation(fieldDef, opType));
        }

        return operations;
    }

    /**
     * Analyzes a single operation from a field definition.
     */
    public GraphQLOperation analyzeOperation(FieldDefinition fieldDef, OperationType opType) {
        GraphQLOperation operation = new GraphQLOperation();
        operation.setName(fieldDef.getName());
        operation.setOperationType(opType);
        operation.setReturnType(fieldAnalyzer.getFullTypeName(fieldDef.getType()));
        operation.setReturnTypeRequired(fieldAnalyzer.isNonNull(fieldDef.getType()));
        operation.setReturnTypeList(fieldAnalyzer.isList(fieldDef.getType()));
        operation.setDeprecated(directiveAnalyzer.isDeprecated(fieldDef));
        directiveAnalyzer.getDeprecationReason(fieldDef).ifPresent(operation::setDeprecationReason);

        if (fieldDef.getDescription() != null) {
            operation.setDescription(fieldDef.getDescription().getContent());
        }

        // Analyze arguments
        operation.setArguments(fieldAnalyzer.analyzeArguments(fieldDef.getInputValueDefinitions()));

        return operation;
    }

    /**
     * Gets the count of queries in a registry.
     */
    public int getQueryCount(TypeDefinitionRegistry registry) {
        return typeAnalyzer.getQueryType(registry)
                .map(q -> q.getFieldDefinitions().size())
                .orElse(0);
    }

    /**
     * Gets the count of mutations in a registry.
     */
    public int getMutationCount(TypeDefinitionRegistry registry) {
        return typeAnalyzer.getMutationType(registry)
                .map(m -> m.getFieldDefinitions().size())
                .orElse(0);
    }

    /**
     * Gets the count of subscriptions in a registry.
     */
    public int getSubscriptionCount(TypeDefinitionRegistry registry) {
        return typeAnalyzer.getSubscriptionType(registry)
                .map(s -> s.getFieldDefinitions().size())
                .orElse(0);
    }
}

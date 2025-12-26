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

public class OperationAnalyzer {

    private final FieldAnalyzer fieldAnalyzer = new FieldAnalyzer();
    private final DirectiveAnalyzer directiveAnalyzer = new DirectiveAnalyzer();
    private final TypeAnalyzer typeAnalyzer = new TypeAnalyzer();

    public List<GraphQLOperation> analyzeOperations(TypeDefinitionRegistry registry) {
        List<GraphQLOperation> operations = new ArrayList<>();

        typeAnalyzer.getQueryType(registry)
                .ifPresent(queryType -> operations.addAll(analyzeQueries(queryType)));

        typeAnalyzer.getMutationType(registry)
                .ifPresent(mutationType -> operations.addAll(analyzeMutations(mutationType)));

        typeAnalyzer.getSubscriptionType(registry)
                .ifPresent(subType -> operations.addAll(analyzeSubscriptions(subType)));

        return operations;
    }

    public List<GraphQLOperation> analyzeQueries(ObjectTypeDefinition queryType) {
        return analyzeOperationsOfType(queryType, OperationType.QUERY);
    }

    public List<GraphQLOperation> analyzeMutations(ObjectTypeDefinition mutationType) {
        return analyzeOperationsOfType(mutationType, OperationType.MUTATION);
    }

    public List<GraphQLOperation> analyzeSubscriptions(ObjectTypeDefinition subscriptionType) {
        return analyzeOperationsOfType(subscriptionType, OperationType.SUBSCRIPTION);
    }

    private List<GraphQLOperation> analyzeOperationsOfType(ObjectTypeDefinition typeDef, OperationType opType) {
        List<GraphQLOperation> operations = new ArrayList<>();

        for (FieldDefinition fieldDef : typeDef.getFieldDefinitions()) {
            operations.add(analyzeOperation(fieldDef, opType));
        }

        return operations;
    }

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

        operation.setArguments(fieldAnalyzer.analyzeArguments(fieldDef.getInputValueDefinitions()));

        return operation;
    }

    public int getQueryCount(TypeDefinitionRegistry registry) {
        return typeAnalyzer.getQueryType(registry)
                .map(q -> q.getFieldDefinitions().size())
                .orElse(0);
    }

    public int getMutationCount(TypeDefinitionRegistry registry) {
        return typeAnalyzer.getMutationType(registry)
                .map(m -> m.getFieldDefinitions().size())
                .orElse(0);
    }

    public int getSubscriptionCount(TypeDefinitionRegistry registry) {
        return typeAnalyzer.getSubscriptionType(registry)
                .map(s -> s.getFieldDefinitions().size())
                .orElse(0);
    }
}

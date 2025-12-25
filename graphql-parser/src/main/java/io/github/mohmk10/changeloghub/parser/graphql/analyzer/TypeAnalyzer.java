package io.github.mohmk10.changeloghub.parser.graphql.analyzer;

import graphql.language.*;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.github.mohmk10.changeloghub.parser.graphql.model.GraphQLType;
import io.github.mohmk10.changeloghub.parser.graphql.util.GraphQLConstants;
import io.github.mohmk10.changeloghub.parser.graphql.util.GraphQLTypeKind;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Analyzes GraphQL type definitions.
 */
public class TypeAnalyzer {

    private final FieldAnalyzer fieldAnalyzer = new FieldAnalyzer();
    private final DirectiveAnalyzer directiveAnalyzer = new DirectiveAnalyzer();

    /**
     * Analyzes all types from a TypeDefinitionRegistry.
     */
    public List<GraphQLType> analyzeTypes(TypeDefinitionRegistry registry) {
        List<GraphQLType> types = new ArrayList<>();

        // Analyze object types
        registry.types().values().stream()
                .filter(t -> t instanceof ObjectTypeDefinition)
                .map(t -> (ObjectTypeDefinition) t)
                .filter(t -> !GraphQLConstants.isRootOperationType(t.getName()))
                .forEach(t -> types.add(analyzeObjectType(t)));

        // Analyze input types
        registry.types().values().stream()
                .filter(t -> t instanceof InputObjectTypeDefinition)
                .map(t -> (InputObjectTypeDefinition) t)
                .forEach(t -> types.add(analyzeInputType(t)));

        // Analyze interface types
        registry.types().values().stream()
                .filter(t -> t instanceof InterfaceTypeDefinition)
                .map(t -> (InterfaceTypeDefinition) t)
                .forEach(t -> types.add(analyzeInterfaceType(t)));

        // Analyze union types
        registry.types().values().stream()
                .filter(t -> t instanceof UnionTypeDefinition)
                .map(t -> (UnionTypeDefinition) t)
                .forEach(t -> types.add(analyzeUnionType(t)));

        // Analyze enum types
        registry.types().values().stream()
                .filter(t -> t instanceof EnumTypeDefinition)
                .map(t -> (EnumTypeDefinition) t)
                .forEach(t -> types.add(analyzeEnumType(t)));

        // Analyze scalar types
        registry.scalars().values().stream()
                .filter(s -> !GraphQLConstants.isBuiltInScalar(s.getName()))
                .forEach(s -> types.add(analyzeScalarType(s)));

        return types;
    }

    /**
     * Analyzes an object type definition.
     */
    public GraphQLType analyzeObjectType(ObjectTypeDefinition def) {
        GraphQLType type = new GraphQLType(def.getName(), GraphQLTypeKind.OBJECT);

        if (def.getDescription() != null) {
            type.setDescription(def.getDescription().getContent());
        }

        // Add implemented interfaces
        for (graphql.language.Type<?> implementedType : def.getImplements()) {
            if (implementedType instanceof TypeName) {
                type.addInterface(((TypeName) implementedType).getName());
            }
        }

        // Add fields
        type.setFields(fieldAnalyzer.analyzeFields(def));

        return type;
    }

    /**
     * Analyzes an input object type definition.
     */
    public GraphQLType analyzeInputType(InputObjectTypeDefinition def) {
        GraphQLType type = new GraphQLType(def.getName(), GraphQLTypeKind.INPUT_OBJECT);

        if (def.getDescription() != null) {
            type.setDescription(def.getDescription().getContent());
        }

        // Add fields
        type.setFields(fieldAnalyzer.analyzeInputFields(def));

        return type;
    }

    /**
     * Analyzes an interface type definition.
     */
    public GraphQLType analyzeInterfaceType(InterfaceTypeDefinition def) {
        GraphQLType type = new GraphQLType(def.getName(), GraphQLTypeKind.INTERFACE);

        if (def.getDescription() != null) {
            type.setDescription(def.getDescription().getContent());
        }

        // Add fields
        type.setFields(fieldAnalyzer.analyzeFields(def));

        return type;
    }

    /**
     * Analyzes a union type definition.
     */
    public GraphQLType analyzeUnionType(UnionTypeDefinition def) {
        GraphQLType type = new GraphQLType(def.getName(), GraphQLTypeKind.UNION);

        if (def.getDescription() != null) {
            type.setDescription(def.getDescription().getContent());
        }

        // Add member types
        for (graphql.language.Type<?> memberType : def.getMemberTypes()) {
            if (memberType instanceof TypeName) {
                type.addPossibleType(((TypeName) memberType).getName());
            }
        }

        return type;
    }

    /**
     * Analyzes an enum type definition.
     */
    public GraphQLType analyzeEnumType(EnumTypeDefinition def) {
        GraphQLType type = new GraphQLType(def.getName(), GraphQLTypeKind.ENUM);

        if (def.getDescription() != null) {
            type.setDescription(def.getDescription().getContent());
        }

        // Add enum values
        for (EnumValueDefinition valueDef : def.getEnumValueDefinitions()) {
            type.addEnumValue(valueDef.getName());
        }

        return type;
    }

    /**
     * Analyzes a scalar type definition.
     */
    public GraphQLType analyzeScalarType(ScalarTypeDefinition def) {
        GraphQLType type = new GraphQLType(def.getName(), GraphQLTypeKind.SCALAR);

        if (def.getDescription() != null) {
            type.setDescription(def.getDescription().getContent());
        }

        return type;
    }

    /**
     * Gets the Query type definition from the registry.
     */
    public Optional<ObjectTypeDefinition> getQueryType(TypeDefinitionRegistry registry) {
        return getOperationType(registry, GraphQLConstants.QUERY_TYPE);
    }

    /**
     * Gets the Mutation type definition from the registry.
     */
    public Optional<ObjectTypeDefinition> getMutationType(TypeDefinitionRegistry registry) {
        return getOperationType(registry, GraphQLConstants.MUTATION_TYPE);
    }

    /**
     * Gets the Subscription type definition from the registry.
     */
    public Optional<ObjectTypeDefinition> getSubscriptionType(TypeDefinitionRegistry registry) {
        return getOperationType(registry, GraphQLConstants.SUBSCRIPTION_TYPE);
    }

    private Optional<ObjectTypeDefinition> getOperationType(TypeDefinitionRegistry registry, String typeName) {
        return registry.getType(typeName)
                .filter(t -> t instanceof ObjectTypeDefinition)
                .map(t -> (ObjectTypeDefinition) t);
    }

    /**
     * Checks if a field is deprecated.
     */
    public boolean isDeprecated(FieldDefinition field) {
        return directiveAnalyzer.isDeprecated(field);
    }
}

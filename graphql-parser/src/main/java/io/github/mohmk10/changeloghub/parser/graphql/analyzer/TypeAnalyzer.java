package io.github.mohmk10.changeloghub.parser.graphql.analyzer;

import graphql.language.*;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.github.mohmk10.changeloghub.parser.graphql.model.GraphQLType;
import io.github.mohmk10.changeloghub.parser.graphql.util.GraphQLConstants;
import io.github.mohmk10.changeloghub.parser.graphql.util.GraphQLTypeKind;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TypeAnalyzer {

    private final FieldAnalyzer fieldAnalyzer = new FieldAnalyzer();
    private final DirectiveAnalyzer directiveAnalyzer = new DirectiveAnalyzer();

    public List<GraphQLType> analyzeTypes(TypeDefinitionRegistry registry) {
        List<GraphQLType> types = new ArrayList<>();

        registry.types().values().stream()
                .filter(t -> t instanceof ObjectTypeDefinition)
                .map(t -> (ObjectTypeDefinition) t)
                .filter(t -> !GraphQLConstants.isRootOperationType(t.getName()))
                .forEach(t -> types.add(analyzeObjectType(t)));

        registry.types().values().stream()
                .filter(t -> t instanceof InputObjectTypeDefinition)
                .map(t -> (InputObjectTypeDefinition) t)
                .forEach(t -> types.add(analyzeInputType(t)));

        registry.types().values().stream()
                .filter(t -> t instanceof InterfaceTypeDefinition)
                .map(t -> (InterfaceTypeDefinition) t)
                .forEach(t -> types.add(analyzeInterfaceType(t)));

        registry.types().values().stream()
                .filter(t -> t instanceof UnionTypeDefinition)
                .map(t -> (UnionTypeDefinition) t)
                .forEach(t -> types.add(analyzeUnionType(t)));

        registry.types().values().stream()
                .filter(t -> t instanceof EnumTypeDefinition)
                .map(t -> (EnumTypeDefinition) t)
                .forEach(t -> types.add(analyzeEnumType(t)));

        registry.scalars().values().stream()
                .filter(s -> !GraphQLConstants.isBuiltInScalar(s.getName()))
                .forEach(s -> types.add(analyzeScalarType(s)));

        return types;
    }

    public GraphQLType analyzeObjectType(ObjectTypeDefinition def) {
        GraphQLType type = new GraphQLType(def.getName(), GraphQLTypeKind.OBJECT);

        if (def.getDescription() != null) {
            type.setDescription(def.getDescription().getContent());
        }

        for (graphql.language.Type<?> implementedType : def.getImplements()) {
            if (implementedType instanceof TypeName) {
                type.addInterface(((TypeName) implementedType).getName());
            }
        }

        type.setFields(fieldAnalyzer.analyzeFields(def));

        return type;
    }

    public GraphQLType analyzeInputType(InputObjectTypeDefinition def) {
        GraphQLType type = new GraphQLType(def.getName(), GraphQLTypeKind.INPUT_OBJECT);

        if (def.getDescription() != null) {
            type.setDescription(def.getDescription().getContent());
        }

        type.setFields(fieldAnalyzer.analyzeInputFields(def));

        return type;
    }

    public GraphQLType analyzeInterfaceType(InterfaceTypeDefinition def) {
        GraphQLType type = new GraphQLType(def.getName(), GraphQLTypeKind.INTERFACE);

        if (def.getDescription() != null) {
            type.setDescription(def.getDescription().getContent());
        }

        type.setFields(fieldAnalyzer.analyzeFields(def));

        return type;
    }

    public GraphQLType analyzeUnionType(UnionTypeDefinition def) {
        GraphQLType type = new GraphQLType(def.getName(), GraphQLTypeKind.UNION);

        if (def.getDescription() != null) {
            type.setDescription(def.getDescription().getContent());
        }

        for (graphql.language.Type<?> memberType : def.getMemberTypes()) {
            if (memberType instanceof TypeName) {
                type.addPossibleType(((TypeName) memberType).getName());
            }
        }

        return type;
    }

    public GraphQLType analyzeEnumType(EnumTypeDefinition def) {
        GraphQLType type = new GraphQLType(def.getName(), GraphQLTypeKind.ENUM);

        if (def.getDescription() != null) {
            type.setDescription(def.getDescription().getContent());
        }

        for (EnumValueDefinition valueDef : def.getEnumValueDefinitions()) {
            type.addEnumValue(valueDef.getName());
        }

        return type;
    }

    public GraphQLType analyzeScalarType(ScalarTypeDefinition def) {
        GraphQLType type = new GraphQLType(def.getName(), GraphQLTypeKind.SCALAR);

        if (def.getDescription() != null) {
            type.setDescription(def.getDescription().getContent());
        }

        return type;
    }

    public Optional<ObjectTypeDefinition> getQueryType(TypeDefinitionRegistry registry) {
        return getOperationType(registry, GraphQLConstants.QUERY_TYPE);
    }

    public Optional<ObjectTypeDefinition> getMutationType(TypeDefinitionRegistry registry) {
        return getOperationType(registry, GraphQLConstants.MUTATION_TYPE);
    }

    public Optional<ObjectTypeDefinition> getSubscriptionType(TypeDefinitionRegistry registry) {
        return getOperationType(registry, GraphQLConstants.SUBSCRIPTION_TYPE);
    }

    private Optional<ObjectTypeDefinition> getOperationType(TypeDefinitionRegistry registry, String typeName) {
        return registry.getType(typeName)
                .filter(t -> t instanceof ObjectTypeDefinition)
                .map(t -> (ObjectTypeDefinition) t);
    }

    public boolean isDeprecated(FieldDefinition field) {
        return directiveAnalyzer.isDeprecated(field);
    }
}

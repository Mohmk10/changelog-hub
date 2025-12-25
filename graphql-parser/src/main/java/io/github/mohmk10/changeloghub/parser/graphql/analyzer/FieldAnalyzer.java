package io.github.mohmk10.changeloghub.parser.graphql.analyzer;

import graphql.language.*;
import io.github.mohmk10.changeloghub.parser.graphql.model.GraphQLArgument;
import io.github.mohmk10.changeloghub.parser.graphql.model.GraphQLField;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Analyzes GraphQL fields and their arguments.
 */
public class FieldAnalyzer {

    private final DirectiveAnalyzer directiveAnalyzer = new DirectiveAnalyzer();

    /**
     * Analyzes all fields from an object type definition.
     */
    public List<GraphQLField> analyzeFields(ObjectTypeDefinition type) {
        List<GraphQLField> fields = new ArrayList<>();
        for (FieldDefinition fieldDef : type.getFieldDefinitions()) {
            fields.add(analyzeField(fieldDef));
        }
        return fields;
    }

    /**
     * Analyzes all fields from an interface type definition.
     */
    public List<GraphQLField> analyzeFields(InterfaceTypeDefinition type) {
        List<GraphQLField> fields = new ArrayList<>();
        for (FieldDefinition fieldDef : type.getFieldDefinitions()) {
            fields.add(analyzeField(fieldDef));
        }
        return fields;
    }

    /**
     * Analyzes all fields from an input object type definition.
     */
    public List<GraphQLField> analyzeInputFields(InputObjectTypeDefinition type) {
        List<GraphQLField> fields = new ArrayList<>();
        for (InputValueDefinition inputDef : type.getInputValueDefinitions()) {
            fields.add(analyzeInputField(inputDef));
        }
        return fields;
    }

    /**
     * Analyzes a single field definition.
     */
    public GraphQLField analyzeField(FieldDefinition fieldDef) {
        GraphQLField field = new GraphQLField();
        field.setName(fieldDef.getName());
        field.setType(getBaseTypeName(fieldDef.getType()));
        field.setRequired(isNonNull(fieldDef.getType()));
        field.setList(isList(fieldDef.getType()));
        field.setListItemRequired(isListItemNonNull(fieldDef.getType()));
        field.setDeprecated(directiveAnalyzer.isDeprecated(fieldDef));
        directiveAnalyzer.getDeprecationReason(fieldDef).ifPresent(field::setDeprecationReason);

        if (fieldDef.getDescription() != null) {
            field.setDescription(fieldDef.getDescription().getContent());
        }

        // Analyze arguments
        for (InputValueDefinition argDef : fieldDef.getInputValueDefinitions()) {
            field.addArgument(analyzeArgument(argDef));
        }

        return field;
    }

    /**
     * Analyzes an input field definition.
     */
    public GraphQLField analyzeInputField(InputValueDefinition inputDef) {
        GraphQLField field = new GraphQLField();
        field.setName(inputDef.getName());
        field.setType(getBaseTypeName(inputDef.getType()));
        field.setRequired(isNonNull(inputDef.getType()));
        field.setList(isList(inputDef.getType()));
        field.setListItemRequired(isListItemNonNull(inputDef.getType()));

        if (inputDef.getDescription() != null) {
            field.setDescription(inputDef.getDescription().getContent());
        }

        return field;
    }

    /**
     * Analyzes an argument definition.
     */
    public GraphQLArgument analyzeArgument(InputValueDefinition argDef) {
        GraphQLArgument arg = new GraphQLArgument();
        arg.setName(argDef.getName());
        arg.setType(getFullTypeName(argDef.getType()));
        arg.setRequired(isNonNull(argDef.getType()));

        if (argDef.getDescription() != null) {
            arg.setDescription(argDef.getDescription().getContent());
        }

        if (argDef.getDefaultValue() != null) {
            arg.setDefaultValue(valueToString(argDef.getDefaultValue()));
        }

        return arg;
    }

    /**
     * Analyzes a list of input value definitions as arguments.
     */
    public List<GraphQLArgument> analyzeArguments(List<InputValueDefinition> inputDefs) {
        List<GraphQLArgument> arguments = new ArrayList<>();
        for (InputValueDefinition inputDef : inputDefs) {
            arguments.add(analyzeArgument(inputDef));
        }
        return arguments;
    }

    /**
     * Gets the base type name without wrappers (NonNull, List).
     */
    public String getBaseTypeName(Type<?> type) {
        if (type instanceof NonNullType) {
            return getBaseTypeName(((NonNullType) type).getType());
        } else if (type instanceof ListType) {
            return getBaseTypeName(((ListType) type).getType());
        } else if (type instanceof TypeName) {
            return ((TypeName) type).getName();
        }
        return "Unknown";
    }

    /**
     * Gets the full type name including wrappers.
     */
    public String getFullTypeName(Type<?> type) {
        if (type instanceof NonNullType) {
            return getFullTypeName(((NonNullType) type).getType()) + "!";
        } else if (type instanceof ListType) {
            return "[" + getFullTypeName(((ListType) type).getType()) + "]";
        } else if (type instanceof TypeName) {
            return ((TypeName) type).getName();
        }
        return "Unknown";
    }

    /**
     * Checks if a type is non-null.
     */
    public boolean isNonNull(Type<?> type) {
        return type instanceof NonNullType;
    }

    /**
     * Checks if a type is a list.
     */
    public boolean isList(Type<?> type) {
        if (type instanceof NonNullType) {
            return isList(((NonNullType) type).getType());
        }
        return type instanceof ListType;
    }

    /**
     * Checks if the list item type is non-null.
     */
    public boolean isListItemNonNull(Type<?> type) {
        if (type instanceof NonNullType) {
            return isListItemNonNull(((NonNullType) type).getType());
        }
        if (type instanceof ListType) {
            Type<?> innerType = ((ListType) type).getType();
            return innerType instanceof NonNullType;
        }
        return false;
    }

    /**
     * Converts a GraphQL value to a string representation.
     */
    private String valueToString(Value<?> value) {
        if (value instanceof StringValue) {
            return "\"" + ((StringValue) value).getValue() + "\"";
        } else if (value instanceof IntValue) {
            return ((IntValue) value).getValue().toString();
        } else if (value instanceof FloatValue) {
            return ((FloatValue) value).getValue().toString();
        } else if (value instanceof BooleanValue) {
            return String.valueOf(((BooleanValue) value).isValue());
        } else if (value instanceof EnumValue) {
            return ((EnumValue) value).getName();
        } else if (value instanceof NullValue) {
            return "null";
        } else if (value instanceof ArrayValue) {
            return value.toString();
        } else if (value instanceof ObjectValue) {
            return value.toString();
        }
        return value.toString();
    }
}

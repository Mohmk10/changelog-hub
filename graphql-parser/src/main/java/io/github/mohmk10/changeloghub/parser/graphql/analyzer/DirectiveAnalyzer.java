package io.github.mohmk10.changeloghub.parser.graphql.analyzer;

import graphql.language.*;
import io.github.mohmk10.changeloghub.parser.graphql.util.GraphQLConstants;

import java.util.List;
import java.util.Optional;

/**
 * Analyzes GraphQL directives, particularly @deprecated.
 */
public class DirectiveAnalyzer {

    /**
     * Checks if a definition has the @deprecated directive.
     */
    public boolean isDeprecated(DirectivesContainer<?> definition) {
        return definition.getDirectives().stream()
                .anyMatch(d -> GraphQLConstants.DIRECTIVE_DEPRECATED.equals(d.getName()));
    }

    /**
     * Gets the deprecation reason from a @deprecated directive.
     */
    public Optional<String> getDeprecationReason(DirectivesContainer<?> definition) {
        return definition.getDirectives().stream()
                .filter(d -> GraphQLConstants.DIRECTIVE_DEPRECATED.equals(d.getName()))
                .findFirst()
                .flatMap(this::extractReason);
    }

    private Optional<String> extractReason(Directive directive) {
        Argument reasonArg = directive.getArgument(GraphQLConstants.DEPRECATED_REASON);
        if (reasonArg != null && reasonArg.getValue() instanceof StringValue) {
            return Optional.of(((StringValue) reasonArg.getValue()).getValue());
        }
        return Optional.of(GraphQLConstants.DEFAULT_DEPRECATED_REASON);
    }

    /**
     * Checks if a field definition is deprecated.
     */
    public boolean isFieldDeprecated(FieldDefinition field) {
        return isDeprecated(field);
    }

    /**
     * Checks if an enum value is deprecated.
     */
    public boolean isEnumValueDeprecated(EnumValueDefinition enumValue) {
        return isDeprecated(enumValue);
    }

    /**
     * Gets all directive names from a definition.
     */
    public List<String> getDirectiveNames(DirectivesContainer<?> definition) {
        return definition.getDirectives().stream()
                .map(Directive::getName)
                .toList();
    }

    /**
     * Checks if a definition has a specific directive.
     */
    public boolean hasDirective(DirectivesContainer<?> definition, String directiveName) {
        return definition.getDirectives().stream()
                .anyMatch(d -> directiveName.equals(d.getName()));
    }

    /**
     * Gets a directive argument value as a string.
     */
    public Optional<String> getDirectiveArgumentAsString(Directive directive, String argumentName) {
        Argument arg = directive.getArgument(argumentName);
        if (arg != null && arg.getValue() instanceof StringValue) {
            return Optional.of(((StringValue) arg.getValue()).getValue());
        }
        return Optional.empty();
    }
}

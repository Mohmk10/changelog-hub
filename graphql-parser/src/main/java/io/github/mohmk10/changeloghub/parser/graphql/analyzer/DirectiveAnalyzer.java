package io.github.mohmk10.changeloghub.parser.graphql.analyzer;

import graphql.language.*;
import io.github.mohmk10.changeloghub.parser.graphql.util.GraphQLConstants;

import java.util.List;
import java.util.Optional;

public class DirectiveAnalyzer {

    public boolean isDeprecated(DirectivesContainer<?> definition) {
        return definition.getDirectives().stream()
                .anyMatch(d -> GraphQLConstants.DIRECTIVE_DEPRECATED.equals(d.getName()));
    }

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

    public boolean isFieldDeprecated(FieldDefinition field) {
        return isDeprecated(field);
    }

    public boolean isEnumValueDeprecated(EnumValueDefinition enumValue) {
        return isDeprecated(enumValue);
    }

    public List<String> getDirectiveNames(DirectivesContainer<?> definition) {
        return definition.getDirectives().stream()
                .map(Directive::getName)
                .toList();
    }

    public boolean hasDirective(DirectivesContainer<?> definition, String directiveName) {
        return definition.getDirectives().stream()
                .anyMatch(d -> directiveName.equals(d.getName()));
    }

    public Optional<String> getDirectiveArgumentAsString(Directive directive, String argumentName) {
        Argument arg = directive.getArgument(argumentName);
        if (arg != null && arg.getValue() instanceof StringValue) {
            return Optional.of(((StringValue) arg.getValue()).getValue());
        }
        return Optional.empty();
    }
}

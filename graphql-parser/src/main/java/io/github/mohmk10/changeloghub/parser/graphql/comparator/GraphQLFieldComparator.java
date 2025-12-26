package io.github.mohmk10.changeloghub.parser.graphql.comparator;

import io.github.mohmk10.changeloghub.core.model.*;
import io.github.mohmk10.changeloghub.parser.graphql.model.GraphQLArgument;
import io.github.mohmk10.changeloghub.parser.graphql.model.GraphQLField;

import java.util.*;

public class GraphQLFieldComparator {

    public List<Change> compareFields(List<GraphQLField> oldFields, List<GraphQLField> newFields, String parentPath) {
        List<Change> changes = new ArrayList<>();

        Map<String, GraphQLField> oldFieldMap = toMap(oldFields);
        Map<String, GraphQLField> newFieldMap = toMap(newFields);

        for (String fieldName : oldFieldMap.keySet()) {
            if (!newFieldMap.containsKey(fieldName)) {
                changes.add(createChange(
                        ChangeType.REMOVED,
                        ChangeCategory.FIELD,
                        Severity.BREAKING,
                        parentPath + "." + fieldName,
                        "Field '" + fieldName + "' removed",
                        oldFieldMap.get(fieldName),
                        null
                ));
            }
        }

        for (String fieldName : newFieldMap.keySet()) {
            if (!oldFieldMap.containsKey(fieldName)) {
                changes.add(createChange(
                        ChangeType.ADDED,
                        ChangeCategory.FIELD,
                        Severity.INFO,
                        parentPath + "." + fieldName,
                        "Field '" + fieldName + "' added",
                        null,
                        newFieldMap.get(fieldName)
                ));
            }
        }

        for (String fieldName : oldFieldMap.keySet()) {
            if (newFieldMap.containsKey(fieldName)) {
                GraphQLField oldField = oldFieldMap.get(fieldName);
                GraphQLField newField = newFieldMap.get(fieldName);
                changes.addAll(compareField(oldField, newField, parentPath + "." + fieldName));
            }
        }

        return changes;
    }

    public List<Change> compareField(GraphQLField oldField, GraphQLField newField, String path) {
        List<Change> changes = new ArrayList<>();

        if (!Objects.equals(oldField.getType(), newField.getType())) {
            changes.add(createChange(
                    ChangeType.MODIFIED,
                    ChangeCategory.FIELD,
                    Severity.BREAKING,
                    path,
                    "Field type changed from '" + oldField.getType() + "' to '" + newField.getType() + "'",
                    oldField.getType(),
                    newField.getType()
            ));
        }

        if (!oldField.isRequired() && newField.isRequired()) {
            changes.add(createChange(
                    ChangeType.MODIFIED,
                    ChangeCategory.FIELD,
                    Severity.DANGEROUS,
                    path,
                    "Field changed from nullable to non-null",
                    false,
                    true
            ));
        }

        if (!oldField.isDeprecated() && newField.isDeprecated()) {
            changes.add(createChange(
                    ChangeType.MODIFIED,
                    ChangeCategory.FIELD,
                    Severity.WARNING,
                    path,
                    "Field marked as deprecated",
                    false,
                    true
            ));
        }

        changes.addAll(compareArguments(oldField.getArguments(), newField.getArguments(), path));

        return changes;
    }

    public List<Change> compareArguments(List<GraphQLArgument> oldArgs, List<GraphQLArgument> newArgs, String parentPath) {
        List<Change> changes = new ArrayList<>();

        Map<String, GraphQLArgument> oldArgMap = toArgMap(oldArgs);
        Map<String, GraphQLArgument> newArgMap = toArgMap(newArgs);

        for (String argName : oldArgMap.keySet()) {
            if (!newArgMap.containsKey(argName)) {
                changes.add(createChange(
                        ChangeType.REMOVED,
                        ChangeCategory.PARAMETER,
                        Severity.DANGEROUS,
                        parentPath + "(" + argName + ")",
                        "Argument '" + argName + "' removed",
                        oldArgMap.get(argName),
                        null
                ));
            }
        }

        for (String argName : newArgMap.keySet()) {
            GraphQLArgument newArg = newArgMap.get(argName);
            if (!oldArgMap.containsKey(argName)) {
                
                Severity severity = newArg.isRequired() && !newArg.hasDefaultValue()
                        ? Severity.BREAKING : Severity.INFO;
                changes.add(createChange(
                        ChangeType.ADDED,
                        ChangeCategory.PARAMETER,
                        severity,
                        parentPath + "(" + argName + ")",
                        "Argument '" + argName + "' added" + (severity == Severity.BREAKING ? " (required)" : ""),
                        null,
                        newArg
                ));
            }
        }

        for (String argName : oldArgMap.keySet()) {
            if (newArgMap.containsKey(argName)) {
                GraphQLArgument oldArg = oldArgMap.get(argName);
                GraphQLArgument newArg = newArgMap.get(argName);
                changes.addAll(compareArgument(oldArg, newArg, parentPath + "(" + argName + ")"));
            }
        }

        return changes;
    }

    public List<Change> compareArgument(GraphQLArgument oldArg, GraphQLArgument newArg, String path) {
        List<Change> changes = new ArrayList<>();

        if (!Objects.equals(oldArg.getType(), newArg.getType())) {
            changes.add(createChange(
                    ChangeType.MODIFIED,
                    ChangeCategory.PARAMETER,
                    Severity.BREAKING,
                    path,
                    "Argument type changed from '" + oldArg.getType() + "' to '" + newArg.getType() + "'",
                    oldArg.getType(),
                    newArg.getType()
            ));
        }

        if (!Objects.equals(oldArg.getDefaultValue(), newArg.getDefaultValue())) {
            changes.add(createChange(
                    ChangeType.MODIFIED,
                    ChangeCategory.PARAMETER,
                    Severity.DANGEROUS,
                    path,
                    "Argument default value changed",
                    oldArg.getDefaultValue(),
                    newArg.getDefaultValue()
            ));
        }

        return changes;
    }

    private Map<String, GraphQLField> toMap(List<GraphQLField> fields) {
        Map<String, GraphQLField> map = new LinkedHashMap<>();
        for (GraphQLField field : fields) {
            map.put(field.getName(), field);
        }
        return map;
    }

    private Map<String, GraphQLArgument> toArgMap(List<GraphQLArgument> args) {
        Map<String, GraphQLArgument> map = new LinkedHashMap<>();
        for (GraphQLArgument arg : args) {
            map.put(arg.getName(), arg);
        }
        return map;
    }

    private Change createChange(ChangeType type, ChangeCategory category, Severity severity,
                                 String path, String description, Object oldValue, Object newValue) {
        return Change.builder()
                .type(type)
                .category(category)
                .severity(severity)
                .path(path)
                .description(description)
                .oldValue(oldValue)
                .newValue(newValue)
                .build();
    }
}

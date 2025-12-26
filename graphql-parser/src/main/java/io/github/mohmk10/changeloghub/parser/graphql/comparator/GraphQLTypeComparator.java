package io.github.mohmk10.changeloghub.parser.graphql.comparator;

import io.github.mohmk10.changeloghub.core.model.*;
import io.github.mohmk10.changeloghub.parser.graphql.model.GraphQLType;

import java.util.*;

public class GraphQLTypeComparator {

    private final GraphQLFieldComparator fieldComparator = new GraphQLFieldComparator();

    public List<Change> compareTypes(Map<String, GraphQLType> oldTypes, Map<String, GraphQLType> newTypes) {
        List<Change> changes = new ArrayList<>();

        for (String typeName : oldTypes.keySet()) {
            if (!newTypes.containsKey(typeName)) {
                changes.add(createChange(
                        ChangeType.REMOVED,
                        ChangeCategory.TYPE,
                        Severity.BREAKING,
                        typeName,
                        "Type '" + typeName + "' removed",
                        oldTypes.get(typeName),
                        null
                ));
            }
        }

        for (String typeName : newTypes.keySet()) {
            if (!oldTypes.containsKey(typeName)) {
                changes.add(createChange(
                        ChangeType.ADDED,
                        ChangeCategory.TYPE,
                        Severity.INFO,
                        typeName,
                        "Type '" + typeName + "' added",
                        null,
                        newTypes.get(typeName)
                ));
            }
        }

        for (String typeName : oldTypes.keySet()) {
            if (newTypes.containsKey(typeName)) {
                GraphQLType oldType = oldTypes.get(typeName);
                GraphQLType newType = newTypes.get(typeName);
                changes.addAll(compareType(oldType, newType));
            }
        }

        return changes;
    }

    public List<Change> compareType(GraphQLType oldType, GraphQLType newType) {
        List<Change> changes = new ArrayList<>();
        String path = oldType.getName();

        if (oldType.getKind() != newType.getKind()) {
            changes.add(createChange(
                    ChangeType.MODIFIED,
                    ChangeCategory.TYPE,
                    Severity.BREAKING,
                    path,
                    "Type kind changed from " + oldType.getKind() + " to " + newType.getKind(),
                    oldType.getKind(),
                    newType.getKind()
            ));
            return changes; 
        }

        switch (oldType.getKind()) {
            case OBJECT, INPUT_OBJECT, INTERFACE:
                changes.addAll(fieldComparator.compareFields(oldType.getFields(), newType.getFields(), path));
                break;
            case ENUM:
                changes.addAll(compareEnumValues(oldType, newType, path));
                break;
            case UNION:
                changes.addAll(compareUnionTypes(oldType, newType, path));
                break;
            default:
                break;
        }

        if (oldType.isObjectType()) {
            changes.addAll(compareInterfaces(oldType, newType, path));
        }

        return changes;
    }

    public List<Change> compareEnumValues(GraphQLType oldType, GraphQLType newType, String path) {
        List<Change> changes = new ArrayList<>();

        Set<String> oldValues = new HashSet<>(oldType.getEnumValues());
        Set<String> newValues = new HashSet<>(newType.getEnumValues());

        for (String value : oldValues) {
            if (!newValues.contains(value)) {
                changes.add(createChange(
                        ChangeType.REMOVED,
                        ChangeCategory.ENUM_VALUE,
                        Severity.BREAKING,
                        path + "." + value,
                        "Enum value '" + value + "' removed from " + path,
                        value,
                        null
                ));
            }
        }

        for (String value : newValues) {
            if (!oldValues.contains(value)) {
                changes.add(createChange(
                        ChangeType.ADDED,
                        ChangeCategory.ENUM_VALUE,
                        Severity.INFO,
                        path + "." + value,
                        "Enum value '" + value + "' added to " + path,
                        null,
                        value
                ));
            }
        }

        return changes;
    }

    public List<Change> compareUnionTypes(GraphQLType oldType, GraphQLType newType, String path) {
        List<Change> changes = new ArrayList<>();

        Set<String> oldMembers = new HashSet<>(oldType.getPossibleTypes());
        Set<String> newMembers = new HashSet<>(newType.getPossibleTypes());

        for (String member : oldMembers) {
            if (!newMembers.contains(member)) {
                changes.add(createChange(
                        ChangeType.REMOVED,
                        ChangeCategory.UNION_MEMBER,
                        Severity.BREAKING,
                        path + "." + member,
                        "Type '" + member + "' removed from union " + path,
                        member,
                        null
                ));
            }
        }

        for (String member : newMembers) {
            if (!oldMembers.contains(member)) {
                changes.add(createChange(
                        ChangeType.ADDED,
                        ChangeCategory.UNION_MEMBER,
                        Severity.INFO,
                        path + "." + member,
                        "Type '" + member + "' added to union " + path,
                        null,
                        member
                ));
            }
        }

        return changes;
    }

    public List<Change> compareInterfaces(GraphQLType oldType, GraphQLType newType, String path) {
        List<Change> changes = new ArrayList<>();

        Set<String> oldInterfaces = new HashSet<>(oldType.getInterfaces());
        Set<String> newInterfaces = new HashSet<>(newType.getInterfaces());

        for (String iface : oldInterfaces) {
            if (!newInterfaces.contains(iface)) {
                changes.add(createChange(
                        ChangeType.REMOVED,
                        ChangeCategory.INTERFACE,
                        Severity.BREAKING,
                        path + " implements " + iface,
                        "Interface '" + iface + "' removed from type " + path,
                        iface,
                        null
                ));
            }
        }

        for (String iface : newInterfaces) {
            if (!oldInterfaces.contains(iface)) {
                changes.add(createChange(
                        ChangeType.ADDED,
                        ChangeCategory.INTERFACE,
                        Severity.INFO,
                        path + " implements " + iface,
                        "Interface '" + iface + "' added to type " + path,
                        null,
                        iface
                ));
            }
        }

        return changes;
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

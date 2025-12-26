package io.github.mohmk10.changeloghub.parser.graphql.comparator;

import io.github.mohmk10.changeloghub.core.model.*;
import io.github.mohmk10.changeloghub.parser.graphql.model.GraphQLOperation;
import io.github.mohmk10.changeloghub.parser.graphql.model.GraphQLSchema;

import java.util.*;

public class GraphQLSchemaComparator {

    private final GraphQLTypeComparator typeComparator = new GraphQLTypeComparator();
    private final GraphQLFieldComparator fieldComparator = new GraphQLFieldComparator();

    public List<Change> compare(GraphQLSchema oldSchema, GraphQLSchema newSchema) {
        List<Change> changes = new ArrayList<>();

        changes.addAll(typeComparator.compareTypes(oldSchema.getTypes(), newSchema.getTypes()));

        changes.addAll(compareOperations(oldSchema.getQueries(), newSchema.getQueries(), "Query"));

        changes.addAll(compareOperations(oldSchema.getMutations(), newSchema.getMutations(), "Mutation"));

        changes.addAll(compareOperations(oldSchema.getSubscriptions(), newSchema.getSubscriptions(), "Subscription"));

        return changes;
    }

    public List<Change> compareOperations(List<GraphQLOperation> oldOps, List<GraphQLOperation> newOps, String opType) {
        List<Change> changes = new ArrayList<>();

        Map<String, GraphQLOperation> oldOpMap = toMap(oldOps);
        Map<String, GraphQLOperation> newOpMap = toMap(newOps);

        for (String opName : oldOpMap.keySet()) {
            if (!newOpMap.containsKey(opName)) {
                changes.add(createChange(
                        ChangeType.REMOVED,
                        ChangeCategory.ENDPOINT,
                        Severity.BREAKING,
                        opType + "." + opName,
                        opType + " '" + opName + "' removed",
                        oldOpMap.get(opName),
                        null
                ));
            }
        }

        for (String opName : newOpMap.keySet()) {
            if (!oldOpMap.containsKey(opName)) {
                changes.add(createChange(
                        ChangeType.ADDED,
                        ChangeCategory.ENDPOINT,
                        Severity.INFO,
                        opType + "." + opName,
                        opType + " '" + opName + "' added",
                        null,
                        newOpMap.get(opName)
                ));
            }
        }

        for (String opName : oldOpMap.keySet()) {
            if (newOpMap.containsKey(opName)) {
                GraphQLOperation oldOp = oldOpMap.get(opName);
                GraphQLOperation newOp = newOpMap.get(opName);
                changes.addAll(compareOperation(oldOp, newOp, opType + "." + opName));
            }
        }

        return changes;
    }

    public List<Change> compareOperation(GraphQLOperation oldOp, GraphQLOperation newOp, String path) {
        List<Change> changes = new ArrayList<>();

        if (!Objects.equals(oldOp.getReturnType(), newOp.getReturnType())) {
            changes.add(createChange(
                    ChangeType.MODIFIED,
                    ChangeCategory.ENDPOINT,
                    Severity.BREAKING,
                    path,
                    "Return type changed from '" + oldOp.getReturnType() + "' to '" + newOp.getReturnType() + "'",
                    oldOp.getReturnType(),
                    newOp.getReturnType()
            ));
        }

        if (!oldOp.isDeprecated() && newOp.isDeprecated()) {
            changes.add(createChange(
                    ChangeType.MODIFIED,
                    ChangeCategory.ENDPOINT,
                    Severity.WARNING,
                    path,
                    "Operation marked as deprecated",
                    false,
                    true
            ));
        }

        changes.addAll(fieldComparator.compareArguments(oldOp.getArguments(), newOp.getArguments(), path));

        return changes;
    }

    public boolean hasBreakingChanges(List<Change> changes) {
        return changes.stream().anyMatch(c -> c.getSeverity() == Severity.BREAKING);
    }

    public List<Change> getBreakingChanges(List<Change> changes) {
        return changes.stream()
                .filter(c -> c.getSeverity() == Severity.BREAKING)
                .toList();
    }

    public List<Change> getChangesBySeverity(List<Change> changes, Severity severity) {
        return changes.stream()
                .filter(c -> c.getSeverity() == severity)
                .toList();
    }

    public Map<Severity, Long> countBySeverity(List<Change> changes) {
        Map<Severity, Long> counts = new EnumMap<>(Severity.class);
        for (Severity s : Severity.values()) {
            counts.put(s, 0L);
        }
        for (Change c : changes) {
            counts.merge(c.getSeverity(), 1L, Long::sum);
        }
        return counts;
    }

    private Map<String, GraphQLOperation> toMap(List<GraphQLOperation> operations) {
        Map<String, GraphQLOperation> map = new LinkedHashMap<>();
        for (GraphQLOperation op : operations) {
            map.put(op.getName(), op);
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

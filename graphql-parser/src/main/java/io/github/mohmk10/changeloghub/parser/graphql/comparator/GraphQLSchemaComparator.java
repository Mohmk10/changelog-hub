package io.github.mohmk10.changeloghub.parser.graphql.comparator;

import io.github.mohmk10.changeloghub.core.model.*;
import io.github.mohmk10.changeloghub.parser.graphql.model.GraphQLOperation;
import io.github.mohmk10.changeloghub.parser.graphql.model.GraphQLSchema;

import java.util.*;

/**
 * Compares two GraphQL schemas and detects breaking changes.
 */
public class GraphQLSchemaComparator {

    private final GraphQLTypeComparator typeComparator = new GraphQLTypeComparator();
    private final GraphQLFieldComparator fieldComparator = new GraphQLFieldComparator();

    /**
     * Compares two GraphQL schemas and returns all detected changes.
     */
    public List<Change> compare(GraphQLSchema oldSchema, GraphQLSchema newSchema) {
        List<Change> changes = new ArrayList<>();

        // Compare types
        changes.addAll(typeComparator.compareTypes(oldSchema.getTypes(), newSchema.getTypes()));

        // Compare queries
        changes.addAll(compareOperations(oldSchema.getQueries(), newSchema.getQueries(), "Query"));

        // Compare mutations
        changes.addAll(compareOperations(oldSchema.getMutations(), newSchema.getMutations(), "Mutation"));

        // Compare subscriptions
        changes.addAll(compareOperations(oldSchema.getSubscriptions(), newSchema.getSubscriptions(), "Subscription"));

        return changes;
    }

    /**
     * Compares two lists of operations.
     */
    public List<Change> compareOperations(List<GraphQLOperation> oldOps, List<GraphQLOperation> newOps, String opType) {
        List<Change> changes = new ArrayList<>();

        Map<String, GraphQLOperation> oldOpMap = toMap(oldOps);
        Map<String, GraphQLOperation> newOpMap = toMap(newOps);

        // Check for removed operations (BREAKING)
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

        // Check for added operations (INFO)
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

        // Check for modified operations
        for (String opName : oldOpMap.keySet()) {
            if (newOpMap.containsKey(opName)) {
                GraphQLOperation oldOp = oldOpMap.get(opName);
                GraphQLOperation newOp = newOpMap.get(opName);
                changes.addAll(compareOperation(oldOp, newOp, opType + "." + opName));
            }
        }

        return changes;
    }

    /**
     * Compares two individual operations.
     */
    public List<Change> compareOperation(GraphQLOperation oldOp, GraphQLOperation newOp, String path) {
        List<Change> changes = new ArrayList<>();

        // Check return type change (BREAKING if incompatible)
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

        // Check deprecation (WARNING)
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

        // Compare arguments
        changes.addAll(fieldComparator.compareArguments(oldOp.getArguments(), newOp.getArguments(), path));

        return changes;
    }

    /**
     * Checks if there are any breaking changes.
     */
    public boolean hasBreakingChanges(List<Change> changes) {
        return changes.stream().anyMatch(c -> c.getSeverity() == Severity.BREAKING);
    }

    /**
     * Gets only breaking changes.
     */
    public List<Change> getBreakingChanges(List<Change> changes) {
        return changes.stream()
                .filter(c -> c.getSeverity() == Severity.BREAKING)
                .toList();
    }

    /**
     * Gets changes by severity.
     */
    public List<Change> getChangesBySeverity(List<Change> changes, Severity severity) {
        return changes.stream()
                .filter(c -> c.getSeverity() == severity)
                .toList();
    }

    /**
     * Counts changes by severity.
     */
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

package io.github.mohmk10.changeloghub.parser.asyncapi.comparator;

import io.github.mohmk10.changeloghub.core.model.Change;
import io.github.mohmk10.changeloghub.core.model.ChangeCategory;
import io.github.mohmk10.changeloghub.core.model.ChangeType;
import io.github.mohmk10.changeloghub.core.model.Severity;
import io.github.mohmk10.changeloghub.parser.asyncapi.model.AsyncSchema;

import java.util.*;

/**
 * Compares AsyncAPI schemas to detect changes.
 *
 * Breaking change rules:
 * - BREAKING: Required field removed, type changed incompatibly
 * - DANGEROUS: Optional field removed, schema reference changed
 * - WARNING: Field deprecated
 * - INFO: New fields, new enum values
 */
public class SchemaComparator {

    /**
     * Compare two schemas and return detected changes.
     */
    public List<Change> compare(String schemaName, AsyncSchema oldSchema, AsyncSchema newSchema) {
        List<Change> changes = new ArrayList<>();

        if (oldSchema == null && newSchema == null) {
            return changes;
        }

        String context = "schema:" + schemaName;

        // Schema added
        if (oldSchema == null) {
            changes.add(createChange(context, ChangeCategory.SCHEMA, ChangeType.ADDED, Severity.INFO,
                    "Schema '" + schemaName + "' added"));
            return changes;
        }

        // Schema removed
        if (newSchema == null) {
            changes.add(createChange(context, ChangeCategory.SCHEMA, ChangeType.REMOVED, Severity.BREAKING,
                    "Schema '" + schemaName + "' removed"));
            return changes;
        }

        // Type changed
        if (!Objects.equals(oldSchema.getType(), newSchema.getType())) {
            if (isCompatibleTypeChange(oldSchema.getType(), newSchema.getType())) {
                changes.add(createChange(context, ChangeCategory.SCHEMA, ChangeType.MODIFIED, Severity.WARNING,
                        "Schema type changed from '" + oldSchema.getType() + "' to '" + newSchema.getType() + "'"));
            } else {
                changes.add(createChange(context, ChangeCategory.SCHEMA, ChangeType.MODIFIED, Severity.BREAKING,
                        "Schema type changed incompatibly from '" + oldSchema.getType() + "' to '" + newSchema.getType() + "'"));
            }
        }

        // Format changed
        if (!Objects.equals(oldSchema.getFormat(), newSchema.getFormat())) {
            changes.add(createChange(context, ChangeCategory.SCHEMA, ChangeType.MODIFIED, Severity.WARNING,
                    "Schema format changed from '" + oldSchema.getFormat() + "' to '" + newSchema.getFormat() + "'"));
        }

        // Reference changed
        if (!Objects.equals(oldSchema.getRef(), newSchema.getRef())) {
            changes.add(createChange(context, ChangeCategory.SCHEMA, ChangeType.MODIFIED, Severity.DANGEROUS,
                    "Schema reference changed from '" + oldSchema.getRef() + "' to '" + newSchema.getRef() + "'"));
        }

        // Deprecated status changed
        if (!oldSchema.isDeprecated() && newSchema.isDeprecated()) {
            changes.add(createChange(context, ChangeCategory.SCHEMA, ChangeType.DEPRECATED, Severity.WARNING,
                    "Schema '" + schemaName + "' deprecated"));
        }

        // Compare properties
        changes.addAll(compareProperties(context, oldSchema, newSchema));

        // Compare enum values
        changes.addAll(compareEnumValues(context, oldSchema, newSchema));

        // Compare items (for arrays)
        if (oldSchema.getItems() != null || newSchema.getItems() != null) {
            changes.addAll(compare(schemaName + ".items", oldSchema.getItems(), newSchema.getItems()));
        }

        return changes;
    }

    /**
     * Compare schema properties.
     */
    public List<Change> compareProperties(String context,
                                           AsyncSchema oldSchema, AsyncSchema newSchema) {
        List<Change> changes = new ArrayList<>();

        Map<String, AsyncSchema> oldProps = oldSchema.getProperties() != null ?
                oldSchema.getProperties() : Collections.emptyMap();
        Map<String, AsyncSchema> newProps = newSchema.getProperties() != null ?
                newSchema.getProperties() : Collections.emptyMap();

        Set<String> oldRequired = oldSchema.getRequiredFields() != null ?
                new HashSet<>(oldSchema.getRequiredFields()) : Collections.emptySet();
        Set<String> newRequired = newSchema.getRequiredFields() != null ?
                new HashSet<>(newSchema.getRequiredFields()) : Collections.emptySet();

        // Find removed properties
        for (String propName : oldProps.keySet()) {
            if (!newProps.containsKey(propName)) {
                if (oldRequired.contains(propName)) {
                    changes.add(createChange(context, ChangeCategory.FIELD, ChangeType.REMOVED, Severity.BREAKING,
                            "Required property '" + propName + "' removed"));
                } else {
                    changes.add(createChange(context, ChangeCategory.FIELD, ChangeType.REMOVED, Severity.DANGEROUS,
                            "Optional property '" + propName + "' removed"));
                }
            }
        }

        // Find added properties
        for (String propName : newProps.keySet()) {
            if (!oldProps.containsKey(propName)) {
                if (newRequired.contains(propName)) {
                    changes.add(createChange(context, ChangeCategory.FIELD, ChangeType.ADDED, Severity.BREAKING,
                            "New required property '" + propName + "' added"));
                } else {
                    changes.add(createChange(context, ChangeCategory.FIELD, ChangeType.ADDED, Severity.INFO,
                            "New optional property '" + propName + "' added"));
                }
            }
        }

        // Compare existing properties
        for (String propName : oldProps.keySet()) {
            if (newProps.containsKey(propName)) {
                // Check required status changed
                boolean wasRequired = oldRequired.contains(propName);
                boolean isRequired = newRequired.contains(propName);

                if (!wasRequired && isRequired) {
                    changes.add(createChange(context, ChangeCategory.FIELD, ChangeType.MODIFIED, Severity.BREAKING,
                            "Property '" + propName + "' changed from optional to required"));
                } else if (wasRequired && !isRequired) {
                    changes.add(createChange(context, ChangeCategory.FIELD, ChangeType.MODIFIED, Severity.INFO,
                            "Property '" + propName + "' changed from required to optional"));
                }

                // Recursively compare property schemas
                changes.addAll(compare(context + "." + propName,
                        oldProps.get(propName), newProps.get(propName)));
            }
        }

        return changes;
    }

    /**
     * Compare enum values.
     */
    public List<Change> compareEnumValues(String context,
                                           AsyncSchema oldSchema, AsyncSchema newSchema) {
        List<Change> changes = new ArrayList<>();

        List<String> oldEnums = oldSchema.getEnumValues() != null ?
                oldSchema.getEnumValues() : Collections.emptyList();
        List<String> newEnums = newSchema.getEnumValues() != null ?
                newSchema.getEnumValues() : Collections.emptyList();

        Set<String> oldSet = new HashSet<>(oldEnums);
        Set<String> newSet = new HashSet<>(newEnums);

        // Find removed enum values
        for (String value : oldSet) {
            if (!newSet.contains(value)) {
                changes.add(createChange(context, ChangeCategory.ENUM_VALUE, ChangeType.REMOVED, Severity.BREAKING,
                        "Enum value '" + value + "' removed"));
            }
        }

        // Find added enum values
        for (String value : newSet) {
            if (!oldSet.contains(value)) {
                changes.add(createChange(context, ChangeCategory.ENUM_VALUE, ChangeType.ADDED, Severity.INFO,
                        "Enum value '" + value + "' added"));
            }
        }

        return changes;
    }

    /**
     * Check if type change is compatible.
     */
    private boolean isCompatibleTypeChange(String oldType, String newType) {
        if (oldType == null || newType == null) {
            return false;
        }

        // Integer to number is compatible (widening)
        if ("integer".equals(oldType) && "number".equals(newType)) {
            return true;
        }

        return false;
    }

    /**
     * Create a change object.
     */
    private Change createChange(String path, ChangeCategory category, ChangeType type,
                                  Severity severity, String description) {
        return Change.builder()
                .path(path)
                .category(category)
                .type(type)
                .severity(severity)
                .description(description)
                .build();
    }

    /**
     * Compare multiple schemas.
     */
    public List<Change> compareAll(Map<String, AsyncSchema> oldSchemas,
                                    Map<String, AsyncSchema> newSchemas) {
        List<Change> changes = new ArrayList<>();

        Map<String, AsyncSchema> old = oldSchemas != null ? oldSchemas : Collections.emptyMap();
        Map<String, AsyncSchema> neu = newSchemas != null ? newSchemas : Collections.emptyMap();

        Set<String> allNames = new LinkedHashSet<>();
        allNames.addAll(old.keySet());
        allNames.addAll(neu.keySet());

        for (String name : allNames) {
            changes.addAll(compare(name, old.get(name), neu.get(name)));
        }

        return changes;
    }

    /**
     * Create schema signature for comparison.
     */
    public String createSignature(AsyncSchema schema) {
        if (schema == null) {
            return "null";
        }

        StringBuilder sb = new StringBuilder();

        if (schema.getRef() != null) {
            sb.append("$ref:").append(schema.getRef());
        } else {
            sb.append(schema.getType() != null ? schema.getType() : "object");
            if (schema.getFormat() != null) {
                sb.append(":").append(schema.getFormat());
            }
        }

        return sb.toString();
    }
}

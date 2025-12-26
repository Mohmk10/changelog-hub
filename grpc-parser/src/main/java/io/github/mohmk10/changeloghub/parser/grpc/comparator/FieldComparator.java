package io.github.mohmk10.changeloghub.parser.grpc.comparator;

import io.github.mohmk10.changeloghub.core.model.BreakingChange;
import io.github.mohmk10.changeloghub.core.model.ChangeCategory;
import io.github.mohmk10.changeloghub.core.model.ChangeType;
import io.github.mohmk10.changeloghub.core.model.Severity;
import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoField;
import io.github.mohmk10.changeloghub.parser.grpc.util.ProtoFieldRule;
import io.github.mohmk10.changeloghub.parser.grpc.util.ProtoFieldType;

import java.time.LocalDateTime;
import java.util.*;

public class FieldComparator {

    public List<BreakingChange> compareFields(List<ProtoField> oldFields, List<ProtoField> newFields,
                                               String messagePath) {
        List<BreakingChange> changes = new ArrayList<>();

        Map<String, ProtoField> oldByName = mapByName(oldFields);
        Map<String, ProtoField> newByName = mapByName(newFields);
        Map<Integer, ProtoField> oldByNumber = mapByNumber(oldFields);
        Map<Integer, ProtoField> newByNumber = mapByNumber(newFields);

        for (ProtoField oldField : oldFields) {
            if (!newByName.containsKey(oldField.getName())) {
                changes.add(createFieldRemovedChange(oldField, messagePath));
            }
        }

        for (ProtoField newField : newFields) {
            if (!oldByName.containsKey(newField.getName())) {
                
                if (oldByNumber.containsKey(newField.getNumber())) {
                    ProtoField oldFieldWithSameNumber = oldByNumber.get(newField.getNumber());
                    changes.add(createFieldNumberReusedChange(oldFieldWithSameNumber, newField, messagePath));
                } else {
                    changes.add(createFieldAddedChange(newField, messagePath));
                }
            }
        }

        for (ProtoField oldField : oldFields) {
            ProtoField newField = newByName.get(oldField.getName());
            if (newField != null) {
                changes.addAll(compareField(oldField, newField, messagePath));
            }
        }

        return changes;
    }

    public List<BreakingChange> compareField(ProtoField oldField, ProtoField newField, String messagePath) {
        List<BreakingChange> changes = new ArrayList<>();
        String fieldPath = messagePath + "." + oldField.getName();

        if (oldField.getNumber() != newField.getNumber()) {
            changes.add(createFieldNumberChangedChange(oldField, newField, fieldPath));
        }

        if (!oldField.getTypeName().equals(newField.getTypeName())) {
            boolean wireCompatible = ProtoFieldType.isWireCompatible(oldField.getType(), newField.getType());
            changes.add(createTypeChangedChange(oldField, newField, fieldPath, wireCompatible));
        }

        if (oldField.getRule() != newField.getRule()) {
            changes.add(createRuleChangedChange(oldField, newField, fieldPath));
        }

        if (!oldField.isDeprecated() && newField.isDeprecated()) {
            changes.add(createDeprecationChange(newField, fieldPath));
        }

        if (!Objects.equals(oldField.getDefaultValue().orElse(null),
                            newField.getDefaultValue().orElse(null))) {
            changes.add(createDefaultValueChangedChange(oldField, newField, fieldPath));
        }

        return changes;
    }

    private BreakingChange createFieldRemovedChange(ProtoField field, String messagePath) {
        String path = messagePath + "." + field.getName();

        Severity severity = field.isRequired() ? Severity.BREAKING : Severity.DANGEROUS;

        return BreakingChange.breakingChangeBuilder()
                .id(UUID.randomUUID().toString())
                .type(ChangeType.REMOVED)
                .category(ChangeCategory.FIELD)
                .severity(severity)
                .path(path)
                .description("Field '" + field.getName() + "' (number " + field.getNumber() + ") removed from message")
                .oldValue(field.getFullTypeSignature())
                .newValue(null)
                .detectedAt(LocalDateTime.now())
                .migrationSuggestion("Remove usage of field '" + field.getName() +
                        "'. If field contains important data, migrate to a new field before removing.")
                .impactScore(field.isRequired() ? 90 : 60)
                .build();
    }

    private BreakingChange createFieldAddedChange(ProtoField field, String messagePath) {
        String path = messagePath + "." + field.getName();

        Severity severity = field.isRequired() ? Severity.BREAKING : Severity.INFO;

        return BreakingChange.breakingChangeBuilder()
                .id(UUID.randomUUID().toString())
                .type(ChangeType.ADDED)
                .category(ChangeCategory.FIELD)
                .severity(severity)
                .path(path)
                .description("Field '" + field.getName() + "' (number " + field.getNumber() + ") added to message")
                .oldValue(null)
                .newValue(field.getFullTypeSignature())
                .detectedAt(LocalDateTime.now())
                .migrationSuggestion(field.isRequired()
                        ? "Ensure all clients populate the new required field '" + field.getName() + "'"
                        : "New optional field added. No migration required.")
                .impactScore(field.isRequired() ? 80 : 10)
                .build();
    }

    private BreakingChange createFieldNumberChangedChange(ProtoField oldField, ProtoField newField, String path) {
        return BreakingChange.breakingChangeBuilder()
                .id(UUID.randomUUID().toString())
                .type(ChangeType.MODIFIED)
                .category(ChangeCategory.FIELD_NUMBER)
                .severity(Severity.BREAKING)
                .path(path)
                .description("Field number changed from " + oldField.getNumber() + " to " + newField.getNumber())
                .oldValue(oldField.getNumber())
                .newValue(newField.getNumber())
                .detectedAt(LocalDateTime.now())
                .migrationSuggestion("CRITICAL: Field numbers must never change. " +
                        "This breaks binary compatibility. Revert field number to " + oldField.getNumber() +
                        " or create a new field with a new number.")
                .impactScore(100)
                .build();
    }

    private BreakingChange createFieldNumberReusedChange(ProtoField oldField, ProtoField newField, String messagePath) {
        String path = messagePath + ".field_number_" + newField.getNumber();

        return BreakingChange.breakingChangeBuilder()
                .id(UUID.randomUUID().toString())
                .type(ChangeType.MODIFIED)
                .category(ChangeCategory.FIELD_NUMBER)
                .severity(Severity.BREAKING)
                .path(path)
                .description("Field number " + newField.getNumber() + " reused: was '" + oldField.getName() +
                        "' (" + oldField.getTypeName() + "), now '" + newField.getName() +
                        "' (" + newField.getTypeName() + ")")
                .oldValue(oldField.getName() + ":" + oldField.getTypeName())
                .newValue(newField.getName() + ":" + newField.getTypeName())
                .detectedAt(LocalDateTime.now())
                .migrationSuggestion("CRITICAL: Field number " + newField.getNumber() +
                        " was reused. This breaks binary compatibility. " +
                        "Use a new field number for '" + newField.getName() + "'.")
                .impactScore(100)
                .build();
    }

    private BreakingChange createTypeChangedChange(ProtoField oldField, ProtoField newField,
                                                    String path, boolean wireCompatible) {
        Severity severity = wireCompatible ? Severity.DANGEROUS : Severity.BREAKING;

        String migrationSuggestion;
        if (wireCompatible) {
            migrationSuggestion = "Type changed from " + oldField.getTypeName() + " to " + newField.getTypeName() +
                    ". Types are wire-compatible but may cause runtime issues. Verify client compatibility.";
        } else {
            migrationSuggestion = "BREAKING: Type changed from " + oldField.getTypeName() + " to " + newField.getTypeName() +
                    ". Types are NOT wire-compatible. Create a new field with the new type.";
        }

        return BreakingChange.breakingChangeBuilder()
                .id(UUID.randomUUID().toString())
                .type(ChangeType.MODIFIED)
                .category(ChangeCategory.FIELD)
                .severity(severity)
                .path(path)
                .description("Field type changed from " + oldField.getTypeName() + " to " + newField.getTypeName())
                .oldValue(oldField.getTypeName())
                .newValue(newField.getTypeName())
                .detectedAt(LocalDateTime.now())
                .migrationSuggestion(migrationSuggestion)
                .impactScore(wireCompatible ? 60 : 95)
                .build();
    }

    private BreakingChange createRuleChangedChange(ProtoField oldField, ProtoField newField, String path) {
        ProtoFieldRule oldRule = oldField.getRule();
        ProtoFieldRule newRule = newField.getRule();

        Severity severity;
        String migrationSuggestion;

        if (oldRule == ProtoFieldRule.OPTIONAL && newRule == ProtoFieldRule.REQUIRED) {
            severity = Severity.BREAKING;
            migrationSuggestion = "Field changed from optional to required. " +
                    "All existing messages without this field will become invalid.";
        } else if (oldRule == ProtoFieldRule.REQUIRED && newRule == ProtoFieldRule.OPTIONAL) {
            severity = Severity.INFO;
            migrationSuggestion = "Field changed from required to optional. This is safe.";
        } else if (oldRule == ProtoFieldRule.REPEATED && newRule != ProtoFieldRule.REPEATED) {
            severity = Severity.BREAKING;
            migrationSuggestion = "Field changed from repeated to singular. " +
                    "Messages with multiple values will lose data.";
        } else if (oldRule != ProtoFieldRule.REPEATED && newRule == ProtoFieldRule.REPEATED) {
            severity = Severity.DANGEROUS;
            migrationSuggestion = "Field changed to repeated. Existing messages may need conversion.";
        } else {
            severity = Severity.DANGEROUS;
            migrationSuggestion = "Field rule changed from " + oldRule + " to " + newRule + ".";
        }

        return BreakingChange.breakingChangeBuilder()
                .id(UUID.randomUUID().toString())
                .type(ChangeType.MODIFIED)
                .category(ChangeCategory.FIELD)
                .severity(severity)
                .path(path)
                .description("Field rule changed from " + oldRule.getKeyword() + " to " + newRule.getKeyword())
                .oldValue(oldRule.getKeyword())
                .newValue(newRule.getKeyword())
                .detectedAt(LocalDateTime.now())
                .migrationSuggestion(migrationSuggestion)
                .impactScore(severity == Severity.BREAKING ? 85 : severity == Severity.DANGEROUS ? 50 : 10)
                .build();
    }

    private BreakingChange createDeprecationChange(ProtoField field, String path) {
        return BreakingChange.breakingChangeBuilder()
                .id(UUID.randomUUID().toString())
                .type(ChangeType.DEPRECATED)
                .category(ChangeCategory.FIELD)
                .severity(Severity.WARNING)
                .path(path)
                .description("Field '" + field.getName() + "' marked as deprecated")
                .oldValue(false)
                .newValue(true)
                .detectedAt(LocalDateTime.now())
                .migrationSuggestion("Field '" + field.getName() + "' is deprecated. " +
                        "Plan migration to alternative fields before it's removed.")
                .impactScore(30)
                .build();
    }

    private BreakingChange createDefaultValueChangedChange(ProtoField oldField, ProtoField newField, String path) {
        return BreakingChange.breakingChangeBuilder()
                .id(UUID.randomUUID().toString())
                .type(ChangeType.MODIFIED)
                .category(ChangeCategory.FIELD)
                .severity(Severity.DANGEROUS)
                .path(path)
                .description("Default value changed for field '" + oldField.getName() + "'")
                .oldValue(oldField.getDefaultValue().orElse("(none)"))
                .newValue(newField.getDefaultValue().orElse("(none)"))
                .detectedAt(LocalDateTime.now())
                .migrationSuggestion("Default value changed. Messages that relied on the old default " +
                        "may behave differently. Review all usages.")
                .impactScore(40)
                .build();
    }

    private Map<String, ProtoField> mapByName(List<ProtoField> fields) {
        Map<String, ProtoField> map = new LinkedHashMap<>();
        for (ProtoField field : fields) {
            map.put(field.getName(), field);
        }
        return map;
    }

    private Map<Integer, ProtoField> mapByNumber(List<ProtoField> fields) {
        Map<Integer, ProtoField> map = new LinkedHashMap<>();
        for (ProtoField field : fields) {
            map.put(field.getNumber(), field);
        }
        return map;
    }
}

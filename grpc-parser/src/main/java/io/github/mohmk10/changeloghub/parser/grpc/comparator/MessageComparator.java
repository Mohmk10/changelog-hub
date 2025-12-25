package io.github.mohmk10.changeloghub.parser.grpc.comparator;

import io.github.mohmk10.changeloghub.core.model.BreakingChange;
import io.github.mohmk10.changeloghub.core.model.ChangeCategory;
import io.github.mohmk10.changeloghub.core.model.ChangeType;
import io.github.mohmk10.changeloghub.core.model.Severity;
import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoEnum;
import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoEnumValue;
import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoMessage;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Comparator for detecting breaking changes in Protocol Buffers messages.
 */
public class MessageComparator {

    private final FieldComparator fieldComparator;

    public MessageComparator() {
        this.fieldComparator = new FieldComparator();
    }

    public MessageComparator(FieldComparator fieldComparator) {
        this.fieldComparator = fieldComparator;
    }

    /**
     * Compare two message lists and detect changes.
     */
    public List<BreakingChange> compareMessages(List<ProtoMessage> oldMessages, List<ProtoMessage> newMessages,
                                                 String packagePath) {
        List<BreakingChange> changes = new ArrayList<>();

        Map<String, ProtoMessage> oldByName = mapByName(oldMessages);
        Map<String, ProtoMessage> newByName = mapByName(newMessages);

        // Detect removed messages
        for (ProtoMessage oldMessage : oldMessages) {
            if (!newByName.containsKey(oldMessage.getName())) {
                changes.add(createMessageRemovedChange(oldMessage, packagePath));
            }
        }

        // Detect added messages
        for (ProtoMessage newMessage : newMessages) {
            if (!oldByName.containsKey(newMessage.getName())) {
                changes.add(createMessageAddedChange(newMessage, packagePath));
            }
        }

        // Detect modified messages
        for (ProtoMessage oldMessage : oldMessages) {
            ProtoMessage newMessage = newByName.get(oldMessage.getName());
            if (newMessage != null) {
                String messagePath = packagePath.isEmpty()
                        ? oldMessage.getName()
                        : packagePath + "." + oldMessage.getName();
                changes.addAll(compareMessage(oldMessage, newMessage, messagePath));
            }
        }

        return changes;
    }

    /**
     * Compare two individual messages.
     */
    public List<BreakingChange> compareMessage(ProtoMessage oldMessage, ProtoMessage newMessage, String messagePath) {
        List<BreakingChange> changes = new ArrayList<>();

        // Compare fields
        changes.addAll(fieldComparator.compareFields(
                oldMessage.getFields(),
                newMessage.getFields(),
                messagePath
        ));

        // Compare reserved numbers (new reservations might indicate removed fields)
        Set<Integer> newReserved = new HashSet<>(newMessage.getReservedNumbers());
        newReserved.removeAll(oldMessage.getReservedNumbers());
        for (Integer reserved : newReserved) {
            changes.add(createReservedNumberAddedChange(reserved, messagePath));
        }

        // Compare nested messages
        changes.addAll(compareMessages(
                oldMessage.getNestedMessages(),
                newMessage.getNestedMessages(),
                messagePath
        ));

        // Compare nested enums
        changes.addAll(compareEnums(
                oldMessage.getNestedEnums(),
                newMessage.getNestedEnums(),
                messagePath
        ));

        // Check deprecation
        if (!oldMessage.isDeprecated() && newMessage.isDeprecated()) {
            changes.add(createMessageDeprecationChange(newMessage, messagePath));
        }

        return changes;
    }

    /**
     * Compare two enum lists.
     */
    public List<BreakingChange> compareEnums(List<ProtoEnum> oldEnums, List<ProtoEnum> newEnums,
                                              String parentPath) {
        List<BreakingChange> changes = new ArrayList<>();

        Map<String, ProtoEnum> oldByName = mapEnumsByName(oldEnums);
        Map<String, ProtoEnum> newByName = mapEnumsByName(newEnums);

        // Detect removed enums
        for (ProtoEnum oldEnum : oldEnums) {
            if (!newByName.containsKey(oldEnum.getName())) {
                changes.add(createEnumRemovedChange(oldEnum, parentPath));
            }
        }

        // Detect added enums
        for (ProtoEnum newEnum : newEnums) {
            if (!oldByName.containsKey(newEnum.getName())) {
                changes.add(createEnumAddedChange(newEnum, parentPath));
            }
        }

        // Detect modified enums
        for (ProtoEnum oldEnum : oldEnums) {
            ProtoEnum newEnum = newByName.get(oldEnum.getName());
            if (newEnum != null) {
                String enumPath = parentPath + "." + oldEnum.getName();
                changes.addAll(compareEnum(oldEnum, newEnum, enumPath));
            }
        }

        return changes;
    }

    /**
     * Compare two individual enums.
     */
    public List<BreakingChange> compareEnum(ProtoEnum oldEnum, ProtoEnum newEnum, String enumPath) {
        List<BreakingChange> changes = new ArrayList<>();

        Map<String, ProtoEnumValue> oldValues = mapEnumValuesByName(oldEnum.getValues());
        Map<String, ProtoEnumValue> newValues = mapEnumValuesByName(newEnum.getValues());
        Map<Integer, ProtoEnumValue> oldByNumber = mapEnumValuesByNumber(oldEnum.getValues());

        // Detect removed enum values
        for (ProtoEnumValue oldValue : oldEnum.getValues()) {
            if (!newValues.containsKey(oldValue.getName())) {
                changes.add(createEnumValueRemovedChange(oldValue, enumPath));
            }
        }

        // Detect added enum values
        for (ProtoEnumValue newValue : newEnum.getValues()) {
            if (!oldValues.containsKey(newValue.getName())) {
                // Check if number was reused
                if (oldByNumber.containsKey(newValue.getNumber())) {
                    ProtoEnumValue oldValueWithSameNumber = oldByNumber.get(newValue.getNumber());
                    changes.add(createEnumValueNumberReusedChange(oldValueWithSameNumber, newValue, enumPath));
                } else {
                    changes.add(createEnumValueAddedChange(newValue, enumPath));
                }
            }
        }

        // Check for number changes
        for (ProtoEnumValue oldValue : oldEnum.getValues()) {
            ProtoEnumValue newValue = newValues.get(oldValue.getName());
            if (newValue != null && oldValue.getNumber() != newValue.getNumber()) {
                changes.add(createEnumValueNumberChangedChange(oldValue, newValue, enumPath));
            }
        }

        // Check deprecation
        if (!oldEnum.isDeprecated() && newEnum.isDeprecated()) {
            changes.add(createEnumDeprecationChange(newEnum, enumPath));
        }

        return changes;
    }

    // Message change creation methods

    private BreakingChange createMessageRemovedChange(ProtoMessage message, String packagePath) {
        String path = packagePath.isEmpty() ? message.getName() : packagePath + "." + message.getName();

        return BreakingChange.breakingChangeBuilder()
                .id(UUID.randomUUID().toString())
                .type(ChangeType.REMOVED)
                .category(ChangeCategory.MESSAGE)
                .severity(Severity.BREAKING)
                .path(path)
                .description("Message '" + message.getName() + "' removed")
                .oldValue(message.getFieldCount() + " fields")
                .newValue(null)
                .detectedAt(LocalDateTime.now())
                .migrationSuggestion("Message '" + message.getName() + "' was removed. " +
                        "All RPC methods using this message will fail. Migrate to alternative message types.")
                .impactScore(95)
                .build();
    }

    private BreakingChange createMessageAddedChange(ProtoMessage message, String packagePath) {
        String path = packagePath.isEmpty() ? message.getName() : packagePath + "." + message.getName();

        return BreakingChange.breakingChangeBuilder()
                .id(UUID.randomUUID().toString())
                .type(ChangeType.ADDED)
                .category(ChangeCategory.MESSAGE)
                .severity(Severity.INFO)
                .path(path)
                .description("Message '" + message.getName() + "' added with " + message.getFieldCount() + " fields")
                .oldValue(null)
                .newValue(message.getFieldCount() + " fields")
                .detectedAt(LocalDateTime.now())
                .migrationSuggestion("New message type available. No migration required.")
                .impactScore(5)
                .build();
    }

    private BreakingChange createReservedNumberAddedChange(Integer number, String messagePath) {
        return BreakingChange.breakingChangeBuilder()
                .id(UUID.randomUUID().toString())
                .type(ChangeType.MODIFIED)
                .category(ChangeCategory.FIELD_NUMBER)
                .severity(Severity.INFO)
                .path(messagePath + ".reserved")
                .description("Field number " + number + " added to reserved list")
                .oldValue(null)
                .newValue(number)
                .detectedAt(LocalDateTime.now())
                .migrationSuggestion("Field number " + number + " is now reserved. " +
                        "This prevents future reuse of a removed field number.")
                .impactScore(5)
                .build();
    }

    private BreakingChange createMessageDeprecationChange(ProtoMessage message, String path) {
        return BreakingChange.breakingChangeBuilder()
                .id(UUID.randomUUID().toString())
                .type(ChangeType.DEPRECATED)
                .category(ChangeCategory.MESSAGE)
                .severity(Severity.WARNING)
                .path(path)
                .description("Message '" + message.getName() + "' marked as deprecated")
                .oldValue(false)
                .newValue(true)
                .detectedAt(LocalDateTime.now())
                .migrationSuggestion("Message '" + message.getName() + "' is deprecated. " +
                        "Plan migration to alternative message types.")
                .impactScore(30)
                .build();
    }

    // Enum change creation methods

    private BreakingChange createEnumRemovedChange(ProtoEnum protoEnum, String parentPath) {
        String path = parentPath + "." + protoEnum.getName();

        return BreakingChange.breakingChangeBuilder()
                .id(UUID.randomUUID().toString())
                .type(ChangeType.REMOVED)
                .category(ChangeCategory.TYPE)
                .severity(Severity.BREAKING)
                .path(path)
                .description("Enum '" + protoEnum.getName() + "' removed")
                .oldValue(protoEnum.getValues().size() + " values")
                .newValue(null)
                .detectedAt(LocalDateTime.now())
                .migrationSuggestion("Enum '" + protoEnum.getName() + "' was removed. " +
                        "All fields using this enum will fail. Migrate to alternative types.")
                .impactScore(90)
                .build();
    }

    private BreakingChange createEnumAddedChange(ProtoEnum protoEnum, String parentPath) {
        String path = parentPath + "." + protoEnum.getName();

        return BreakingChange.breakingChangeBuilder()
                .id(UUID.randomUUID().toString())
                .type(ChangeType.ADDED)
                .category(ChangeCategory.TYPE)
                .severity(Severity.INFO)
                .path(path)
                .description("Enum '" + protoEnum.getName() + "' added with " + protoEnum.getValues().size() + " values")
                .oldValue(null)
                .newValue(protoEnum.getValues().size() + " values")
                .detectedAt(LocalDateTime.now())
                .migrationSuggestion("New enum type available. No migration required.")
                .impactScore(5)
                .build();
    }

    private BreakingChange createEnumValueRemovedChange(ProtoEnumValue value, String enumPath) {
        String path = enumPath + "." + value.getName();

        return BreakingChange.breakingChangeBuilder()
                .id(UUID.randomUUID().toString())
                .type(ChangeType.REMOVED)
                .category(ChangeCategory.ENUM_VALUE)
                .severity(Severity.BREAKING)
                .path(path)
                .description("Enum value '" + value.getName() + "' (= " + value.getNumber() + ") removed")
                .oldValue(value.getNumber())
                .newValue(null)
                .detectedAt(LocalDateTime.now())
                .migrationSuggestion("Enum value '" + value.getName() + "' was removed. " +
                        "Messages with this value will fail validation. Map to a valid value.")
                .impactScore(85)
                .build();
    }

    private BreakingChange createEnumValueAddedChange(ProtoEnumValue value, String enumPath) {
        String path = enumPath + "." + value.getName();

        return BreakingChange.breakingChangeBuilder()
                .id(UUID.randomUUID().toString())
                .type(ChangeType.ADDED)
                .category(ChangeCategory.ENUM_VALUE)
                .severity(Severity.INFO)
                .path(path)
                .description("Enum value '" + value.getName() + "' (= " + value.getNumber() + ") added")
                .oldValue(null)
                .newValue(value.getNumber())
                .detectedAt(LocalDateTime.now())
                .migrationSuggestion("New enum value available. Old clients will treat it as unknown.")
                .impactScore(10)
                .build();
    }

    private BreakingChange createEnumValueNumberChangedChange(ProtoEnumValue oldValue, ProtoEnumValue newValue,
                                                               String enumPath) {
        String path = enumPath + "." + oldValue.getName();

        return BreakingChange.breakingChangeBuilder()
                .id(UUID.randomUUID().toString())
                .type(ChangeType.MODIFIED)
                .category(ChangeCategory.ENUM_VALUE)
                .severity(Severity.BREAKING)
                .path(path)
                .description("Enum value '" + oldValue.getName() + "' number changed from " +
                        oldValue.getNumber() + " to " + newValue.getNumber())
                .oldValue(oldValue.getNumber())
                .newValue(newValue.getNumber())
                .detectedAt(LocalDateTime.now())
                .migrationSuggestion("CRITICAL: Enum value numbers must never change. " +
                        "This breaks binary compatibility.")
                .impactScore(100)
                .build();
    }

    private BreakingChange createEnumValueNumberReusedChange(ProtoEnumValue oldValue, ProtoEnumValue newValue,
                                                              String enumPath) {
        String path = enumPath + ".value_" + newValue.getNumber();

        return BreakingChange.breakingChangeBuilder()
                .id(UUID.randomUUID().toString())
                .type(ChangeType.MODIFIED)
                .category(ChangeCategory.ENUM_VALUE)
                .severity(Severity.BREAKING)
                .path(path)
                .description("Enum value number " + newValue.getNumber() + " reused: was '" +
                        oldValue.getName() + "', now '" + newValue.getName() + "'")
                .oldValue(oldValue.getName())
                .newValue(newValue.getName())
                .detectedAt(LocalDateTime.now())
                .migrationSuggestion("CRITICAL: Enum value number reused. " +
                        "Existing messages will be decoded with wrong value name.")
                .impactScore(100)
                .build();
    }

    private BreakingChange createEnumDeprecationChange(ProtoEnum protoEnum, String path) {
        return BreakingChange.breakingChangeBuilder()
                .id(UUID.randomUUID().toString())
                .type(ChangeType.DEPRECATED)
                .category(ChangeCategory.TYPE)
                .severity(Severity.WARNING)
                .path(path)
                .description("Enum '" + protoEnum.getName() + "' marked as deprecated")
                .oldValue(false)
                .newValue(true)
                .detectedAt(LocalDateTime.now())
                .migrationSuggestion("Enum '" + protoEnum.getName() + "' is deprecated. " +
                        "Plan migration to alternative types.")
                .impactScore(25)
                .build();
    }

    // Helper methods

    private Map<String, ProtoMessage> mapByName(List<ProtoMessage> messages) {
        Map<String, ProtoMessage> map = new LinkedHashMap<>();
        for (ProtoMessage message : messages) {
            map.put(message.getName(), message);
        }
        return map;
    }

    private Map<String, ProtoEnum> mapEnumsByName(List<ProtoEnum> enums) {
        Map<String, ProtoEnum> map = new LinkedHashMap<>();
        for (ProtoEnum protoEnum : enums) {
            map.put(protoEnum.getName(), protoEnum);
        }
        return map;
    }

    private Map<String, ProtoEnumValue> mapEnumValuesByName(List<ProtoEnumValue> values) {
        Map<String, ProtoEnumValue> map = new LinkedHashMap<>();
        for (ProtoEnumValue value : values) {
            map.put(value.getName(), value);
        }
        return map;
    }

    private Map<Integer, ProtoEnumValue> mapEnumValuesByNumber(List<ProtoEnumValue> values) {
        Map<Integer, ProtoEnumValue> map = new LinkedHashMap<>();
        for (ProtoEnumValue value : values) {
            map.put(value.getNumber(), value);
        }
        return map;
    }
}

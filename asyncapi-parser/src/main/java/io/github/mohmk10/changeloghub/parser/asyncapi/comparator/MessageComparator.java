package io.github.mohmk10.changeloghub.parser.asyncapi.comparator;

import io.github.mohmk10.changeloghub.core.model.Change;
import io.github.mohmk10.changeloghub.core.model.ChangeCategory;
import io.github.mohmk10.changeloghub.core.model.ChangeType;
import io.github.mohmk10.changeloghub.core.model.Severity;
import io.github.mohmk10.changeloghub.parser.asyncapi.model.AsyncMessage;

import java.util.*;

/**
 * Compares AsyncAPI messages to detect changes.
 *
 * Breaking change rules:
 * - BREAKING: Message removed, required field added to payload, content type changed
 * - DANGEROUS: Optional field removed from payload, schema reference changed
 * - WARNING: Message deprecated, description changed
 * - INFO: New message, new optional field
 */
public class MessageComparator {

    private final SchemaComparator schemaComparator;

    public MessageComparator() {
        this.schemaComparator = new SchemaComparator();
    }

    public MessageComparator(SchemaComparator schemaComparator) {
        this.schemaComparator = schemaComparator;
    }

    /**
     * Compare two messages and return detected changes.
     */
    public List<Change> compare(String messageName, AsyncMessage oldMessage, AsyncMessage newMessage) {
        List<Change> changes = new ArrayList<>();

        if (oldMessage == null && newMessage == null) {
            return changes;
        }

        String context = "message:" + messageName;

        // Message added
        if (oldMessage == null) {
            changes.add(createChange(context, ChangeCategory.MESSAGE_PAYLOAD, ChangeType.ADDED, Severity.INFO,
                    "Message '" + messageName + "' added"));
            return changes;
        }

        // Message removed
        if (newMessage == null) {
            changes.add(createChange(context, ChangeCategory.MESSAGE_PAYLOAD, ChangeType.REMOVED, Severity.BREAKING,
                    "Message '" + messageName + "' removed"));
            return changes;
        }

        // Content type changed
        if (!Objects.equals(oldMessage.getContentType(), newMessage.getContentType())) {
            changes.add(createChange(context, ChangeCategory.MESSAGE_PAYLOAD, ChangeType.MODIFIED, Severity.BREAKING,
                    "Message content type changed from '" + oldMessage.getContentType() +
                    "' to '" + newMessage.getContentType() + "'"));
        }

        // Schema format changed
        if (!Objects.equals(oldMessage.getSchemaFormat(), newMessage.getSchemaFormat())) {
            changes.add(createChange(context, ChangeCategory.MESSAGE_PAYLOAD, ChangeType.MODIFIED, Severity.BREAKING,
                    "Message schema format changed from '" + oldMessage.getSchemaFormat() +
                    "' to '" + newMessage.getSchemaFormat() + "'"));
        }

        // Deprecated status changed
        if (!oldMessage.isDeprecated() && newMessage.isDeprecated()) {
            changes.add(createChange(context, ChangeCategory.MESSAGE_PAYLOAD, ChangeType.DEPRECATED, Severity.WARNING,
                    "Message '" + messageName + "' deprecated"));
        }

        // Correlation ID changed
        if (!Objects.equals(oldMessage.getCorrelationId(), newMessage.getCorrelationId())) {
            changes.add(createChange(context, ChangeCategory.MESSAGE_PAYLOAD, ChangeType.MODIFIED, Severity.DANGEROUS,
                    "Message correlation ID changed from '" + oldMessage.getCorrelationId() +
                    "' to '" + newMessage.getCorrelationId() + "'"));
        }

        // Title/summary changes (informational)
        if (!Objects.equals(oldMessage.getTitle(), newMessage.getTitle())) {
            changes.add(createChange(context, ChangeCategory.MESSAGE_PAYLOAD, ChangeType.MODIFIED, Severity.INFO,
                    "Message title changed"));
        }

        if (!Objects.equals(oldMessage.getSummary(), newMessage.getSummary())) {
            changes.add(createChange(context, ChangeCategory.MESSAGE_PAYLOAD, ChangeType.MODIFIED, Severity.INFO,
                    "Message summary changed"));
        }

        // Payload schema changed
        if (oldMessage.getPayload() != null || newMessage.getPayload() != null) {
            String payloadContext = context + ".payload";

            if (oldMessage.getPayload() == null) {
                changes.add(createChange(payloadContext, ChangeCategory.MESSAGE_PAYLOAD, ChangeType.ADDED, Severity.INFO,
                        "Message payload added"));
            } else if (newMessage.getPayload() == null) {
                changes.add(createChange(payloadContext, ChangeCategory.MESSAGE_PAYLOAD, ChangeType.REMOVED, Severity.BREAKING,
                        "Message payload removed"));
            } else {
                changes.addAll(schemaComparator.compare(
                        messageName + ".payload",
                        oldMessage.getPayload(),
                        newMessage.getPayload()));
            }
        }

        // Headers schema changed
        if (oldMessage.getHeaders() != null || newMessage.getHeaders() != null) {
            String headersContext = context + ".headers";

            if (oldMessage.getHeaders() == null) {
                changes.add(createChange(headersContext, ChangeCategory.MESSAGE_HEADERS, ChangeType.ADDED, Severity.INFO,
                        "Message headers added"));
            } else if (newMessage.getHeaders() == null) {
                changes.add(createChange(headersContext, ChangeCategory.MESSAGE_HEADERS, ChangeType.REMOVED, Severity.DANGEROUS,
                        "Message headers removed"));
            } else {
                changes.addAll(schemaComparator.compare(
                        messageName + ".headers",
                        oldMessage.getHeaders(),
                        newMessage.getHeaders()));
            }
        }

        // Bindings changed
        changes.addAll(compareBindings(context, oldMessage.getBindings(), newMessage.getBindings()));

        // Tags changed
        changes.addAll(compareTags(context, oldMessage.getTags(), newMessage.getTags()));

        return changes;
    }

    /**
     * Compare message bindings.
     */
    public List<Change> compareBindings(String context,
                                         Map<String, Object> oldBindings,
                                         Map<String, Object> newBindings) {
        List<Change> changes = new ArrayList<>();

        Map<String, Object> old = oldBindings != null ? oldBindings : Collections.emptyMap();
        Map<String, Object> neu = newBindings != null ? newBindings : Collections.emptyMap();

        // Check for removed bindings
        for (String bindingName : old.keySet()) {
            if (!neu.containsKey(bindingName)) {
                changes.add(createChange(context, ChangeCategory.BINDING, ChangeType.REMOVED, Severity.DANGEROUS,
                        "Message binding '" + bindingName + "' removed"));
            }
        }

        // Check for added bindings
        for (String bindingName : neu.keySet()) {
            if (!old.containsKey(bindingName)) {
                changes.add(createChange(context, ChangeCategory.BINDING, ChangeType.ADDED, Severity.INFO,
                        "Message binding '" + bindingName + "' added"));
            }
        }

        // Check for changed bindings
        for (String bindingName : old.keySet()) {
            if (neu.containsKey(bindingName)) {
                if (!Objects.equals(old.get(bindingName), neu.get(bindingName))) {
                    changes.add(createChange(context, ChangeCategory.BINDING, ChangeType.MODIFIED, Severity.WARNING,
                            "Message binding '" + bindingName + "' modified"));
                }
            }
        }

        return changes;
    }

    /**
     * Compare message tags.
     */
    public List<Change> compareTags(String context, List<String> oldTags, List<String> newTags) {
        List<Change> changes = new ArrayList<>();

        Set<String> old = oldTags != null ? new HashSet<>(oldTags) : Collections.emptySet();
        Set<String> neu = newTags != null ? new HashSet<>(newTags) : Collections.emptySet();

        // Check for removed tags
        for (String tag : old) {
            if (!neu.contains(tag)) {
                changes.add(createChange(context, ChangeCategory.MESSAGE_PAYLOAD, ChangeType.MODIFIED, Severity.INFO,
                        "Tag '" + tag + "' removed from message"));
            }
        }

        // Check for added tags
        for (String tag : neu) {
            if (!old.contains(tag)) {
                changes.add(createChange(context, ChangeCategory.MESSAGE_PAYLOAD, ChangeType.MODIFIED, Severity.INFO,
                        "Tag '" + tag + "' added to message"));
            }
        }

        return changes;
    }

    /**
     * Compare multiple messages.
     */
    public List<Change> compareAll(Map<String, AsyncMessage> oldMessages,
                                    Map<String, AsyncMessage> newMessages) {
        List<Change> changes = new ArrayList<>();

        Map<String, AsyncMessage> old = oldMessages != null ? oldMessages : Collections.emptyMap();
        Map<String, AsyncMessage> neu = newMessages != null ? newMessages : Collections.emptyMap();

        Set<String> allNames = new LinkedHashSet<>();
        allNames.addAll(old.keySet());
        allNames.addAll(neu.keySet());

        for (String name : allNames) {
            changes.addAll(compare(name, old.get(name), neu.get(name)));
        }

        return changes;
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
     * Create message signature for comparison.
     */
    public String createSignature(AsyncMessage message) {
        if (message == null) {
            return "null";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(message.getName() != null ? message.getName() : "unnamed");

        if (message.getContentType() != null) {
            sb.append(":").append(message.getContentType());
        }

        if (message.getPayload() != null) {
            sb.append(":payload=").append(schemaComparator.createSignature(message.getPayload()));
        }

        return sb.toString();
    }
}

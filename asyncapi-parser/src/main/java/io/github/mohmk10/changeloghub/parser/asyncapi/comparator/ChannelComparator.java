package io.github.mohmk10.changeloghub.parser.asyncapi.comparator;

import io.github.mohmk10.changeloghub.core.model.Change;
import io.github.mohmk10.changeloghub.core.model.ChangeCategory;
import io.github.mohmk10.changeloghub.core.model.ChangeType;
import io.github.mohmk10.changeloghub.core.model.Severity;
import io.github.mohmk10.changeloghub.parser.asyncapi.model.AsyncChannel;
import io.github.mohmk10.changeloghub.parser.asyncapi.model.AsyncMessage;
import io.github.mohmk10.changeloghub.parser.asyncapi.model.AsyncOperation;

import java.util.*;

/**
 * Compares AsyncAPI channels to detect changes.
 *
 * Breaking change rules:
 * - BREAKING: Channel removed, operation removed, required parameter added
 * - DANGEROUS: Channel parameters modified, optional parameter removed
 * - WARNING: Channel deprecated, description changed
 * - INFO: New channel, new operation, new optional parameter
 */
public class ChannelComparator {

    private final MessageComparator messageComparator;
    private final SchemaComparator schemaComparator;

    public ChannelComparator() {
        this.schemaComparator = new SchemaComparator();
        this.messageComparator = new MessageComparator(schemaComparator);
    }

    public ChannelComparator(MessageComparator messageComparator, SchemaComparator schemaComparator) {
        this.messageComparator = messageComparator;
        this.schemaComparator = schemaComparator;
    }

    /**
     * Compare two channels and return detected changes.
     */
    public List<Change> compare(String channelName, AsyncChannel oldChannel, AsyncChannel newChannel) {
        List<Change> changes = new ArrayList<>();

        if (oldChannel == null && newChannel == null) {
            return changes;
        }

        String context = "channel:" + channelName;

        // Channel added
        if (oldChannel == null) {
            changes.add(createChange(context, ChangeCategory.CHANNEL, ChangeType.ADDED, Severity.INFO,
                    "Channel '" + channelName + "' added"));
            return changes;
        }

        // Channel removed
        if (newChannel == null) {
            changes.add(createChange(context, ChangeCategory.CHANNEL, ChangeType.REMOVED, Severity.BREAKING,
                    "Channel '" + channelName + "' removed"));
            return changes;
        }

        // Address changed (AsyncAPI 3.x)
        if (!Objects.equals(oldChannel.getAddress(), newChannel.getAddress())) {
            changes.add(createChange(context, ChangeCategory.CHANNEL, ChangeType.MODIFIED, Severity.BREAKING,
                    "Channel address changed from '" + oldChannel.getAddress() +
                    "' to '" + newChannel.getAddress() + "'"));
        }

        // Deprecated status changed
        if (!oldChannel.isDeprecated() && newChannel.isDeprecated()) {
            changes.add(createChange(context, ChangeCategory.CHANNEL, ChangeType.DEPRECATED, Severity.WARNING,
                    "Channel '" + channelName + "' deprecated"));
        }

        // Description changed
        if (!Objects.equals(oldChannel.getDescription(), newChannel.getDescription())) {
            changes.add(createChange(context, ChangeCategory.CHANNEL, ChangeType.MODIFIED, Severity.INFO,
                    "Channel description changed"));
        }

        // Compare publish operation
        changes.addAll(compareOperation(context, "publish",
                oldChannel.getPublishOperation(), newChannel.getPublishOperation()));

        // Compare subscribe operation
        changes.addAll(compareOperation(context, "subscribe",
                oldChannel.getSubscribeOperation(), newChannel.getSubscribeOperation()));

        // Compare channel parameters
        changes.addAll(compareParameters(context, oldChannel.getParameters(), newChannel.getParameters()));

        // Compare channel messages (AsyncAPI 3.x)
        changes.addAll(compareMessages(context, oldChannel.getMessages(), newChannel.getMessages()));

        // Compare bindings
        changes.addAll(compareBindings(context, oldChannel.getBindings(), newChannel.getBindings()));

        // Compare servers
        changes.addAll(compareServers(context, oldChannel.getServers(), newChannel.getServers()));

        return changes;
    }

    /**
     * Compare operations.
     */
    public List<Change> compareOperation(String context, String opType,
                                          AsyncOperation oldOp, AsyncOperation newOp) {
        List<Change> changes = new ArrayList<>();
        String opContext = context + "." + opType;

        // Operation added
        if (oldOp == null && newOp != null) {
            changes.add(createChange(opContext, ChangeCategory.OPERATION, ChangeType.ADDED, Severity.INFO,
                    "'" + opType + "' operation added"));
            return changes;
        }

        // Operation removed
        if (oldOp != null && newOp == null) {
            changes.add(createChange(opContext, ChangeCategory.OPERATION, ChangeType.REMOVED, Severity.BREAKING,
                    "'" + opType + "' operation removed"));
            return changes;
        }

        if (oldOp == null || newOp == null) {
            return changes;
        }

        // Operation ID changed
        if (!Objects.equals(oldOp.getOperationId(), newOp.getOperationId())) {
            changes.add(createChange(opContext, ChangeCategory.OPERATION, ChangeType.MODIFIED, Severity.WARNING,
                    "Operation ID changed from '" + oldOp.getOperationId() +
                    "' to '" + newOp.getOperationId() + "'"));
        }

        // Deprecated status changed
        if (!oldOp.isDeprecated() && newOp.isDeprecated()) {
            changes.add(createChange(opContext, ChangeCategory.OPERATION, ChangeType.DEPRECATED, Severity.WARNING,
                    "Operation deprecated"));
        }

        // Compare single message
        if (oldOp.getMessage() != null || newOp.getMessage() != null) {
            changes.addAll(compareOperationMessage(opContext, oldOp.getMessage(), newOp.getMessage()));
        }

        // Compare multiple messages
        if (oldOp.getMessages() != null || newOp.getMessages() != null) {
            changes.addAll(compareOperationMessages(opContext, oldOp.getMessages(), newOp.getMessages()));
        }

        // Compare bindings
        changes.addAll(compareBindings(opContext, oldOp.getBindings(), newOp.getBindings()));

        return changes;
    }

    /**
     * Compare operation message.
     */
    private List<Change> compareOperationMessage(String context,
                                                   AsyncMessage oldMessage, AsyncMessage newMessage) {
        List<Change> changes = new ArrayList<>();

        if (oldMessage == null && newMessage == null) {
            return changes;
        }

        String messageName = "operationMessage";
        if (oldMessage != null && oldMessage.getName() != null) {
            messageName = oldMessage.getName();
        } else if (newMessage != null && newMessage.getName() != null) {
            messageName = newMessage.getName();
        }

        changes.addAll(messageComparator.compare(messageName, oldMessage, newMessage));

        return changes;
    }

    /**
     * Compare operation messages list.
     */
    private List<Change> compareOperationMessages(String context,
                                                    List<AsyncMessage> oldMessages, List<AsyncMessage> newMessages) {
        List<Change> changes = new ArrayList<>();

        List<AsyncMessage> old = oldMessages != null ? oldMessages : Collections.emptyList();
        List<AsyncMessage> neu = newMessages != null ? newMessages : Collections.emptyList();

        // Build maps by message name/ref for comparison
        Map<String, AsyncMessage> oldMap = buildMessageMap(old);
        Map<String, AsyncMessage> newMap = buildMessageMap(neu);

        changes.addAll(messageComparator.compareAll(oldMap, newMap));

        return changes;
    }

    /**
     * Build a map of messages by name.
     */
    private Map<String, AsyncMessage> buildMessageMap(List<AsyncMessage> messages) {
        Map<String, AsyncMessage> map = new LinkedHashMap<>();
        int index = 0;
        for (AsyncMessage msg : messages) {
            String key = msg.getName() != null ? msg.getName() :
                        (msg.getRef() != null ? msg.getRef() : "message" + index++);
            map.put(key, msg);
        }
        return map;
    }

    /**
     * Compare channel parameters.
     */
    public List<Change> compareParameters(String context,
                                           Map<String, AsyncChannel.ChannelParameter> oldParams,
                                           Map<String, AsyncChannel.ChannelParameter> newParams) {
        List<Change> changes = new ArrayList<>();

        Map<String, AsyncChannel.ChannelParameter> old = oldParams != null ?
                oldParams : Collections.emptyMap();
        Map<String, AsyncChannel.ChannelParameter> neu = newParams != null ?
                newParams : Collections.emptyMap();

        // Check for removed parameters
        for (String paramName : old.keySet()) {
            if (!neu.containsKey(paramName)) {
                changes.add(createChange(context, ChangeCategory.PARAMETER, ChangeType.REMOVED, Severity.BREAKING,
                        "Channel parameter '" + paramName + "' removed"));
            }
        }

        // Check for added parameters
        for (String paramName : neu.keySet()) {
            if (!old.containsKey(paramName)) {
                changes.add(createChange(context, ChangeCategory.PARAMETER, ChangeType.ADDED, Severity.BREAKING,
                        "New channel parameter '" + paramName + "' added (breaking: requires path change)"));
            }
        }

        // Check for changed parameters
        for (String paramName : old.keySet()) {
            if (neu.containsKey(paramName)) {
                AsyncChannel.ChannelParameter oldParam = old.get(paramName);
                AsyncChannel.ChannelParameter newParam = neu.get(paramName);

                // Compare schemas
                if (oldParam.getSchema() != null || newParam.getSchema() != null) {
                    changes.addAll(schemaComparator.compare(
                            context + ".parameters." + paramName,
                            oldParam.getSchema(), newParam.getSchema()));
                }
            }
        }

        return changes;
    }

    /**
     * Compare channel messages (AsyncAPI 3.x).
     */
    public List<Change> compareMessages(String context,
                                         Map<String, AsyncMessage> oldMessages,
                                         Map<String, AsyncMessage> newMessages) {
        if (oldMessages == null && newMessages == null) {
            return Collections.emptyList();
        }

        return messageComparator.compareAll(oldMessages, newMessages);
    }

    /**
     * Compare bindings.
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
                        "Binding '" + bindingName + "' removed"));
            }
        }

        // Check for added bindings
        for (String bindingName : neu.keySet()) {
            if (!old.containsKey(bindingName)) {
                changes.add(createChange(context, ChangeCategory.BINDING, ChangeType.ADDED, Severity.INFO,
                        "Binding '" + bindingName + "' added"));
            }
        }

        // Check for changed bindings
        for (String bindingName : old.keySet()) {
            if (neu.containsKey(bindingName)) {
                if (!Objects.equals(old.get(bindingName), neu.get(bindingName))) {
                    changes.add(createChange(context, ChangeCategory.BINDING, ChangeType.MODIFIED, Severity.WARNING,
                            "Binding '" + bindingName + "' modified"));
                }
            }
        }

        return changes;
    }

    /**
     * Compare channel servers.
     */
    public List<Change> compareServers(String context, List<String> oldServers, List<String> newServers) {
        List<Change> changes = new ArrayList<>();

        Set<String> old = oldServers != null ? new HashSet<>(oldServers) : Collections.emptySet();
        Set<String> neu = newServers != null ? new HashSet<>(newServers) : Collections.emptySet();

        // Check for removed servers
        for (String server : old) {
            if (!neu.contains(server)) {
                changes.add(createChange(context, ChangeCategory.SERVER, ChangeType.REMOVED, Severity.WARNING,
                        "Server '" + server + "' removed from channel"));
            }
        }

        // Check for added servers
        for (String server : neu) {
            if (!old.contains(server)) {
                changes.add(createChange(context, ChangeCategory.SERVER, ChangeType.ADDED, Severity.INFO,
                        "Server '" + server + "' added to channel"));
            }
        }

        return changes;
    }

    /**
     * Compare multiple channels.
     */
    public List<Change> compareAll(Map<String, AsyncChannel> oldChannels,
                                    Map<String, AsyncChannel> newChannels) {
        List<Change> changes = new ArrayList<>();

        Map<String, AsyncChannel> old = oldChannels != null ? oldChannels : Collections.emptyMap();
        Map<String, AsyncChannel> neu = newChannels != null ? newChannels : Collections.emptyMap();

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
     * Create channel signature for comparison.
     */
    public String createSignature(AsyncChannel channel) {
        if (channel == null) {
            return "null";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(channel.getName() != null ? channel.getName() : "unnamed");

        if (channel.getAddress() != null) {
            sb.append(":").append(channel.getAddress());
        }

        if (channel.getPublishOperation() != null) {
            sb.append(":pub");
        }
        if (channel.getSubscribeOperation() != null) {
            sb.append(":sub");
        }

        return sb.toString();
    }
}

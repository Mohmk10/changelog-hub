package io.github.mohmk10.changeloghub.parser.asyncapi.comparator;

import io.github.mohmk10.changeloghub.core.model.Change;
import io.github.mohmk10.changeloghub.core.model.ChangeCategory;
import io.github.mohmk10.changeloghub.core.model.ChangeType;
import io.github.mohmk10.changeloghub.core.model.Severity;
import io.github.mohmk10.changeloghub.parser.asyncapi.model.*;
import io.github.mohmk10.changeloghub.parser.asyncapi.util.AsyncApiVersion;

import java.util.*;

public class AsyncApiComparator {

    private final ChannelComparator channelComparator;
    private final MessageComparator messageComparator;
    private final SchemaComparator schemaComparator;

    public AsyncApiComparator() {
        this.schemaComparator = new SchemaComparator();
        this.messageComparator = new MessageComparator(schemaComparator);
        this.channelComparator = new ChannelComparator(messageComparator, schemaComparator);
    }

    public AsyncApiComparator(ChannelComparator channelComparator,
                               MessageComparator messageComparator,
                               SchemaComparator schemaComparator) {
        this.channelComparator = channelComparator;
        this.messageComparator = messageComparator;
        this.schemaComparator = schemaComparator;
    }

    public List<Change> compare(AsyncApiSpec oldSpec, AsyncApiSpec newSpec) {
        List<Change> changes = new ArrayList<>();

        if (oldSpec == null && newSpec == null) {
            return changes;
        }

        if (oldSpec == null) {
            changes.add(createChange("spec", ChangeCategory.ENDPOINT, ChangeType.ADDED, Severity.INFO,
                    "AsyncAPI specification created"));
            return changes;
        }

        if (newSpec == null) {
            changes.add(createChange("spec", ChangeCategory.ENDPOINT, ChangeType.REMOVED, Severity.BREAKING,
                    "AsyncAPI specification removed"));
            return changes;
        }

        changes.addAll(compareInfo(oldSpec, newSpec));

        changes.addAll(compareAsyncApiVersion(oldSpec.getAsyncApiVersion(), newSpec.getAsyncApiVersion()));

        changes.addAll(compareServers(oldSpec.getServers(), newSpec.getServers()));

        changes.addAll(channelComparator.compareAll(oldSpec.getChannels(), newSpec.getChannels()));

        changes.addAll(compareOperations(oldSpec.getOperations(), newSpec.getOperations()));

        changes.addAll(compareComponents(oldSpec.getComponents(), newSpec.getComponents()));

        changes.addAll(compareTags(oldSpec.getTags(), newSpec.getTags()));

        return changes;
    }

    public List<Change> compareInfo(AsyncApiSpec oldSpec, AsyncApiSpec newSpec) {
        List<Change> changes = new ArrayList<>();
        String context = "info";

        if (!Objects.equals(oldSpec.getTitle(), newSpec.getTitle())) {
            changes.add(createChange(context, ChangeCategory.ENDPOINT, ChangeType.MODIFIED, Severity.INFO,
                    "API title changed from '" + oldSpec.getTitle() + "' to '" + newSpec.getTitle() + "'"));
        }

        if (!Objects.equals(oldSpec.getApiVersion(), newSpec.getApiVersion())) {
            changes.add(createChange(context, ChangeCategory.ENDPOINT, ChangeType.MODIFIED, Severity.INFO,
                    "API version changed from '" + oldSpec.getApiVersion() + "' to '" + newSpec.getApiVersion() + "'"));
        }

        if (!Objects.equals(oldSpec.getDescription(), newSpec.getDescription())) {
            changes.add(createChange(context, ChangeCategory.ENDPOINT, ChangeType.MODIFIED, Severity.INFO,
                    "API description changed"));
        }

        if (!contactEquals(oldSpec.getContact(), newSpec.getContact())) {
            changes.add(createChange(context, ChangeCategory.ENDPOINT, ChangeType.MODIFIED, Severity.INFO,
                    "Contact information changed"));
        }

        if (!licenseEquals(oldSpec.getLicense(), newSpec.getLicense())) {
            changes.add(createChange(context, ChangeCategory.ENDPOINT, ChangeType.MODIFIED, Severity.INFO,
                    "License information changed"));
        }

        return changes;
    }

    public List<Change> compareAsyncApiVersion(AsyncApiVersion oldVersion, AsyncApiVersion newVersion) {
        List<Change> changes = new ArrayList<>();

        if (Objects.equals(oldVersion, newVersion)) {
            return changes;
        }

        String context = "asyncapi";
        String oldV = oldVersion != null ? oldVersion.getVersion() : "unknown";
        String newV = newVersion != null ? newVersion.getVersion() : "unknown";

        if (oldVersion != null && newVersion != null) {
            if (oldVersion.isV2() && newVersion.isV3()) {
                changes.add(createChange(context, ChangeCategory.ENDPOINT, ChangeType.MODIFIED, Severity.BREAKING,
                        "AsyncAPI version upgraded from " + oldV + " to " + newV +
                        " (major version change: v2→v3 has structural differences)"));
            } else if (oldVersion.isV3() && newVersion.isV2()) {
                changes.add(createChange(context, ChangeCategory.ENDPOINT, ChangeType.MODIFIED, Severity.BREAKING,
                        "AsyncAPI version downgraded from " + oldV + " to " + newV +
                        " (major version change)"));
            } else {
                changes.add(createChange(context, ChangeCategory.ENDPOINT, ChangeType.MODIFIED, Severity.INFO,
                        "AsyncAPI version changed from " + oldV + " to " + newV));
            }
        } else {
            changes.add(createChange(context, ChangeCategory.ENDPOINT, ChangeType.MODIFIED, Severity.INFO,
                    "AsyncAPI version changed from " + oldV + " to " + newV));
        }

        return changes;
    }

    public List<Change> compareServers(Map<String, AsyncServer> oldServers,
                                        Map<String, AsyncServer> newServers) {
        List<Change> changes = new ArrayList<>();

        Map<String, AsyncServer> old = oldServers != null ? oldServers : Collections.emptyMap();
        Map<String, AsyncServer> neu = newServers != null ? newServers : Collections.emptyMap();

        for (String serverName : old.keySet()) {
            if (!neu.containsKey(serverName)) {
                changes.add(createChange("server:" + serverName, ChangeCategory.SERVER, ChangeType.REMOVED, Severity.BREAKING,
                        "Server '" + serverName + "' removed"));
            }
        }

        for (String serverName : neu.keySet()) {
            if (!old.containsKey(serverName)) {
                changes.add(createChange("server:" + serverName, ChangeCategory.SERVER, ChangeType.ADDED, Severity.INFO,
                        "Server '" + serverName + "' added"));
            }
        }

        for (String serverName : old.keySet()) {
            if (neu.containsKey(serverName)) {
                changes.addAll(compareServer(serverName, old.get(serverName), neu.get(serverName)));
            }
        }

        return changes;
    }

    private List<Change> compareServer(String serverName, AsyncServer oldServer, AsyncServer newServer) {
        List<Change> changes = new ArrayList<>();
        String context = "server:" + serverName;

        if (!Objects.equals(oldServer.getUrl(), newServer.getUrl())) {
            changes.add(createChange(context, ChangeCategory.SERVER, ChangeType.MODIFIED, Severity.WARNING,
                    "Server URL changed from '" + oldServer.getUrl() + "' to '" + newServer.getUrl() + "'"));
        }

        if (!Objects.equals(oldServer.getProtocol(), newServer.getProtocol())) {
            changes.add(createChange(context, ChangeCategory.PROTOCOL, ChangeType.MODIFIED, Severity.BREAKING,
                    "Server protocol changed from '" + oldServer.getProtocol() +
                    "' to '" + newServer.getProtocol() + "'"));
        }

        if (!Objects.equals(oldServer.getProtocolVersion(), newServer.getProtocolVersion())) {
            changes.add(createChange(context, ChangeCategory.PROTOCOL, ChangeType.MODIFIED, Severity.WARNING,
                    "Server protocol version changed from '" + oldServer.getProtocolVersion() +
                    "' to '" + newServer.getProtocolVersion() + "'"));
        }

        if (!oldServer.isDeprecated() && newServer.isDeprecated()) {
            changes.add(createChange(context, ChangeCategory.SERVER, ChangeType.DEPRECATED, Severity.WARNING,
                    "Server deprecated"));
        }

        changes.addAll(compareServerVariables(context,
                oldServer.getVariables(), newServer.getVariables()));

        return changes;
    }

    private List<Change> compareServerVariables(String context,
                                                  Map<String, AsyncServer.ServerVariable> oldVars,
                                                  Map<String, AsyncServer.ServerVariable> newVars) {
        List<Change> changes = new ArrayList<>();

        Map<String, AsyncServer.ServerVariable> old = oldVars != null ? oldVars : Collections.emptyMap();
        Map<String, AsyncServer.ServerVariable> neu = newVars != null ? newVars : Collections.emptyMap();

        for (String varName : old.keySet()) {
            if (!neu.containsKey(varName)) {
                changes.add(createChange(context, ChangeCategory.PARAMETER, ChangeType.REMOVED, Severity.DANGEROUS,
                        "Server variable '" + varName + "' removed"));
            }
        }

        for (String varName : neu.keySet()) {
            if (!old.containsKey(varName)) {
                changes.add(createChange(context, ChangeCategory.PARAMETER, ChangeType.ADDED, Severity.INFO,
                        "Server variable '" + varName + "' added"));
            }
        }

        for (String varName : old.keySet()) {
            if (neu.containsKey(varName)) {
                AsyncServer.ServerVariable oldVar = old.get(varName);
                AsyncServer.ServerVariable newVar = neu.get(varName);

                if (!Objects.equals(oldVar.getDefaultValue(), newVar.getDefaultValue())) {
                    changes.add(createChange(context, ChangeCategory.PARAMETER, ChangeType.MODIFIED, Severity.WARNING,
                            "Server variable '" + varName + "' default value changed"));
                }

                Set<String> oldAllowed = oldVar.getAllowedValues() != null ?
                        new HashSet<>(oldVar.getAllowedValues()) : Collections.emptySet();
                Set<String> newAllowed = newVar.getAllowedValues() != null ?
                        new HashSet<>(newVar.getAllowedValues()) : Collections.emptySet();

                for (String value : oldAllowed) {
                    if (!newAllowed.contains(value)) {
                        changes.add(createChange(context, ChangeCategory.ENUM_VALUE, ChangeType.REMOVED, Severity.BREAKING,
                                "Server variable '" + varName + "' allowed value '" + value + "' removed"));
                    }
                }
            }
        }

        return changes;
    }

    public List<Change> compareOperations(Map<String, AsyncOperation> oldOperations,
                                           Map<String, AsyncOperation> newOperations) {
        List<Change> changes = new ArrayList<>();

        Map<String, AsyncOperation> old = oldOperations != null ? oldOperations : Collections.emptyMap();
        Map<String, AsyncOperation> neu = newOperations != null ? newOperations : Collections.emptyMap();

        for (String opId : old.keySet()) {
            if (!neu.containsKey(opId)) {
                changes.add(createChange("operation:" + opId, ChangeCategory.OPERATION, ChangeType.REMOVED, Severity.BREAKING,
                        "Operation '" + opId + "' removed"));
            }
        }

        for (String opId : neu.keySet()) {
            if (!old.containsKey(opId)) {
                changes.add(createChange("operation:" + opId, ChangeCategory.OPERATION, ChangeType.ADDED, Severity.INFO,
                        "Operation '" + opId + "' added"));
            }
        }

        for (String opId : old.keySet()) {
            if (neu.containsKey(opId)) {
                changes.addAll(compareOperation(opId, old.get(opId), neu.get(opId)));
            }
        }

        return changes;
    }

    private List<Change> compareOperation(String opId, AsyncOperation oldOp, AsyncOperation newOp) {
        List<Change> changes = new ArrayList<>();
        String context = "operation:" + opId;

        if (!Objects.equals(oldOp.getType(), newOp.getType())) {
            changes.add(createChange(context, ChangeCategory.OPERATION, ChangeType.MODIFIED, Severity.BREAKING,
                    "Operation action changed from '" + oldOp.getType() +
                    "' to '" + newOp.getType() + "'"));
        }

        if (!Objects.equals(oldOp.getChannelRef(), newOp.getChannelRef())) {
            changes.add(createChange(context, ChangeCategory.OPERATION, ChangeType.MODIFIED, Severity.BREAKING,
                    "Operation channel reference changed from '" + oldOp.getChannelRef() +
                    "' to '" + newOp.getChannelRef() + "'"));
        }

        if (!oldOp.isDeprecated() && newOp.isDeprecated()) {
            changes.add(createChange(context, ChangeCategory.OPERATION, ChangeType.DEPRECATED, Severity.WARNING,
                    "Operation deprecated"));
        }

        return changes;
    }

    public List<Change> compareComponents(AsyncApiSpec.Components oldComponents,
                                           AsyncApiSpec.Components newComponents) {
        List<Change> changes = new ArrayList<>();

        if (oldComponents == null && newComponents == null) {
            return changes;
        }

        Map<String, AsyncMessage> oldMessages = oldComponents != null && oldComponents.getMessages() != null ?
                oldComponents.getMessages() : Collections.emptyMap();
        Map<String, AsyncMessage> newMessages = newComponents != null && newComponents.getMessages() != null ?
                newComponents.getMessages() : Collections.emptyMap();
        changes.addAll(messageComparator.compareAll(oldMessages, newMessages));

        Map<String, AsyncSchema> oldSchemas = oldComponents != null && oldComponents.getSchemas() != null ?
                oldComponents.getSchemas() : Collections.emptyMap();
        Map<String, AsyncSchema> newSchemas = newComponents != null && newComponents.getSchemas() != null ?
                newComponents.getSchemas() : Collections.emptyMap();
        changes.addAll(schemaComparator.compareAll(oldSchemas, newSchemas));

        return changes;
    }

    public List<Change> compareTags(List<AsyncApiSpec.Tag> oldTags, List<AsyncApiSpec.Tag> newTags) {
        List<Change> changes = new ArrayList<>();

        Set<String> oldTagNames = new HashSet<>();
        Set<String> newTagNames = new HashSet<>();

        if (oldTags != null) {
            for (AsyncApiSpec.Tag tag : oldTags) {
                oldTagNames.add(tag.getName());
            }
        }

        if (newTags != null) {
            for (AsyncApiSpec.Tag tag : newTags) {
                newTagNames.add(tag.getName());
            }
        }

        for (String tag : oldTagNames) {
            if (!newTagNames.contains(tag)) {
                changes.add(createChange("tags", ChangeCategory.ENDPOINT, ChangeType.MODIFIED, Severity.INFO,
                        "Tag '" + tag + "' removed"));
            }
        }

        for (String tag : newTagNames) {
            if (!oldTagNames.contains(tag)) {
                changes.add(createChange("tags", ChangeCategory.ENDPOINT, ChangeType.MODIFIED, Severity.INFO,
                        "Tag '" + tag + "' added"));
            }
        }

        return changes;
    }

    private boolean contactEquals(AsyncApiSpec.Contact a, AsyncApiSpec.Contact b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return Objects.equals(a.getName(), b.getName()) &&
               Objects.equals(a.getEmail(), b.getEmail()) &&
               Objects.equals(a.getUrl(), b.getUrl());
    }

    private boolean licenseEquals(AsyncApiSpec.License a, AsyncApiSpec.License b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return Objects.equals(a.getName(), b.getName()) &&
               Objects.equals(a.getUrl(), b.getUrl());
    }

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

    public Map<Severity, Integer> getChangesSummary(List<Change> changes) {
        Map<Severity, Integer> summary = new LinkedHashMap<>();
        for (Severity severity : Severity.values()) {
            summary.put(severity, 0);
        }

        for (Change change : changes) {
            Severity sev = change.getSeverity();
            summary.put(sev, summary.get(sev) + 1);
        }

        return summary;
    }

    public boolean hasBreakingChanges(List<Change> changes) {
        for (Change change : changes) {
            if (change.getSeverity() == Severity.BREAKING) {
                return true;
            }
        }
        return false;
    }

    public List<Change> filterBySeverity(List<Change> changes, Severity severity) {
        List<Change> filtered = new ArrayList<>();
        for (Change change : changes) {
            if (change.getSeverity() == severity) {
                filtered.add(change);
            }
        }
        return filtered;
    }

    public List<Change> filterByCategory(List<Change> changes, ChangeCategory category) {
        List<Change> filtered = new ArrayList<>();
        for (Change change : changes) {
            if (change.getCategory() == category) {
                filtered.add(change);
            }
        }
        return filtered;
    }
}

package io.github.mohmk10.changeloghub.parser.grpc.comparator;

import io.github.mohmk10.changeloghub.core.model.BreakingChange;
import io.github.mohmk10.changeloghub.core.model.ChangeCategory;
import io.github.mohmk10.changeloghub.core.model.ChangeType;
import io.github.mohmk10.changeloghub.core.model.Severity;
import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoRpcMethod;
import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoService;
import io.github.mohmk10.changeloghub.parser.grpc.util.StreamType;

import java.time.LocalDateTime;
import java.util.*;

public class ServiceComparator {

    public List<BreakingChange> compareServices(List<ProtoService> oldServices, List<ProtoService> newServices,
                                                 String packageName) {
        List<BreakingChange> changes = new ArrayList<>();

        Map<String, ProtoService> oldByName = mapByName(oldServices);
        Map<String, ProtoService> newByName = mapByName(newServices);

        for (ProtoService oldService : oldServices) {
            if (!newByName.containsKey(oldService.getName())) {
                changes.add(createServiceRemovedChange(oldService, packageName));
            }
        }

        for (ProtoService newService : newServices) {
            if (!oldByName.containsKey(newService.getName())) {
                changes.add(createServiceAddedChange(newService, packageName));
            }
        }

        for (ProtoService oldService : oldServices) {
            ProtoService newService = newByName.get(oldService.getName());
            if (newService != null) {
                String servicePath = getServicePath(packageName, oldService.getName());
                changes.addAll(compareService(oldService, newService, servicePath));
            }
        }

        return changes;
    }

    public List<BreakingChange> compareService(ProtoService oldService, ProtoService newService, String servicePath) {
        List<BreakingChange> changes = new ArrayList<>();

        changes.addAll(compareRpcMethods(oldService.getMethods(), newService.getMethods(), servicePath));

        if (!oldService.isDeprecated() && newService.isDeprecated()) {
            changes.add(createServiceDeprecationChange(newService, servicePath));
        }

        return changes;
    }

    public List<BreakingChange> compareRpcMethods(List<ProtoRpcMethod> oldMethods, List<ProtoRpcMethod> newMethods,
                                                   String servicePath) {
        List<BreakingChange> changes = new ArrayList<>();

        Map<String, ProtoRpcMethod> oldByName = mapMethodsByName(oldMethods);
        Map<String, ProtoRpcMethod> newByName = mapMethodsByName(newMethods);

        for (ProtoRpcMethod oldMethod : oldMethods) {
            if (!newByName.containsKey(oldMethod.getName())) {
                changes.add(createMethodRemovedChange(oldMethod, servicePath));
            }
        }

        for (ProtoRpcMethod newMethod : newMethods) {
            if (!oldByName.containsKey(newMethod.getName())) {
                changes.add(createMethodAddedChange(newMethod, servicePath));
            }
        }

        for (ProtoRpcMethod oldMethod : oldMethods) {
            ProtoRpcMethod newMethod = newByName.get(oldMethod.getName());
            if (newMethod != null) {
                String methodPath = servicePath + "/" + oldMethod.getName();
                changes.addAll(compareRpcMethod(oldMethod, newMethod, methodPath));
            }
        }

        return changes;
    }

    public List<BreakingChange> compareRpcMethod(ProtoRpcMethod oldMethod, ProtoRpcMethod newMethod, String methodPath) {
        List<BreakingChange> changes = new ArrayList<>();

        if (!oldMethod.getInputType().equals(newMethod.getInputType())) {
            changes.add(createInputTypeChangedChange(oldMethod, newMethod, methodPath));
        }

        if (!oldMethod.getOutputType().equals(newMethod.getOutputType())) {
            changes.add(createOutputTypeChangedChange(oldMethod, newMethod, methodPath));
        }

        StreamType oldStreamType = oldMethod.getStreamType();
        StreamType newStreamType = newMethod.getStreamType();
        if (oldStreamType != newStreamType) {
            changes.add(createStreamingTypeChangedChange(oldMethod, newMethod, methodPath));
        }

        if (!oldMethod.isDeprecated() && newMethod.isDeprecated()) {
            changes.add(createMethodDeprecationChange(newMethod, methodPath));
        }

        return changes;
    }

    private BreakingChange createServiceRemovedChange(ProtoService service, String packageName) {
        String path = getServicePath(packageName, service.getName());

        return BreakingChange.breakingChangeBuilder()
                .id(UUID.randomUUID().toString())
                .type(ChangeType.REMOVED)
                .category(ChangeCategory.SERVICE)
                .severity(Severity.BREAKING)
                .path(path)
                .description("Service '" + service.getName() + "' removed with " +
                        service.getMethodCount() + " RPC methods")
                .oldValue(service.getMethodCount() + " methods")
                .newValue(null)
                .detectedAt(LocalDateTime.now())
                .migrationSuggestion("Service '" + service.getName() + "' was removed. " +
                        "All clients calling this service will fail. Migrate to alternative service.")
                .impactScore(100)
                .build();
    }

    private BreakingChange createServiceAddedChange(ProtoService service, String packageName) {
        String path = getServicePath(packageName, service.getName());

        return BreakingChange.breakingChangeBuilder()
                .id(UUID.randomUUID().toString())
                .type(ChangeType.ADDED)
                .category(ChangeCategory.SERVICE)
                .severity(Severity.INFO)
                .path(path)
                .description("Service '" + service.getName() + "' added with " +
                        service.getMethodCount() + " RPC methods")
                .oldValue(null)
                .newValue(service.getMethodCount() + " methods")
                .detectedAt(LocalDateTime.now())
                .migrationSuggestion("New service available. No migration required.")
                .impactScore(5)
                .build();
    }

    private BreakingChange createServiceDeprecationChange(ProtoService service, String servicePath) {
        return BreakingChange.breakingChangeBuilder()
                .id(UUID.randomUUID().toString())
                .type(ChangeType.DEPRECATED)
                .category(ChangeCategory.SERVICE)
                .severity(Severity.WARNING)
                .path(servicePath)
                .description("Service '" + service.getName() + "' marked as deprecated")
                .oldValue(false)
                .newValue(true)
                .detectedAt(LocalDateTime.now())
                .migrationSuggestion("Service '" + service.getName() + "' is deprecated. " +
                        "Plan migration to alternative services.")
                .impactScore(30)
                .build();
    }

    private BreakingChange createMethodRemovedChange(ProtoRpcMethod method, String servicePath) {
        String path = servicePath + "/" + method.getName();

        return BreakingChange.breakingChangeBuilder()
                .id(UUID.randomUUID().toString())
                .type(ChangeType.REMOVED)
                .category(ChangeCategory.RPC_METHOD)
                .severity(Severity.BREAKING)
                .path(path)
                .description("RPC method '" + method.getName() + "' removed: " + method.getSignature())
                .oldValue(method.getSignature())
                .newValue(null)
                .detectedAt(LocalDateTime.now())
                .migrationSuggestion("RPC method '" + method.getName() + "' was removed. " +
                        "All clients calling this method will fail. Migrate to alternative method.")
                .impactScore(95)
                .build();
    }

    private BreakingChange createMethodAddedChange(ProtoRpcMethod method, String servicePath) {
        String path = servicePath + "/" + method.getName();

        return BreakingChange.breakingChangeBuilder()
                .id(UUID.randomUUID().toString())
                .type(ChangeType.ADDED)
                .category(ChangeCategory.RPC_METHOD)
                .severity(Severity.INFO)
                .path(path)
                .description("RPC method '" + method.getName() + "' added: " + method.getSignature())
                .oldValue(null)
                .newValue(method.getSignature())
                .detectedAt(LocalDateTime.now())
                .migrationSuggestion("New RPC method available. No migration required.")
                .impactScore(5)
                .build();
    }

    private BreakingChange createInputTypeChangedChange(ProtoRpcMethod oldMethod, ProtoRpcMethod newMethod,
                                                         String methodPath) {
        return BreakingChange.breakingChangeBuilder()
                .id(UUID.randomUUID().toString())
                .type(ChangeType.MODIFIED)
                .category(ChangeCategory.RPC_METHOD)
                .severity(Severity.BREAKING)
                .path(methodPath)
                .description("RPC method input type changed from " + oldMethod.getInputType() +
                        " to " + newMethod.getInputType())
                .oldValue(oldMethod.getInputType())
                .newValue(newMethod.getInputType())
                .detectedAt(LocalDateTime.now())
                .migrationSuggestion("Input type changed. Update all clients to use the new request message type '" +
                        newMethod.getInputType() + "'.")
                .impactScore(90)
                .build();
    }

    private BreakingChange createOutputTypeChangedChange(ProtoRpcMethod oldMethod, ProtoRpcMethod newMethod,
                                                          String methodPath) {
        return BreakingChange.breakingChangeBuilder()
                .id(UUID.randomUUID().toString())
                .type(ChangeType.MODIFIED)
                .category(ChangeCategory.RPC_METHOD)
                .severity(Severity.BREAKING)
                .path(methodPath)
                .description("RPC method output type changed from " + oldMethod.getOutputType() +
                        " to " + newMethod.getOutputType())
                .oldValue(oldMethod.getOutputType())
                .newValue(newMethod.getOutputType())
                .detectedAt(LocalDateTime.now())
                .migrationSuggestion("Output type changed. Update all clients to handle the new response message type '" +
                        newMethod.getOutputType() + "'.")
                .impactScore(90)
                .build();
    }

    private BreakingChange createStreamingTypeChangedChange(ProtoRpcMethod oldMethod, ProtoRpcMethod newMethod,
                                                             String methodPath) {
        StreamType oldType = oldMethod.getStreamType();
        StreamType newType = newMethod.getStreamType();

        Severity severity;
        String migrationSuggestion;

        if (oldType == StreamType.UNARY && newType != StreamType.UNARY) {
            severity = Severity.BREAKING;
            migrationSuggestion = "Method changed from unary to streaming. Clients must update to handle streams.";
        } else if (oldType != StreamType.UNARY && newType == StreamType.UNARY) {
            severity = Severity.BREAKING;
            migrationSuggestion = "Method changed from streaming to unary. Clients must update to use single request/response.";
        } else {
            severity = Severity.BREAKING;
            migrationSuggestion = "Streaming type changed. Clients must update their streaming handlers.";
        }

        return BreakingChange.breakingChangeBuilder()
                .id(UUID.randomUUID().toString())
                .type(ChangeType.MODIFIED)
                .category(ChangeCategory.STREAMING_TYPE)
                .severity(severity)
                .path(methodPath)
                .description("RPC streaming type changed from " + oldType + " to " + newType)
                .oldValue(oldType.name())
                .newValue(newType.name())
                .detectedAt(LocalDateTime.now())
                .migrationSuggestion(migrationSuggestion)
                .impactScore(85)
                .build();
    }

    private BreakingChange createMethodDeprecationChange(ProtoRpcMethod method, String methodPath) {
        return BreakingChange.breakingChangeBuilder()
                .id(UUID.randomUUID().toString())
                .type(ChangeType.DEPRECATED)
                .category(ChangeCategory.RPC_METHOD)
                .severity(Severity.WARNING)
                .path(methodPath)
                .description("RPC method '" + method.getName() + "' marked as deprecated")
                .oldValue(false)
                .newValue(true)
                .detectedAt(LocalDateTime.now())
                .migrationSuggestion("RPC method '" + method.getName() + "' is deprecated. " +
                        "Plan migration to alternative methods.")
                .impactScore(25)
                .build();
    }

    private String getServicePath(String packageName, String serviceName) {
        if (packageName == null || packageName.isEmpty()) {
            return "/" + serviceName;
        }
        return "/" + packageName + "." + serviceName;
    }

    private Map<String, ProtoService> mapByName(List<ProtoService> services) {
        Map<String, ProtoService> map = new LinkedHashMap<>();
        for (ProtoService service : services) {
            map.put(service.getName(), service);
        }
        return map;
    }

    private Map<String, ProtoRpcMethod> mapMethodsByName(List<ProtoRpcMethod> methods) {
        Map<String, ProtoRpcMethod> map = new LinkedHashMap<>();
        for (ProtoRpcMethod method : methods) {
            map.put(method.getName(), method);
        }
        return map;
    }
}

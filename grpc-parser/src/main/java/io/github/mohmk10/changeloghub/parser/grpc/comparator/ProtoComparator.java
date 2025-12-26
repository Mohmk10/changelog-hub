package io.github.mohmk10.changeloghub.parser.grpc.comparator;

import io.github.mohmk10.changeloghub.core.model.BreakingChange;
import io.github.mohmk10.changeloghub.core.model.ChangeCategory;
import io.github.mohmk10.changeloghub.core.model.ChangeType;
import io.github.mohmk10.changeloghub.core.model.Severity;
import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoFile;

import java.time.LocalDateTime;
import java.util.*;

public class ProtoComparator {

    private final ServiceComparator serviceComparator;
    private final MessageComparator messageComparator;

    public ProtoComparator() {
        this.serviceComparator = new ServiceComparator();
        this.messageComparator = new MessageComparator();
    }

    public ProtoComparator(ServiceComparator serviceComparator, MessageComparator messageComparator) {
        this.serviceComparator = serviceComparator;
        this.messageComparator = messageComparator;
    }

    public List<BreakingChange> compare(ProtoFile oldProto, ProtoFile newProto) {
        List<BreakingChange> changes = new ArrayList<>();

        if (!Objects.equals(oldProto.getPackageName(), newProto.getPackageName())) {
            changes.add(createPackageChangedChange(oldProto.getPackageName(), newProto.getPackageName()));
        }

        if (!Objects.equals(oldProto.getSyntax(), newProto.getSyntax())) {
            changes.add(createSyntaxChangedChange(oldProto.getSyntax(), newProto.getSyntax()));
        }

        changes.addAll(serviceComparator.compareServices(
                oldProto.getServices(),
                newProto.getServices(),
                newProto.getPackageName()
        ));

        changes.addAll(messageComparator.compareMessages(
                oldProto.getMessages(),
                newProto.getMessages(),
                newProto.getPackageName()
        ));

        changes.addAll(messageComparator.compareEnums(
                oldProto.getEnums(),
                newProto.getEnums(),
                newProto.getPackageName()
        ));

        return changes;
    }

    public Map<Severity, List<BreakingChange>> groupBySeverity(List<BreakingChange> changes) {
        Map<Severity, List<BreakingChange>> grouped = new EnumMap<>(Severity.class);

        for (Severity severity : Severity.values()) {
            grouped.put(severity, new ArrayList<>());
        }

        for (BreakingChange change : changes) {
            grouped.get(change.getSeverity()).add(change);
        }

        return grouped;
    }

    public Map<ChangeCategory, List<BreakingChange>> groupByCategory(List<BreakingChange> changes) {
        Map<ChangeCategory, List<BreakingChange>> grouped = new LinkedHashMap<>();

        for (BreakingChange change : changes) {
            grouped.computeIfAbsent(change.getCategory(), k -> new ArrayList<>()).add(change);
        }

        return grouped;
    }

    public Map<ChangeType, List<BreakingChange>> groupByType(List<BreakingChange> changes) {
        Map<ChangeType, List<BreakingChange>> grouped = new EnumMap<>(ChangeType.class);

        for (ChangeType type : ChangeType.values()) {
            grouped.put(type, new ArrayList<>());
        }

        for (BreakingChange change : changes) {
            grouped.get(change.getType()).add(change);
        }

        return grouped;
    }

    public boolean hasBreakingChanges(List<BreakingChange> changes) {
        return changes.stream().anyMatch(c -> c.getSeverity() == Severity.BREAKING);
    }

    public boolean hasDangerousChanges(List<BreakingChange> changes) {
        return changes.stream().anyMatch(c ->
                c.getSeverity() == Severity.BREAKING || c.getSeverity() == Severity.DANGEROUS);
    }

    public Map<String, Object> getStatistics(List<BreakingChange> changes) {
        Map<String, Object> stats = new LinkedHashMap<>();

        stats.put("totalChanges", changes.size());

        Map<Severity, Long> bySeverity = new EnumMap<>(Severity.class);
        for (Severity severity : Severity.values()) {
            long count = changes.stream().filter(c -> c.getSeverity() == severity).count();
            bySeverity.put(severity, count);
        }
        stats.put("bySeverity", bySeverity);

        Map<ChangeType, Long> byType = new EnumMap<>(ChangeType.class);
        for (ChangeType type : ChangeType.values()) {
            long count = changes.stream().filter(c -> c.getType() == type).count();
            byType.put(type, count);
        }
        stats.put("byType", byType);

        Map<ChangeCategory, Long> byCategory = new LinkedHashMap<>();
        for (BreakingChange change : changes) {
            byCategory.merge(change.getCategory(), 1L, Long::sum);
        }
        stats.put("byCategory", byCategory);

        double avgImpact = changes.stream()
                .mapToInt(BreakingChange::getImpactScore)
                .average()
                .orElse(0.0);
        stats.put("averageImpactScore", avgImpact);

        int maxImpact = changes.stream()
                .mapToInt(BreakingChange::getImpactScore)
                .max()
                .orElse(0);
        stats.put("maxImpactScore", maxImpact);

        return stats;
    }

    public List<BreakingChange> filterByMinSeverity(List<BreakingChange> changes, Severity minSeverity) {
        return changes.stream()
                .filter(c -> c.getSeverity().ordinal() <= minSeverity.ordinal())
                .toList();
    }

    public List<BreakingChange> filterByCategories(List<BreakingChange> changes, Set<ChangeCategory> categories) {
        return changes.stream()
                .filter(c -> categories.contains(c.getCategory()))
                .toList();
    }

    private BreakingChange createPackageChangedChange(String oldPackage, String newPackage) {
        String oldPkg = oldPackage != null ? oldPackage : "(empty)";
        String newPkg = newPackage != null ? newPackage : "(empty)";

        return BreakingChange.breakingChangeBuilder()
                .id(UUID.randomUUID().toString())
                .type(ChangeType.MODIFIED)
                .category(ChangeCategory.PACKAGE)
                .severity(Severity.BREAKING)
                .path("package")
                .description("Package changed from '" + oldPkg + "' to '" + newPkg + "'")
                .oldValue(oldPackage)
                .newValue(newPackage)
                .detectedAt(LocalDateTime.now())
                .migrationSuggestion("Package name changed. All gRPC service paths will change. " +
                        "Update all client configurations and service registrations.")
                .impactScore(95)
                .build();
    }

    private BreakingChange createSyntaxChangedChange(String oldSyntax, String newSyntax) {
        
        Severity severity;
        String migrationSuggestion;

        if ("proto2".equals(oldSyntax) && "proto3".equals(newSyntax)) {
            severity = Severity.WARNING;
            migrationSuggestion = "Upgraded from proto2 to proto3. " +
                    "Required fields are no longer enforced. Default values behavior changed.";
        } else if ("proto3".equals(oldSyntax) && "proto2".equals(newSyntax)) {
            severity = Severity.BREAKING;
            migrationSuggestion = "Downgraded from proto3 to proto2. " +
                    "This is unusual and may indicate a configuration issue.";
        } else {
            severity = Severity.WARNING;
            migrationSuggestion = "Syntax version changed. Review field semantics.";
        }

        return BreakingChange.breakingChangeBuilder()
                .id(UUID.randomUUID().toString())
                .type(ChangeType.MODIFIED)
                .category(ChangeCategory.SCHEMA)
                .severity(severity)
                .path("syntax")
                .description("Syntax changed from '" + oldSyntax + "' to '" + newSyntax + "'")
                .oldValue(oldSyntax)
                .newValue(newSyntax)
                .detectedAt(LocalDateTime.now())
                .migrationSuggestion(migrationSuggestion)
                .impactScore(severity == Severity.BREAKING ? 80 : 40)
                .build();
    }
}

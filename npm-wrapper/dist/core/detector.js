"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.detectBreakingChanges = detectBreakingChanges;
exports.detectBreakingChangesFromSpecs = detectBreakingChangesFromSpecs;
exports.hasBreakingChanges = hasBreakingChanges;
exports.getBreakingChangesSummary = getBreakingChangesSummary;
const parser_1 = require("./parser");
const comparator_1 = require("./comparator");
const file_1 = require("../utils/file");
function detectBreakingChanges(oldSpecPath, newSpecPath, options = {}) {
    if (!(0, file_1.fileExists)(oldSpecPath)) {
        throw new Error(`File not found: ${oldSpecPath}`);
    }
    if (!(0, file_1.fileExists)(newSpecPath)) {
        throw new Error(`File not found: ${newSpecPath}`);
    }
    const oldContent = (0, file_1.readFile)(oldSpecPath);
    const newContent = (0, file_1.readFile)(newSpecPath);
    const oldSpec = (0, parser_1.parseSpec)(oldContent, oldSpecPath);
    const newSpec = (0, parser_1.parseSpec)(newContent, newSpecPath);
    let result = (0, comparator_1.compareSpecs)(oldSpec, newSpec);
    if (options.severityThreshold) {
        result = filterBySeverity(result, options.severityThreshold);
    }
    if (options.includeDeprecations === false) {
        result = {
            ...result,
            changes: result.changes.filter((c) => c.type !== 'DEPRECATED'),
        };
    }
    return result;
}
function detectBreakingChangesFromSpecs(oldSpec, newSpec, options = {}) {
    let result = (0, comparator_1.compareSpecs)(oldSpec, newSpec);
    if (options.severityThreshold) {
        result = filterBySeverity(result, options.severityThreshold);
    }
    if (options.includeDeprecations === false) {
        result = {
            ...result,
            changes: result.changes.filter((c) => c.type !== 'DEPRECATED'),
        };
    }
    return result;
}
function filterBySeverity(result, threshold) {
    const severityOrder = {
        INFO: 0,
        WARNING: 1,
        DANGEROUS: 2,
        BREAKING: 3,
    };
    const thresholdLevel = severityOrder[threshold] ?? 0;
    const filteredChanges = result.changes.filter((change) => {
        const changeLevel = severityOrder[change.severity] ?? 0;
        return changeLevel >= thresholdLevel;
    });
    return {
        ...result,
        changes: filteredChanges,
        totalChanges: filteredChanges.length,
    };
}
function hasBreakingChanges(oldSpecPath, newSpecPath) {
    const result = detectBreakingChanges(oldSpecPath, newSpecPath);
    return result.breakingChanges.length > 0;
}
function getBreakingChangesSummary(oldSpecPath, newSpecPath) {
    const result = detectBreakingChanges(oldSpecPath, newSpecPath);
    return {
        count: result.breakingChanges.length,
        riskLevel: result.riskLevel,
        recommendation: result.semverRecommendation,
        changes: result.breakingChanges.map((c) => c.description),
    };
}
//# sourceMappingURL=detector.js.map
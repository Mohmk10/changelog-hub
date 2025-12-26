"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.generateReport = generateReport;
exports.generateShortSummary = generateShortSummary;
const chalk_1 = __importDefault(require("chalk"));
function generateReport(result, format) {
    switch (format) {
        case 'json':
            return generateJsonReport(result);
        case 'markdown':
            return generateMarkdownReport(result);
        case 'html':
            return generateHtmlReport(result);
        case 'console':
        default:
            return generateConsoleReport(result);
    }
}
function generateJsonReport(result) {
    const report = {
        metadata: {
            generatedAt: new Date().toISOString(),
            apiName: result.apiName,
            fromVersion: result.fromVersion,
            toVersion: result.toVersion,
        },
        summary: {
            totalChanges: result.totalChanges,
            breakingChanges: result.breakingChanges.length,
            riskScore: result.riskScore,
            riskLevel: result.riskLevel,
            semverRecommendation: result.semverRecommendation,
            ...result.summary,
        },
        breakingChanges: result.breakingChanges.map((c) => ({
            type: c.type,
            category: c.category,
            path: c.path,
            description: c.description,
            migrationSuggestion: c.migrationSuggestion,
            impactScore: c.impactScore,
        })),
        changes: result.changes.map((c) => ({
            type: c.type,
            category: c.category,
            severity: c.severity,
            path: c.path,
            description: c.description,
            oldValue: c.oldValue,
            newValue: c.newValue,
        })),
    };
    return JSON.stringify(report, null, 2);
}
function generateMarkdownReport(result) {
    const lines = [];
    lines.push('# API Changelog');
    lines.push('');
    lines.push(`**${result.apiName}** v${result.fromVersion} → v${result.toVersion}`);
    lines.push('');
    lines.push('## Summary');
    lines.push('');
    lines.push('| Metric | Value |');
    lines.push('|--------|-------|');
    lines.push(`| Total Changes | ${result.totalChanges} |`);
    lines.push(`| Breaking Changes | ${result.breakingChanges.length} |`);
    lines.push(`| Risk Level | ${result.riskLevel} |`);
    lines.push(`| Risk Score | ${result.riskScore}/100 |`);
    lines.push(`| Recommended Bump | **${result.semverRecommendation}** |`);
    lines.push('');
    if (result.breakingChanges.length > 0) {
        lines.push('## Breaking Changes');
        lines.push('');
        lines.push('> **Warning:** These changes may break existing clients.');
        lines.push('');
        for (const change of result.breakingChanges) {
            lines.push(`### \`${change.path}\``);
            lines.push('');
            lines.push(`- **Type:** ${change.type}`);
            lines.push(`- **Description:** ${change.description}`);
            lines.push(`- **Impact Score:** ${change.impactScore}/100`);
            lines.push(`- **Migration:** ${change.migrationSuggestion}`);
            lines.push('');
        }
    }
    const dangerousChanges = result.changes.filter((c) => c.severity === 'DANGEROUS');
    if (dangerousChanges.length > 0) {
        lines.push('## Dangerous Changes');
        lines.push('');
        for (const change of dangerousChanges) {
            lines.push(`- **${change.path}**: ${change.description}`);
        }
        lines.push('');
    }
    const warnings = result.changes.filter((c) => c.severity === 'WARNING');
    if (warnings.length > 0) {
        lines.push('## Warnings');
        lines.push('');
        for (const change of warnings) {
            lines.push(`- **${change.path}**: ${change.description}`);
        }
        lines.push('');
    }
    const additions = result.changes.filter((c) => c.type === 'ADDED' && c.severity === 'INFO');
    if (additions.length > 0) {
        lines.push('## Additions');
        lines.push('');
        for (const change of additions) {
            lines.push(`- ${change.description}`);
        }
        lines.push('');
    }
    lines.push('---');
    lines.push(`*Generated at ${new Date().toISOString()} by Changelog Hub*`);
    return lines.join('\n');
}
function generateConsoleReport(result) {
    const lines = [];
    lines.push('');
    lines.push(chalk_1.default.bold.blue('═'.repeat(60)));
    lines.push(chalk_1.default.bold.blue('  API CHANGELOG'));
    lines.push(chalk_1.default.bold.blue('═'.repeat(60)));
    lines.push('');
    lines.push(chalk_1.default.bold(`  ${result.apiName}`));
    lines.push(`  ${chalk_1.default.gray('v' + result.fromVersion)} ${chalk_1.default.yellow('→')} ${chalk_1.default.green('v' + result.toVersion)}`);
    lines.push('');
    lines.push(chalk_1.default.gray('─'.repeat(60)));
    lines.push(chalk_1.default.bold('\n  SUMMARY\n'));
    lines.push(`  Total Changes:       ${result.totalChanges}`);
    lines.push(`  Breaking Changes:    ${chalk_1.default.red(result.breakingChanges.length.toString())}`);
    lines.push(`  Risk Level:          ${formatRiskLevel(result.riskLevel)}`);
    lines.push(`  Risk Score:          ${formatRiskScore(result.riskScore)}`);
    lines.push(`  Recommended Bump:    ${chalk_1.default.bold(result.semverRecommendation)}`);
    lines.push('');
    if (result.breakingChanges.length > 0) {
        lines.push(chalk_1.default.gray('─'.repeat(60)));
        lines.push(chalk_1.default.red.bold('\n  BREAKING CHANGES\n'));
        for (const change of result.breakingChanges) {
            lines.push(chalk_1.default.red(`  • ${change.path}`));
            lines.push(chalk_1.default.white(`    ${change.description}`));
            lines.push(chalk_1.default.gray(`    Migration: ${change.migrationSuggestion}`));
            lines.push('');
        }
    }
    const dangerousChanges = result.changes.filter((c) => c.severity === 'DANGEROUS');
    if (dangerousChanges.length > 0) {
        lines.push(chalk_1.default.gray('─'.repeat(60)));
        lines.push(chalk_1.default.yellow.bold('\n  DANGEROUS CHANGES\n'));
        for (const change of dangerousChanges) {
            lines.push(chalk_1.default.yellow(`  • ${change.description}`));
        }
        lines.push('');
    }
    const warnings = result.changes.filter((c) => c.severity === 'WARNING');
    if (warnings.length > 0) {
        lines.push(chalk_1.default.gray('─'.repeat(60)));
        lines.push(chalk_1.default.yellow.bold('\n  WARNINGS\n'));
        for (const change of warnings) {
            lines.push(chalk_1.default.yellow(`  • ${change.description}`));
        }
        lines.push('');
    }
    const additions = result.changes.filter((c) => c.type === 'ADDED' && c.severity === 'INFO');
    if (additions.length > 0) {
        lines.push(chalk_1.default.gray('─'.repeat(60)));
        lines.push(chalk_1.default.green.bold('\n  ADDITIONS\n'));
        for (const change of additions) {
            lines.push(chalk_1.default.green(`  • ${change.description}`));
        }
        lines.push('');
    }
    lines.push(chalk_1.default.bold.blue('═'.repeat(60)));
    lines.push('');
    return lines.join('\n');
}
function generateHtmlReport(result) {
    const breakingRows = result.breakingChanges
        .map((c) => `
      <tr class="breaking">
        <td><code>${escapeHtml(c.path)}</code></td>
        <td>${c.type}</td>
        <td>${escapeHtml(c.description)}</td>
        <td>${c.impactScore}/100</td>
        <td>${escapeHtml(c.migrationSuggestion)}</td>
      </tr>`)
        .join('');
    const otherRows = result.changes
        .filter((c) => c.severity !== 'BREAKING')
        .map((c) => `
      <tr class="${c.severity.toLowerCase()}">
        <td><code>${escapeHtml(c.path)}</code></td>
        <td>${c.type}</td>
        <td>${c.severity}</td>
        <td>${escapeHtml(c.description)}</td>
      </tr>`)
        .join('');
    return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>API Changelog - ${result.apiName}</title>
  <style>
    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; margin: 40px; }
    h1 { color: #333; }
    .summary { background: #f5f5f5; padding: 20px; border-radius: 8px; margin: 20px 0; }
    .summary-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: 15px; }
    .metric { text-align: center; }
    .metric-value { font-size: 24px; font-weight: bold; }
    .metric-label { color: #666; font-size: 14px; }
    table { width: 100%; border-collapse: collapse; margin: 20px 0; }
    th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
    th { background: #f0f0f0; font-weight: 600; }
    tr.breaking { background: #fff0f0; }
    tr.dangerous { background: #fff8e1; }
    tr.warning { background: #fffde7; }
    tr.info { background: #e3f2fd; }
    code { background: #f5f5f5; padding: 2px 6px; border-radius: 4px; }
    .risk-low { color: #4caf50; }
    .risk-medium { color: #ff9800; }
    .risk-high { color: #f44336; }
    .risk-critical { color: #b71c1c; font-weight: bold; }
  </style>
</head>
<body>
  <h1>API Changelog</h1>
  <p><strong>${result.apiName}</strong> v${result.fromVersion} → v${result.toVersion}</p>

  <div class="summary">
    <div class="summary-grid">
      <div class="metric">
        <div class="metric-value">${result.totalChanges}</div>
        <div class="metric-label">Total Changes</div>
      </div>
      <div class="metric">
        <div class="metric-value" style="color: #f44336;">${result.breakingChanges.length}</div>
        <div class="metric-label">Breaking</div>
      </div>
      <div class="metric">
        <div class="metric-value ${getRiskClass(result.riskLevel)}">${result.riskLevel}</div>
        <div class="metric-label">Risk Level</div>
      </div>
      <div class="metric">
        <div class="metric-value">${result.riskScore}/100</div>
        <div class="metric-label">Risk Score</div>
      </div>
      <div class="metric">
        <div class="metric-value">${result.semverRecommendation}</div>
        <div class="metric-label">Recommended</div>
      </div>
    </div>
  </div>

  ${result.breakingChanges.length > 0 ? `
  <h2>Breaking Changes</h2>
  <table>
    <thead>
      <tr><th>Path</th><th>Type</th><th>Description</th><th>Impact</th><th>Migration</th></tr>
    </thead>
    <tbody>${breakingRows}</tbody>
  </table>
  ` : ''}

  ${otherRows ? `
  <h2>Other Changes</h2>
  <table>
    <thead>
      <tr><th>Path</th><th>Type</th><th>Severity</th><th>Description</th></tr>
    </thead>
    <tbody>${otherRows}</tbody>
  </table>
  ` : ''}

  <footer style="margin-top: 40px; color: #666; font-size: 12px;">
    Generated at ${new Date().toISOString()} by Changelog Hub
  </footer>
</body>
</html>`;
}
function formatRiskLevel(level) {
    switch (level) {
        case 'CRITICAL':
            return chalk_1.default.bgRed.white(' CRITICAL ');
        case 'HIGH':
            return chalk_1.default.red('HIGH');
        case 'MEDIUM':
            return chalk_1.default.yellow('MEDIUM');
        case 'LOW':
            return chalk_1.default.green('LOW');
        default:
            return level;
    }
}
function formatRiskScore(score) {
    if (score >= 75)
        return chalk_1.default.red(`${score}/100`);
    if (score >= 50)
        return chalk_1.default.yellow(`${score}/100`);
    if (score >= 25)
        return chalk_1.default.cyan(`${score}/100`);
    return chalk_1.default.green(`${score}/100`);
}
function getRiskClass(level) {
    return `risk-${level.toLowerCase()}`;
}
function escapeHtml(text) {
    return text
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}
/**
 * Generate a short summary string
 */
function generateShortSummary(result) {
    const parts = [];
    if (result.breakingChanges.length > 0) {
        parts.push(`${result.breakingChanges.length} breaking`);
    }
    const additions = result.changes.filter((c) => c.type === 'ADDED').length;
    if (additions > 0) {
        parts.push(`${additions} added`);
    }
    const modifications = result.changes.filter((c) => c.type === 'MODIFIED').length;
    if (modifications > 0) {
        parts.push(`${modifications} modified`);
    }
    const removals = result.changes.filter((c) => c.type === 'REMOVED').length;
    if (removals > 0) {
        parts.push(`${removals} removed`);
    }
    if (parts.length === 0) {
        return 'No API changes detected';
    }
    return `API changes: ${parts.join(', ')}`;
}
//# sourceMappingURL=reporter.js.map
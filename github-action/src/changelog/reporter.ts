import { ComparisonResult } from './comparator';
import { Change } from './detector';
import { ApiSpec } from './parser';

export interface ReportOptions {
  
  oldSpec: ApiSpec;
  
  newSpec: ApiSpec;
  
  includeMigration?: boolean;
  
  includeTimestamp?: boolean;
}

export function generateReport(
  result: ComparisonResult,
  format: string,
  options: ReportOptions
): string {
  switch (format) {
    case 'json':
      return generateJsonReport(result, options);
    case 'markdown':
      return generateMarkdownReport(result, options);
    case 'console':
    default:
      return generateConsoleReport(result, options);
  }
}

function generateJsonReport(result: ComparisonResult, options: ReportOptions): string {
  const report = {
    metadata: {
      generatedAt: new Date().toISOString(),
      oldVersion: {
        name: options.oldSpec.name,
        version: options.oldSpec.version,
      },
      newVersion: {
        name: options.newSpec.name,
        version: options.newSpec.version,
      },
    },
    summary: {
      totalChanges: result.changes.length,
      breakingChanges: result.breakingChanges.length,
      riskScore: result.riskScore,
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

function generateMarkdownReport(result: ComparisonResult, options: ReportOptions): string {
  const lines: string[] = [];

  lines.push('# API Changelog');
  lines.push('');

  lines.push(`**${options.oldSpec.name}** v${options.oldSpec.version} → v${options.newSpec.version}`);
  lines.push('');

  lines.push('## Summary');
  lines.push('');
  lines.push('| Metric | Value |');
  lines.push('|--------|-------|');
  lines.push(`| Total Changes | ${result.changes.length} |`);
  lines.push(`| Breaking Changes | ${result.breakingChanges.length} |`);
  lines.push(`| Risk Score | ${result.riskScore}/100 |`);
  lines.push(`| Endpoints Added | ${result.summary.endpointsAdded} |`);
  lines.push(`| Endpoints Removed | ${result.summary.endpointsRemoved} |`);
  lines.push(`| Endpoints Modified | ${result.summary.endpointsModified} |`);
  lines.push(`| Schemas Modified | ${result.summary.schemasModified} |`);
  lines.push('');

  if (result.breakingChanges.length > 0) {
    lines.push('## 🔴 Breaking Changes');
    lines.push('');
    lines.push('> **Warning:** These changes may break existing clients.');
    lines.push('');

    const groupedBreaking = groupByCategory(result.breakingChanges);
    for (const [category, changes] of Object.entries(groupedBreaking)) {
      lines.push(`### ${formatCategory(category)}`);
      lines.push('');
      for (const change of changes) {
        lines.push(`#### \`${change.path}\``);
        lines.push('');
        lines.push(`- **Type:** ${change.type}`);
        lines.push(`- **Description:** ${change.description}`);
        lines.push(`- **Impact Score:** ${change.impactScore}/100`);
        lines.push(`- **Migration:** ${change.migrationSuggestion}`);
        if (change.oldValue) {
          lines.push(`- **Old Value:** \`${change.oldValue}\``);
        }
        if (change.newValue) {
          lines.push(`- **New Value:** \`${change.newValue}\``);
        }
        lines.push('');
      }
    }
  }

  const dangerousChanges = result.changes.filter((c) => c.severity === 'DANGEROUS');
  if (dangerousChanges.length > 0) {
    lines.push('## 🟠 Dangerous Changes');
    lines.push('');
    lines.push('> These changes may cause issues in some cases.');
    lines.push('');
    for (const change of dangerousChanges) {
      lines.push(`- **${change.path}**: ${change.description}`);
    }
    lines.push('');
  }

  const warnings = result.changes.filter((c) => c.severity === 'WARNING');
  if (warnings.length > 0) {
    lines.push('## 🟡 Deprecations & Warnings');
    lines.push('');
    for (const change of warnings) {
      lines.push(`- **${change.path}**: ${change.description}`);
    }
    lines.push('');
  }

  const additions = result.changes.filter((c) => c.type === 'ADDED' && c.severity === 'INFO');
  if (additions.length > 0) {
    lines.push('## 🟢 Additions');
    lines.push('');
    const groupedAdditions = groupByCategory(additions);
    for (const [category, changes] of Object.entries(groupedAdditions)) {
      lines.push(`### ${formatCategory(category)}`);
      lines.push('');
      for (const change of changes) {
        lines.push(`- ${change.description}`);
      }
      lines.push('');
    }
  }

  const modifications = result.changes.filter(
    (c) => c.type === 'MODIFIED' && c.severity === 'INFO'
  );
  if (modifications.length > 0) {
    lines.push('## 📝 Other Modifications');
    lines.push('');
    for (const change of modifications) {
      lines.push(`- **${change.path}**: ${change.description}`);
    }
    lines.push('');
  }

  if (options.includeTimestamp !== false) {
    lines.push('---');
    lines.push(`*Generated at ${new Date().toISOString()} by Changelog Hub*`);
  }

  return lines.join('\n');
}

function generateConsoleReport(result: ComparisonResult, options: ReportOptions): string {
  const lines: string[] = [];
  const separator = '═'.repeat(60);
  const thinSeparator = '─'.repeat(60);

  lines.push(separator);
  lines.push('  API CHANGELOG');
  lines.push(separator);
  lines.push('');
  lines.push(`  ${options.oldSpec.name}`);
  lines.push(`  v${options.oldSpec.version} → v${options.newSpec.version}`);
  lines.push('');
  lines.push(thinSeparator);

  lines.push('  SUMMARY');
  lines.push(thinSeparator);
  lines.push(`  Total Changes:       ${result.changes.length}`);
  lines.push(`  Breaking Changes:    ${result.breakingChanges.length}`);
  lines.push(`  Risk Score:          ${result.riskScore}/100`);
  lines.push('');

  if (result.breakingChanges.length > 0) {
    lines.push(thinSeparator);
    lines.push('  🔴 BREAKING CHANGES');
    lines.push(thinSeparator);
    for (const change of result.breakingChanges) {
      lines.push(`  • ${change.path}`);
      lines.push(`    ${change.description}`);
      lines.push(`    Migration: ${change.migrationSuggestion}`);
      lines.push('');
    }
  }

  const dangerousChanges = result.changes.filter((c) => c.severity === 'DANGEROUS');
  if (dangerousChanges.length > 0) {
    lines.push(thinSeparator);
    lines.push('  🟠 DANGEROUS CHANGES');
    lines.push(thinSeparator);
    for (const change of dangerousChanges) {
      lines.push(`  • ${change.description}`);
    }
    lines.push('');
  }

  const warnings = result.changes.filter((c) => c.severity === 'WARNING');
  if (warnings.length > 0) {
    lines.push(thinSeparator);
    lines.push('  🟡 WARNINGS');
    lines.push(thinSeparator);
    for (const change of warnings) {
      lines.push(`  • ${change.description}`);
    }
    lines.push('');
  }

  const additions = result.changes.filter((c) => c.type === 'ADDED' && c.severity === 'INFO');
  if (additions.length > 0) {
    lines.push(thinSeparator);
    lines.push('  🟢 ADDITIONS');
    lines.push(thinSeparator);
    for (const change of additions) {
      lines.push(`  • ${change.description}`);
    }
    lines.push('');
  }

  lines.push(separator);

  return lines.join('\n');
}

function groupByCategory<T extends Change>(changes: T[]): Record<string, T[]> {
  const grouped: Record<string, T[]> = {};
  for (const change of changes) {
    if (!grouped[change.category]) {
      grouped[change.category] = [];
    }
    grouped[change.category].push(change);
  }
  return grouped;
}

function formatCategory(category: string): string {
  const categoryMap: Record<string, string> = {
    ENDPOINT: 'Endpoints',
    PARAMETER: 'Parameters',
    REQUEST_BODY: 'Request Bodies',
    RESPONSE: 'Responses',
    SCHEMA: 'Schemas',
    SCHEMA_PROPERTY: 'Schema Properties',
    SECURITY: 'Security',
  };
  return categoryMap[category] || category;
}

export function generateShortSummary(result: ComparisonResult): string {
  const parts: string[] = [];

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

export function generateReleaseNotes(result: ComparisonResult, options: ReportOptions): string {
  const lines: string[] = [];

  lines.push(`## API Changes (v${options.newSpec.version})`);
  lines.push('');

  if (result.breakingChanges.length > 0) {
    lines.push('### Breaking Changes');
    lines.push('');
    for (const change of result.breakingChanges) {
      lines.push(`- ${change.description}`);
    }
    lines.push('');
  }

  const additions = result.changes.filter((c) => c.type === 'ADDED' && c.severity === 'INFO');
  if (additions.length > 0) {
    lines.push('### New Features');
    lines.push('');
    for (const change of additions) {
      lines.push(`- ${change.description}`);
    }
    lines.push('');
  }

  const deprecations = result.changes.filter((c) => c.type === 'DEPRECATED');
  if (deprecations.length > 0) {
    lines.push('### Deprecations');
    lines.push('');
    for (const change of deprecations) {
      lines.push(`- ${change.description}`);
    }
    lines.push('');
  }

  return lines.join('\n');
}

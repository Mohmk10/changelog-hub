import * as core from '@actions/core';
import { ChangelogResult } from './changelog/detector';

/**
 * Output keys for the GitHub Action
 */
export enum OutputKey {
  HAS_BREAKING_CHANGES = 'has-breaking-changes',
  BREAKING_CHANGES_COUNT = 'breaking-changes-count',
  TOTAL_CHANGES_COUNT = 'total-changes-count',
  RISK_LEVEL = 'risk-level',
  RISK_SCORE = 'risk-score',
  SEMVER_RECOMMENDATION = 'semver-recommendation',
  CHANGELOG = 'changelog',
  CHANGELOG_FILE = 'changelog-file',
  SUMMARY = 'summary',
}

/**
 * Sets all output values from the changelog result.
 * These outputs can be used by subsequent steps in the workflow.
 *
 * @param result - The changelog detection result
 */
export function setOutputs(result: ChangelogResult): void {
  core.setOutput(OutputKey.HAS_BREAKING_CHANGES, result.hasBreakingChanges.toString());
  core.setOutput(OutputKey.BREAKING_CHANGES_COUNT, result.breakingChangesCount.toString());
  core.setOutput(OutputKey.TOTAL_CHANGES_COUNT, result.totalChangesCount.toString());
  core.setOutput(OutputKey.RISK_LEVEL, result.riskLevel);
  core.setOutput(OutputKey.RISK_SCORE, result.riskScore.toString());
  core.setOutput(OutputKey.SEMVER_RECOMMENDATION, result.semverRecommendation);
  core.setOutput(OutputKey.CHANGELOG, result.changelog);

  if (result.changelogFile) {
    core.setOutput(OutputKey.CHANGELOG_FILE, result.changelogFile);
  }

  core.setOutput(OutputKey.SUMMARY, generateSummary(result));
}

/**
 * Generates a brief summary of the changes
 */
function generateSummary(result: ChangelogResult): string {
  const parts: string[] = [];

  if (result.hasBreakingChanges) {
    parts.push(`${result.breakingChangesCount} breaking change(s)`);
  }

  const nonBreakingCount = result.totalChangesCount - result.breakingChangesCount;
  if (nonBreakingCount > 0) {
    parts.push(`${nonBreakingCount} non-breaking change(s)`);
  }

  if (parts.length === 0) {
    return 'No changes detected';
  }

  return `${parts.join(', ')}. Risk: ${result.riskLevel}. Recommended: ${result.semverRecommendation}`;
}

/**
 * Writes the changelog to the GitHub Actions summary
 */
export async function writeSummary(result: ChangelogResult): Promise<void> {
  const summary = core.summary
    .addHeading('Changelog Hub Results', 2)
    .addTable([
      [
        { data: 'Metric', header: true },
        { data: 'Value', header: true },
      ],
      ['Total Changes', result.totalChangesCount.toString()],
      ['Breaking Changes', result.breakingChangesCount.toString()],
      ['Risk Level', result.riskLevel],
      ['Risk Score', `${result.riskScore}/100`],
      ['Recommended Bump', result.semverRecommendation],
    ]);

  if (result.hasBreakingChanges) {
    summary.addHeading('Breaking Changes', 3);
    for (const change of result.breakingChanges) {
      summary.addRaw(`- **${change.path}**: ${change.description}\n`);
    }
  }

  summary.addDetails('Full Changelog', result.changelog);

  await summary.write();
}

/**
 * Logs the result summary to the action output
 */
export function logSummary(result: ChangelogResult): void {
  core.info('');
  core.info('='.repeat(60));
  core.info('CHANGELOG HUB RESULTS');
  core.info('='.repeat(60));
  core.info(`Total changes:        ${result.totalChangesCount}`);
  core.info(`Breaking changes:     ${result.breakingChangesCount}`);
  core.info(`Risk level:           ${result.riskLevel}`);
  core.info(`Risk score:           ${result.riskScore}/100`);
  core.info(`Recommended bump:     ${result.semverRecommendation}`);
  core.info('='.repeat(60));
  core.info('');
}

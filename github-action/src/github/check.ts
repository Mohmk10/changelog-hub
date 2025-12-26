import * as github from '@actions/github';
import { ChangelogResult } from '../changelog/detector';
import { Logger } from '../utils/logger';

const logger = new Logger('CheckRun');

/** Name of the check run */
const CHECK_RUN_NAME = 'Changelog Hub';

/**
 * Creates a new GitHub Check Run with changelog results.
 *
 * @param token - GitHub token for API access
 * @param result - Changelog detection results
 * @returns Check run ID
 */
export async function createCheckRun(token: string, result: ChangelogResult): Promise<number> {
  const octokit = github.getOctokit(token);
  const { owner, repo } = github.context.repo;
  const sha = github.context.sha;

  const conclusion = getConclusion(result);
  const { title, summary, text } = formatCheckOutput(result);

  const { data } = await octokit.rest.checks.create({
    owner,
    repo,
    name: CHECK_RUN_NAME,
    head_sha: sha,
    status: 'completed',
    conclusion,
    output: {
      title,
      summary,
      text,
      annotations: generateAnnotations(result),
    },
  });

  logger.info(`Created check run #${data.id} with conclusion: ${conclusion}`);
  return data.id;
}

/**
 * Updates an existing GitHub Check Run.
 *
 * @param token - GitHub token for API access
 * @param checkRunId - ID of the check run to update
 * @param result - Changelog detection results
 */
export async function updateCheckRun(
  token: string,
  checkRunId: number,
  result: ChangelogResult
): Promise<void> {
  const octokit = github.getOctokit(token);
  const { owner, repo } = github.context.repo;

  const conclusion = getConclusion(result);
  const { title, summary, text } = formatCheckOutput(result);

  await octokit.rest.checks.update({
    owner,
    repo,
    check_run_id: checkRunId,
    status: 'completed',
    conclusion,
    output: {
      title,
      summary,
      text,
      annotations: generateAnnotations(result),
    },
  });

  logger.info(`Updated check run #${checkRunId}`);
}

/**
 * Creates an in-progress check run.
 *
 * @param token - GitHub token for API access
 * @returns Check run ID
 */
export async function createInProgressCheckRun(token: string): Promise<number> {
  const octokit = github.getOctokit(token);
  const { owner, repo } = github.context.repo;
  const sha = github.context.sha;

  const { data } = await octokit.rest.checks.create({
    owner,
    repo,
    name: CHECK_RUN_NAME,
    head_sha: sha,
    status: 'in_progress',
    output: {
      title: 'Analyzing API changes...',
      summary: 'Changelog Hub is detecting breaking changes in your API specifications.',
    },
  });

  logger.info(`Created in-progress check run #${data.id}`);
  return data.id;
}

/**
 * Finds an existing Changelog Hub check run for the current SHA.
 *
 * @param token - GitHub token for API access
 * @returns Check run ID if found, null otherwise
 */
export async function findExistingCheckRun(token: string): Promise<number | null> {
  const octokit = github.getOctokit(token);
  const { owner, repo } = github.context.repo;
  const sha = github.context.sha;

  const { data } = await octokit.rest.checks.listForRef({
    owner,
    repo,
    ref: sha,
  });

  const existingCheck = data.check_runs.find((c) => c.name === CHECK_RUN_NAME);
  return existingCheck?.id ?? null;
}

/**
 * Determines the check conclusion based on results.
 */
function getConclusion(
  result: ChangelogResult
): 'success' | 'failure' | 'neutral' | 'cancelled' | 'skipped' {
  if (result.hasBreakingChanges) {
    // Allow action.ts to decide whether to actually fail
    // Here we mark as failure for visibility
    return 'failure';
  }

  if (result.totalChangesCount === 0) {
    return 'neutral';
  }

  return 'success';
}

/**
 * Formats the check run output.
 */
function formatCheckOutput(result: ChangelogResult): {
  title: string;
  summary: string;
  text: string;
} {
  // Title
  let title: string;
  if (result.hasBreakingChanges) {
    title = `${result.breakingChangesCount} breaking change(s) detected`;
  } else if (result.totalChangesCount === 0) {
    title = 'No API changes detected';
  } else {
    title = `${result.totalChangesCount} API change(s), no breaking changes`;
  }

  // Summary
  const summaryLines: string[] = [
    '## API Change Analysis',
    '',
    '| Metric | Value |',
    '|--------|-------|',
    `| Total Changes | ${result.totalChangesCount} |`,
    `| Breaking Changes | ${result.breakingChangesCount} |`,
    `| Risk Level | ${result.riskLevel} |`,
    `| Risk Score | ${result.riskScore}/100 |`,
    `| Recommended Bump | ${result.semverRecommendation} |`,
    '',
  ];

  if (result.hasBreakingChanges) {
    summaryLines.push('### Breaking Changes');
    summaryLines.push('');
    for (const change of result.breakingChanges.slice(0, 5)) {
      summaryLines.push(`- **${change.path}**: ${change.description}`);
    }
    if (result.breakingChanges.length > 5) {
      summaryLines.push(`- ... and ${result.breakingChanges.length - 5} more`);
    }
    summaryLines.push('');
  }

  // Text (detailed changelog)
  const text = result.changelog;

  return {
    title,
    summary: summaryLines.join('\n'),
    text,
  };
}

/**
 * Generates annotations for the check run.
 * Annotations appear inline in the diff view.
 */
function generateAnnotations(
  result: ChangelogResult
): Array<{
  path: string;
  start_line: number;
  end_line: number;
  annotation_level: 'notice' | 'warning' | 'failure';
  message: string;
  title: string;
}> {
  const annotations: Array<{
    path: string;
    start_line: number;
    end_line: number;
    annotation_level: 'notice' | 'warning' | 'failure';
    message: string;
    title: string;
  }> = [];

  // Only create annotations for breaking changes
  // Note: We can't determine exact line numbers without more context
  // This is a simplified version - a full implementation would need to
  // correlate changes back to specific file locations

  for (const change of result.breakingChanges.slice(0, 50)) {
    // GitHub limits to 50 annotations
    const pathParts = change.path.split(' ');
    const method = pathParts[0];
    const endpoint = pathParts[1] || change.path;

    annotations.push({
      path: 'api/openapi.yaml', // Would need actual file path
      start_line: 1,
      end_line: 1,
      annotation_level: 'failure',
      message: `${change.description}\n\nMigration: ${change.migrationSuggestion}`,
      title: `Breaking Change: ${method} ${endpoint}`,
    });
  }

  return annotations;
}

/**
 * Creates a summary check run for multiple spec analyses.
 */
export async function createSummaryCheckRun(
  token: string,
  results: ChangelogResult[]
): Promise<number> {
  const octokit = github.getOctokit(token);
  const { owner, repo } = github.context.repo;
  const sha = github.context.sha;

  const totalBreaking = results.reduce((sum, r) => sum + r.breakingChangesCount, 0);
  const totalChanges = results.reduce((sum, r) => sum + r.totalChangesCount, 0);
  const maxRiskScore = Math.max(...results.map((r) => r.riskScore));

  const conclusion = totalBreaking > 0 ? 'failure' : 'success';

  const summaryLines: string[] = [
    '## Multi-Spec API Analysis',
    '',
    `Analyzed ${results.length} API specification(s)`,
    '',
    '| Spec | Changes | Breaking | Risk |',
    '|------|---------|----------|------|',
  ];

  for (const result of results) {
    summaryLines.push(
      `| ${result.oldSpec.name} | ${result.totalChangesCount} | ${result.breakingChangesCount} | ${result.riskLevel} |`
    );
  }

  summaryLines.push('');
  summaryLines.push('### Totals');
  summaryLines.push(`- **Total Changes:** ${totalChanges}`);
  summaryLines.push(`- **Breaking Changes:** ${totalBreaking}`);
  summaryLines.push(`- **Max Risk Score:** ${maxRiskScore}/100`);

  const { data } = await octokit.rest.checks.create({
    owner,
    repo,
    name: `${CHECK_RUN_NAME} (Summary)`,
    head_sha: sha,
    status: 'completed',
    conclusion,
    output: {
      title:
        totalBreaking > 0
          ? `${totalBreaking} breaking change(s) across ${results.length} specs`
          : `${totalChanges} change(s) across ${results.length} specs`,
      summary: summaryLines.join('\n'),
    },
  });

  logger.info(`Created summary check run #${data.id}`);
  return data.id;
}

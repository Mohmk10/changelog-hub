import * as core from '@actions/core';
import * as github from '@actions/github';
import { getInputs, validateInputs } from './inputs';
import { setOutputs, writeSummary, logSummary } from './outputs';
import { detectBreakingChanges } from './changelog/detector';
import { postPrComment, updatePrComment } from './github/comment';
import { createCheckRun, updateCheckRun } from './github/check';
import { Logger } from './utils/logger';

/**
 * Main action runner.
 * Orchestrates the detection of breaking changes and reporting.
 */
export async function run(): Promise<void> {
  const logger = new Logger('Action');

  // Parse and validate inputs
  logger.info('Parsing action inputs...');
  const inputs = getInputs();
  validateInputs(inputs);

  logger.debug('Inputs:', JSON.stringify(inputs, null, 2));

  // Log configuration
  core.startGroup('Configuration');
  core.info(`Spec path: ${inputs.specPath}`);
  core.info(`Base ref: ${inputs.baseRef}`);
  core.info(`Head ref: ${inputs.headRef}`);
  core.info(`Output format: ${inputs.format}`);
  core.info(`Fail on breaking: ${inputs.failOnBreaking}`);
  core.info(`Comment on PR: ${inputs.commentOnPr}`);
  core.info(`Create check: ${inputs.createCheck}`);
  core.endGroup();

  // Detect breaking changes
  core.startGroup('Detecting API Changes');
  logger.info('Analyzing API specifications...');
  const result = await detectBreakingChanges(inputs);
  core.endGroup();

  // Set outputs
  setOutputs(result);

  // Log summary
  logSummary(result);

  // Write GitHub Actions summary
  await writeSummary(result);

  // Comment on PR if enabled and in PR context
  if (inputs.commentOnPr && github.context.payload.pull_request) {
    core.startGroup('Posting PR Comment');
    try {
      const prNumber = github.context.payload.pull_request.number;
      const existingComment = await findExistingComment(inputs.githubToken, prNumber);

      if (existingComment) {
        logger.info(`Updating existing comment #${existingComment}`);
        await updatePrComment(inputs.githubToken, existingComment, result);
      } else {
        logger.info('Creating new PR comment');
        await postPrComment(inputs.githubToken, result);
      }
      logger.info('PR comment posted successfully');
    } catch (error) {
      logger.warn(`Failed to post PR comment: ${error}`);
    }
    core.endGroup();
  }

  // Create check run if enabled
  if (inputs.createCheck && inputs.githubToken) {
    core.startGroup('Creating Check Run');
    try {
      const checkRunId = await findExistingCheckRun(inputs.githubToken);

      if (checkRunId) {
        logger.info(`Updating existing check run #${checkRunId}`);
        await updateCheckRun(inputs.githubToken, checkRunId, result);
      } else {
        logger.info('Creating new check run');
        await createCheckRun(inputs.githubToken, result);
      }
      logger.info('Check run created successfully');
    } catch (error) {
      logger.warn(`Failed to create check run: ${error}`);
    }
    core.endGroup();
  }

  // Determine final status
  if (inputs.failOnBreaking && result.hasBreakingChanges) {
    const message = formatFailureMessage(result);
    core.setFailed(message);
  } else if (result.hasBreakingChanges) {
    core.warning(
      `${result.breakingChangesCount} breaking change(s) detected (not failing due to configuration)`
    );
  } else {
    core.info('No breaking changes detected');
  }
}

/**
 * Finds an existing Changelog Hub comment on the PR
 */
async function findExistingComment(token: string, prNumber: number): Promise<number | null> {
  const octokit = github.getOctokit(token);
  const { owner, repo } = github.context.repo;

  const { data: comments } = await octokit.rest.issues.listComments({
    owner,
    repo,
    issue_number: prNumber,
  });

  const marker = '<!-- changelog-hub-comment -->';
  const existingComment = comments.find((c) => c.body?.includes(marker));

  return existingComment?.id ?? null;
}

/**
 * Finds an existing Changelog Hub check run
 */
async function findExistingCheckRun(token: string): Promise<number | null> {
  const octokit = github.getOctokit(token);
  const { owner, repo } = github.context.repo;
  const sha = github.context.sha;

  const { data: checks } = await octokit.rest.checks.listForRef({
    owner,
    repo,
    ref: sha,
  });

  const existingCheck = checks.check_runs.find((c) => c.name === 'Changelog Hub');

  return existingCheck?.id ?? null;
}

/**
 * Formats the failure message with details about breaking changes
 */
function formatFailureMessage(result: import('./changelog/detector').ChangelogResult): string {
  const lines: string[] = [
    `${result.breakingChangesCount} breaking change(s) detected!`,
    '',
    'Breaking Changes:',
  ];

  for (const change of result.breakingChanges.slice(0, 5)) {
    lines.push(`  - ${change.path}: ${change.description}`);
  }

  if (result.breakingChanges.length > 5) {
    lines.push(`  ... and ${result.breakingChanges.length - 5} more`);
  }

  lines.push('');
  lines.push(`Risk Level: ${result.riskLevel}`);
  lines.push(`Recommended version bump: ${result.semverRecommendation}`);

  return lines.join('\n');
}

import * as core from '@actions/core';
import * as github from '@actions/github';
import { getInputs, validateInputs } from './inputs';
import { setOutputs, writeSummary, logSummary } from './outputs';
import { detectBreakingChanges } from './changelog/detector';
import { postPrComment, updatePrComment } from './github/comment';
import { createCheckRun, updateCheckRun } from './github/check';
import { Logger } from './utils/logger';

export async function run(): Promise<void> {
  const logger = new Logger('Action');

  logger.info('Parsing action inputs...');
  const inputs = getInputs();
  validateInputs(inputs);

  logger.debug('Inputs:', JSON.stringify(inputs, null, 2));

  core.startGroup('Configuration');
  core.info(`Spec path: ${inputs.specPath}`);
  core.info(`Base ref: ${inputs.baseRef}`);
  core.info(`Head ref: ${inputs.headRef}`);
  core.info(`Output format: ${inputs.format}`);
  core.info(`Fail on breaking: ${inputs.failOnBreaking}`);
  core.info(`Comment on PR: ${inputs.commentOnPr}`);
  core.info(`Create check: ${inputs.createCheck}`);
  core.endGroup();

  core.startGroup('Detecting API Changes');
  logger.info('Analyzing API specifications...');
  const result = await detectBreakingChanges(inputs);
  core.endGroup();

  setOutputs(result);

  logSummary(result);

  await writeSummary(result);

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

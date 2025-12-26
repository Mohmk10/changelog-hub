import * as github from '@actions/github';
import { ChangelogResult } from '../changelog/detector';
import { Logger } from '../utils/logger';
import { parseVersion, bumpVersion, formatVersion } from '../utils/version';

const logger = new Logger('Release');

export interface ReleaseOptions {
  
  tagName?: string;
  
  name?: string;
  
  draft?: boolean;
  
  prerelease?: boolean;
  
  generateNotes?: boolean;
  
  targetCommitish?: string;
}

export interface ReleaseInfo {
  id: number;
  tagName: string;
  name: string;
  htmlUrl: string;
  body: string;
}

export async function createRelease(
  token: string,
  result: ChangelogResult,
  options: ReleaseOptions = {}
): Promise<ReleaseInfo> {
  const octokit = github.getOctokit(token);
  const { owner, repo } = github.context.repo;

  const tagName = options.tagName ?? determineTagName(result);
  const name = options.name ?? `Release ${tagName}`;

  const body = options.generateNotes !== false ? generateReleaseBody(result) : '';

  const { data } = await octokit.rest.repos.createRelease({
    owner,
    repo,
    tag_name: tagName,
    name,
    body,
    draft: options.draft ?? false,
    prerelease: options.prerelease ?? false,
    target_commitish: options.targetCommitish,
  });

  logger.info(`Created release ${tagName} (${data.html_url})`);

  return {
    id: data.id,
    tagName: data.tag_name,
    name: data.name ?? tagName,
    htmlUrl: data.html_url,
    body: data.body ?? '',
  };
}

export async function updateRelease(
  token: string,
  releaseId: number,
  result: ChangelogResult,
  options: Partial<ReleaseOptions> = {}
): Promise<void> {
  const octokit = github.getOctokit(token);
  const { owner, repo } = github.context.repo;

  const updateData: Record<string, unknown> = {};

  if (options.name) {
    updateData.name = options.name;
  }
  if (options.tagName) {
    updateData.tag_name = options.tagName;
  }
  if (options.draft !== undefined) {
    updateData.draft = options.draft;
  }
  if (options.prerelease !== undefined) {
    updateData.prerelease = options.prerelease;
  }
  if (options.generateNotes !== false) {
    updateData.body = generateReleaseBody(result);
  }

  await octokit.rest.repos.updateRelease({
    owner,
    repo,
    release_id: releaseId,
    ...updateData,
  });

  logger.info(`Updated release #${releaseId}`);
}

export async function getLatestRelease(token: string): Promise<ReleaseInfo | null> {
  const octokit = github.getOctokit(token);
  const { owner, repo } = github.context.repo;

  try {
    const { data } = await octokit.rest.repos.getLatestRelease({
      owner,
      repo,
    });

    return {
      id: data.id,
      tagName: data.tag_name,
      name: data.name ?? data.tag_name,
      htmlUrl: data.html_url,
      body: data.body ?? '',
    };
  } catch (error) {
    
    return null;
  }
}

export async function listReleases(token: string, count = 10): Promise<ReleaseInfo[]> {
  const octokit = github.getOctokit(token);
  const { owner, repo } = github.context.repo;

  const { data } = await octokit.rest.repos.listReleases({
    owner,
    repo,
    per_page: count,
  });

  return data.map((release) => ({
    id: release.id,
    tagName: release.tag_name,
    name: release.name ?? release.tag_name,
    htmlUrl: release.html_url,
    body: release.body ?? '',
  }));
}

export async function findReleaseByTag(token: string, tagName: string): Promise<ReleaseInfo | null> {
  const octokit = github.getOctokit(token);
  const { owner, repo } = github.context.repo;

  try {
    const { data } = await octokit.rest.repos.getReleaseByTag({
      owner,
      repo,
      tag: tagName,
    });

    return {
      id: data.id,
      tagName: data.tag_name,
      name: data.name ?? data.tag_name,
      htmlUrl: data.html_url,
      body: data.body ?? '',
    };
  } catch (error) {
    return null;
  }
}

function determineTagName(result: ChangelogResult): string {
  
  if (result.newSpec.version && result.newSpec.version !== '0.0.0') {
    const version = result.newSpec.version;
    return version.startsWith('v') ? version : `v${version}`;
  }

  const oldVersion = result.oldSpec.version || '0.0.0';
  const parsed = parseVersion(oldVersion);
  const bumped = bumpVersion(parsed, result.semverRecommendation);
  return `v${formatVersion(bumped)}`;
}

function generateReleaseBody(result: ChangelogResult): string {
  const lines: string[] = [];

  lines.push(`## What's Changed`);
  lines.push('');

  // Risk indicator
  if (result.hasBreakingChanges) {
    lines.push(`> **Warning:** This release contains ${result.breakingChangesCount} breaking change(s).`);
    lines.push('');
  }

  // Breaking changes
  if (result.breakingChanges.length > 0) {
    lines.push('### Breaking Changes');
    lines.push('');
    for (const change of result.breakingChanges) {
      lines.push(`- **${change.path}**: ${change.description}`);
      if (change.migrationSuggestion) {
        lines.push(`  - _Migration:_ ${change.migrationSuggestion}`);
      }
    }
    lines.push('');
  }

  // New features (additions)
  const additions = result.changes.filter((c) => c.type === 'ADDED' && c.severity !== 'BREAKING');
  if (additions.length > 0) {
    lines.push('### New Features');
    lines.push('');
    for (const change of additions) {
      lines.push(`- ${change.description}`);
    }
    lines.push('');
  }

  // Deprecations
  const deprecations = result.changes.filter((c) => c.type === 'DEPRECATED');
  if (deprecations.length > 0) {
    lines.push('### Deprecations');
    lines.push('');
    for (const change of deprecations) {
      lines.push(`- ${change.description}`);
    }
    lines.push('');
  }

  // Other changes
  const modifications = result.changes.filter(
    (c) =>
      c.type === 'MODIFIED' && c.severity !== 'BREAKING' && !deprecations.includes(c)
  );
  if (modifications.length > 0) {
    lines.push('### Other Changes');
    lines.push('');
    for (const change of modifications.slice(0, 10)) {
      lines.push(`- ${change.description}`);
    }
    if (modifications.length > 10) {
      lines.push(`- ... and ${modifications.length - 10} more changes`);
    }
    lines.push('');
  }

  // Summary
  lines.push('### Summary');
  lines.push('');
  lines.push(`- **Total Changes:** ${result.totalChangesCount}`);
  lines.push(`- **Risk Level:** ${result.riskLevel}`);
  lines.push(`- **Risk Score:** ${result.riskScore}/100`);
  lines.push('');

  // Footer
  lines.push('---');
  lines.push('*Release notes generated by [Changelog Hub](https:

  return lines.join('\n');
}

export async function createDraftRelease(
  token: string,
  result: ChangelogResult
): Promise<ReleaseInfo> {
  return createRelease(token, result, { draft: true });
}

export async function publishRelease(token: string, releaseId: number): Promise<void> {
  const octokit = github.getOctokit(token);
  const { owner, repo } = github.context.repo;

  await octokit.rest.repos.updateRelease({
    owner,
    repo,
    release_id: releaseId,
    draft: false,
  });

  logger.info(`Published release #${releaseId}`);
}

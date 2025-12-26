import * as github from '@actions/github';
import { Logger } from '../utils/logger';

const logger = new Logger('GitHubAPI');

export class GitHubApiClient {
  private octokit: ReturnType<typeof github.getOctokit>;
  private owner: string;
  private repo: string;

  constructor(token: string) {
    this.octokit = github.getOctokit(token);
    const { owner, repo } = github.context.repo;
    this.owner = owner;
    this.repo = repo;
  }

  async getRepository(): Promise<{
    owner: string;
    repo: string;
    defaultBranch: string;
  }> {
    const { data } = await this.octokit.rest.repos.get({
      owner: this.owner,
      repo: this.repo,
    });

    return {
      owner: this.owner,
      repo: this.repo,
      defaultBranch: data.default_branch,
    };
  }

  async getPullRequest(prNumber: number): Promise<{
    number: number;
    title: string;
    body: string | null;
    base: string;
    head: string;
    author: string;
  }> {
    const { data } = await this.octokit.rest.pulls.get({
      owner: this.owner,
      repo: this.repo,
      pull_number: prNumber,
    });

    return {
      number: data.number,
      title: data.title,
      body: data.body,
      base: data.base.ref,
      head: data.head.ref,
      author: data.user?.login ?? 'unknown',
    };
  }

  async listComments(issueNumber: number): Promise<
    Array<{
      id: number;
      body: string;
      author: string;
      createdAt: string;
      updatedAt: string;
    }>
  > {
    const { data } = await this.octokit.rest.issues.listComments({
      owner: this.owner,
      repo: this.repo,
      issue_number: issueNumber,
    });

    return data.map((comment) => ({
      id: comment.id,
      body: comment.body ?? '',
      author: comment.user?.login ?? 'unknown',
      createdAt: comment.created_at,
      updatedAt: comment.updated_at,
    }));
  }

  async createComment(issueNumber: number, body: string): Promise<number> {
    const { data } = await this.octokit.rest.issues.createComment({
      owner: this.owner,
      repo: this.repo,
      issue_number: issueNumber,
      body,
    });

    logger.info(`Created comment #${data.id} on issue #${issueNumber}`);
    return data.id;
  }

  async updateComment(commentId: number, body: string): Promise<void> {
    await this.octokit.rest.issues.updateComment({
      owner: this.owner,
      repo: this.repo,
      comment_id: commentId,
      body,
    });

    logger.info(`Updated comment #${commentId}`);
  }

  async deleteComment(commentId: number): Promise<void> {
    await this.octokit.rest.issues.deleteComment({
      owner: this.owner,
      repo: this.repo,
      comment_id: commentId,
    });

    logger.info(`Deleted comment #${commentId}`);
  }

  async createCheckRun(params: {
    name: string;
    headSha: string;
    status: 'queued' | 'in_progress' | 'completed';
    conclusion?: 'success' | 'failure' | 'neutral' | 'cancelled' | 'skipped';
    title: string;
    summary: string;
    text?: string;
    annotations?: Array<{
      path: string;
      startLine: number;
      endLine: number;
      level: 'notice' | 'warning' | 'failure';
      message: string;
    }>;
  }): Promise<number> {
    
    const transformedAnnotations = params.annotations?.map((a) => ({
      path: a.path,
      start_line: a.startLine,
      end_line: a.endLine,
      annotation_level: a.level,
      message: a.message,
    }));

    const { data } = await this.octokit.rest.checks.create({
      owner: this.owner,
      repo: this.repo,
      name: params.name,
      head_sha: params.headSha,
      status: params.status,
      conclusion: params.conclusion,
      output: {
        title: params.title,
        summary: params.summary,
        text: params.text,
        annotations: transformedAnnotations,
      },
    });

    logger.info(`Created check run #${data.id}`);
    return data.id;
  }

  async updateCheckRun(
    checkRunId: number,
    params: {
      status?: 'queued' | 'in_progress' | 'completed';
      conclusion?: 'success' | 'failure' | 'neutral' | 'cancelled' | 'skipped';
      title?: string;
      summary?: string;
      text?: string;
    }
  ): Promise<void> {
    await this.octokit.rest.checks.update({
      owner: this.owner,
      repo: this.repo,
      check_run_id: checkRunId,
      status: params.status,
      conclusion: params.conclusion,
      output: params.title
        ? {
            title: params.title,
            summary: params.summary ?? '',
            text: params.text,
          }
        : undefined,
    });

    logger.info(`Updated check run #${checkRunId}`);
  }

  async listCheckRuns(ref: string): Promise<
    Array<{
      id: number;
      name: string;
      status: string;
      conclusion: string | null;
    }>
  > {
    const { data } = await this.octokit.rest.checks.listForRef({
      owner: this.owner,
      repo: this.repo,
      ref,
    });

    return data.check_runs.map((check) => ({
      id: check.id,
      name: check.name,
      status: check.status,
      conclusion: check.conclusion,
    }));
  }

  async getFileContent(path: string, ref?: string): Promise<string> {
    const { data } = await this.octokit.rest.repos.getContent({
      owner: this.owner,
      repo: this.repo,
      path,
      ref,
    });

    if (Array.isArray(data) || data.type !== 'file') {
      throw new Error(`Path '${path}' is not a file`);
    }

    const content = Buffer.from(data.content, 'base64').toString('utf-8');
    return content;
  }

  async compareCommits(
    base: string,
    head: string
  ): Promise<{
    aheadBy: number;
    behindBy: number;
    status: string;
    files: Array<{
      filename: string;
      status: string;
      additions: number;
      deletions: number;
    }>;
  }> {
    const { data } = await this.octokit.rest.repos.compareCommits({
      owner: this.owner,
      repo: this.repo,
      base,
      head,
    });

    return {
      aheadBy: data.ahead_by,
      behindBy: data.behind_by,
      status: data.status,
      files: (data.files ?? []).map((file) => ({
        filename: file.filename,
        status: file.status ?? 'unknown',
        additions: file.additions,
        deletions: file.deletions,
      })),
    };
  }

  async createRelease(params: {
    tagName: string;
    name: string;
    body: string;
    draft?: boolean;
    prerelease?: boolean;
  }): Promise<{
    id: number;
    htmlUrl: string;
  }> {
    const { data } = await this.octokit.rest.repos.createRelease({
      owner: this.owner,
      repo: this.repo,
      tag_name: params.tagName,
      name: params.name,
      body: params.body,
      draft: params.draft ?? false,
      prerelease: params.prerelease ?? false,
    });

    logger.info(`Created release ${params.tagName}`);

    return {
      id: data.id,
      htmlUrl: data.html_url,
    };
  }

  static getContext(): {
    sha: string;
    ref: string;
    workflow: string;
    action: string;
    actor: string;
    eventName: string;
    runId: number;
    runNumber: number;
  } {
    return {
      sha: github.context.sha,
      ref: github.context.ref,
      workflow: github.context.workflow,
      action: github.context.action,
      actor: github.context.actor,
      eventName: github.context.eventName,
      runId: github.context.runId,
      runNumber: github.context.runNumber,
    };
  }

  static getPullRequestNumber(): number | undefined {
    return github.context.payload.pull_request?.number;
  }
}

export function createGitHubClient(token: string): GitHubApiClient {
  return new GitHubApiClient(token);
}

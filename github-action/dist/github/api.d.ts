/**
 * GitHub API client wrapper
 */
export declare class GitHubApiClient {
    private octokit;
    private owner;
    private repo;
    constructor(token: string);
    /**
     * Gets repository information
     */
    getRepository(): Promise<{
        owner: string;
        repo: string;
        defaultBranch: string;
    }>;
    /**
     * Gets pull request information
     */
    getPullRequest(prNumber: number): Promise<{
        number: number;
        title: string;
        body: string | null;
        base: string;
        head: string;
        author: string;
    }>;
    /**
     * Lists comments on an issue/PR
     */
    listComments(issueNumber: number): Promise<Array<{
        id: number;
        body: string;
        author: string;
        createdAt: string;
        updatedAt: string;
    }>>;
    /**
     * Creates a comment on an issue/PR
     */
    createComment(issueNumber: number, body: string): Promise<number>;
    /**
     * Updates an existing comment
     */
    updateComment(commentId: number, body: string): Promise<void>;
    /**
     * Deletes a comment
     */
    deleteComment(commentId: number): Promise<void>;
    /**
     * Creates a check run
     */
    createCheckRun(params: {
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
    }): Promise<number>;
    /**
     * Updates a check run
     */
    updateCheckRun(checkRunId: number, params: {
        status?: 'queued' | 'in_progress' | 'completed';
        conclusion?: 'success' | 'failure' | 'neutral' | 'cancelled' | 'skipped';
        title?: string;
        summary?: string;
        text?: string;
    }): Promise<void>;
    /**
     * Lists check runs for a ref
     */
    listCheckRuns(ref: string): Promise<Array<{
        id: number;
        name: string;
        status: string;
        conclusion: string | null;
    }>>;
    /**
     * Gets file content from a specific ref
     */
    getFileContent(path: string, ref?: string): Promise<string>;
    /**
     * Compares two refs
     */
    compareCommits(base: string, head: string): Promise<{
        aheadBy: number;
        behindBy: number;
        status: string;
        files: Array<{
            filename: string;
            status: string;
            additions: number;
            deletions: number;
        }>;
    }>;
    /**
     * Creates a release
     */
    createRelease(params: {
        tagName: string;
        name: string;
        body: string;
        draft?: boolean;
        prerelease?: boolean;
    }): Promise<{
        id: number;
        htmlUrl: string;
    }>;
    /**
     * Gets the current context information
     */
    static getContext(): {
        sha: string;
        ref: string;
        workflow: string;
        action: string;
        actor: string;
        eventName: string;
        runId: number;
        runNumber: number;
    };
    /**
     * Gets pull request number from context if available
     */
    static getPullRequestNumber(): number | undefined;
}
/**
 * Creates a GitHub API client with the provided token
 */
export declare function createGitHubClient(token: string): GitHubApiClient;
//# sourceMappingURL=api.d.ts.map
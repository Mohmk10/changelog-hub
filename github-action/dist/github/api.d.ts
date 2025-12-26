export declare class GitHubApiClient {
    private octokit;
    private owner;
    private repo;
    constructor(token: string);
    getRepository(): Promise<{
        owner: string;
        repo: string;
        defaultBranch: string;
    }>;
    getPullRequest(prNumber: number): Promise<{
        number: number;
        title: string;
        body: string | null;
        base: string;
        head: string;
        author: string;
    }>;
    listComments(issueNumber: number): Promise<Array<{
        id: number;
        body: string;
        author: string;
        createdAt: string;
        updatedAt: string;
    }>>;
    createComment(issueNumber: number, body: string): Promise<number>;
    updateComment(commentId: number, body: string): Promise<void>;
    deleteComment(commentId: number): Promise<void>;
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
    updateCheckRun(checkRunId: number, params: {
        status?: 'queued' | 'in_progress' | 'completed';
        conclusion?: 'success' | 'failure' | 'neutral' | 'cancelled' | 'skipped';
        title?: string;
        summary?: string;
        text?: string;
    }): Promise<void>;
    listCheckRuns(ref: string): Promise<Array<{
        id: number;
        name: string;
        status: string;
        conclusion: string | null;
    }>>;
    getFileContent(path: string, ref?: string): Promise<string>;
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
    static getPullRequestNumber(): number | undefined;
}
export declare function createGitHubClient(token: string): GitHubApiClient;
//# sourceMappingURL=api.d.ts.map
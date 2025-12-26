import { ChangelogResult } from '../changelog/detector';
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
export declare function createRelease(token: string, result: ChangelogResult, options?: ReleaseOptions): Promise<ReleaseInfo>;
export declare function updateRelease(token: string, releaseId: number, result: ChangelogResult, options?: Partial<ReleaseOptions>): Promise<void>;
export declare function getLatestRelease(token: string): Promise<ReleaseInfo | null>;
export declare function listReleases(token: string, count?: number): Promise<ReleaseInfo[]>;
export declare function findReleaseByTag(token: string, tagName: string): Promise<ReleaseInfo | null>;
export declare function createDraftRelease(token: string, result: ChangelogResult): Promise<ReleaseInfo>;
export declare function publishRelease(token: string, releaseId: number): Promise<void>;
//# sourceMappingURL=release.d.ts.map
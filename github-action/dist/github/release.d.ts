import { ChangelogResult } from '../changelog/detector';
/**
 * Options for release creation
 */
export interface ReleaseOptions {
    /** Tag name for the release (e.g., 'v1.2.0') */
    tagName?: string;
    /** Release title/name */
    name?: string;
    /** Create as draft release */
    draft?: boolean;
    /** Mark as pre-release */
    prerelease?: boolean;
    /** Generate release notes from changelog */
    generateNotes?: boolean;
    /** Target commitish for the release */
    targetCommitish?: string;
}
/**
 * Release information
 */
export interface ReleaseInfo {
    id: number;
    tagName: string;
    name: string;
    htmlUrl: string;
    body: string;
}
/**
 * Creates a GitHub release with changelog information.
 *
 * @param token - GitHub token for API access
 * @param result - Changelog detection results
 * @param options - Release options
 * @returns Release information
 */
export declare function createRelease(token: string, result: ChangelogResult, options?: ReleaseOptions): Promise<ReleaseInfo>;
/**
 * Updates an existing GitHub release.
 *
 * @param token - GitHub token for API access
 * @param releaseId - Release ID to update
 * @param result - Changelog detection results
 * @param options - Release options
 */
export declare function updateRelease(token: string, releaseId: number, result: ChangelogResult, options?: Partial<ReleaseOptions>): Promise<void>;
/**
 * Finds the latest release.
 *
 * @param token - GitHub token for API access
 * @returns Release info or null if no releases exist
 */
export declare function getLatestRelease(token: string): Promise<ReleaseInfo | null>;
/**
 * Lists recent releases.
 *
 * @param token - GitHub token for API access
 * @param count - Number of releases to fetch
 * @returns List of release info
 */
export declare function listReleases(token: string, count?: number): Promise<ReleaseInfo[]>;
/**
 * Finds a release by tag name.
 *
 * @param token - GitHub token for API access
 * @param tagName - Tag name to search for
 * @returns Release info or null if not found
 */
export declare function findReleaseByTag(token: string, tagName: string): Promise<ReleaseInfo | null>;
/**
 * Creates a draft release for review.
 */
export declare function createDraftRelease(token: string, result: ChangelogResult): Promise<ReleaseInfo>;
/**
 * Publishes a draft release.
 */
export declare function publishRelease(token: string, releaseId: number): Promise<void>;
//# sourceMappingURL=release.d.ts.map
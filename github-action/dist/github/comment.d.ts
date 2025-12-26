import { ChangelogResult } from '../changelog/detector';
/**
 * Posts a new comment on the PR with changelog results.
 *
 * @param token - GitHub token for API access
 * @param result - Changelog detection results
 */
export declare function postPrComment(token: string, result: ChangelogResult): Promise<number>;
/**
 * Updates an existing Changelog Hub comment on the PR.
 *
 * @param token - GitHub token for API access
 * @param commentId - ID of the comment to update
 * @param result - Changelog detection results
 */
export declare function updatePrComment(token: string, commentId: number, result: ChangelogResult): Promise<void>;
/**
 * Finds an existing Changelog Hub comment on a PR.
 *
 * @param token - GitHub token for API access
 * @param prNumber - Pull request number
 * @returns Comment ID if found, null otherwise
 */
export declare function findExistingComment(token: string, prNumber: number): Promise<number | null>;
/**
 * Deletes a Changelog Hub comment from a PR.
 *
 * @param token - GitHub token for API access
 * @param commentId - ID of the comment to delete
 */
export declare function deleteComment(token: string, commentId: number): Promise<void>;
//# sourceMappingURL=comment.d.ts.map
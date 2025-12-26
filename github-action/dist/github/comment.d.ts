import { ChangelogResult } from '../changelog/detector';
export declare function postPrComment(token: string, result: ChangelogResult): Promise<number>;
export declare function updatePrComment(token: string, commentId: number, result: ChangelogResult): Promise<void>;
export declare function findExistingComment(token: string, prNumber: number): Promise<number | null>;
export declare function deleteComment(token: string, commentId: number): Promise<void>;
//# sourceMappingURL=comment.d.ts.map
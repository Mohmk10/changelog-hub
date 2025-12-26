export interface SemanticVersion {
    major: number;
    minor: number;
    patch: number;
    prerelease?: string;
    buildMetadata?: string;
}
export type VersionBump = 'MAJOR' | 'MINOR' | 'PATCH' | 'PRERELEASE';
export declare function parseVersion(version: string): SemanticVersion;
export declare function formatVersion(version: SemanticVersion, includeV?: boolean): string;
export declare function bumpVersion(version: SemanticVersion, bump: VersionBump): SemanticVersion;
export declare function compareVersions(a: SemanticVersion, b: SemanticVersion): number;
export declare function isValidVersion(version: string): boolean;
export declare function determineBumpType(oldVersion: SemanticVersion, newVersion: SemanticVersion): VersionBump | null;
export declare function getNextVersion(currentVersion: string, bump: VersionBump): string;
export declare function isGreaterThan(a: string, b: string): boolean;
export declare function isLessThan(a: string, b: string): boolean;
export declare function isEqual(a: string, b: string): boolean;
export declare function getMajor(version: string): number;
export declare function getMinor(version: string): number;
export declare function getPatch(version: string): number;
//# sourceMappingURL=version.d.ts.map
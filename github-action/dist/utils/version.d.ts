/**
 * Represents a parsed semantic version
 */
export interface SemanticVersion {
    major: number;
    minor: number;
    patch: number;
    prerelease?: string;
    buildMetadata?: string;
}
/**
 * Version bump types
 */
export type VersionBump = 'MAJOR' | 'MINOR' | 'PATCH' | 'PRERELEASE';
/**
 * Parses a version string into a SemanticVersion object.
 *
 * @param version - Version string (e.g., "1.2.3", "v1.2.3", "1.2.3-beta.1")
 * @returns Parsed semantic version
 * @throws Error if version string is invalid
 */
export declare function parseVersion(version: string): SemanticVersion;
/**
 * Formats a SemanticVersion object as a string.
 *
 * @param version - Semantic version object
 * @param includeV - Whether to include leading 'v'
 * @returns Formatted version string
 */
export declare function formatVersion(version: SemanticVersion, includeV?: boolean): string;
/**
 * Bumps a version according to the specified type.
 *
 * @param version - Current version
 * @param bump - Type of bump to apply
 * @returns New bumped version
 */
export declare function bumpVersion(version: SemanticVersion, bump: VersionBump): SemanticVersion;
/**
 * Compares two versions.
 *
 * @param a - First version
 * @param b - Second version
 * @returns -1 if a < b, 0 if a == b, 1 if a > b
 */
export declare function compareVersions(a: SemanticVersion, b: SemanticVersion): number;
/**
 * Checks if a version string is valid.
 *
 * @param version - Version string to validate
 * @returns true if valid
 */
export declare function isValidVersion(version: string): boolean;
/**
 * Determines the type of bump needed between two versions.
 *
 * @param oldVersion - Previous version
 * @param newVersion - New version
 * @returns Type of bump applied
 */
export declare function determineBumpType(oldVersion: SemanticVersion, newVersion: SemanticVersion): VersionBump | null;
/**
 * Gets the next version based on a bump type.
 *
 * @param currentVersion - Current version string
 * @param bump - Type of bump
 * @returns New version string
 */
export declare function getNextVersion(currentVersion: string, bump: VersionBump): string;
/**
 * Checks if version A is greater than version B.
 */
export declare function isGreaterThan(a: string, b: string): boolean;
/**
 * Checks if version A is less than version B.
 */
export declare function isLessThan(a: string, b: string): boolean;
/**
 * Checks if two versions are equal.
 */
export declare function isEqual(a: string, b: string): boolean;
/**
 * Gets the major version number.
 */
export declare function getMajor(version: string): number;
/**
 * Gets the minor version number.
 */
export declare function getMinor(version: string): number;
/**
 * Gets the patch version number.
 */
export declare function getPatch(version: string): number;
//# sourceMappingURL=version.d.ts.map
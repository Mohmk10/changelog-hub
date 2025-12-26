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
export function parseVersion(version: string): SemanticVersion {
  // Remove leading 'v' if present
  const cleanVersion = version.startsWith('v') ? version.substring(1) : version;

  // Split by build metadata first
  const [versionAndPrerelease, buildMetadata] = cleanVersion.split('+');

  // Split by prerelease
  const [versionPart, prerelease] = versionAndPrerelease.split('-');

  // Parse major.minor.patch
  const parts = versionPart.split('.');

  if (parts.length < 1 || parts.length > 3) {
    throw new Error(`Invalid version format: ${version}`);
  }

  const major = parseInt(parts[0], 10);
  const minor = parts.length > 1 ? parseInt(parts[1], 10) : 0;
  const patch = parts.length > 2 ? parseInt(parts[2], 10) : 0;

  if (isNaN(major) || isNaN(minor) || isNaN(patch)) {
    throw new Error(`Invalid version numbers in: ${version}`);
  }

  if (major < 0 || minor < 0 || patch < 0) {
    throw new Error(`Version numbers cannot be negative: ${version}`);
  }

  return {
    major,
    minor,
    patch,
    prerelease,
    buildMetadata,
  };
}

/**
 * Formats a SemanticVersion object as a string.
 *
 * @param version - Semantic version object
 * @param includeV - Whether to include leading 'v'
 * @returns Formatted version string
 */
export function formatVersion(version: SemanticVersion, includeV = false): string {
  let result = `${version.major}.${version.minor}.${version.patch}`;

  if (version.prerelease) {
    result += `-${version.prerelease}`;
  }

  if (version.buildMetadata) {
    result += `+${version.buildMetadata}`;
  }

  return includeV ? `v${result}` : result;
}

/**
 * Bumps a version according to the specified type.
 *
 * @param version - Current version
 * @param bump - Type of bump to apply
 * @returns New bumped version
 */
export function bumpVersion(version: SemanticVersion, bump: VersionBump): SemanticVersion {
  const result = { ...version };

  // Clear prerelease and build metadata on major/minor/patch bumps
  if (bump !== 'PRERELEASE') {
    result.prerelease = undefined;
    result.buildMetadata = undefined;
  }

  switch (bump) {
    case 'MAJOR':
      result.major += 1;
      result.minor = 0;
      result.patch = 0;
      break;
    case 'MINOR':
      result.minor += 1;
      result.patch = 0;
      break;
    case 'PATCH':
      result.patch += 1;
      break;
    case 'PRERELEASE':
      result.prerelease = incrementPrerelease(result.prerelease);
      break;
  }

  return result;
}

/**
 * Increments a prerelease version.
 *
 * @param prerelease - Current prerelease string
 * @returns New prerelease string
 */
function incrementPrerelease(prerelease?: string): string {
  if (!prerelease) {
    return 'alpha.1';
  }

  // Try to find a numeric suffix
  const match = prerelease.match(/^(.+?)\.?(\d+)$/);

  if (match) {
    const prefix = match[1];
    const num = parseInt(match[2], 10) + 1;
    return `${prefix}.${num}`;
  }

  // No numeric suffix, add .1
  return `${prerelease}.1`;
}

/**
 * Compares two versions.
 *
 * @param a - First version
 * @param b - Second version
 * @returns -1 if a < b, 0 if a == b, 1 if a > b
 */
export function compareVersions(a: SemanticVersion, b: SemanticVersion): number {
  // Compare major
  if (a.major !== b.major) {
    return a.major < b.major ? -1 : 1;
  }

  // Compare minor
  if (a.minor !== b.minor) {
    return a.minor < b.minor ? -1 : 1;
  }

  // Compare patch
  if (a.patch !== b.patch) {
    return a.patch < b.patch ? -1 : 1;
  }

  // Compare prerelease (version without prerelease > version with prerelease)
  if (a.prerelease && !b.prerelease) {
    return -1;
  }
  if (!a.prerelease && b.prerelease) {
    return 1;
  }
  if (a.prerelease && b.prerelease) {
    return comparePrerelease(a.prerelease, b.prerelease);
  }

  return 0;
}

/**
 * Compares prerelease strings.
 */
function comparePrerelease(a: string, b: string): number {
  const aParts = a.split('.');
  const bParts = b.split('.');

  for (let i = 0; i < Math.max(aParts.length, bParts.length); i++) {
    const aPart = aParts[i];
    const bPart = bParts[i];

    if (aPart === undefined) return -1;
    if (bPart === undefined) return 1;

    const aNum = parseInt(aPart, 10);
    const bNum = parseInt(bPart, 10);

    if (!isNaN(aNum) && !isNaN(bNum)) {
      if (aNum !== bNum) {
        return aNum < bNum ? -1 : 1;
      }
    } else if (!isNaN(aNum)) {
      return -1; // Numbers come before strings
    } else if (!isNaN(bNum)) {
      return 1;
    } else {
      // Both are strings
      if (aPart !== bPart) {
        return aPart < bPart ? -1 : 1;
      }
    }
  }

  return 0;
}

/**
 * Checks if a version string is valid.
 *
 * @param version - Version string to validate
 * @returns true if valid
 */
export function isValidVersion(version: string): boolean {
  try {
    parseVersion(version);
    return true;
  } catch {
    return false;
  }
}

/**
 * Determines the type of bump needed between two versions.
 *
 * @param oldVersion - Previous version
 * @param newVersion - New version
 * @returns Type of bump applied
 */
export function determineBumpType(
  oldVersion: SemanticVersion,
  newVersion: SemanticVersion
): VersionBump | null {
  if (newVersion.major > oldVersion.major) {
    return 'MAJOR';
  }
  if (newVersion.minor > oldVersion.minor) {
    return 'MINOR';
  }
  if (newVersion.patch > oldVersion.patch) {
    return 'PATCH';
  }
  if (newVersion.prerelease !== oldVersion.prerelease) {
    return 'PRERELEASE';
  }
  return null;
}

/**
 * Gets the next version based on a bump type.
 *
 * @param currentVersion - Current version string
 * @param bump - Type of bump
 * @returns New version string
 */
export function getNextVersion(currentVersion: string, bump: VersionBump): string {
  const parsed = parseVersion(currentVersion);
  const bumped = bumpVersion(parsed, bump);
  return formatVersion(bumped);
}

/**
 * Checks if version A is greater than version B.
 */
export function isGreaterThan(a: string, b: string): boolean {
  return compareVersions(parseVersion(a), parseVersion(b)) > 0;
}

/**
 * Checks if version A is less than version B.
 */
export function isLessThan(a: string, b: string): boolean {
  return compareVersions(parseVersion(a), parseVersion(b)) < 0;
}

/**
 * Checks if two versions are equal.
 */
export function isEqual(a: string, b: string): boolean {
  return compareVersions(parseVersion(a), parseVersion(b)) === 0;
}

/**
 * Gets the major version number.
 */
export function getMajor(version: string): number {
  return parseVersion(version).major;
}

/**
 * Gets the minor version number.
 */
export function getMinor(version: string): number {
  return parseVersion(version).minor;
}

/**
 * Gets the patch version number.
 */
export function getPatch(version: string): number {
  return parseVersion(version).patch;
}


export interface SemanticVersion {
  major: number;
  minor: number;
  patch: number;
  prerelease?: string;
  buildMetadata?: string;
}

export type VersionBump = 'MAJOR' | 'MINOR' | 'PATCH' | 'PRERELEASE';

export function parseVersion(version: string): SemanticVersion {
  
  const cleanVersion = version.startsWith('v') ? version.substring(1) : version;

  const [versionAndPrerelease, buildMetadata] = cleanVersion.split('+');

  const [versionPart, prerelease] = versionAndPrerelease.split('-');

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

export function bumpVersion(version: SemanticVersion, bump: VersionBump): SemanticVersion {
  const result = { ...version };

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

function incrementPrerelease(prerelease?: string): string {
  if (!prerelease) {
    return 'alpha.1';
  }

  const match = prerelease.match(/^(.+?)\.?(\d+)$/);

  if (match) {
    const prefix = match[1];
    const num = parseInt(match[2], 10) + 1;
    return `${prefix}.${num}`;
  }

  return `${prerelease}.1`;
}

export function compareVersions(a: SemanticVersion, b: SemanticVersion): number {
  
  if (a.major !== b.major) {
    return a.major < b.major ? -1 : 1;
  }

  if (a.minor !== b.minor) {
    return a.minor < b.minor ? -1 : 1;
  }

  if (a.patch !== b.patch) {
    return a.patch < b.patch ? -1 : 1;
  }

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
      return -1; 
    } else if (!isNaN(bNum)) {
      return 1;
    } else {
      
      if (aPart !== bPart) {
        return aPart < bPart ? -1 : 1;
      }
    }
  }

  return 0;
}

export function isValidVersion(version: string): boolean {
  try {
    parseVersion(version);
    return true;
  } catch {
    return false;
  }
}

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

export function getNextVersion(currentVersion: string, bump: VersionBump): string {
  const parsed = parseVersion(currentVersion);
  const bumped = bumpVersion(parsed, bump);
  return formatVersion(bumped);
}

export function isGreaterThan(a: string, b: string): boolean {
  return compareVersions(parseVersion(a), parseVersion(b)) > 0;
}

export function isLessThan(a: string, b: string): boolean {
  return compareVersions(parseVersion(a), parseVersion(b)) < 0;
}

export function isEqual(a: string, b: string): boolean {
  return compareVersions(parseVersion(a), parseVersion(b)) === 0;
}

export function getMajor(version: string): number {
  return parseVersion(version).major;
}

export function getMinor(version: string): number {
  return parseVersion(version).minor;
}

export function getPatch(version: string): number {
  return parseVersion(version).patch;
}

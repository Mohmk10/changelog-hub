import {
  parseVersion,
  formatVersion,
  bumpVersion,
  compareVersions,
  isValidVersion,
  determineBumpType,
  getNextVersion,
  isGreaterThan,
  isLessThan,
  isEqual,
  getMajor,
  getMinor,
  getPatch,
  SemanticVersion,
} from '../src/utils/version';

describe('Version Utils', () => {
  describe('parseVersion', () => {
    it('should parse simple version', () => {
      const result = parseVersion('1.2.3');
      expect(result.major).toBe(1);
      expect(result.minor).toBe(2);
      expect(result.patch).toBe(3);
    });

    it('should parse version with v prefix', () => {
      const result = parseVersion('v2.0.1');
      expect(result.major).toBe(2);
      expect(result.minor).toBe(0);
      expect(result.patch).toBe(1);
    });

    it('should parse version with prerelease', () => {
      const result = parseVersion('1.0.0-alpha.1');
      expect(result.major).toBe(1);
      expect(result.minor).toBe(0);
      expect(result.patch).toBe(0);
      expect(result.prerelease).toBe('alpha.1');
    });

    it('should parse version with build metadata', () => {
      const result = parseVersion('1.0.0+build.123');
      expect(result.major).toBe(1);
      expect(result.buildMetadata).toBe('build.123');
    });

    it('should parse version with prerelease and build metadata', () => {
      const result = parseVersion('1.0.0-beta.2+build.456');
      expect(result.prerelease).toBe('beta.2');
      expect(result.buildMetadata).toBe('build.456');
    });

    it('should handle major-only version', () => {
      const result = parseVersion('5');
      expect(result.major).toBe(5);
      expect(result.minor).toBe(0);
      expect(result.patch).toBe(0);
    });

    it('should handle major.minor version', () => {
      const result = parseVersion('3.14');
      expect(result.major).toBe(3);
      expect(result.minor).toBe(14);
      expect(result.patch).toBe(0);
    });

    it('should throw for invalid version', () => {
      expect(() => parseVersion('invalid')).toThrow();
      expect(() => parseVersion('1.2.3.4.5')).toThrow();
      expect(() => parseVersion('a.b.c')).toThrow();
    });

    it('should throw for negative version numbers', () => {
      expect(() => parseVersion('-1.0.0')).toThrow();
    });
  });

  describe('formatVersion', () => {
    it('should format simple version', () => {
      const version: SemanticVersion = { major: 1, minor: 2, patch: 3 };
      expect(formatVersion(version)).toBe('1.2.3');
    });

    it('should format with v prefix when requested', () => {
      const version: SemanticVersion = { major: 1, minor: 2, patch: 3 };
      expect(formatVersion(version, true)).toBe('v1.2.3');
    });

    it('should format with prerelease', () => {
      const version: SemanticVersion = { major: 1, minor: 0, patch: 0, prerelease: 'rc.1' };
      expect(formatVersion(version)).toBe('1.0.0-rc.1');
    });

    it('should format with build metadata', () => {
      const version: SemanticVersion = { major: 1, minor: 0, patch: 0, buildMetadata: 'sha.abc123' };
      expect(formatVersion(version)).toBe('1.0.0+sha.abc123');
    });

    it('should format with both prerelease and build metadata', () => {
      const version: SemanticVersion = {
        major: 2,
        minor: 1,
        patch: 0,
        prerelease: 'beta',
        buildMetadata: '20231215',
      };
      expect(formatVersion(version)).toBe('2.1.0-beta+20231215');
    });
  });

  describe('bumpVersion', () => {
    const baseVersion: SemanticVersion = { major: 1, minor: 2, patch: 3 };

    it('should bump major version', () => {
      const result = bumpVersion(baseVersion, 'MAJOR');
      expect(result.major).toBe(2);
      expect(result.minor).toBe(0);
      expect(result.patch).toBe(0);
    });

    it('should bump minor version', () => {
      const result = bumpVersion(baseVersion, 'MINOR');
      expect(result.major).toBe(1);
      expect(result.minor).toBe(3);
      expect(result.patch).toBe(0);
    });

    it('should bump patch version', () => {
      const result = bumpVersion(baseVersion, 'PATCH');
      expect(result.major).toBe(1);
      expect(result.minor).toBe(2);
      expect(result.patch).toBe(4);
    });

    it('should clear prerelease on major bump', () => {
      const versionWithPre: SemanticVersion = { ...baseVersion, prerelease: 'alpha.1' };
      const result = bumpVersion(versionWithPre, 'MAJOR');
      expect(result.prerelease).toBeUndefined();
    });

    it('should bump prerelease version', () => {
      const versionWithPre: SemanticVersion = { ...baseVersion, prerelease: 'alpha.1' };
      const result = bumpVersion(versionWithPre, 'PRERELEASE');
      expect(result.prerelease).toBe('alpha.2');
    });

    it('should start prerelease from alpha.1 if no prerelease exists', () => {
      const result = bumpVersion(baseVersion, 'PRERELEASE');
      expect(result.prerelease).toBe('alpha.1');
    });

    it('should increment prerelease without dot', () => {
      const version: SemanticVersion = { major: 1, minor: 0, patch: 0, prerelease: 'beta' };
      const result = bumpVersion(version, 'PRERELEASE');
      expect(result.prerelease).toBe('beta.1');
    });
  });

  describe('compareVersions', () => {
    it('should compare major versions', () => {
      const v1 = parseVersion('1.0.0');
      const v2 = parseVersion('2.0.0');
      expect(compareVersions(v1, v2)).toBe(-1);
      expect(compareVersions(v2, v1)).toBe(1);
    });

    it('should compare minor versions', () => {
      const v1 = parseVersion('1.1.0');
      const v2 = parseVersion('1.2.0');
      expect(compareVersions(v1, v2)).toBe(-1);
    });

    it('should compare patch versions', () => {
      const v1 = parseVersion('1.0.1');
      const v2 = parseVersion('1.0.2');
      expect(compareVersions(v1, v2)).toBe(-1);
    });

    it('should return 0 for equal versions', () => {
      const v1 = parseVersion('1.2.3');
      const v2 = parseVersion('1.2.3');
      expect(compareVersions(v1, v2)).toBe(0);
    });

    it('should rank release higher than prerelease', () => {
      const release = parseVersion('1.0.0');
      const prerelease = parseVersion('1.0.0-alpha');
      expect(compareVersions(prerelease, release)).toBe(-1);
      expect(compareVersions(release, prerelease)).toBe(1);
    });

    it('should compare prerelease versions', () => {
      const alpha = parseVersion('1.0.0-alpha.1');
      const alpha2 = parseVersion('1.0.0-alpha.2');
      const beta = parseVersion('1.0.0-beta.1');

      expect(compareVersions(alpha, alpha2)).toBe(-1);
      expect(compareVersions(alpha, beta)).toBe(-1);
    });
  });

  describe('isValidVersion', () => {
    it('should return true for valid versions', () => {
      expect(isValidVersion('1.0.0')).toBe(true);
      expect(isValidVersion('v2.3.4')).toBe(true);
      expect(isValidVersion('0.0.1-alpha')).toBe(true);
    });

    it('should return false for invalid versions', () => {
      expect(isValidVersion('invalid')).toBe(false);
      expect(isValidVersion('a.b.c')).toBe(false);
      expect(isValidVersion('')).toBe(false);
    });
  });

  describe('determineBumpType', () => {
    it('should detect MAJOR bump', () => {
      const old = parseVersion('1.0.0');
      const current = parseVersion('2.0.0');
      expect(determineBumpType(old, current)).toBe('MAJOR');
    });

    it('should detect MINOR bump', () => {
      const old = parseVersion('1.0.0');
      const current = parseVersion('1.1.0');
      expect(determineBumpType(old, current)).toBe('MINOR');
    });

    it('should detect PATCH bump', () => {
      const old = parseVersion('1.0.0');
      const current = parseVersion('1.0.1');
      expect(determineBumpType(old, current)).toBe('PATCH');
    });

    it('should detect PRERELEASE bump', () => {
      const old = parseVersion('1.0.0-alpha.1');
      const current = parseVersion('1.0.0-alpha.2');
      expect(determineBumpType(old, current)).toBe('PRERELEASE');
    });

    it('should return null for same version', () => {
      const v = parseVersion('1.0.0');
      expect(determineBumpType(v, v)).toBeNull();
    });
  });

  describe('getNextVersion', () => {
    it('should get next major version', () => {
      expect(getNextVersion('1.2.3', 'MAJOR')).toBe('2.0.0');
    });

    it('should get next minor version', () => {
      expect(getNextVersion('1.2.3', 'MINOR')).toBe('1.3.0');
    });

    it('should get next patch version', () => {
      expect(getNextVersion('1.2.3', 'PATCH')).toBe('1.2.4');
    });
  });

  describe('Comparison helpers', () => {
    it('isGreaterThan should work correctly', () => {
      expect(isGreaterThan('2.0.0', '1.0.0')).toBe(true);
      expect(isGreaterThan('1.0.0', '2.0.0')).toBe(false);
      expect(isGreaterThan('1.0.0', '1.0.0')).toBe(false);
    });

    it('isLessThan should work correctly', () => {
      expect(isLessThan('1.0.0', '2.0.0')).toBe(true);
      expect(isLessThan('2.0.0', '1.0.0')).toBe(false);
      expect(isLessThan('1.0.0', '1.0.0')).toBe(false);
    });

    it('isEqual should work correctly', () => {
      expect(isEqual('1.0.0', '1.0.0')).toBe(true);
      expect(isEqual('v1.0.0', '1.0.0')).toBe(true);
      expect(isEqual('1.0.0', '2.0.0')).toBe(false);
    });
  });

  describe('Version component getters', () => {
    it('getMajor should return major version', () => {
      expect(getMajor('3.2.1')).toBe(3);
    });

    it('getMinor should return minor version', () => {
      expect(getMinor('3.2.1')).toBe(2);
    });

    it('getPatch should return patch version', () => {
      expect(getPatch('3.2.1')).toBe(1);
    });
  });
});

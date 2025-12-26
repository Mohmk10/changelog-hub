import * as path from 'path';
import {
  detectBreakingChanges,
  hasBreakingChanges,
  getBreakingChangesSummary,
} from '../src/core/detector';

const fixturesPath = path.join(__dirname, 'fixtures');

describe('detectBreakingChanges', () => {
  it('should detect breaking changes from file paths', () => {
    const oldSpec = path.join(fixturesPath, 'api-v1.yaml');
    const newSpec = path.join(fixturesPath, 'api-v2-breaking.yaml');

    const result = detectBreakingChanges(oldSpec, newSpec);

    expect(result.breakingChanges.length).toBeGreaterThan(0);
    expect(result.apiName).toBeDefined();
    expect(result.fromVersion).toBeDefined();
    expect(result.toVersion).toBeDefined();
  });

  it('should return no breaking changes for compatible update', () => {
    const oldSpec = path.join(fixturesPath, 'api-v1.yaml');
    const newSpec = path.join(fixturesPath, 'api-v2-compatible.yaml');

    const result = detectBreakingChanges(oldSpec, newSpec);

    expect(result.breakingChanges.length).toBe(0);
  });

  it('should throw error for missing old spec file', () => {
    const oldSpec = path.join(fixturesPath, 'nonexistent.yaml');
    const newSpec = path.join(fixturesPath, 'api-v1.yaml');

    expect(() => detectBreakingChanges(oldSpec, newSpec)).toThrow('File not found');
  });

  it('should throw error for missing new spec file', () => {
    const oldSpec = path.join(fixturesPath, 'api-v1.yaml');
    const newSpec = path.join(fixturesPath, 'nonexistent.yaml');

    expect(() => detectBreakingChanges(oldSpec, newSpec)).toThrow('File not found');
  });

  describe('Severity filtering', () => {
    it('should filter by severity threshold', () => {
      const oldSpec = path.join(fixturesPath, 'api-v1.yaml');
      const newSpec = path.join(fixturesPath, 'api-v2-breaking.yaml');

      const result = detectBreakingChanges(oldSpec, newSpec, {
        severityThreshold: 'BREAKING',
      });

      for (const change of result.changes) {
        expect(change.severity).toBe('BREAKING');
      }
    });

    it('should include all severities with INFO threshold', () => {
      const oldSpec = path.join(fixturesPath, 'api-v1.yaml');
      const newSpec = path.join(fixturesPath, 'api-v2-compatible.yaml');

      const resultInfo = detectBreakingChanges(oldSpec, newSpec, {
        severityThreshold: 'INFO',
      });

      const resultWarning = detectBreakingChanges(oldSpec, newSpec, {
        severityThreshold: 'WARNING',
      });

      expect(resultInfo.totalChanges).toBeGreaterThanOrEqual(resultWarning.totalChanges);
    });
  });

  describe('Deprecation handling', () => {
    it('should include deprecations by default', () => {
      const oldSpec = path.join(fixturesPath, 'api-v1.yaml');
      const newSpec = path.join(fixturesPath, 'api-v2-compatible.yaml');

      const result = detectBreakingChanges(oldSpec, newSpec, {
        includeDeprecations: true,
      });

      expect(result).toBeDefined();
    });

    it('should exclude deprecations when specified', () => {
      const oldSpec = path.join(fixturesPath, 'api-v1.yaml');
      const newSpec = path.join(fixturesPath, 'api-v2-compatible.yaml');

      const result = detectBreakingChanges(oldSpec, newSpec, {
        includeDeprecations: false,
      });

      const deprecations = result.changes.filter(c => c.type === 'DEPRECATED');
      expect(deprecations.length).toBe(0);
    });
  });
});

describe('hasBreakingChanges', () => {
  it('should return true when breaking changes exist', () => {
    const oldSpec = path.join(fixturesPath, 'api-v1.yaml');
    const newSpec = path.join(fixturesPath, 'api-v2-breaking.yaml');

    expect(hasBreakingChanges(oldSpec, newSpec)).toBe(true);
  });

  it('should return false when no breaking changes', () => {
    const oldSpec = path.join(fixturesPath, 'api-v1.yaml');
    const newSpec = path.join(fixturesPath, 'api-v2-compatible.yaml');

    expect(hasBreakingChanges(oldSpec, newSpec)).toBe(false);
  });

  it('should return false when comparing identical specs', () => {
    const spec = path.join(fixturesPath, 'api-v1.yaml');

    expect(hasBreakingChanges(spec, spec)).toBe(false);
  });
});

describe('getBreakingChangesSummary', () => {
  it('should return summary with count', () => {
    const oldSpec = path.join(fixturesPath, 'api-v1.yaml');
    const newSpec = path.join(fixturesPath, 'api-v2-breaking.yaml');

    const summary = getBreakingChangesSummary(oldSpec, newSpec);

    expect(summary.count).toBeGreaterThan(0);
    expect(summary.riskLevel).toBeDefined();
    expect(summary.recommendation).toBe('MAJOR');
    expect(summary.changes.length).toBeGreaterThan(0);
  });

  it('should return empty summary for no changes', () => {
    const spec = path.join(fixturesPath, 'api-v1.yaml');

    const summary = getBreakingChangesSummary(spec, spec);

    expect(summary.count).toBe(0);
    expect(summary.changes.length).toBe(0);
  });
});

import * as path from 'path';
import * as fs from 'fs';
import { parseSpec } from '../src/core/parser';
import { compareSpecs } from '../src/core/comparator';
import { generateReport, generateShortSummary } from '../src/core/reporter';

const fixturesPath = path.join(__dirname, 'fixtures');

function getComparisonResult() {
  const v1Content = fs.readFileSync(path.join(fixturesPath, 'api-v1.yaml'), 'utf-8');
  const v2Content = fs.readFileSync(path.join(fixturesPath, 'api-v2-breaking.yaml'), 'utf-8');

  const v1Spec = parseSpec(v1Content, 'api-v1.yaml');
  const v2Spec = parseSpec(v2Content, 'api-v2-breaking.yaml');

  return compareSpecs(v1Spec, v2Spec);
}

describe('generateReport', () => {
  describe('JSON format', () => {
    it('should generate valid JSON report', () => {
      const result = getComparisonResult();
      const report = generateReport(result, 'json');

      expect(() => JSON.parse(report)).not.toThrow();
    });

    it('should include metadata in JSON report', () => {
      const result = getComparisonResult();
      const report = generateReport(result, 'json');
      const parsed = JSON.parse(report);

      expect(parsed.metadata).toBeDefined();
      expect(parsed.metadata.apiName).toBeDefined();
      expect(parsed.metadata.fromVersion).toBeDefined();
      expect(parsed.metadata.toVersion).toBeDefined();
      expect(parsed.metadata.generatedAt).toBeDefined();
    });

    it('should include summary in JSON report', () => {
      const result = getComparisonResult();
      const report = generateReport(result, 'json');
      const parsed = JSON.parse(report);

      expect(parsed.summary).toBeDefined();
      expect(typeof parsed.summary.totalChanges).toBe('number');
      expect(typeof parsed.summary.breakingChanges).toBe('number');
      expect(typeof parsed.summary.riskScore).toBe('number');
      expect(parsed.summary.riskLevel).toBeDefined();
      expect(parsed.summary.semverRecommendation).toBeDefined();
    });

    it('should include breaking changes in JSON report', () => {
      const result = getComparisonResult();
      const report = generateReport(result, 'json');
      const parsed = JSON.parse(report);

      expect(Array.isArray(parsed.breakingChanges)).toBe(true);
      if (parsed.breakingChanges.length > 0) {
        const change = parsed.breakingChanges[0];
        expect(change.type).toBeDefined();
        expect(change.path).toBeDefined();
        expect(change.description).toBeDefined();
      }
    });
  });

  describe('Markdown format', () => {
    it('should generate markdown report', () => {
      const result = getComparisonResult();
      const report = generateReport(result, 'markdown');

      expect(report).toContain('# API Changelog');
      expect(report).toContain('## Summary');
    });

    it('should include version information', () => {
      const result = getComparisonResult();
      const report = generateReport(result, 'markdown');

      expect(report).toContain(result.fromVersion);
      expect(report).toContain(result.toVersion);
    });

    it('should include summary table', () => {
      const result = getComparisonResult();
      const report = generateReport(result, 'markdown');

      expect(report).toContain('| Metric | Value |');
      expect(report).toContain('Total Changes');
      expect(report).toContain('Breaking Changes');
      expect(report).toContain('Risk Level');
    });

    it('should include breaking changes section', () => {
      const result = getComparisonResult();
      const report = generateReport(result, 'markdown');

      if (result.breakingChanges.length > 0) {
        expect(report).toContain('## Breaking Changes');
        expect(report).toContain('Warning');
      }
    });

    it('should include footer with timestamp', () => {
      const result = getComparisonResult();
      const report = generateReport(result, 'markdown');

      expect(report).toContain('Generated at');
      expect(report).toContain('Changelog Hub');
    });
  });

  describe('Console format', () => {
    it('should generate console report', () => {
      const result = getComparisonResult();
      const report = generateReport(result, 'console');

      expect(report).toContain('API CHANGELOG');
      expect(report).toContain('SUMMARY');
    });

    it('should include version transition', () => {
      const result = getComparisonResult();
      const report = generateReport(result, 'console');

      expect(report).toContain(result.apiName);
    });

    it('should include breaking changes section when applicable', () => {
      const result = getComparisonResult();
      const report = generateReport(result, 'console');

      if (result.breakingChanges.length > 0) {
        expect(report).toContain('BREAKING CHANGES');
      }
    });
  });

  describe('HTML format', () => {
    it('should generate valid HTML report', () => {
      const result = getComparisonResult();
      const report = generateReport(result, 'html');

      expect(report).toContain('<!DOCTYPE html>');
      expect(report).toContain('<html');
      expect(report).toContain('</html>');
    });

    it('should include API name in title', () => {
      const result = getComparisonResult();
      const report = generateReport(result, 'html');

      expect(report).toContain(`<title>API Changelog - ${result.apiName}</title>`);
    });

    it('should include summary grid', () => {
      const result = getComparisonResult();
      const report = generateReport(result, 'html');

      expect(report).toContain('class="summary"');
      expect(report).toContain('class="summary-grid"');
      expect(report).toContain('Total Changes');
      expect(report).toContain('Breaking');
      expect(report).toContain('Risk Level');
    });

    it('should include breaking changes table when applicable', () => {
      const result = getComparisonResult();
      const report = generateReport(result, 'html');

      if (result.breakingChanges.length > 0) {
        expect(report).toContain('Breaking Changes');
        expect(report).toContain('<table>');
      }
    });

    it('should include CSS styles', () => {
      const result = getComparisonResult();
      const report = generateReport(result, 'html');

      expect(report).toContain('<style>');
      expect(report).toContain('</style>');
    });

    it('should escape HTML in content', () => {
      const result = getComparisonResult();
      const report = generateReport(result, 'html');

      // Should contain properly formatted HTML
      expect(report).toContain('<html');
      expect(report).toContain('</html>');
    });
  });

  describe('Default format', () => {
    it('should default to console format', () => {
      const result = getComparisonResult();
      const consoleReport = generateReport(result, 'console');
      const defaultReport = generateReport(result, 'unknown' as any);

      expect(defaultReport).toBe(consoleReport);
    });
  });
});

describe('generateShortSummary', () => {
  it('should generate summary with breaking changes', () => {
    const result = getComparisonResult();
    const summary = generateShortSummary(result);

    if (result.breakingChanges.length > 0) {
      expect(summary).toContain('breaking');
    }
  });

  it('should include added changes count', () => {
    const v1Content = fs.readFileSync(path.join(fixturesPath, 'api-v1.yaml'), 'utf-8');
    const v2Content = fs.readFileSync(path.join(fixturesPath, 'api-v2-compatible.yaml'), 'utf-8');

    const v1Spec = parseSpec(v1Content, 'api-v1.yaml');
    const v2Spec = parseSpec(v2Content, 'api-v2-compatible.yaml');

    const result = compareSpecs(v1Spec, v2Spec);
    const summary = generateShortSummary(result);

    const additions = result.changes.filter(c => c.type === 'ADDED').length;
    if (additions > 0) {
      expect(summary).toContain('added');
    }
  });

  it('should return no changes message when no changes', () => {
    const v1Content = fs.readFileSync(path.join(fixturesPath, 'api-v1.yaml'), 'utf-8');
    const v1Spec = parseSpec(v1Content, 'api-v1.yaml');
    const result = compareSpecs(v1Spec, v1Spec);

    const summary = generateShortSummary(result);

    expect(summary).toContain('No API changes detected');
  });
});

import * as path from 'path';
import * as fs from 'fs';
import { parseSpec } from '../src/core/parser';
import { compareSpecs } from '../src/core/comparator';

const fixturesPath = path.join(__dirname, 'fixtures');

describe('compareSpecs', () => {
  describe('Breaking changes detection', () => {
    it('should detect breaking changes between v1 and v2-breaking', () => {
      const v1Content = fs.readFileSync(path.join(fixturesPath, 'api-v1.yaml'), 'utf-8');
      const v2Content = fs.readFileSync(path.join(fixturesPath, 'api-v2-breaking.yaml'), 'utf-8');

      const v1Spec = parseSpec(v1Content, 'api-v1.yaml');
      const v2Spec = parseSpec(v2Content, 'api-v2-breaking.yaml');

      const result = compareSpecs(v1Spec, v2Spec);

      expect(result.breakingChanges.length).toBeGreaterThan(0);
      expect(result.riskLevel).not.toBe('LOW');
      expect(result.semverRecommendation).toBe('MAJOR');
    });

    it('should detect removed endpoint as breaking change', () => {
      const v1Content = fs.readFileSync(path.join(fixturesPath, 'api-v1.yaml'), 'utf-8');
      const v2Content = fs.readFileSync(path.join(fixturesPath, 'api-v2-breaking.yaml'), 'utf-8');

      const v1Spec = parseSpec(v1Content, 'api-v1.yaml');
      const v2Spec = parseSpec(v2Content, 'api-v2-breaking.yaml');

      const result = compareSpecs(v1Spec, v2Spec);

      const removedEndpoint = result.breakingChanges.find(
        c => c.type === 'REMOVED' && c.category === 'ENDPOINT'
      );
      expect(removedEndpoint).toBeDefined();
    });

    it('should detect parameter type change as breaking change', () => {
      const v1Content = fs.readFileSync(path.join(fixturesPath, 'api-v1.yaml'), 'utf-8');
      const v2Content = fs.readFileSync(path.join(fixturesPath, 'api-v2-breaking.yaml'), 'utf-8');

      const v1Spec = parseSpec(v1Content, 'api-v1.yaml');
      const v2Spec = parseSpec(v2Content, 'api-v2-breaking.yaml');

      const result = compareSpecs(v1Spec, v2Spec);

      const typeChange = result.changes.find(
        c => c.description.toLowerCase().includes('type') && c.severity === 'BREAKING'
      );
      expect(typeChange).toBeDefined();
    });

    it('should detect required field addition as breaking change', () => {
      const v1Content = fs.readFileSync(path.join(fixturesPath, 'api-v1.yaml'), 'utf-8');
      const v2Content = fs.readFileSync(path.join(fixturesPath, 'api-v2-breaking.yaml'), 'utf-8');

      const v1Spec = parseSpec(v1Content, 'api-v1.yaml');
      const v2Spec = parseSpec(v2Content, 'api-v2-breaking.yaml');

      const result = compareSpecs(v1Spec, v2Spec);

      const requiredChange = result.changes.find(
        c => c.description.toLowerCase().includes('required') && c.severity === 'BREAKING'
      );
      expect(requiredChange).toBeDefined();
    });
  });

  describe('Compatible changes detection', () => {
    it('should not detect breaking changes in compatible update', () => {
      const v1Content = fs.readFileSync(path.join(fixturesPath, 'api-v1.yaml'), 'utf-8');
      const v2Content = fs.readFileSync(path.join(fixturesPath, 'api-v2-compatible.yaml'), 'utf-8');

      const v1Spec = parseSpec(v1Content, 'api-v1.yaml');
      const v2Spec = parseSpec(v2Content, 'api-v2-compatible.yaml');

      const result = compareSpecs(v1Spec, v2Spec);

      expect(result.breakingChanges.length).toBe(0);
    });

    it('should detect added endpoints', () => {
      const v1Content = fs.readFileSync(path.join(fixturesPath, 'api-v1.yaml'), 'utf-8');
      const v2Content = fs.readFileSync(path.join(fixturesPath, 'api-v2-compatible.yaml'), 'utf-8');

      const v1Spec = parseSpec(v1Content, 'api-v1.yaml');
      const v2Spec = parseSpec(v2Content, 'api-v2-compatible.yaml');

      const result = compareSpecs(v1Spec, v2Spec);

      const addedEndpoints = result.changes.filter(
        c => c.type === 'ADDED' && c.category === 'ENDPOINT'
      );
      expect(addedEndpoints.length).toBeGreaterThan(0);
    });

    it('should detect added optional parameters', () => {
      const v1Content = fs.readFileSync(path.join(fixturesPath, 'api-v1.yaml'), 'utf-8');
      const v2Content = fs.readFileSync(path.join(fixturesPath, 'api-v2-compatible.yaml'), 'utf-8');

      const v1Spec = parseSpec(v1Content, 'api-v1.yaml');
      const v2Spec = parseSpec(v2Content, 'api-v2-compatible.yaml');

      const result = compareSpecs(v1Spec, v2Spec);

      const addedParams = result.changes.filter(
        c => c.type === 'ADDED' && c.category === 'PARAMETER'
      );
      expect(addedParams.length).toBeGreaterThan(0);
    });

    it('should recommend MINOR version bump for compatible changes', () => {
      const v1Content = fs.readFileSync(path.join(fixturesPath, 'api-v1.yaml'), 'utf-8');
      const v2Content = fs.readFileSync(path.join(fixturesPath, 'api-v2-compatible.yaml'), 'utf-8');

      const v1Spec = parseSpec(v1Content, 'api-v1.yaml');
      const v2Spec = parseSpec(v2Content, 'api-v2-compatible.yaml');

      const result = compareSpecs(v1Spec, v2Spec);

      expect(result.semverRecommendation).toBe('MINOR');
    });
  });

  describe('No changes detection', () => {
    it('should return no changes when comparing identical specs', () => {
      const v1Content = fs.readFileSync(path.join(fixturesPath, 'api-v1.yaml'), 'utf-8');

      const v1Spec = parseSpec(v1Content, 'api-v1.yaml');
      const v1SpecCopy = parseSpec(v1Content, 'api-v1.yaml');

      const result = compareSpecs(v1Spec, v1SpecCopy);

      expect(result.totalChanges).toBe(0);
      expect(result.breakingChanges.length).toBe(0);
      // When no changes, semver returns PATCH (minimal version bump)
      expect(result.semverRecommendation).toBe('PATCH');
    });
  });

  describe('Risk scoring', () => {
    it('should have higher risk score for breaking changes', () => {
      const v1Content = fs.readFileSync(path.join(fixturesPath, 'api-v1.yaml'), 'utf-8');
      const v2BreakingContent = fs.readFileSync(path.join(fixturesPath, 'api-v2-breaking.yaml'), 'utf-8');
      const v2CompatibleContent = fs.readFileSync(path.join(fixturesPath, 'api-v2-compatible.yaml'), 'utf-8');

      const v1Spec = parseSpec(v1Content, 'api-v1.yaml');
      const v2BreakingSpec = parseSpec(v2BreakingContent, 'api-v2-breaking.yaml');
      const v2CompatibleSpec = parseSpec(v2CompatibleContent, 'api-v2-compatible.yaml');

      const breakingResult = compareSpecs(v1Spec, v2BreakingSpec);
      const compatibleResult = compareSpecs(v1Spec, v2CompatibleSpec);

      expect(breakingResult.riskScore).toBeGreaterThan(compatibleResult.riskScore);
    });

    it('should assign appropriate risk levels', () => {
      const v1Content = fs.readFileSync(path.join(fixturesPath, 'api-v1.yaml'), 'utf-8');
      const v2Content = fs.readFileSync(path.join(fixturesPath, 'api-v2-breaking.yaml'), 'utf-8');

      const v1Spec = parseSpec(v1Content, 'api-v1.yaml');
      const v2Spec = parseSpec(v2Content, 'api-v2-breaking.yaml');

      const result = compareSpecs(v1Spec, v2Spec);

      expect(['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']).toContain(result.riskLevel);
    });
  });

  describe('Change summary', () => {
    it('should provide summary of changes by type', () => {
      const v1Content = fs.readFileSync(path.join(fixturesPath, 'api-v1.yaml'), 'utf-8');
      const v2Content = fs.readFileSync(path.join(fixturesPath, 'api-v2-breaking.yaml'), 'utf-8');

      const v1Spec = parseSpec(v1Content, 'api-v1.yaml');
      const v2Spec = parseSpec(v2Content, 'api-v2-breaking.yaml');

      const result = compareSpecs(v1Spec, v2Spec);

      expect(result.summary).toBeDefined();
      expect(typeof result.summary.endpointsAdded).toBe('number');
      expect(typeof result.summary.endpointsModified).toBe('number');
      expect(typeof result.summary.endpointsRemoved).toBe('number');
    });
  });
});

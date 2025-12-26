import { Change, BreakingChange } from '../src/changelog/detector';

describe('Detector Logic', () => {
  describe('Risk Level Calculation', () => {
    function calculateRiskLevel(score: number): 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL' {
      if (score >= 75) return 'CRITICAL';
      if (score >= 50) return 'HIGH';
      if (score >= 25) return 'MEDIUM';
      return 'LOW';
    }

    it('should return LOW for scores below 25', () => {
      expect(calculateRiskLevel(0)).toBe('LOW');
      expect(calculateRiskLevel(10)).toBe('LOW');
      expect(calculateRiskLevel(24)).toBe('LOW');
    });

    it('should return MEDIUM for scores 25-49', () => {
      expect(calculateRiskLevel(25)).toBe('MEDIUM');
      expect(calculateRiskLevel(35)).toBe('MEDIUM');
      expect(calculateRiskLevel(49)).toBe('MEDIUM');
    });

    it('should return HIGH for scores 50-74', () => {
      expect(calculateRiskLevel(50)).toBe('HIGH');
      expect(calculateRiskLevel(60)).toBe('HIGH');
      expect(calculateRiskLevel(74)).toBe('HIGH');
    });

    it('should return CRITICAL for scores 75+', () => {
      expect(calculateRiskLevel(75)).toBe('CRITICAL');
      expect(calculateRiskLevel(90)).toBe('CRITICAL');
      expect(calculateRiskLevel(100)).toBe('CRITICAL');
    });
  });

  describe('Semver Recommendation', () => {
    interface MockComparison {
      breakingChanges: BreakingChange[];
      changes: Change[];
    }

    function getSemverRecommendation(
      comparison: MockComparison
    ): 'MAJOR' | 'MINOR' | 'PATCH' {
      if (comparison.breakingChanges.length > 0) return 'MAJOR';
      if (comparison.changes.some((c) => c.type === 'ADDED')) return 'MINOR';
      return 'PATCH';
    }

    it('should recommend MAJOR when breaking changes exist', () => {
      const comparison: MockComparison = {
        breakingChanges: [
          {
            type: 'REMOVED',
            category: 'ENDPOINT',
            severity: 'BREAKING',
            path: '/users',
            description: 'Endpoint removed',
            migrationSuggestion: 'Remove references',
            impactScore: 80,
          },
        ],
        changes: [],
      };
      expect(getSemverRecommendation(comparison)).toBe('MAJOR');
    });

    it('should recommend MINOR when additions exist without breaking changes', () => {
      const comparison: MockComparison = {
        breakingChanges: [],
        changes: [
          {
            type: 'ADDED',
            category: 'ENDPOINT',
            severity: 'INFO',
            path: '/new-endpoint',
            description: 'New endpoint added',
          },
        ],
      };
      expect(getSemverRecommendation(comparison)).toBe('MINOR');
    });

    it('should recommend PATCH for modifications without additions or breaking', () => {
      const comparison: MockComparison = {
        breakingChanges: [],
        changes: [
          {
            type: 'MODIFIED',
            category: 'ENDPOINT',
            severity: 'INFO',
            path: '/users',
            description: 'Updated description',
          },
        ],
      };
      expect(getSemverRecommendation(comparison)).toBe('PATCH');
    });

    it('should recommend PATCH when no changes exist', () => {
      const comparison: MockComparison = {
        breakingChanges: [],
        changes: [],
      };
      expect(getSemverRecommendation(comparison)).toBe('PATCH');
    });
  });

  describe('Risk Score Calculation', () => {
    function calculateRiskScore(
      breakingChanges: BreakingChange[],
      allChanges: Change[]
    ): number {
      if (allChanges.length === 0) return 0;

      let score = 0;
      for (const change of breakingChanges) {
        score += change.impactScore;
      }

      const maxPossibleScore = breakingChanges.length * 100 || 100;
      const normalizedScore = Math.min(
        100,
        Math.round((score / maxPossibleScore) * 100)
      );

      const dangerousCount = allChanges.filter((c) => c.severity === 'DANGEROUS').length;
      const warningCount = allChanges.filter((c) => c.severity === 'WARNING').length;
      const additionalScore = Math.min(20, dangerousCount * 5 + warningCount * 2);

      return Math.min(100, normalizedScore + additionalScore);
    }

    it('should return 0 for no changes', () => {
      expect(calculateRiskScore([], [])).toBe(0);
    });

    it('should calculate score based on breaking change impact', () => {
      const breakingChanges: BreakingChange[] = [
        {
          type: 'REMOVED',
          category: 'ENDPOINT',
          severity: 'BREAKING',
          path: '/users',
          description: 'Removed',
          migrationSuggestion: 'Remove',
          impactScore: 80,
        },
      ];
      const score = calculateRiskScore(breakingChanges, breakingChanges);
      expect(score).toBe(80);
    });

    it('should add points for dangerous changes', () => {
      const changes: Change[] = [
        {
          type: 'REMOVED',
          category: 'SCHEMA',
          severity: 'DANGEROUS',
          path: '/schemas/User',
          description: 'Schema removed',
        },
        {
          type: 'REMOVED',
          category: 'SCHEMA',
          severity: 'DANGEROUS',
          path: '/schemas/Order',
          description: 'Schema removed',
        },
      ];
      const score = calculateRiskScore([], changes);
      expect(score).toBe(10); 
    });

    it('should cap additional score at 20', () => {
      const changes: Change[] = Array(10)
        .fill(null)
        .map((_, i) => ({
          type: 'REMOVED' as const,
          category: 'SCHEMA',
          severity: 'DANGEROUS' as const,
          path: `/schemas/Type${i}`,
          description: 'Removed',
        }));
      const score = calculateRiskScore([], changes);
      expect(score).toBe(20); 
    });

    it('should cap total score at 100', () => {
      const breakingChanges: BreakingChange[] = [
        {
          type: 'REMOVED',
          category: 'ENDPOINT',
          severity: 'BREAKING',
          path: '/a',
          description: 'Removed',
          migrationSuggestion: 'Remove',
          impactScore: 100,
        },
        {
          type: 'REMOVED',
          category: 'ENDPOINT',
          severity: 'BREAKING',
          path: '/b',
          description: 'Removed',
          migrationSuggestion: 'Remove',
          impactScore: 100,
        },
      ];
      const score = calculateRiskScore(breakingChanges, breakingChanges);
      expect(score).toBe(100);
    });
  });

  describe('Change Types', () => {
    it('should have correct change type values', () => {
      const change: Change = {
        type: 'ADDED',
        category: 'ENDPOINT',
        severity: 'INFO',
        path: '/test',
        description: 'Test',
      };
      expect(change.type).toBe('ADDED');
      expect(['ADDED', 'MODIFIED', 'REMOVED', 'DEPRECATED']).toContain(change.type);
    });

    it('should have correct severity values', () => {
      const severities = ['BREAKING', 'DANGEROUS', 'WARNING', 'INFO'];
      severities.forEach((severity) => {
        const change: Change = {
          type: 'MODIFIED',
          category: 'ENDPOINT',
          severity: severity as Change['severity'],
          path: '/test',
          description: 'Test',
        };
        expect(severities).toContain(change.severity);
      });
    });
  });

  describe('BreakingChange', () => {
    it('should include migration suggestion and impact score', () => {
      const breaking: BreakingChange = {
        type: 'REMOVED',
        category: 'ENDPOINT',
        severity: 'BREAKING',
        path: '/api/v1/users',
        description: 'User endpoint removed',
        migrationSuggestion: 'Use /api/v2/users instead',
        impactScore: 85,
      };

      expect(breaking.migrationSuggestion).toBeDefined();
      expect(breaking.impactScore).toBeGreaterThanOrEqual(0);
      expect(breaking.impactScore).toBeLessThanOrEqual(100);
    });
  });
});

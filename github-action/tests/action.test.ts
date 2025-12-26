import * as core from '@actions/core';
import * as github from '@actions/github';

// Mock the modules before importing
jest.mock('@actions/core');
jest.mock('@actions/github');
jest.mock('@actions/exec');

// Mock the imports
jest.mock('../src/changelog/detector', () => ({
  detectBreakingChanges: jest.fn(),
}));

jest.mock('../src/github/comment', () => ({
  postPrComment: jest.fn(),
  updatePrComment: jest.fn(),
}));

jest.mock('../src/github/check', () => ({
  createCheckRun: jest.fn(),
  updateCheckRun: jest.fn(),
}));

import { run } from '../src/action';
import { detectBreakingChanges } from '../src/changelog/detector';
import { postPrComment } from '../src/github/comment';
import { createCheckRun } from '../src/github/check';

describe('Action', () => {
  const mockInputs = new Map<string, string>();
  const mockContext = {
    repo: { owner: 'test-owner', repo: 'test-repo' },
    sha: 'abc123',
    payload: {},
  };

  beforeEach(() => {
    jest.clearAllMocks();
    mockInputs.clear();

    // Mock core.getInput
    (core.getInput as jest.Mock).mockImplementation((name: string) => {
      return mockInputs.get(name) || '';
    });

    // Mock core.getBooleanInput
    (core.getBooleanInput as jest.Mock).mockImplementation((name: string) => {
      const value = mockInputs.get(name);
      return value === 'true';
    });

    // Mock core.summary
    const mockSummary = {
      addHeading: jest.fn().mockReturnThis(),
      addTable: jest.fn().mockReturnThis(),
      addRaw: jest.fn().mockReturnThis(),
      addDetails: jest.fn().mockReturnThis(),
      write: jest.fn().mockResolvedValue(undefined),
    };
    (core as any).summary = mockSummary;

    // Mock github.context
    Object.defineProperty(github, 'context', {
      value: mockContext,
      writable: true,
    });

    // Mock github.getOctokit
    (github.getOctokit as jest.Mock).mockReturnValue({
      rest: {
        issues: {
          listComments: jest.fn().mockResolvedValue({ data: [] }),
          createComment: jest.fn().mockResolvedValue({ data: { id: 1 } }),
        },
        checks: {
          listForRef: jest.fn().mockResolvedValue({ data: { check_runs: [] } }),
          create: jest.fn().mockResolvedValue({ data: { id: 1 } }),
        },
      },
    });
  });

  describe('run', () => {
    it('should run successfully with no breaking changes', async () => {
      // Setup inputs
      mockInputs.set('spec-path', 'api/openapi.yaml');
      mockInputs.set('base-ref', 'main');
      mockInputs.set('head-ref', 'HEAD');
      mockInputs.set('fail-on-breaking', 'false');
      mockInputs.set('comment-on-pr', 'false');
      mockInputs.set('create-check', 'false');

      // Mock detectBreakingChanges
      (detectBreakingChanges as jest.Mock).mockResolvedValue({
        hasBreakingChanges: false,
        breakingChangesCount: 0,
        totalChangesCount: 2,
        riskLevel: 'LOW',
        riskScore: 10,
        semverRecommendation: 'MINOR',
        changelog: '# Changelog\n\n- Added new endpoint',
        changes: [],
        breakingChanges: [],
        oldSpec: { name: 'API', version: '1.0.0' },
        newSpec: { name: 'API', version: '1.1.0' },
      });

      await run();

      expect(core.setFailed).not.toHaveBeenCalled();
      expect(core.info).toHaveBeenCalledWith('No breaking changes detected');
    });

    it('should fail when breaking changes detected and fail-on-breaking is true', async () => {
      mockInputs.set('spec-path', 'api/openapi.yaml');
      mockInputs.set('fail-on-breaking', 'true');
      mockInputs.set('comment-on-pr', 'false');
      mockInputs.set('create-check', 'false');

      (detectBreakingChanges as jest.Mock).mockResolvedValue({
        hasBreakingChanges: true,
        breakingChangesCount: 2,
        totalChangesCount: 5,
        riskLevel: 'HIGH',
        riskScore: 65,
        semverRecommendation: 'MAJOR',
        changelog: '# Changelog\n\n## Breaking Changes\n- Removed endpoint',
        changes: [],
        breakingChanges: [
          {
            type: 'REMOVED',
            category: 'ENDPOINT',
            severity: 'BREAKING',
            path: 'DELETE /users/{id}',
            description: 'Endpoint removed',
            migrationSuggestion: 'Remove references',
            impactScore: 80,
          },
        ],
        oldSpec: { name: 'API', version: '1.0.0' },
        newSpec: { name: 'API', version: '2.0.0' },
      });

      await run();

      expect(core.setFailed).toHaveBeenCalled();
      expect((core.setFailed as jest.Mock).mock.calls[0][0]).toContain('breaking change');
    });

    it('should warn but not fail when fail-on-breaking is false', async () => {
      mockInputs.set('spec-path', 'api/openapi.yaml');
      mockInputs.set('fail-on-breaking', 'false');
      mockInputs.set('comment-on-pr', 'false');
      mockInputs.set('create-check', 'false');

      (detectBreakingChanges as jest.Mock).mockResolvedValue({
        hasBreakingChanges: true,
        breakingChangesCount: 1,
        totalChangesCount: 3,
        riskLevel: 'MEDIUM',
        riskScore: 40,
        semverRecommendation: 'MAJOR',
        changelog: '# Changelog',
        changes: [],
        breakingChanges: [
          {
            type: 'MODIFIED',
            category: 'PARAMETER',
            severity: 'BREAKING',
            path: 'POST /users',
            description: 'Required parameter added',
            migrationSuggestion: 'Add parameter',
            impactScore: 50,
          },
        ],
        oldSpec: { name: 'API', version: '1.0.0' },
        newSpec: { name: 'API', version: '2.0.0' },
      });

      await run();

      expect(core.setFailed).not.toHaveBeenCalled();
      expect(core.warning).toHaveBeenCalled();
    });

    it('should post PR comment when enabled and in PR context', async () => {
      mockInputs.set('spec-path', 'api/openapi.yaml');
      mockInputs.set('comment-on-pr', 'true');
      mockInputs.set('create-check', 'false');
      mockInputs.set('fail-on-breaking', 'false');
      mockInputs.set('github-token', 'test-token');

      // Set PR context
      Object.defineProperty(github, 'context', {
        value: {
          ...mockContext,
          payload: {
            pull_request: { number: 123 },
          },
        },
      });

      (detectBreakingChanges as jest.Mock).mockResolvedValue({
        hasBreakingChanges: false,
        breakingChangesCount: 0,
        totalChangesCount: 1,
        riskLevel: 'LOW',
        riskScore: 5,
        semverRecommendation: 'PATCH',
        changelog: '# Changelog',
        changes: [],
        breakingChanges: [],
        oldSpec: { name: 'API', version: '1.0.0' },
        newSpec: { name: 'API', version: '1.0.1' },
      });

      (postPrComment as jest.Mock).mockResolvedValue(1);

      await run();

      expect(postPrComment).toHaveBeenCalled();
    });

    it('should create check run when enabled', async () => {
      mockInputs.set('spec-path', 'api/openapi.yaml');
      mockInputs.set('comment-on-pr', 'false');
      mockInputs.set('create-check', 'true');
      mockInputs.set('fail-on-breaking', 'false');
      mockInputs.set('github-token', 'test-token');

      (detectBreakingChanges as jest.Mock).mockResolvedValue({
        hasBreakingChanges: false,
        breakingChangesCount: 0,
        totalChangesCount: 0,
        riskLevel: 'LOW',
        riskScore: 0,
        semverRecommendation: 'PATCH',
        changelog: '# No changes',
        changes: [],
        breakingChanges: [],
        oldSpec: { name: 'API', version: '1.0.0' },
        newSpec: { name: 'API', version: '1.0.0' },
      });

      (createCheckRun as jest.Mock).mockResolvedValue(1);

      await run();

      expect(createCheckRun).toHaveBeenCalled();
    });
  });

  describe('input parsing', () => {
    it('should use default values when inputs not provided', async () => {
      mockInputs.set('fail-on-breaking', 'false');
      mockInputs.set('comment-on-pr', 'false');
      mockInputs.set('create-check', 'false');

      (detectBreakingChanges as jest.Mock).mockResolvedValue({
        hasBreakingChanges: false,
        breakingChangesCount: 0,
        totalChangesCount: 0,
        riskLevel: 'LOW',
        riskScore: 0,
        semverRecommendation: 'PATCH',
        changelog: '',
        changes: [],
        breakingChanges: [],
        oldSpec: { name: 'API', version: '1.0.0' },
        newSpec: { name: 'API', version: '1.0.0' },
      });

      await run();

      // Verify detectBreakingChanges was called with default spec-path
      const callArgs = (detectBreakingChanges as jest.Mock).mock.calls[0][0];
      expect(callArgs.specPath).toBe('api/openapi.yaml');
    });
  });

  describe('output setting', () => {
    it('should set all outputs correctly', async () => {
      mockInputs.set('fail-on-breaking', 'false');
      mockInputs.set('comment-on-pr', 'false');
      mockInputs.set('create-check', 'false');

      const mockResult = {
        hasBreakingChanges: true,
        breakingChangesCount: 3,
        totalChangesCount: 10,
        riskLevel: 'HIGH',
        riskScore: 70,
        semverRecommendation: 'MAJOR',
        changelog: '# Full changelog',
        changelogFile: 'changelog.md',
        changes: [],
        breakingChanges: [],
        oldSpec: { name: 'API', version: '1.0.0' },
        newSpec: { name: 'API', version: '2.0.0' },
      };

      (detectBreakingChanges as jest.Mock).mockResolvedValue(mockResult);

      await run();

      expect(core.setOutput).toHaveBeenCalledWith('has-breaking-changes', 'true');
      expect(core.setOutput).toHaveBeenCalledWith('breaking-changes-count', '3');
      expect(core.setOutput).toHaveBeenCalledWith('total-changes-count', '10');
      expect(core.setOutput).toHaveBeenCalledWith('risk-level', 'HIGH');
      expect(core.setOutput).toHaveBeenCalledWith('risk-score', '70');
      expect(core.setOutput).toHaveBeenCalledWith('semver-recommendation', 'MAJOR');
    });
  });
});

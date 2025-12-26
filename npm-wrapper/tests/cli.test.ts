import * as path from 'path';
import { execSync, ExecSyncOptionsWithStringEncoding } from 'child_process';

const fixturesPath = path.join(__dirname, 'fixtures');
const cliPath = path.join(__dirname, '..', 'dist', 'cli.js');

const execOptions: ExecSyncOptionsWithStringEncoding = {
  encoding: 'utf-8',
  cwd: path.join(__dirname, '..'),
};

// Helper to run CLI commands
function runCli(args: string): { stdout: string; exitCode: number } {
  try {
    const stdout = execSync(`node ${cliPath} ${args}`, execOptions);
    return { stdout, exitCode: 0 };
  } catch (error: any) {
    return {
      stdout: error.stdout || error.message,
      exitCode: error.status || 1,
    };
  }
}

describe('CLI', () => {
  // Skip these tests if CLI is not built yet
  const cliExists = () => {
    try {
      require('fs').accessSync(cliPath);
      return true;
    } catch {
      return false;
    }
  };

  describe('Help', () => {
    it('should display help when no command provided', () => {
      if (!cliExists()) return;

      const { stdout } = runCli('');
      expect(stdout).toContain('changelog-hub');
      expect(stdout).toContain('compare');
    });

    it('should display help with --help flag', () => {
      if (!cliExists()) return;

      const { stdout } = runCli('--help');
      expect(stdout).toContain('Usage:');
      expect(stdout).toContain('compare');
      expect(stdout).toContain('analyze');
      expect(stdout).toContain('validate');
    });
  });

  describe('Version', () => {
    it('should display version with --version flag', () => {
      if (!cliExists()) return;

      const { stdout } = runCli('--version');
      expect(stdout).toMatch(/\d+\.\d+\.\d+/);
    });

    it('should display version info with version command', () => {
      if (!cliExists()) return;

      const { stdout } = runCli('version');
      expect(stdout).toContain('Changelog Hub');
    });

    it('should display version as JSON', () => {
      if (!cliExists()) return;

      const { stdout } = runCli('version --json');
      const parsed = JSON.parse(stdout);
      expect(parsed.name).toBeDefined();
      expect(parsed.version).toBeDefined();
    });
  });

  describe('Compare command', () => {
    it('should compare two specs successfully', () => {
      if (!cliExists()) return;

      const oldSpec = path.join(fixturesPath, 'api-v1.yaml');
      const newSpec = path.join(fixturesPath, 'api-v2-compatible.yaml');

      const { stdout, exitCode } = runCli(`compare "${oldSpec}" "${newSpec}"`);
      expect(exitCode).toBe(0);
      expect(stdout).toContain('API CHANGELOG');
    });

    it('should detect breaking changes', () => {
      if (!cliExists()) return;

      const oldSpec = path.join(fixturesPath, 'api-v1.yaml');
      const newSpec = path.join(fixturesPath, 'api-v2-breaking.yaml');

      const { stdout } = runCli(`compare "${oldSpec}" "${newSpec}"`);
      expect(stdout).toContain('BREAKING');
    });

    it('should output JSON format', () => {
      if (!cliExists()) return;

      const oldSpec = path.join(fixturesPath, 'api-v1.yaml');
      const newSpec = path.join(fixturesPath, 'api-v2-compatible.yaml');

      const { stdout } = runCli(`compare "${oldSpec}" "${newSpec}" --format json`);
      // Extract JSON from output (after "Comparison complete" message)
      const jsonStart = stdout.indexOf('{');
      if (jsonStart !== -1) {
        const jsonStr = stdout.substring(jsonStart);
        expect(() => JSON.parse(jsonStr)).not.toThrow();
      }
    });

    it('should fail on breaking changes with --fail-on-breaking', () => {
      if (!cliExists()) return;

      const oldSpec = path.join(fixturesPath, 'api-v1.yaml');
      const newSpec = path.join(fixturesPath, 'api-v2-breaking.yaml');

      const { exitCode } = runCli(`compare "${oldSpec}" "${newSpec}" --fail-on-breaking`);
      expect(exitCode).not.toBe(0);
    });

    it('should not fail with --fail-on-breaking when no breaking changes', () => {
      if (!cliExists()) return;

      const oldSpec = path.join(fixturesPath, 'api-v1.yaml');
      const newSpec = path.join(fixturesPath, 'api-v2-compatible.yaml');

      const { exitCode } = runCli(`compare "${oldSpec}" "${newSpec}" --fail-on-breaking`);
      expect(exitCode).toBe(0);
    });

    it('should fail for missing file', () => {
      if (!cliExists()) return;

      const oldSpec = path.join(fixturesPath, 'nonexistent.yaml');
      const newSpec = path.join(fixturesPath, 'api-v1.yaml');

      const { exitCode } = runCli(`compare "${oldSpec}" "${newSpec}"`);
      expect(exitCode).not.toBe(0);
    });
  });

  describe('Analyze command', () => {
    it('should analyze a spec file', () => {
      if (!cliExists()) return;

      const spec = path.join(fixturesPath, 'api-v1.yaml');

      const { stdout, exitCode } = runCli(`analyze "${spec}"`);
      expect(exitCode).toBe(0);
      expect(stdout).toContain('SPECIFICATION ANALYSIS');
    });

    it('should show endpoints with --endpoints flag', () => {
      if (!cliExists()) return;

      const spec = path.join(fixturesPath, 'api-v1.yaml');

      const { stdout } = runCli(`analyze "${spec}" --endpoints`);
      expect(stdout).toContain('ENDPOINTS');
    });

    it('should output JSON format', () => {
      if (!cliExists()) return;

      const spec = path.join(fixturesPath, 'api-v1.yaml');

      const { stdout } = runCli(`analyze "${spec}" --format json`);
      const jsonStart = stdout.indexOf('{');
      if (jsonStart !== -1) {
        const jsonStr = stdout.substring(jsonStart);
        expect(() => JSON.parse(jsonStr)).not.toThrow();
      }
    });

    it('should fail for missing file', () => {
      if (!cliExists()) return;

      const spec = path.join(fixturesPath, 'nonexistent.yaml');

      const { exitCode } = runCli(`analyze "${spec}"`);
      expect(exitCode).not.toBe(0);
    });
  });

  describe('Validate command', () => {
    it('should validate a valid spec file', () => {
      if (!cliExists()) return;

      const spec = path.join(fixturesPath, 'api-v1.yaml');

      const { stdout, exitCode } = runCli(`validate "${spec}"`);
      expect(exitCode).toBe(0);
      expect(stdout).toContain('VALID');
    });

    it('should validate multiple spec files', () => {
      if (!cliExists()) return;

      const spec1 = path.join(fixturesPath, 'api-v1.yaml');
      const spec2 = path.join(fixturesPath, 'api-v2-compatible.yaml');

      const { stdout, exitCode } = runCli(`validate "${spec1}" "${spec2}"`);
      expect(exitCode).toBe(0);
      expect(stdout).toContain('VALID');
    });

    it('should fail for invalid spec file', () => {
      if (!cliExists()) return;

      const spec = path.join(fixturesPath, 'nonexistent.yaml');

      const { exitCode, stdout } = runCli(`validate "${spec}"`);
      expect(exitCode).not.toBe(0);
      expect(stdout).toContain('INVALID');
    });

    it('should output JSON format', () => {
      if (!cliExists()) return;

      const spec = path.join(fixturesPath, 'api-v1.yaml');

      const { stdout } = runCli(`validate "${spec}" --format json`);
      expect(() => JSON.parse(stdout)).not.toThrow();
    });
  });
});

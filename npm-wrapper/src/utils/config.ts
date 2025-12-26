import * as fs from 'fs';
import * as path from 'path';
import * as yaml from 'yaml';

/**
 * Default configuration
 */
export interface Config {
  /** Default output format */
  defaultFormat: 'console' | 'markdown' | 'json' | 'html';
  /** Whether to fail on breaking changes by default */
  failOnBreaking: boolean;
  /** Default spec path pattern */
  specPath: string;
  /** Severity threshold for reporting */
  severityThreshold: 'INFO' | 'WARNING' | 'DANGEROUS' | 'BREAKING';
  /** Include deprecation warnings */
  includeDeprecations: boolean;
  /** Custom rules for breaking change detection */
  customRules: CustomRule[];
}

/**
 * Custom rule for breaking change detection
 */
export interface CustomRule {
  name: string;
  pattern: string;
  severity: 'BREAKING' | 'DANGEROUS' | 'WARNING' | 'INFO';
  message: string;
}

/**
 * Default configuration values
 */
const defaultConfig: Config = {
  defaultFormat: 'console',
  failOnBreaking: false,
  specPath: 'api/openapi.yaml',
  severityThreshold: 'INFO',
  includeDeprecations: true,
  customRules: [],
};

/**
 * Configuration file names to search for
 */
const configFileNames = [
  '.changelog-hub.yaml',
  '.changelog-hub.yml',
  '.changelog-hub.json',
  'changelog-hub.config.yaml',
  'changelog-hub.config.yml',
  'changelog-hub.config.json',
];

/**
 * Load configuration from file
 * @param configPath - Optional path to config file
 * @returns Merged configuration
 */
export function loadConfig(configPath?: string): Config {
  let configFile: string | undefined;

  if (configPath) {
    // Use provided config path
    configFile = path.resolve(configPath);
    if (!fs.existsSync(configFile)) {
      throw new Error(`Config file not found: ${configPath}`);
    }
  } else {
    // Search for config file in current directory
    const cwd = process.cwd();
    for (const name of configFileNames) {
      const fullPath = path.join(cwd, name);
      if (fs.existsSync(fullPath)) {
        configFile = fullPath;
        break;
      }
    }
  }

  if (!configFile) {
    return defaultConfig;
  }

  // Read and parse config file
  const content = fs.readFileSync(configFile, 'utf-8');
  const ext = path.extname(configFile).toLowerCase();

  let userConfig: Partial<Config>;
  if (ext === '.json') {
    userConfig = JSON.parse(content);
  } else {
    userConfig = yaml.parse(content);
  }

  // Merge with defaults
  return {
    ...defaultConfig,
    ...userConfig,
  };
}

/**
 * Save configuration to file
 * @param config - Configuration to save
 * @param filePath - Path to save to
 */
export function saveConfig(config: Config, filePath: string): void {
  const absolutePath = path.resolve(filePath);
  const ext = path.extname(absolutePath).toLowerCase();

  let content: string;
  if (ext === '.json') {
    content = JSON.stringify(config, null, 2);
  } else {
    content = yaml.stringify(config);
  }

  fs.writeFileSync(absolutePath, content, 'utf-8');
}

/**
 * Create a default configuration file
 * @param format - Format of config file ('yaml' or 'json')
 * @returns Path to created config file
 */
export function createDefaultConfig(format: 'yaml' | 'json' = 'yaml'): string {
  const fileName = format === 'json' ? '.changelog-hub.json' : '.changelog-hub.yaml';
  const filePath = path.join(process.cwd(), fileName);
  saveConfig(defaultConfig, filePath);
  return filePath;
}

/**
 * Get default configuration
 * @returns Default configuration
 */
export function getDefaultConfig(): Config {
  return { ...defaultConfig };
}

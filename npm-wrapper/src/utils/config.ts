import * as fs from 'fs';
import * as path from 'path';
import * as yaml from 'yaml';

export interface Config {
  
  defaultFormat: 'console' | 'markdown' | 'json' | 'html';
  
  failOnBreaking: boolean;
  
  specPath: string;
  
  severityThreshold: 'INFO' | 'WARNING' | 'DANGEROUS' | 'BREAKING';
  
  includeDeprecations: boolean;
  
  customRules: CustomRule[];
}

export interface CustomRule {
  name: string;
  pattern: string;
  severity: 'BREAKING' | 'DANGEROUS' | 'WARNING' | 'INFO';
  message: string;
}

const defaultConfig: Config = {
  defaultFormat: 'console',
  failOnBreaking: false,
  specPath: 'api/openapi.yaml',
  severityThreshold: 'INFO',
  includeDeprecations: true,
  customRules: [],
};

const configFileNames = [
  '.changelog-hub.yaml',
  '.changelog-hub.yml',
  '.changelog-hub.json',
  'changelog-hub.config.yaml',
  'changelog-hub.config.yml',
  'changelog-hub.config.json',
];

export function loadConfig(configPath?: string): Config {
  let configFile: string | undefined;

  if (configPath) {
    
    configFile = path.resolve(configPath);
    if (!fs.existsSync(configFile)) {
      throw new Error(`Config file not found: ${configPath}`);
    }
  } else {
    
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

  const content = fs.readFileSync(configFile, 'utf-8');
  const ext = path.extname(configFile).toLowerCase();

  let userConfig: Partial<Config>;
  if (ext === '.json') {
    userConfig = JSON.parse(content);
  } else {
    userConfig = yaml.parse(content);
  }

  return {
    ...defaultConfig,
    ...userConfig,
  };
}

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

export function createDefaultConfig(format: 'yaml' | 'json' = 'yaml'): string {
  const fileName = format === 'json' ? '.changelog-hub.json' : '.changelog-hub.yaml';
  const filePath = path.join(process.cwd(), fileName);
  saveConfig(defaultConfig, filePath);
  return filePath;
}

export function getDefaultConfig(): Config {
  return { ...defaultConfig };
}

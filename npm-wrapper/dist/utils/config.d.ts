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
export declare function loadConfig(configPath?: string): Config;
export declare function saveConfig(config: Config, filePath: string): void;
export declare function createDefaultConfig(format?: 'yaml' | 'json'): string;
export declare function getDefaultConfig(): Config;
//# sourceMappingURL=config.d.ts.map
export type OutputFormat = 'console' | 'markdown' | 'json';
export type SeverityThreshold = 'INFO' | 'WARNING' | 'DANGEROUS' | 'BREAKING';
export interface ActionInputs {
    oldSpec?: string;
    newSpec?: string;
    specPath: string;
    baseRef: string;
    headRef: string;
    format: OutputFormat;
    failOnBreaking: boolean;
    commentOnPr: boolean;
    createCheck: boolean;
    githubToken: string;
    includePatterns?: string[];
    excludePatterns?: string[];
    severityThreshold: SeverityThreshold;
}
export declare function getInputs(): ActionInputs;
export declare function validateInputs(inputs: ActionInputs): void;

export type SpecType = 'openapi' | 'asyncapi' | 'graphql' | 'grpc' | 'unknown';

export type ParameterLocation = 'path' | 'query' | 'header' | 'cookie' | 'body';

export type ChangeType = 'ADDED' | 'REMOVED' | 'MODIFIED' | 'DEPRECATED';

export type ChangeCategory = 'ENDPOINT' | 'PARAMETER' | 'SCHEMA' | 'SECURITY' | 'RESPONSE';

export type ChangeSeverity = 'INFO' | 'WARNING' | 'DANGEROUS' | 'BREAKING';

export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export type SemverRecommendation = 'MAJOR' | 'MINOR' | 'PATCH' | 'NONE';

export type OutputFormat = 'console' | 'markdown' | 'json' | 'html';

export interface ApiSpec {
  name: string;
  version: string;
  type: SpecType;
  endpoints: Endpoint[];
  schemas: Schema[];
  security: SecurityScheme[];
  raw: unknown;
}

export interface Endpoint {
  id: string;
  path: string;
  method: string;
  operationId?: string;
  summary?: string;
  description?: string;
  parameters: Parameter[];
  requestBody?: RequestBody;
  responses: Response[];
  deprecated: boolean;
  tags: string[];
}

export interface Parameter {
  name: string;
  location: ParameterLocation;
  type: string;
  required: boolean;
  description?: string;
  defaultValue?: unknown;
  schema?: string;
}

export interface RequestBody {
  contentTypes: string[];
  required: boolean;
  schema?: string;
  description?: string;
}

export interface Response {
  statusCode: string;
  description: string;
  contentType?: string;
  schema?: string;
}

export interface Schema {
  name: string;
  type: string;
  properties: SchemaProperty[];
  required: string[];
  description?: string;
}

export interface SchemaProperty {
  name: string;
  type: string;
  required: boolean;
  description?: string;
  format?: string;
  enum?: unknown[];
}

export interface SecurityScheme {
  name: string;
  type: string;
  description?: string;
}

export interface Change {
  type: ChangeType;
  category: ChangeCategory;
  severity: ChangeSeverity;
  path: string;
  description: string;
  oldValue?: unknown;
  newValue?: unknown;
}

export interface BreakingChange {
  type: ChangeType;
  category: ChangeCategory;
  path: string;
  description: string;
  migrationSuggestion: string;
  impactScore: number;
}

export interface ComparisonSummary {
  endpointsAdded: number;
  endpointsRemoved: number;
  endpointsModified: number;
  endpointsDeprecated: number;
  schemasAdded: number;
  schemasRemoved: number;
  schemasModified: number;
  parametersAdded: number;
  parametersRemoved: number;
  parametersModified: number;
}

export interface ComparisonResult {
  apiName: string;
  fromVersion: string;
  toVersion: string;
  changes: Change[];
  breakingChanges: BreakingChange[];
  totalChanges: number;
  riskScore: number;
  riskLevel: RiskLevel;
  semverRecommendation: SemverRecommendation;
  summary: ComparisonSummary;
}

export interface ExtensionConfig {
  defaultFormat: OutputFormat;
  autoDetectSpecs: boolean;
  specPatterns: string[];
  showInlineWarnings: boolean;
  baseRef: string;
  severityThreshold: ChangeSeverity;
}

export interface TreeItemData {
  label: string;
  description?: string;
  tooltip?: string;
  iconPath?: string;
  contextValue?: string;
  command?: {
    command: string;
    title: string;
    arguments?: unknown[];
  };
}

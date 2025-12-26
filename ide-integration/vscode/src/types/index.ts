/**
 * Supported API specification types
 */
export type SpecType = 'openapi' | 'asyncapi' | 'graphql' | 'grpc' | 'unknown';

/**
 * Parameter location in HTTP request
 */
export type ParameterLocation = 'path' | 'query' | 'header' | 'cookie' | 'body';

/**
 * Change type enumeration
 */
export type ChangeType = 'ADDED' | 'REMOVED' | 'MODIFIED' | 'DEPRECATED';

/**
 * Change category
 */
export type ChangeCategory = 'ENDPOINT' | 'PARAMETER' | 'SCHEMA' | 'SECURITY' | 'RESPONSE';

/**
 * Severity levels for changes
 */
export type ChangeSeverity = 'INFO' | 'WARNING' | 'DANGEROUS' | 'BREAKING';

/**
 * Risk level for overall comparison
 */
export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

/**
 * Semantic versioning recommendation
 */
export type SemverRecommendation = 'MAJOR' | 'MINOR' | 'PATCH' | 'NONE';

/**
 * Output format options
 */
export type OutputFormat = 'console' | 'markdown' | 'json' | 'html';

/**
 * API specification structure
 */
export interface ApiSpec {
  name: string;
  version: string;
  type: SpecType;
  endpoints: Endpoint[];
  schemas: Schema[];
  security: SecurityScheme[];
  raw: unknown;
}

/**
 * API endpoint definition
 */
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

/**
 * API parameter definition
 */
export interface Parameter {
  name: string;
  location: ParameterLocation;
  type: string;
  required: boolean;
  description?: string;
  defaultValue?: unknown;
  schema?: string;
}

/**
 * Request body definition
 */
export interface RequestBody {
  contentTypes: string[];
  required: boolean;
  schema?: string;
  description?: string;
}

/**
 * Response definition
 */
export interface Response {
  statusCode: string;
  description: string;
  contentType?: string;
  schema?: string;
}

/**
 * Schema definition
 */
export interface Schema {
  name: string;
  type: string;
  properties: SchemaProperty[];
  required: string[];
  description?: string;
}

/**
 * Schema property definition
 */
export interface SchemaProperty {
  name: string;
  type: string;
  required: boolean;
  description?: string;
  format?: string;
  enum?: unknown[];
}

/**
 * Security scheme definition
 */
export interface SecurityScheme {
  name: string;
  type: string;
  description?: string;
}

/**
 * Represents a single change between API versions
 */
export interface Change {
  type: ChangeType;
  category: ChangeCategory;
  severity: ChangeSeverity;
  path: string;
  description: string;
  oldValue?: unknown;
  newValue?: unknown;
}

/**
 * Breaking change with migration info
 */
export interface BreakingChange {
  type: ChangeType;
  category: ChangeCategory;
  path: string;
  description: string;
  migrationSuggestion: string;
  impactScore: number;
}

/**
 * Summary statistics for the comparison
 */
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

/**
 * Full comparison result
 */
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

/**
 * VS Code extension configuration
 */
export interface ExtensionConfig {
  defaultFormat: OutputFormat;
  autoDetectSpecs: boolean;
  specPatterns: string[];
  showInlineWarnings: boolean;
  baseRef: string;
  severityThreshold: ChangeSeverity;
}

/**
 * Tree item data for views
 */
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

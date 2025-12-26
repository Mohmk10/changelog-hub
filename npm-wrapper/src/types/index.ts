/**
 * Supported API specification types
 */
export type SpecType = 'openapi' | 'asyncapi' | 'graphql' | 'grpc' | 'unknown';

/**
 * Risk levels for API changes
 */
export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

/**
 * Semantic versioning recommendations
 */
export type SemverRecommendation = 'MAJOR' | 'MINOR' | 'PATCH';

/**
 * Change types that can be detected
 */
export type ChangeType = 'ADDED' | 'MODIFIED' | 'REMOVED' | 'DEPRECATED';

/**
 * Severity levels for changes
 */
export type ChangeSeverity = 'BREAKING' | 'DANGEROUS' | 'WARNING' | 'INFO';

/**
 * Parameter location types
 */
export type ParameterLocation = 'path' | 'query' | 'header' | 'cookie' | 'body';

/**
 * Output format types
 */
export type OutputFormat = 'console' | 'markdown' | 'json' | 'html';

/**
 * Parsed API specification
 */
export interface ApiSpec {
  /** API name/title */
  name: string;
  /** API version */
  version: string;
  /** Specification type */
  type: SpecType;
  /** List of endpoints/operations */
  endpoints: Endpoint[];
  /** Schemas/models defined */
  schemas: Schema[];
  /** Security definitions */
  security: SecurityDefinition[];
  /** Raw spec data for additional analysis */
  raw: unknown;
}

/**
 * Represents an API endpoint
 */
export interface Endpoint {
  /** Unique identifier for the endpoint */
  id: string;
  /** Path/route of the endpoint */
  path: string;
  /** HTTP method or operation type */
  method: string;
  /** Operation ID if defined */
  operationId?: string;
  /** Summary description */
  summary?: string;
  /** Full description */
  description?: string;
  /** List of parameters */
  parameters: Parameter[];
  /** Request body definition */
  requestBody?: RequestBody;
  /** Response definitions */
  responses: Response[];
  /** Whether the endpoint is deprecated */
  deprecated: boolean;
  /** Tags for categorization */
  tags: string[];
}

/**
 * API parameter definition
 */
export interface Parameter {
  /** Parameter name */
  name: string;
  /** Location: path, query, header, cookie */
  location: ParameterLocation;
  /** Data type */
  type: string;
  /** Whether the parameter is required */
  required: boolean;
  /** Description */
  description?: string;
  /** Default value */
  defaultValue?: unknown;
  /** Schema reference */
  schema?: string;
}

/**
 * Request body definition
 */
export interface RequestBody {
  /** Content types supported */
  contentTypes: string[];
  /** Whether required */
  required: boolean;
  /** Schema reference or inline schema */
  schema?: string;
  /** Description */
  description?: string;
}

/**
 * Response definition
 */
export interface Response {
  /** HTTP status code */
  statusCode: string;
  /** Description */
  description: string;
  /** Content type */
  contentType?: string;
  /** Schema reference */
  schema?: string;
}

/**
 * Schema/model definition
 */
export interface Schema {
  /** Schema name */
  name: string;
  /** Schema type */
  type: string;
  /** Properties for object schemas */
  properties: SchemaProperty[];
  /** Required properties */
  required: string[];
  /** Description */
  description?: string;
}

/**
 * Schema property definition
 */
export interface SchemaProperty {
  /** Property name */
  name: string;
  /** Property type */
  type: string;
  /** Whether required */
  required: boolean;
  /** Description */
  description?: string;
  /** Format (e.g., date-time, email) */
  format?: string;
  /** Enum values if applicable */
  enum?: unknown[];
}

/**
 * Security definition
 */
export interface SecurityDefinition {
  /** Security scheme name */
  name: string;
  /** Scheme type */
  type: string;
  /** Description */
  description?: string;
}

/**
 * Represents a detected change in the API
 */
export interface Change {
  /** Type of change */
  type: ChangeType;
  /** Category of the change (e.g., ENDPOINT, PARAMETER, SCHEMA) */
  category: string;
  /** Severity of the change */
  severity: ChangeSeverity;
  /** Path or location of the change */
  path: string;
  /** Human-readable description */
  description: string;
  /** Old value (for modifications) */
  oldValue?: string;
  /** New value (for modifications) */
  newValue?: string;
}

/**
 * Represents a breaking change with additional migration information
 */
export interface BreakingChange extends Change {
  /** Suggestion for how to migrate */
  migrationSuggestion: string;
  /** Impact score (0-100) */
  impactScore: number;
  /** Affected clients/consumers (if identifiable) */
  affectedClients?: string[];
}

/**
 * Result of comparing two API specifications
 */
export interface ComparisonResult {
  /** API name */
  apiName: string;
  /** From version */
  fromVersion: string;
  /** To version */
  toVersion: string;
  /** All detected changes */
  changes: Change[];
  /** Breaking changes only */
  breakingChanges: BreakingChange[];
  /** Total count of all changes */
  totalChanges: number;
  /** Calculated risk score (0-100) */
  riskScore: number;
  /** Calculated risk level */
  riskLevel: RiskLevel;
  /** Recommended semantic version bump */
  semverRecommendation: SemverRecommendation;
  /** Summary statistics */
  summary: ComparisonSummary;
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
 * API statistics from analysis
 */
export interface ApiStatistics {
  name: string;
  version: string;
  type: SpecType;
  endpoints: number;
  methods: Record<string, number>;
  parameters: number;
  schemas: number;
  deprecated: number;
  securitySchemes: number;
}

/**
 * CLI command options for compare
 */
export interface CompareOptions {
  format: OutputFormat;
  output?: string;
  failOnBreaking: boolean;
  verbose: boolean;
  quiet: boolean;
}

/**
 * CLI command options for analyze
 */
export interface AnalyzeOptions {
  format: 'console' | 'json';
  output?: string;
  verbose: boolean;
}

/**
 * CLI command options for validate
 */
export interface ValidateOptions {
  strict: boolean;
}

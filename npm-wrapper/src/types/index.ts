
export type SpecType = 'openapi' | 'asyncapi' | 'graphql' | 'grpc' | 'unknown';

export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export type SemverRecommendation = 'MAJOR' | 'MINOR' | 'PATCH';

export type ChangeType = 'ADDED' | 'MODIFIED' | 'REMOVED' | 'DEPRECATED';

export type ChangeSeverity = 'BREAKING' | 'DANGEROUS' | 'WARNING' | 'INFO';

export type ParameterLocation = 'path' | 'query' | 'header' | 'cookie' | 'body';

export type OutputFormat = 'console' | 'markdown' | 'json' | 'html';

export interface ApiSpec {
  
  name: string;
  
  version: string;
  
  type: SpecType;
  
  endpoints: Endpoint[];
  
  schemas: Schema[];
  
  security: SecurityDefinition[];
  
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

export interface SecurityDefinition {
  
  name: string;
  
  type: string;
  
  description?: string;
}

export interface Change {
  
  type: ChangeType;
  
  category: string;
  
  severity: ChangeSeverity;
  
  path: string;
  
  description: string;
  
  oldValue?: string;
  
  newValue?: string;
}

export interface BreakingChange extends Change {
  
  migrationSuggestion: string;
  
  impactScore: number;
  
  affectedClients?: string[];
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

export interface CompareOptions {
  format: OutputFormat;
  output?: string;
  failOnBreaking: boolean;
  verbose: boolean;
  quiet: boolean;
}

export interface AnalyzeOptions {
  format: 'console' | 'json';
  output?: string;
  verbose: boolean;
}

export interface ValidateOptions {
  strict: boolean;
}

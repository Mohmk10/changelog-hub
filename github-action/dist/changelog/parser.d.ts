/**
 * Supported API specification types
 */
export type SpecType = 'openapi' | 'asyncapi' | 'graphql' | 'grpc' | 'unknown';
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
    location: 'path' | 'query' | 'header' | 'cookie' | 'body';
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
 * Parses an API specification from content.
 *
 * @param content - Raw specification content (YAML, JSON, etc.)
 * @param filename - Original filename to help determine type
 * @returns Parsed API specification
 */
export declare function parseSpec(content: string, filename: string): ApiSpec;
//# sourceMappingURL=parser.d.ts.map
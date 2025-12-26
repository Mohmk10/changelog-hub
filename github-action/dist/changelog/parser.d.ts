export type SpecType = 'openapi' | 'asyncapi' | 'graphql' | 'grpc' | 'unknown';
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
    parameters: Parameter[];
    requestBody?: RequestBody;
    responses: Response[];
    deprecated: boolean;
    tags: string[];
}
export interface Parameter {
    name: string;
    location: 'path' | 'query' | 'header' | 'cookie' | 'body';
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
export declare function parseSpec(content: string, filename: string): ApiSpec;
//# sourceMappingURL=parser.d.ts.map
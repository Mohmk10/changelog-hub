import * as yaml from 'js-yaml';
import { Logger } from '../utils/logger';

const logger = new Logger('Parser');

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

export function parseSpec(content: string, filename: string): ApiSpec {
  logger.info(`Parsing spec: ${filename}`);

  const ext = getFileExtension(filename);

  try {
    if (ext === 'yaml' || ext === 'yml' || ext === 'json') {
      const spec = ext === 'json' ? JSON.parse(content) : (yaml.load(content) as Record<string, unknown>);

      if (isOpenApiSpec(spec)) {
        logger.info('Detected OpenAPI specification');
        return parseOpenApi(spec);
      } else if (isAsyncApiSpec(spec)) {
        logger.info('Detected AsyncAPI specification');
        return parseAsyncApi(spec);
      } else {
        logger.warn('Unknown YAML/JSON spec format, attempting generic parse');
        return parseGeneric(spec, filename);
      }
    } else if (ext === 'graphql' || ext === 'gql') {
      logger.info('Detected GraphQL schema');
      return parseGraphQL(content);
    } else if (ext === 'proto') {
      logger.info('Detected Protocol Buffer definition');
      return parseProto(content);
    }

    throw new Error(`Unsupported spec format: ${ext}`);
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);
    throw new Error(`Failed to parse spec file '${filename}': ${message}`);
  }
}

function getFileExtension(filename: string): string {
  const parts = filename.split('.');
  return parts.length > 1 ? parts[parts.length - 1].toLowerCase() : '';
}

function isOpenApiSpec(spec: Record<string, unknown>): boolean {
  return Boolean(spec.openapi || spec.swagger);
}

function isAsyncApiSpec(spec: Record<string, unknown>): boolean {
  return Boolean(spec.asyncapi);
}

function parseOpenApi(spec: Record<string, unknown>): ApiSpec {
  const info = (spec.info as Record<string, unknown>) || {};
  const paths = (spec.paths as Record<string, unknown>) || {};
  const components = (spec.components as Record<string, unknown>) || {};
  const definitions = (spec.definitions as Record<string, unknown>) || {};

  const endpoints: Endpoint[] = [];
  const schemas: Schema[] = [];
  const security: SecurityDefinition[] = [];

  for (const [path, pathItem] of Object.entries(paths)) {
    const pathObj = pathItem as Record<string, unknown>;

    for (const [method, operation] of Object.entries(pathObj)) {
      if (['get', 'post', 'put', 'delete', 'patch', 'options', 'head'].includes(method)) {
        const op = operation as Record<string, unknown>;
        endpoints.push(parseEndpoint(path, method, op));
      }
    }
  }

  const schemaSource = (components.schemas as Record<string, unknown>) || definitions;
  for (const [name, schemaDef] of Object.entries(schemaSource || {})) {
    schemas.push(parseSchema(name, schemaDef as Record<string, unknown>));
  }

  const securitySchemes =
    (components.securitySchemes as Record<string, unknown>) ||
    (spec.securityDefinitions as Record<string, unknown>);
  for (const [name, scheme] of Object.entries(securitySchemes || {})) {
    const schemeObj = scheme as Record<string, unknown>;
    security.push({
      name,
      type: String(schemeObj.type || 'unknown'),
      description: schemeObj.description as string | undefined,
    });
  }

  return {
    name: String(info.title || 'Untitled API'),
    version: String(info.version || '1.0.0'),
    type: 'openapi',
    endpoints,
    schemas,
    security,
    raw: spec,
  };
}

function parseEndpoint(path: string, method: string, operation: Record<string, unknown>): Endpoint {
  const parameters: Parameter[] = [];
  const responses: Response[] = [];

  const params = (operation.parameters as Array<Record<string, unknown>>) || [];
  for (const param of params) {
    parameters.push(parseParameter(param));
  }

  let requestBody: RequestBody | undefined;
  if (operation.requestBody) {
    const reqBody = operation.requestBody as Record<string, unknown>;
    const content = (reqBody.content as Record<string, unknown>) || {};
    requestBody = {
      contentTypes: Object.keys(content),
      required: Boolean(reqBody.required),
      description: reqBody.description as string | undefined,
      schema: extractSchemaRef(content),
    };
  }

  const respDefs = (operation.responses as Record<string, unknown>) || {};
  for (const [code, respDef] of Object.entries(respDefs)) {
    const resp = respDef as Record<string, unknown>;
    const content = (resp.content as Record<string, unknown>) || {};
    responses.push({
      statusCode: code,
      description: String(resp.description || ''),
      contentType: Object.keys(content)[0],
      schema: extractSchemaRef(content),
    });
  }

  return {
    id: `${method.toUpperCase()}-${path}`,
    path,
    method: method.toUpperCase(),
    operationId: operation.operationId as string | undefined,
    summary: operation.summary as string | undefined,
    parameters,
    requestBody,
    responses,
    deprecated: Boolean(operation.deprecated),
    tags: (operation.tags as string[]) || [],
  };
}

function parseParameter(param: Record<string, unknown>): Parameter {
  const schema = (param.schema as Record<string, unknown>) || {};
  return {
    name: String(param.name || ''),
    location: param.in as Parameter['location'],
    type: String(schema.type || param.type || 'string'),
    required: Boolean(param.required),
    description: param.description as string | undefined,
    defaultValue: schema.default ?? param.default,
    schema: (schema.$ref as string) || undefined,
  };
}

function parseSchema(name: string, schema: Record<string, unknown>): Schema {
  const properties: SchemaProperty[] = [];
  const props = (schema.properties as Record<string, unknown>) || {};
  const required = (schema.required as string[]) || [];

  for (const [propName, propDef] of Object.entries(props)) {
    const prop = propDef as Record<string, unknown>;
    properties.push({
      name: propName,
      type: String(prop.type || 'any'),
      required: required.includes(propName),
      description: prop.description as string | undefined,
      format: prop.format as string | undefined,
      enum: prop.enum as unknown[] | undefined,
    });
  }

  return {
    name,
    type: String(schema.type || 'object'),
    properties,
    required,
    description: schema.description as string | undefined,
  };
}

function extractSchemaRef(content: Record<string, unknown>): string | undefined {
  for (const mediaType of Object.values(content)) {
    const mt = mediaType as Record<string, unknown>;
    const schema = mt.schema as Record<string, unknown>;
    if (schema?.$ref) {
      return String(schema.$ref);
    }
  }
  return undefined;
}

function parseAsyncApi(spec: Record<string, unknown>): ApiSpec {
  const info = (spec.info as Record<string, unknown>) || {};
  const channels = (spec.channels as Record<string, unknown>) || {};
  const components = (spec.components as Record<string, unknown>) || {};

  const endpoints: Endpoint[] = [];
  const schemas: Schema[] = [];

  for (const [channelName, channelDef] of Object.entries(channels)) {
    const channel = channelDef as Record<string, unknown>;

    if (channel.subscribe) {
      const op = channel.subscribe as Record<string, unknown>;
      endpoints.push({
        id: `SUB-${channelName}`,
        path: channelName,
        method: 'SUBSCRIBE',
        operationId: op.operationId as string | undefined,
        summary: op.summary as string | undefined,
        parameters: [],
        responses: [],
        deprecated: Boolean(op.deprecated),
        tags: (op.tags as string[]) || [],
      });
    }

    if (channel.publish) {
      const op = channel.publish as Record<string, unknown>;
      endpoints.push({
        id: `PUB-${channelName}`,
        path: channelName,
        method: 'PUBLISH',
        operationId: op.operationId as string | undefined,
        summary: op.summary as string | undefined,
        parameters: [],
        responses: [],
        deprecated: Boolean(op.deprecated),
        tags: (op.tags as string[]) || [],
      });
    }
  }

  const schemaSource = (components.schemas as Record<string, unknown>) || {};
  for (const [name, schemaDef] of Object.entries(schemaSource)) {
    schemas.push(parseSchema(name, schemaDef as Record<string, unknown>));
  }

  return {
    name: String(info.title || 'Untitled AsyncAPI'),
    version: String(info.version || '1.0.0'),
    type: 'asyncapi',
    endpoints,
    schemas,
    security: [],
    raw: spec,
  };
}

function parseGraphQL(content: string): ApiSpec {
  const endpoints: Endpoint[] = [];
  const schemas: Schema[] = [];

  const typeRegex = /type\s+(\w+)\s*(?:implements\s+\w+)?\s*\{([^}]+)\}/g;
  let match;

  while ((match = typeRegex.exec(content)) !== null) {
    const [, typeName, fields] = match;

    if (typeName === 'Query' || typeName === 'Mutation' || typeName === 'Subscription') {
      
      const fieldRegex = /(\w+)(?:\([^)]*\))?\s*:\s*(\[?\w+!?\]?!?)/g;
      let fieldMatch;

      while ((fieldMatch = fieldRegex.exec(fields)) !== null) {
        const [, fieldName, returnType] = fieldMatch;
        endpoints.push({
          id: `${typeName}-${fieldName}`,
          path: fieldName,
          method: typeName.toUpperCase(),
          operationId: fieldName,
          parameters: [],
          responses: [{ statusCode: '200', description: returnType }],
          deprecated: false,
          tags: [typeName],
        });
      }
    } else {
      
      const properties: SchemaProperty[] = [];
      const propRegex = /(\w+)\s*:\s*(\[?\w+!?\]?!?)/g;
      let propMatch;

      while ((propMatch = propRegex.exec(fields)) !== null) {
        const [, propName, propType] = propMatch;
        properties.push({
          name: propName,
          type: propType,
          required: propType.endsWith('!'),
        });
      }

      schemas.push({
        name: typeName,
        type: 'object',
        properties,
        required: properties.filter((p) => p.required).map((p) => p.name),
      });
    }
  }

  return {
    name: 'GraphQL Schema',
    version: '1.0.0',
    type: 'graphql',
    endpoints,
    schemas,
    security: [],
    raw: content,
  };
}

function parseProto(content: string): ApiSpec {
  const endpoints: Endpoint[] = [];
  const schemas: Schema[] = [];

  const serviceRegex = /service\s+(\w+)\s*\{([^}]+)\}/g;
  let serviceMatch;

  while ((serviceMatch = serviceRegex.exec(content)) !== null) {
    const [, serviceName, methods] = serviceMatch;

    const methodRegex = /rpc\s+(\w+)\s*\((\w+)\)\s*returns\s*\((\w+)\)/g;
    let methodMatch;

    while ((methodMatch = methodRegex.exec(methods)) !== null) {
      const [, methodName, requestType, responseType] = methodMatch;
      endpoints.push({
        id: `${serviceName}-${methodName}`,
        path: `/${serviceName}/${methodName}`,
        method: 'RPC',
        operationId: methodName,
        parameters: [{ name: 'request', location: 'body', type: requestType, required: true }],
        responses: [{ statusCode: '200', description: responseType }],
        deprecated: false,
        tags: [serviceName],
      });
    }
  }

  const messageRegex = /message\s+(\w+)\s*\{([^}]+)\}/g;
  let messageMatch;

  while ((messageMatch = messageRegex.exec(content)) !== null) {
    const [, messageName, fields] = messageMatch;
    const properties: SchemaProperty[] = [];

    const fieldRegex = /(?:optional|required|repeated)?\s*(\w+)\s+(\w+)\s*=\s*\d+/g;
    let fieldMatch;

    while ((fieldMatch = fieldRegex.exec(fields)) !== null) {
      const [fullMatch, fieldType, fieldName] = fieldMatch;
      properties.push({
        name: fieldName,
        type: fieldType,
        required: fullMatch.includes('required'),
      });
    }

    schemas.push({
      name: messageName,
      type: 'message',
      properties,
      required: properties.filter((p) => p.required).map((p) => p.name),
    });
  }

  return {
    name: 'Protocol Buffer Service',
    version: '1.0.0',
    type: 'grpc',
    endpoints,
    schemas,
    security: [],
    raw: content,
  };
}

function parseGeneric(spec: Record<string, unknown>, filename: string): ApiSpec {
  return {
    name: String(spec.name || spec.title || filename),
    version: String(spec.version || '1.0.0'),
    type: 'unknown',
    endpoints: [],
    schemas: [],
    security: [],
    raw: spec,
  };
}

import * as yaml from 'yaml';
import {
  ApiSpec,
  Endpoint,
  Parameter,
  Schema,
  SchemaProperty,
  SpecType,
} from '../types';

/**
 * Parses an API specification from content.
 *
 * @param content - Raw specification content (YAML, JSON, etc.)
 * @param filename - Original filename to help determine type
 * @returns Parsed API specification
 */
export function parseSpec(content: string, filename: string): ApiSpec {
  const ext = getFileExtension(filename);

  try {
    if (ext === 'yaml' || ext === 'yml' || ext === 'json') {
      const spec = ext === 'json'
        ? JSON.parse(content)
        : yaml.parse(content) as Record<string, unknown>;

      if (isOpenApiSpec(spec)) {
        return parseOpenApi(spec);
      } else if (isAsyncApiSpec(spec)) {
        return parseAsyncApi(spec);
      } else {
        throw new Error('Unknown YAML/JSON spec format');
      }
    } else if (ext === 'graphql' || ext === 'gql') {
      return parseGraphQL(content);
    } else if (ext === 'proto') {
      return parseProto(content);
    }

    throw new Error(`Unsupported spec format: ${ext}`);
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);
    throw new Error(`Failed to parse spec file '${filename}': ${message}`);
  }
}

/**
 * Extracts file extension from filename
 */
function getFileExtension(filename: string): string {
  const parts = filename.split('.');
  return parts.length > 1 ? parts[parts.length - 1].toLowerCase() : '';
}

/**
 * Checks if spec is OpenAPI format
 */
function isOpenApiSpec(spec: Record<string, unknown>): boolean {
  return Boolean(spec.openapi || spec.swagger);
}

/**
 * Checks if spec is AsyncAPI format
 */
function isAsyncApiSpec(spec: Record<string, unknown>): boolean {
  return Boolean(spec.asyncapi);
}

/**
 * Parses OpenAPI/Swagger specification
 */
function parseOpenApi(spec: Record<string, unknown>): ApiSpec {
  const info = (spec.info as Record<string, unknown>) || {};
  const paths = (spec.paths as Record<string, unknown>) || {};
  const components = (spec.components as Record<string, unknown>) || {};
  const definitions = (spec.definitions as Record<string, unknown>) || {};

  const endpoints: Endpoint[] = [];
  const schemas: Schema[] = [];
  const security: ApiSpec['security'] = [];

  // Parse paths/endpoints
  for (const [path, pathItem] of Object.entries(paths)) {
    const pathObj = pathItem as Record<string, unknown>;

    for (const [method, operation] of Object.entries(pathObj)) {
      if (['get', 'post', 'put', 'delete', 'patch', 'options', 'head'].includes(method)) {
        const op = operation as Record<string, unknown>;
        endpoints.push(parseEndpoint(path, method, op));
      }
    }
  }

  // Parse schemas from components (OpenAPI 3.x) or definitions (Swagger 2.x)
  const schemaSource = (components.schemas as Record<string, unknown>) || definitions;
  for (const [name, schemaDef] of Object.entries(schemaSource || {})) {
    schemas.push(parseSchema(name, schemaDef as Record<string, unknown>));
  }

  // Parse security schemes
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

/**
 * Parses a single endpoint from OpenAPI spec
 */
function parseEndpoint(path: string, method: string, operation: Record<string, unknown>): Endpoint {
  const parameters: Parameter[] = [];
  const responses: Endpoint['responses'] = [];

  // Parse parameters
  const params = (operation.parameters as Array<Record<string, unknown>>) || [];
  for (const param of params) {
    parameters.push(parseParameter(param));
  }

  // Parse request body (OpenAPI 3.x)
  let requestBody: Endpoint['requestBody'];
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

  // Parse responses
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
    description: operation.description as string | undefined,
    parameters,
    requestBody,
    responses,
    deprecated: Boolean(operation.deprecated),
    tags: (operation.tags as string[]) || [],
  };
}

/**
 * Parses a parameter definition
 */
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

/**
 * Parses a schema definition
 */
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

/**
 * Extracts schema reference from content object
 */
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

/**
 * Parses AsyncAPI specification
 */
function parseAsyncApi(spec: Record<string, unknown>): ApiSpec {
  const info = (spec.info as Record<string, unknown>) || {};
  const channels = (spec.channels as Record<string, unknown>) || {};
  const components = (spec.components as Record<string, unknown>) || {};

  const endpoints: Endpoint[] = [];
  const schemas: Schema[] = [];

  // Parse channels as endpoints
  for (const [channelName, channelDef] of Object.entries(channels)) {
    const channel = channelDef as Record<string, unknown>;

    // Subscribe operation
    if (channel.subscribe) {
      const op = channel.subscribe as Record<string, unknown>;
      endpoints.push({
        id: `SUB-${channelName}`,
        path: channelName,
        method: 'SUBSCRIBE',
        operationId: op.operationId as string | undefined,
        summary: op.summary as string | undefined,
        description: op.description as string | undefined,
        parameters: [],
        responses: [],
        deprecated: Boolean(op.deprecated),
        tags: (op.tags as string[]) || [],
      });
    }

    // Publish operation
    if (channel.publish) {
      const op = channel.publish as Record<string, unknown>;
      endpoints.push({
        id: `PUB-${channelName}`,
        path: channelName,
        method: 'PUBLISH',
        operationId: op.operationId as string | undefined,
        summary: op.summary as string | undefined,
        description: op.description as string | undefined,
        parameters: [],
        responses: [],
        deprecated: Boolean(op.deprecated),
        tags: (op.tags as string[]) || [],
      });
    }
  }

  // Parse schemas
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

/**
 * Parses GraphQL schema
 */
function parseGraphQL(content: string): ApiSpec {
  const endpoints: Endpoint[] = [];
  const schemas: Schema[] = [];

  // Parse type definitions
  const typeRegex = /type\s+(\w+)\s*(?:implements\s+\w+)?\s*\{([^}]+)\}/g;
  let match;

  while ((match = typeRegex.exec(content)) !== null) {
    const [, typeName, fields] = match;

    if (typeName === 'Query' || typeName === 'Mutation' || typeName === 'Subscription') {
      // Parse operations
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
      // Parse as schema
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

/**
 * Parses Protocol Buffer definition
 */
function parseProto(content: string): ApiSpec {
  const endpoints: Endpoint[] = [];
  const schemas: Schema[] = [];

  // Parse service definitions
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

  // Parse message definitions
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

/**
 * Detect the spec type from content without fully parsing
 */
export function detectSpecType(content: string, filename: string): SpecType {
  const ext = getFileExtension(filename);

  if (ext === 'graphql' || ext === 'gql') {
    return 'graphql';
  }
  if (ext === 'proto') {
    return 'grpc';
  }

  try {
    const spec = ext === 'json' ? JSON.parse(content) : yaml.parse(content);
    if (spec.openapi || spec.swagger) return 'openapi';
    if (spec.asyncapi) return 'asyncapi';
  } catch {
    // Ignore parse errors
  }

  return 'unknown';
}

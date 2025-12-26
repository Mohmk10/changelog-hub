"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
Object.defineProperty(exports, "__esModule", { value: true });
exports.parseSpec = parseSpec;
exports.detectSpecType = detectSpecType;
const yaml = __importStar(require("yaml"));
function parseSpec(content, filename) {
    const ext = getFileExtension(filename);
    try {
        if (ext === 'yaml' || ext === 'yml' || ext === 'json') {
            const spec = ext === 'json'
                ? JSON.parse(content)
                : yaml.parse(content);
            if (isOpenApiSpec(spec)) {
                return parseOpenApi(spec);
            }
            else if (isAsyncApiSpec(spec)) {
                return parseAsyncApi(spec);
            }
            else {
                throw new Error('Unknown YAML/JSON spec format');
            }
        }
        else if (ext === 'graphql' || ext === 'gql') {
            return parseGraphQL(content);
        }
        else if (ext === 'proto') {
            return parseProto(content);
        }
        throw new Error(`Unsupported spec format: ${ext}`);
    }
    catch (error) {
        const message = error instanceof Error ? error.message : String(error);
        throw new Error(`Failed to parse spec file '${filename}': ${message}`);
    }
}
function getFileExtension(filename) {
    const parts = filename.split('.');
    return parts.length > 1 ? parts[parts.length - 1].toLowerCase() : '';
}
function isOpenApiSpec(spec) {
    return Boolean(spec.openapi || spec.swagger);
}
function isAsyncApiSpec(spec) {
    return Boolean(spec.asyncapi);
}
function parseOpenApi(spec) {
    const info = spec.info || {};
    const paths = spec.paths || {};
    const components = spec.components || {};
    const definitions = spec.definitions || {};
    const endpoints = [];
    const schemas = [];
    const security = [];
    for (const [path, pathItem] of Object.entries(paths)) {
        const pathObj = pathItem;
        for (const [method, operation] of Object.entries(pathObj)) {
            if (['get', 'post', 'put', 'delete', 'patch', 'options', 'head'].includes(method)) {
                const op = operation;
                endpoints.push(parseEndpoint(path, method, op));
            }
        }
    }
    const schemaSource = components.schemas || definitions;
    for (const [name, schemaDef] of Object.entries(schemaSource || {})) {
        schemas.push(parseSchema(name, schemaDef));
    }
    const securitySchemes = components.securitySchemes ||
        spec.securityDefinitions;
    for (const [name, scheme] of Object.entries(securitySchemes || {})) {
        const schemeObj = scheme;
        security.push({
            name,
            type: String(schemeObj.type || 'unknown'),
            description: schemeObj.description,
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
function parseEndpoint(path, method, operation) {
    const parameters = [];
    const responses = [];
    const params = operation.parameters || [];
    for (const param of params) {
        parameters.push(parseParameter(param));
    }
    let requestBody;
    if (operation.requestBody) {
        const reqBody = operation.requestBody;
        const content = reqBody.content || {};
        requestBody = {
            contentTypes: Object.keys(content),
            required: Boolean(reqBody.required),
            description: reqBody.description,
            schema: extractSchemaRef(content),
        };
    }
    const respDefs = operation.responses || {};
    for (const [code, respDef] of Object.entries(respDefs)) {
        const resp = respDef;
        const content = resp.content || {};
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
        operationId: operation.operationId,
        summary: operation.summary,
        description: operation.description,
        parameters,
        requestBody,
        responses,
        deprecated: Boolean(operation.deprecated),
        tags: operation.tags || [],
    };
}
function parseParameter(param) {
    const schema = param.schema || {};
    return {
        name: String(param.name || ''),
        location: param.in,
        type: String(schema.type || param.type || 'string'),
        required: Boolean(param.required),
        description: param.description,
        defaultValue: schema.default ?? param.default,
        schema: schema.$ref || undefined,
    };
}
function parseSchema(name, schema) {
    const properties = [];
    const props = schema.properties || {};
    const required = schema.required || [];
    for (const [propName, propDef] of Object.entries(props)) {
        const prop = propDef;
        properties.push({
            name: propName,
            type: String(prop.type || 'any'),
            required: required.includes(propName),
            description: prop.description,
            format: prop.format,
            enum: prop.enum,
        });
    }
    return {
        name,
        type: String(schema.type || 'object'),
        properties,
        required,
        description: schema.description,
    };
}
function extractSchemaRef(content) {
    for (const mediaType of Object.values(content)) {
        const mt = mediaType;
        const schema = mt.schema;
        if (schema?.$ref) {
            return String(schema.$ref);
        }
    }
    return undefined;
}
function parseAsyncApi(spec) {
    const info = spec.info || {};
    const channels = spec.channels || {};
    const components = spec.components || {};
    const endpoints = [];
    const schemas = [];
    for (const [channelName, channelDef] of Object.entries(channels)) {
        const channel = channelDef;
        if (channel.subscribe) {
            const op = channel.subscribe;
            endpoints.push({
                id: `SUB-${channelName}`,
                path: channelName,
                method: 'SUBSCRIBE',
                operationId: op.operationId,
                summary: op.summary,
                description: op.description,
                parameters: [],
                responses: [],
                deprecated: Boolean(op.deprecated),
                tags: op.tags || [],
            });
        }
        if (channel.publish) {
            const op = channel.publish;
            endpoints.push({
                id: `PUB-${channelName}`,
                path: channelName,
                method: 'PUBLISH',
                operationId: op.operationId,
                summary: op.summary,
                description: op.description,
                parameters: [],
                responses: [],
                deprecated: Boolean(op.deprecated),
                tags: op.tags || [],
            });
        }
    }
    const schemaSource = components.schemas || {};
    for (const [name, schemaDef] of Object.entries(schemaSource)) {
        schemas.push(parseSchema(name, schemaDef));
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
function parseGraphQL(content) {
    const endpoints = [];
    const schemas = [];
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
        }
        else {
            const properties = [];
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
function parseProto(content) {
    const endpoints = [];
    const schemas = [];
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
        const properties = [];
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
function detectSpecType(content, filename) {
    const ext = getFileExtension(filename);
    if (ext === 'graphql' || ext === 'gql') {
        return 'graphql';
    }
    if (ext === 'proto') {
        return 'grpc';
    }
    try {
        const spec = ext === 'json' ? JSON.parse(content) : yaml.parse(content);
        if (spec.openapi || spec.swagger)
            return 'openapi';
        if (spec.asyncapi)
            return 'asyncapi';
    }
    catch {
    }
    return 'unknown';
}
//# sourceMappingURL=parser.js.map
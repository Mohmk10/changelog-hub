import * as path from 'path';
import * as fs from 'fs';
import { parseSpec, detectSpecType } from '../src/core/parser';

const fixturesPath = path.join(__dirname, 'fixtures');

describe('parseSpec', () => {
  describe('OpenAPI parsing', () => {
    it('should parse OpenAPI 3.0 YAML specification', () => {
      const content = fs.readFileSync(path.join(fixturesPath, 'api-v1.yaml'), 'utf-8');
      const spec = parseSpec(content, 'api-v1.yaml');

      expect(spec.name).toBe('Pet Store API');
      expect(spec.version).toBe('1.0.0');
      expect(spec.type).toBe('openapi');
      expect(spec.endpoints.length).toBeGreaterThan(0);
      expect(spec.schemas.length).toBeGreaterThan(0);
    });

    it('should parse endpoints with correct methods', () => {
      const content = fs.readFileSync(path.join(fixturesPath, 'api-v1.yaml'), 'utf-8');
      const spec = parseSpec(content, 'api-v1.yaml');

      const listPets = spec.endpoints.find(e => e.operationId === 'listPets');
      expect(listPets).toBeDefined();
      expect(listPets?.method).toBe('GET');
      expect(listPets?.path).toBe('/pets');

      const createPet = spec.endpoints.find(e => e.operationId === 'createPet');
      expect(createPet).toBeDefined();
      expect(createPet?.method).toBe('POST');
    });

    it('should parse parameters correctly', () => {
      const content = fs.readFileSync(path.join(fixturesPath, 'api-v1.yaml'), 'utf-8');
      const spec = parseSpec(content, 'api-v1.yaml');

      const listPets = spec.endpoints.find(e => e.operationId === 'listPets');
      expect(listPets?.parameters).toHaveLength(1);
      expect(listPets?.parameters[0].name).toBe('limit');
      expect(listPets?.parameters[0].required).toBe(false);
    });

    it('should parse schemas correctly', () => {
      const content = fs.readFileSync(path.join(fixturesPath, 'api-v1.yaml'), 'utf-8');
      const spec = parseSpec(content, 'api-v1.yaml');

      const petSchema = spec.schemas.find(s => s.name === 'Pet');
      expect(petSchema).toBeDefined();
      expect(petSchema?.properties.length).toBeGreaterThan(0);

      const idProp = petSchema?.properties.find(p => p.name === 'id');
      expect(idProp?.type).toBe('string');
      expect(idProp?.required).toBe(true);
    });

    it('should parse security schemes', () => {
      const content = fs.readFileSync(path.join(fixturesPath, 'api-v1.yaml'), 'utf-8');
      const spec = parseSpec(content, 'api-v1.yaml');

      expect(spec.security.length).toBeGreaterThan(0);
      const apiKey = spec.security.find(s => s.name === 'apiKey');
      expect(apiKey).toBeDefined();
      expect(apiKey?.type).toBe('apiKey');
    });
  });

  describe('AsyncAPI parsing', () => {
    it('should parse AsyncAPI specification', () => {
      const content = fs.readFileSync(path.join(fixturesPath, 'asyncapi-v1.yaml'), 'utf-8');
      const spec = parseSpec(content, 'asyncapi-v1.yaml');

      expect(spec.name).toBe('Pet Events API');
      expect(spec.version).toBe('1.0.0');
      expect(spec.type).toBe('asyncapi');
    });

    it('should parse channels as endpoints', () => {
      const content = fs.readFileSync(path.join(fixturesPath, 'asyncapi-v1.yaml'), 'utf-8');
      const spec = parseSpec(content, 'asyncapi-v1.yaml');

      expect(spec.endpoints.length).toBeGreaterThan(0);

      const subscribeEndpoint = spec.endpoints.find(e => e.method === 'SUBSCRIBE');
      expect(subscribeEndpoint).toBeDefined();

      const publishEndpoint = spec.endpoints.find(e => e.method === 'PUBLISH');
      expect(publishEndpoint).toBeDefined();
    });
  });

  describe('GraphQL parsing', () => {
    it('should parse GraphQL schema', () => {
      const content = fs.readFileSync(path.join(fixturesPath, 'schema.graphql'), 'utf-8');
      const spec = parseSpec(content, 'schema.graphql');

      expect(spec.type).toBe('graphql');
      expect(spec.endpoints.length).toBeGreaterThan(0);
      expect(spec.schemas.length).toBeGreaterThan(0);
    });

    it('should parse Query operations', () => {
      const content = fs.readFileSync(path.join(fixturesPath, 'schema.graphql'), 'utf-8');
      const spec = parseSpec(content, 'schema.graphql');

      const queryEndpoints = spec.endpoints.filter(e => e.method === 'QUERY');
      expect(queryEndpoints.length).toBeGreaterThan(0);
    });

    it('should parse Mutation operations', () => {
      const content = fs.readFileSync(path.join(fixturesPath, 'schema.graphql'), 'utf-8');
      const spec = parseSpec(content, 'schema.graphql');

      const mutationEndpoints = spec.endpoints.filter(e => e.method === 'MUTATION');
      expect(mutationEndpoints.length).toBeGreaterThan(0);
    });

    it('should parse types as schemas', () => {
      const content = fs.readFileSync(path.join(fixturesPath, 'schema.graphql'), 'utf-8');
      const spec = parseSpec(content, 'schema.graphql');

      const petSchema = spec.schemas.find(s => s.name === 'Pet');
      expect(petSchema).toBeDefined();
      expect(petSchema?.properties.length).toBeGreaterThan(0);
    });
  });

  describe('Protocol Buffer parsing', () => {
    it('should parse proto file', () => {
      const content = fs.readFileSync(path.join(fixturesPath, 'service.proto'), 'utf-8');
      const spec = parseSpec(content, 'service.proto');

      expect(spec.type).toBe('grpc');
      expect(spec.endpoints.length).toBeGreaterThan(0);
      expect(spec.schemas.length).toBeGreaterThan(0);
    });

    it('should parse RPC methods as endpoints', () => {
      const content = fs.readFileSync(path.join(fixturesPath, 'service.proto'), 'utf-8');
      const spec = parseSpec(content, 'service.proto');

      const createPet = spec.endpoints.find(e => e.operationId === 'CreatePet');
      expect(createPet).toBeDefined();
      expect(createPet?.method).toBe('RPC');
    });

    it('should parse messages as schemas', () => {
      const content = fs.readFileSync(path.join(fixturesPath, 'service.proto'), 'utf-8');
      const spec = parseSpec(content, 'service.proto');

      const petMessage = spec.schemas.find(s => s.name === 'Pet');
      expect(petMessage).toBeDefined();
    });
  });

  describe('Error handling', () => {
    it('should throw error for unsupported format', () => {
      expect(() => parseSpec('content', 'file.txt')).toThrow('Unsupported spec format');
    });

    it('should throw error for invalid YAML', () => {
      expect(() => parseSpec('invalid: yaml: content:', 'file.yaml')).toThrow();
    });

    it('should throw error for unknown YAML/JSON spec', () => {
      const content = '{ "unknown": "format" }';
      expect(() => parseSpec(content, 'file.json')).toThrow('Unknown YAML/JSON spec format');
    });
  });
});

describe('detectSpecType', () => {
  it('should detect OpenAPI spec', () => {
    const content = '{ "openapi": "3.0.0" }';
    expect(detectSpecType(content, 'api.json')).toBe('openapi');
  });

  it('should detect Swagger spec', () => {
    const content = '{ "swagger": "2.0" }';
    expect(detectSpecType(content, 'api.json')).toBe('openapi');
  });

  it('should detect AsyncAPI spec', () => {
    const content = '{ "asyncapi": "2.6.0" }';
    expect(detectSpecType(content, 'api.json')).toBe('asyncapi');
  });

  it('should detect GraphQL by extension', () => {
    expect(detectSpecType('type Query {}', 'schema.graphql')).toBe('graphql');
    expect(detectSpecType('type Query {}', 'schema.gql')).toBe('graphql');
  });

  it('should detect Proto by extension', () => {
    expect(detectSpecType('syntax = "proto3";', 'service.proto')).toBe('grpc');
  });

  it('should return unknown for unrecognized content', () => {
    expect(detectSpecType('random content', 'file.yaml')).toBe('unknown');
  });
});

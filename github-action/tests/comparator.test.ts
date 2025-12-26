import { compareSpecs, ComparisonResult } from '../src/changelog/comparator';
import { ApiSpec, Endpoint, Schema } from '../src/changelog/parser';

describe('Comparator', () => {
  // Helper to create a minimal spec
  function createSpec(overrides: Partial<ApiSpec> = {}): ApiSpec {
    return {
      name: 'Test API',
      version: '1.0.0',
      type: 'openapi',
      endpoints: [],
      schemas: [],
      security: [],
      raw: {},
      ...overrides,
    };
  }

  // Helper to create an endpoint
  function createEndpoint(overrides: Partial<Endpoint> = {}): Endpoint {
    const path = overrides.path || '/test';
    const method = overrides.method || 'GET';
    return {
      id: `${method}-${path}`,
      path,
      method,
      parameters: [],
      responses: [],
      deprecated: false,
      tags: [],
      ...overrides,
    };
  }

  // Helper to create a schema
  function createSchema(name: string, overrides: Partial<Schema> = {}): Schema {
    return {
      name,
      type: 'object',
      properties: [],
      required: [],
      ...overrides,
    };
  }

  describe('compareSpecs', () => {
    describe('Identical Specs', () => {
      it('should return no changes for identical specs', () => {
        const endpoint = createEndpoint({ path: '/users', method: 'GET' });
        const spec = createSpec({ endpoints: [endpoint] });

        const result = compareSpecs(spec, spec);

        expect(result.changes).toHaveLength(0);
        expect(result.breakingChanges).toHaveLength(0);
        expect(result.riskScore).toBe(0);
      });

      it('should return empty summary for identical specs', () => {
        const spec = createSpec();
        const result = compareSpecs(spec, spec);

        expect(result.summary.endpointsAdded).toBe(0);
        expect(result.summary.endpointsRemoved).toBe(0);
        expect(result.summary.endpointsModified).toBe(0);
      });
    });

    describe('Endpoint Changes', () => {
      it('should detect removed endpoint as breaking change', () => {
        const oldSpec = createSpec({
          endpoints: [createEndpoint({ path: '/users', method: 'GET' })],
        });
        const newSpec = createSpec({ endpoints: [] });

        const result = compareSpecs(oldSpec, newSpec);

        expect(result.breakingChanges).toHaveLength(1);
        expect(result.breakingChanges[0].type).toBe('REMOVED');
        expect(result.breakingChanges[0].category).toBe('ENDPOINT');
        expect(result.breakingChanges[0].severity).toBe('BREAKING');
        expect(result.summary.endpointsRemoved).toBe(1);
      });

      it('should detect added endpoint as info change', () => {
        const oldSpec = createSpec({ endpoints: [] });
        const newSpec = createSpec({
          endpoints: [createEndpoint({ path: '/users', method: 'GET' })],
        });

        const result = compareSpecs(oldSpec, newSpec);

        expect(result.changes).toHaveLength(1);
        expect(result.changes[0].type).toBe('ADDED');
        expect(result.changes[0].severity).toBe('INFO');
        expect(result.breakingChanges).toHaveLength(0);
        expect(result.summary.endpointsAdded).toBe(1);
      });

      it('should detect multiple endpoint changes', () => {
        const oldSpec = createSpec({
          endpoints: [
            createEndpoint({ path: '/users', method: 'GET' }),
            createEndpoint({ path: '/items', method: 'GET' }),
          ],
        });
        const newSpec = createSpec({
          endpoints: [
            createEndpoint({ path: '/users', method: 'GET' }),
            createEndpoint({ path: '/orders', method: 'GET' }),
          ],
        });

        const result = compareSpecs(oldSpec, newSpec);

        const removed = result.changes.filter((c) => c.type === 'REMOVED');
        const added = result.changes.filter((c) => c.type === 'ADDED');

        expect(removed).toHaveLength(1);
        expect(added).toHaveLength(1);
        expect(result.summary.endpointsRemoved).toBe(1);
        expect(result.summary.endpointsAdded).toBe(1);
      });

      it('should detect endpoint deprecation', () => {
        const oldSpec = createSpec({
          endpoints: [createEndpoint({ path: '/users', deprecated: false })],
        });
        const newSpec = createSpec({
          endpoints: [createEndpoint({ path: '/users', deprecated: true })],
        });

        const result = compareSpecs(oldSpec, newSpec);

        const deprecation = result.changes.find((c) => c.type === 'DEPRECATED');
        expect(deprecation).toBeDefined();
        expect(deprecation?.severity).toBe('WARNING');
        expect(result.summary.endpointsDeprecated).toBe(1);
      });
    });

    describe('Parameter Changes', () => {
      it('should detect removed required parameter as breaking', () => {
        const oldSpec = createSpec({
          endpoints: [
            createEndpoint({
              path: '/users',
              parameters: [{ name: 'id', location: 'path', type: 'string', required: true }],
            }),
          ],
        });
        const newSpec = createSpec({
          endpoints: [createEndpoint({ path: '/users', parameters: [] })],
        });

        const result = compareSpecs(oldSpec, newSpec);

        const paramChange = result.changes.find((c) => c.category === 'PARAMETER');
        expect(paramChange).toBeDefined();
        expect(paramChange?.type).toBe('REMOVED');
        expect(paramChange?.severity).toBe('BREAKING');
      });

      it('should detect removed optional parameter as warning', () => {
        const oldSpec = createSpec({
          endpoints: [
            createEndpoint({
              path: '/users',
              parameters: [{ name: 'limit', location: 'query', type: 'integer', required: false }],
            }),
          ],
        });
        const newSpec = createSpec({
          endpoints: [createEndpoint({ path: '/users', parameters: [] })],
        });

        const result = compareSpecs(oldSpec, newSpec);

        const paramChange = result.changes.find((c) => c.category === 'PARAMETER');
        expect(paramChange?.severity).toBe('WARNING');
      });

      it('should detect new required parameter as breaking', () => {
        const oldSpec = createSpec({
          endpoints: [createEndpoint({ path: '/users', parameters: [] })],
        });
        const newSpec = createSpec({
          endpoints: [
            createEndpoint({
              path: '/users',
              parameters: [{ name: 'apiKey', location: 'header', type: 'string', required: true }],
            }),
          ],
        });

        const result = compareSpecs(oldSpec, newSpec);

        const paramChange = result.changes.find((c) => c.category === 'PARAMETER');
        expect(paramChange?.type).toBe('ADDED');
        expect(paramChange?.severity).toBe('BREAKING');
      });

      it('should detect new optional parameter as info', () => {
        const oldSpec = createSpec({
          endpoints: [createEndpoint({ path: '/users', parameters: [] })],
        });
        const newSpec = createSpec({
          endpoints: [
            createEndpoint({
              path: '/users',
              parameters: [{ name: 'limit', location: 'query', type: 'integer', required: false }],
            }),
          ],
        });

        const result = compareSpecs(oldSpec, newSpec);

        const paramChange = result.changes.find((c) => c.category === 'PARAMETER');
        expect(paramChange?.severity).toBe('INFO');
      });

      it('should detect parameter becoming required as breaking', () => {
        const oldSpec = createSpec({
          endpoints: [
            createEndpoint({
              path: '/users',
              parameters: [{ name: 'limit', location: 'query', type: 'integer', required: false }],
            }),
          ],
        });
        const newSpec = createSpec({
          endpoints: [
            createEndpoint({
              path: '/users',
              parameters: [{ name: 'limit', location: 'query', type: 'integer', required: true }],
            }),
          ],
        });

        const result = compareSpecs(oldSpec, newSpec);

        const paramChange = result.changes.find(
          (c) => c.category === 'PARAMETER' && c.type === 'MODIFIED'
        );
        expect(paramChange?.severity).toBe('BREAKING');
        expect(paramChange?.description).toContain('required');
      });

      it('should detect parameter type change as breaking', () => {
        const oldSpec = createSpec({
          endpoints: [
            createEndpoint({
              path: '/users',
              parameters: [{ name: 'id', location: 'path', type: 'string', required: true }],
            }),
          ],
        });
        const newSpec = createSpec({
          endpoints: [
            createEndpoint({
              path: '/users',
              parameters: [{ name: 'id', location: 'path', type: 'integer', required: true }],
            }),
          ],
        });

        const result = compareSpecs(oldSpec, newSpec);

        const typeChange = result.changes.find(
          (c) => c.category === 'PARAMETER' && c.description.includes('type')
        );
        expect(typeChange).toBeDefined();
        expect(typeChange?.severity).toBe('BREAKING');
      });
    });

    describe('Request Body Changes', () => {
      it('should detect new required request body as breaking', () => {
        const oldSpec = createSpec({
          endpoints: [createEndpoint({ path: '/users', method: 'POST' })],
        });
        const newSpec = createSpec({
          endpoints: [
            createEndpoint({
              path: '/users',
              method: 'POST',
              requestBody: { contentTypes: ['application/json'], required: true },
            }),
          ],
        });

        const result = compareSpecs(oldSpec, newSpec);

        const bodyChange = result.changes.find((c) => c.category === 'REQUEST_BODY');
        expect(bodyChange?.severity).toBe('BREAKING');
      });

      it('should detect request body becoming required as breaking', () => {
        const oldSpec = createSpec({
          endpoints: [
            createEndpoint({
              path: '/users',
              method: 'POST',
              requestBody: { contentTypes: ['application/json'], required: false },
            }),
          ],
        });
        const newSpec = createSpec({
          endpoints: [
            createEndpoint({
              path: '/users',
              method: 'POST',
              requestBody: { contentTypes: ['application/json'], required: true },
            }),
          ],
        });

        const result = compareSpecs(oldSpec, newSpec);

        const bodyChange = result.changes.find((c) => c.category === 'REQUEST_BODY');
        expect(bodyChange?.severity).toBe('BREAKING');
      });

      it('should detect removed content type as breaking', () => {
        const oldSpec = createSpec({
          endpoints: [
            createEndpoint({
              path: '/users',
              method: 'POST',
              requestBody: {
                contentTypes: ['application/json', 'application/xml'],
                required: false,
              },
            }),
          ],
        });
        const newSpec = createSpec({
          endpoints: [
            createEndpoint({
              path: '/users',
              method: 'POST',
              requestBody: { contentTypes: ['application/json'], required: false },
            }),
          ],
        });

        const result = compareSpecs(oldSpec, newSpec);

        const contentTypeChange = result.changes.find(
          (c) => c.category === 'REQUEST_BODY' && c.description.includes('xml')
        );
        expect(contentTypeChange?.severity).toBe('BREAKING');
      });
    });

    describe('Schema Changes', () => {
      it('should detect removed schema as dangerous', () => {
        const oldSpec = createSpec({
          schemas: [createSchema('User')],
        });
        const newSpec = createSpec({ schemas: [] });

        const result = compareSpecs(oldSpec, newSpec);

        const schemaChange = result.changes.find((c) => c.category === 'SCHEMA');
        expect(schemaChange?.type).toBe('REMOVED');
        expect(schemaChange?.severity).toBe('DANGEROUS');
        expect(result.summary.schemasRemoved).toBe(1);
      });

      it('should detect added schema as info', () => {
        const oldSpec = createSpec({ schemas: [] });
        const newSpec = createSpec({
          schemas: [createSchema('User')],
        });

        const result = compareSpecs(oldSpec, newSpec);

        const schemaChange = result.changes.find((c) => c.category === 'SCHEMA');
        expect(schemaChange?.type).toBe('ADDED');
        expect(schemaChange?.severity).toBe('INFO');
        expect(result.summary.schemasAdded).toBe(1);
      });

      it('should detect removed property as dangerous', () => {
        const oldSpec = createSpec({
          schemas: [
            createSchema('User', {
              properties: [
                { name: 'id', type: 'string', required: true },
                { name: 'name', type: 'string', required: true },
              ],
            }),
          ],
        });
        const newSpec = createSpec({
          schemas: [
            createSchema('User', {
              properties: [{ name: 'id', type: 'string', required: true }],
            }),
          ],
        });

        const result = compareSpecs(oldSpec, newSpec);

        const propChange = result.changes.find((c) => c.category === 'SCHEMA_PROPERTY');
        expect(propChange?.type).toBe('REMOVED');
        expect(propChange?.severity).toBe('DANGEROUS');
      });

      it('should detect property type change as breaking', () => {
        const oldSpec = createSpec({
          schemas: [
            createSchema('User', {
              properties: [{ name: 'age', type: 'string', required: false }],
            }),
          ],
        });
        const newSpec = createSpec({
          schemas: [
            createSchema('User', {
              properties: [{ name: 'age', type: 'integer', required: false }],
            }),
          ],
        });

        const result = compareSpecs(oldSpec, newSpec);

        const propChange = result.changes.find(
          (c) => c.category === 'SCHEMA_PROPERTY' && c.description.includes('type')
        );
        expect(propChange?.severity).toBe('BREAKING');
      });

      it('should detect property becoming required as breaking', () => {
        const oldSpec = createSpec({
          schemas: [
            createSchema('User', {
              properties: [{ name: 'email', type: 'string', required: false }],
            }),
          ],
        });
        const newSpec = createSpec({
          schemas: [
            createSchema('User', {
              properties: [{ name: 'email', type: 'string', required: true }],
            }),
          ],
        });

        const result = compareSpecs(oldSpec, newSpec);

        const propChange = result.changes.find(
          (c) => c.category === 'SCHEMA_PROPERTY' && c.description.includes('required')
        );
        expect(propChange?.severity).toBe('BREAKING');
      });
    });

    describe('Security Changes', () => {
      it('should detect removed security scheme as breaking', () => {
        const oldSpec = createSpec({
          security: [{ name: 'bearerAuth', type: 'http' }],
        });
        const newSpec = createSpec({ security: [] });

        const result = compareSpecs(oldSpec, newSpec);

        const secChange = result.changes.find((c) => c.category === 'SECURITY');
        expect(secChange?.type).toBe('REMOVED');
        expect(secChange?.severity).toBe('BREAKING');
      });

      it('should detect added security scheme as info', () => {
        const oldSpec = createSpec({ security: [] });
        const newSpec = createSpec({
          security: [{ name: 'apiKey', type: 'apiKey' }],
        });

        const result = compareSpecs(oldSpec, newSpec);

        const secChange = result.changes.find((c) => c.category === 'SECURITY');
        expect(secChange?.type).toBe('ADDED');
        expect(secChange?.severity).toBe('INFO');
      });
    });

    describe('Risk Score', () => {
      it('should calculate higher risk for more breaking changes', () => {
        const oldSpec = createSpec({
          endpoints: [
            createEndpoint({ path: '/a' }),
            createEndpoint({ path: '/b' }),
            createEndpoint({ path: '/c' }),
          ],
        });
        const newSpec = createSpec({ endpoints: [] });

        const result = compareSpecs(oldSpec, newSpec);

        expect(result.riskScore).toBeGreaterThan(50);
        expect(result.breakingChanges).toHaveLength(3);
      });

      it('should include dangerous changes in risk calculation', () => {
        const oldSpec = createSpec({
          schemas: [createSchema('User'), createSchema('Order')],
        });
        const newSpec = createSpec({ schemas: [] });

        const result = compareSpecs(oldSpec, newSpec);

        expect(result.riskScore).toBeGreaterThan(0);
      });
    });

    describe('Migration Suggestions', () => {
      it('should provide migration suggestion for removed endpoint', () => {
        const oldSpec = createSpec({
          endpoints: [createEndpoint({ path: '/users' })],
        });
        const newSpec = createSpec({ endpoints: [] });

        const result = compareSpecs(oldSpec, newSpec);

        expect(result.breakingChanges[0].migrationSuggestion).toBeDefined();
        expect(result.breakingChanges[0].migrationSuggestion.length).toBeGreaterThan(0);
      });

      it('should provide impact score for breaking changes', () => {
        const oldSpec = createSpec({
          endpoints: [createEndpoint({ path: '/users' })],
        });
        const newSpec = createSpec({ endpoints: [] });

        const result = compareSpecs(oldSpec, newSpec);

        expect(result.breakingChanges[0].impactScore).toBeGreaterThanOrEqual(0);
        expect(result.breakingChanges[0].impactScore).toBeLessThanOrEqual(100);
      });
    });
  });
});

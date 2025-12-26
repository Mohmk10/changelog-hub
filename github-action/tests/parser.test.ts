import { parseSpec, ApiSpec } from '../src/changelog/parser';

describe('Parser', () => {
  describe('parseSpec', () => {
    describe('OpenAPI 3.x', () => {
      const openApiSpec = `
openapi: "3.0.3"
info:
  title: Test API
  version: "1.2.3"
paths:
  /users:
    get:
      operationId: getUsers
      summary: Get all users
      parameters:
        - name: limit
          in: query
          required: false
          schema:
            type: integer
      responses:
        "200":
          description: Success
          content:
            application/json:
              schema:
                type: array
    post:
      operationId: createUser
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: "#/components/schemas/User"
      responses:
        "201":
          description: Created
  /users/{id}:
    get:
      operationId: getUser
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
      responses:
        "200":
          description: Success
    delete:
      operationId: deleteUser
      deprecated: true
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
      responses:
        "204":
          description: Deleted
components:
  schemas:
    User:
      type: object
      required:
        - id
        - name
      properties:
        id:
          type: string
        name:
          type: string
        email:
          type: string
          format: email
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
`;

      it('should parse OpenAPI spec correctly', () => {
        const result = parseSpec(openApiSpec, 'api.yaml');

        expect(result.name).toBe('Test API');
        expect(result.version).toBe('1.2.3');
        expect(result.type).toBe('openapi');
      });

      it('should parse all endpoints', () => {
        const result = parseSpec(openApiSpec, 'api.yaml');

        expect(result.endpoints).toHaveLength(4);

        const getUsersEndpoint = result.endpoints.find((e) => e.operationId === 'getUsers');
        expect(getUsersEndpoint).toBeDefined();
        expect(getUsersEndpoint?.method).toBe('GET');
        expect(getUsersEndpoint?.path).toBe('/users');
      });

      it('should parse parameters correctly', () => {
        const result = parseSpec(openApiSpec, 'api.yaml');

        const getUsersEndpoint = result.endpoints.find((e) => e.operationId === 'getUsers');
        expect(getUsersEndpoint?.parameters).toHaveLength(1);
        expect(getUsersEndpoint?.parameters[0].name).toBe('limit');
        expect(getUsersEndpoint?.parameters[0].location).toBe('query');
        expect(getUsersEndpoint?.parameters[0].required).toBe(false);
      });

      it('should parse path parameters', () => {
        const result = parseSpec(openApiSpec, 'api.yaml');

        const getUserEndpoint = result.endpoints.find((e) => e.operationId === 'getUser');
        expect(getUserEndpoint?.parameters).toHaveLength(1);
        expect(getUserEndpoint?.parameters[0].name).toBe('id');
        expect(getUserEndpoint?.parameters[0].location).toBe('path');
        expect(getUserEndpoint?.parameters[0].required).toBe(true);
      });

      it('should detect deprecated endpoints', () => {
        const result = parseSpec(openApiSpec, 'api.yaml');

        const deleteEndpoint = result.endpoints.find((e) => e.operationId === 'deleteUser');
        expect(deleteEndpoint?.deprecated).toBe(true);

        const getEndpoint = result.endpoints.find((e) => e.operationId === 'getUser');
        expect(getEndpoint?.deprecated).toBe(false);
      });

      it('should parse request body', () => {
        const result = parseSpec(openApiSpec, 'api.yaml');

        const createEndpoint = result.endpoints.find((e) => e.operationId === 'createUser');
        expect(createEndpoint?.requestBody).toBeDefined();
        expect(createEndpoint?.requestBody?.required).toBe(true);
        expect(createEndpoint?.requestBody?.contentTypes).toContain('application/json');
      });

      it('should parse responses', () => {
        const result = parseSpec(openApiSpec, 'api.yaml');

        const getUsersEndpoint = result.endpoints.find((e) => e.operationId === 'getUsers');
        expect(getUsersEndpoint?.responses).toHaveLength(1);
        expect(getUsersEndpoint?.responses[0].statusCode).toBe('200');
        expect(getUsersEndpoint?.responses[0].description).toBe('Success');
      });

      it('should parse schemas', () => {
        const result = parseSpec(openApiSpec, 'api.yaml');

        expect(result.schemas).toHaveLength(1);
        expect(result.schemas[0].name).toBe('User');
        expect(result.schemas[0].type).toBe('object');
        expect(result.schemas[0].properties).toHaveLength(3);
        expect(result.schemas[0].required).toContain('id');
        expect(result.schemas[0].required).toContain('name');
      });

      it('should parse security schemes', () => {
        const result = parseSpec(openApiSpec, 'api.yaml');

        expect(result.security).toHaveLength(1);
        expect(result.security[0].name).toBe('bearerAuth');
        expect(result.security[0].type).toBe('http');
      });
    });

    describe('Swagger 2.0', () => {
      const swaggerSpec = `
swagger: "2.0"
info:
  title: Legacy API
  version: "2.0.0"
paths:
  /items:
    get:
      operationId: getItems
      parameters:
        - name: page
          in: query
          type: integer
      responses:
        200:
          description: OK
definitions:
  Item:
    type: object
    properties:
      id:
        type: string
securityDefinitions:
  apiKey:
    type: apiKey
    name: X-API-Key
    in: header
`;

      it('should parse Swagger 2.0 spec', () => {
        const result = parseSpec(swaggerSpec, 'swagger.yaml');

        expect(result.name).toBe('Legacy API');
        expect(result.version).toBe('2.0.0');
        expect(result.type).toBe('openapi');
      });

      it('should parse Swagger endpoints', () => {
        const result = parseSpec(swaggerSpec, 'swagger.yaml');

        expect(result.endpoints).toHaveLength(1);
        expect(result.endpoints[0].path).toBe('/items');
      });

      it('should parse Swagger definitions as schemas', () => {
        const result = parseSpec(swaggerSpec, 'swagger.yaml');

        expect(result.schemas).toHaveLength(1);
        expect(result.schemas[0].name).toBe('Item');
      });

      it('should parse Swagger security definitions', () => {
        const result = parseSpec(swaggerSpec, 'swagger.yaml');

        expect(result.security).toHaveLength(1);
        expect(result.security[0].name).toBe('apiKey');
      });
    });

    describe('AsyncAPI', () => {
      const asyncApiSpec = `
asyncapi: "2.6.0"
info:
  title: Events API
  version: "1.0.0"
channels:
  user/created:
    subscribe:
      operationId: onUserCreated
      summary: User created event
    publish:
      operationId: publishUserCreated
  order/placed:
    subscribe:
      operationId: onOrderPlaced
components:
  schemas:
    UserCreatedEvent:
      type: object
      properties:
        userId:
          type: string
`;

      it('should parse AsyncAPI spec', () => {
        const result = parseSpec(asyncApiSpec, 'events.yaml');

        expect(result.name).toBe('Events API');
        expect(result.version).toBe('1.0.0');
        expect(result.type).toBe('asyncapi');
      });

      it('should parse channels as endpoints', () => {
        const result = parseSpec(asyncApiSpec, 'events.yaml');

        expect(result.endpoints).toHaveLength(3);

        const subEndpoint = result.endpoints.find((e) => e.id === 'SUB-user/created');
        expect(subEndpoint).toBeDefined();
        expect(subEndpoint?.method).toBe('SUBSCRIBE');

        const pubEndpoint = result.endpoints.find((e) => e.id === 'PUB-user/created');
        expect(pubEndpoint).toBeDefined();
        expect(pubEndpoint?.method).toBe('PUBLISH');
      });

      it('should parse AsyncAPI schemas', () => {
        const result = parseSpec(asyncApiSpec, 'events.yaml');

        expect(result.schemas).toHaveLength(1);
        expect(result.schemas[0].name).toBe('UserCreatedEvent');
      });
    });

    describe('GraphQL', () => {
      const graphqlSchema = `
type Query {
  users: [User!]!
  user(id: ID!): User
}

type Mutation {
  createUser(input: CreateUserInput!): User!
  deleteUser(id: ID!): Boolean!
}

type User {
  id: ID!
  name: String!
  email: String
  posts: [Post!]!
}

type Post {
  id: ID!
  title: String!
  content: String
}

input CreateUserInput {
  name: String!
  email: String
}
`;

      it('should parse GraphQL schema', () => {
        const result = parseSpec(graphqlSchema, 'schema.graphql');

        expect(result.type).toBe('graphql');
        expect(result.name).toBe('GraphQL Schema');
      });

      it('should parse queries and mutations as endpoints', () => {
        const result = parseSpec(graphqlSchema, 'schema.graphql');

        const queries = result.endpoints.filter((e) => e.method === 'QUERY');
        const mutations = result.endpoints.filter((e) => e.method === 'MUTATION');

        expect(queries.length).toBeGreaterThanOrEqual(1);
        expect(mutations.length).toBeGreaterThanOrEqual(1);
      });

      it('should parse types as schemas', () => {
        const result = parseSpec(graphqlSchema, 'schema.graphql');

        const userSchema = result.schemas.find((s) => s.name === 'User');
        expect(userSchema).toBeDefined();
        expect(userSchema?.properties.length).toBeGreaterThanOrEqual(1);
      });
    });

    describe('Protocol Buffers', () => {
      const protoSchema = `
syntax = "proto3";

package users;

service UserService {
  rpc GetUser(GetUserRequest) returns (User);
  rpc CreateUser(CreateUserRequest) returns (User);
  rpc DeleteUser(DeleteUserRequest) returns (Empty);
}

message User {
  string id = 1;
  string name = 2;
  optional string email = 3;
}

message GetUserRequest {
  string id = 1;
}

message CreateUserRequest {
  string name = 1;
  optional string email = 2;
}

message DeleteUserRequest {
  string id = 1;
}

message Empty {}
`;

      it('should parse protobuf schema', () => {
        const result = parseSpec(protoSchema, 'users.proto');

        expect(result.type).toBe('grpc');
        expect(result.name).toBe('Protocol Buffer Service');
      });

      it('should parse service methods as endpoints', () => {
        const result = parseSpec(protoSchema, 'users.proto');

        expect(result.endpoints.length).toBeGreaterThanOrEqual(1);

        const getUser = result.endpoints.find((e) => e.operationId === 'GetUser');
        expect(getUser).toBeDefined();
        expect(getUser?.method).toBe('RPC');
      });

      it('should parse messages as schemas', () => {
        const result = parseSpec(protoSchema, 'users.proto');

        const userMessage = result.schemas.find((s) => s.name === 'User');
        expect(userMessage).toBeDefined();
        expect(userMessage?.type).toBe('message');
      });
    });

    describe('JSON Format', () => {
      it('should parse JSON OpenAPI spec', () => {
        const jsonSpec = JSON.stringify({
          openapi: '3.0.0',
          info: { title: 'JSON API', version: '1.0.0' },
          paths: {
            '/test': {
              get: {
                operationId: 'getTest',
                responses: { '200': { description: 'OK' } },
              },
            },
          },
        });

        const result = parseSpec(jsonSpec, 'api.json');

        expect(result.name).toBe('JSON API');
        expect(result.type).toBe('openapi');
        expect(result.endpoints).toHaveLength(1);
      });
    });

    describe('Error Handling', () => {
      it('should throw for unsupported file formats', () => {
        expect(() => parseSpec('content', 'file.xyz')).toThrow('Unsupported spec format');
      });

      it('should throw for invalid YAML', () => {
        const invalidYaml = `
openapi: 3.0.0
info:
  title: Bad YAML
  version: 1.0.0
paths:
  invalid yaml content here
    - this: is broken
`;
        expect(() => parseSpec(invalidYaml, 'api.yaml')).toThrow();
      });

      it('should throw for invalid JSON', () => {
        expect(() => parseSpec('{ invalid json }', 'api.json')).toThrow();
      });

      it('should handle empty paths gracefully', () => {
        const specWithNoPaths = `
openapi: "3.0.0"
info:
  title: Empty API
  version: "1.0.0"
paths: {}
`;
        const result = parseSpec(specWithNoPaths, 'api.yaml');
        expect(result.endpoints).toHaveLength(0);
      });
    });
  });
});

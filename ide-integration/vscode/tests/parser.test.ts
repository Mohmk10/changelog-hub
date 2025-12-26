import * as assert from 'assert';
import { parseSpec, detectSpecType } from '../src/core/parser';

suite('Parser Test Suite', () => {
  test('Should parse OpenAPI 3.0 spec', () => {
    const content = `
openapi: 3.0.3
info:
  title: Test API
  version: 1.0.0
paths:
  /users:
    get:
      operationId: getUsers
      summary: Get all users
      responses:
        '200':
          description: Success
`;

    const spec = parseSpec(content, 'api.yaml');

    assert.strictEqual(spec.name, 'Test API');
    assert.strictEqual(spec.version, '1.0.0');
    assert.strictEqual(spec.type, 'openapi');
    assert.strictEqual(spec.endpoints.length, 1);
    assert.strictEqual(spec.endpoints[0].method, 'GET');
    assert.strictEqual(spec.endpoints[0].path, '/users');
  });

  test('Should parse AsyncAPI spec', () => {
    const content = `
asyncapi: 2.6.0
info:
  title: Events API
  version: 1.0.0
channels:
  user/created:
    subscribe:
      operationId: onUserCreated
`;

    const spec = parseSpec(content, 'events.yaml');

    assert.strictEqual(spec.name, 'Events API');
    assert.strictEqual(spec.type, 'asyncapi');
    assert.strictEqual(spec.endpoints.length, 1);
    assert.strictEqual(spec.endpoints[0].method, 'SUBSCRIBE');
  });

  test('Should parse GraphQL schema', () => {
    const content = `
type Query {
  users: [User!]!
  user(id: ID!): User
}

type User {
  id: ID!
  name: String!
  email: String
}
`;

    const spec = parseSpec(content, 'schema.graphql');

    assert.strictEqual(spec.type, 'graphql');
    assert.ok(spec.endpoints.length > 0);
    assert.ok(spec.schemas.length > 0);
  });

  test('Should parse Protocol Buffer definition', () => {
    const content = `
syntax = "proto3";

service UserService {
  rpc GetUser(GetUserRequest) returns (User);
  rpc ListUsers(ListUsersRequest) returns (ListUsersResponse);
}

message User {
  string id = 1;
  string name = 2;
}
`;

    const spec = parseSpec(content, 'service.proto');

    assert.strictEqual(spec.type, 'grpc');
    assert.strictEqual(spec.endpoints.length, 2);
    assert.ok(spec.schemas.length > 0);
  });

  test('Should detect spec type correctly', () => {
    assert.strictEqual(
      detectSpecType('{"openapi": "3.0.0"}', 'api.json'),
      'openapi'
    );
    assert.strictEqual(
      detectSpecType('{"swagger": "2.0"}', 'api.json'),
      'openapi'
    );
    assert.strictEqual(
      detectSpecType('{"asyncapi": "2.0.0"}', 'events.json'),
      'asyncapi'
    );
    assert.strictEqual(
      detectSpecType('type Query {}', 'schema.graphql'),
      'graphql'
    );
    assert.strictEqual(
      detectSpecType('syntax = "proto3";', 'service.proto'),
      'grpc'
    );
  });

  test('Should throw error for unsupported format', () => {
    assert.throws(() => {
      parseSpec('random content', 'file.txt');
    }, /Unsupported spec format/);
  });
});

import * as assert from 'assert';
import { parseSpec } from '../src/core/parser';
import { compareSpecs } from '../src/core/comparator';

suite('Comparator Test Suite', () => {
  const oldSpec = `
openapi: 3.0.3
info:
  title: Test API
  version: 1.0.0
paths:
  /users:
    get:
      operationId: getUsers
      responses:
        '200':
          description: Success
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
        '200':
          description: Success
components:
  schemas:
    User:
      type: object
      properties:
        id:
          type: string
        name:
          type: string
`;

  const newSpecWithBreakingChanges = `
openapi: 3.0.3
info:
  title: Test API
  version: 2.0.0
paths:
  /users:
    get:
      operationId: getUsers
      parameters:
        - name: limit
          in: query
          required: true
          schema:
            type: integer
      responses:
        '200':
          description: Success
  /customers/{id}:
    get:
      operationId: getCustomer
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: integer
      responses:
        '200':
          description: Success
components:
  schemas:
    User:
      type: object
      required:
        - id
        - email
      properties:
        id:
          type: integer
        email:
          type: string
`;

  test('Should detect removed endpoint as breaking change', () => {
    const old = parseSpec(oldSpec, 'old.yaml');
    const newS = parseSpec(newSpecWithBreakingChanges, 'new.yaml');

    const result = compareSpecs(old, newS);

    assert.ok(result.breakingChanges.length > 0, 'Should have breaking changes');

    const removedEndpoint = result.breakingChanges.find(
      (c) => c.type === 'REMOVED' && c.category === 'ENDPOINT'
    );
    assert.ok(removedEndpoint, 'Should detect removed endpoint');
  });

  test('Should detect added required parameter as breaking change', () => {
    const old = parseSpec(oldSpec, 'old.yaml');
    const newS = parseSpec(newSpecWithBreakingChanges, 'new.yaml');

    const result = compareSpecs(old, newS);

    const requiredParam = result.changes.find(
      (c) =>
        c.type === 'ADDED' &&
        c.category === 'PARAMETER' &&
        c.severity === 'BREAKING'
    );
    assert.ok(requiredParam, 'Should detect added required parameter');
  });

  test('Should recommend MAJOR version bump for breaking changes', () => {
    const old = parseSpec(oldSpec, 'old.yaml');
    const newS = parseSpec(newSpecWithBreakingChanges, 'new.yaml');

    const result = compareSpecs(old, newS);

    assert.strictEqual(
      result.semverRecommendation,
      'MAJOR',
      'Should recommend MAJOR version bump'
    );
  });

  test('Should return no breaking changes for identical specs', () => {
    const spec = parseSpec(oldSpec, 'api.yaml');
    const specCopy = parseSpec(oldSpec, 'api.yaml');

    const result = compareSpecs(spec, specCopy);

    assert.strictEqual(result.breakingChanges.length, 0);
    assert.strictEqual(result.totalChanges, 0);
  });

  test('Should calculate risk score', () => {
    const old = parseSpec(oldSpec, 'old.yaml');
    const newS = parseSpec(newSpecWithBreakingChanges, 'new.yaml');

    const result = compareSpecs(old, newS);

    assert.ok(result.riskScore >= 0 && result.riskScore <= 100);
    assert.ok(['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'].includes(result.riskLevel));
  });

  test('Should provide migration suggestions', () => {
    const old = parseSpec(oldSpec, 'old.yaml');
    const newS = parseSpec(newSpecWithBreakingChanges, 'new.yaml');

    const result = compareSpecs(old, newS);

    for (const change of result.breakingChanges) {
      assert.ok(
        change.migrationSuggestion,
        'Each breaking change should have a migration suggestion'
      );
      assert.ok(
        change.impactScore >= 0 && change.impactScore <= 100,
        'Impact score should be between 0 and 100'
      );
    }
  });

  test('Should provide summary statistics', () => {
    const old = parseSpec(oldSpec, 'old.yaml');
    const newS = parseSpec(newSpecWithBreakingChanges, 'new.yaml');

    const result = compareSpecs(old, newS);

    assert.ok(result.summary, 'Should have summary');
    assert.ok(
      typeof result.summary.endpointsAdded === 'number',
      'Should have endpointsAdded count'
    );
    assert.ok(
      typeof result.summary.endpointsRemoved === 'number',
      'Should have endpointsRemoved count'
    );
  });
});

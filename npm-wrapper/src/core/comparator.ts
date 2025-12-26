import {
  ApiSpec,
  Endpoint,
  Parameter,
  Schema,
  Change,
  BreakingChange,
  ChangeSeverity,
  ComparisonResult,
  ComparisonSummary,
  RiskLevel,
  SemverRecommendation,
} from '../types';

/**
 * Compares two API specifications and identifies all changes.
 *
 * @param oldSpec - The previous/base API specification
 * @param newSpec - The new/head API specification
 * @returns Comparison result with all changes
 */
export function compareSpecs(oldSpec: ApiSpec, newSpec: ApiSpec): ComparisonResult {
  const changes: Change[] = [];
  const breakingChanges: BreakingChange[] = [];
  const summary: ComparisonSummary = {
    endpointsAdded: 0,
    endpointsRemoved: 0,
    endpointsModified: 0,
    endpointsDeprecated: 0,
    schemasAdded: 0,
    schemasRemoved: 0,
    schemasModified: 0,
    parametersAdded: 0,
    parametersRemoved: 0,
    parametersModified: 0,
  };

  // Compare endpoints
  const endpointChanges = compareEndpoints(oldSpec.endpoints, newSpec.endpoints, summary);
  changes.push(...endpointChanges);

  // Compare schemas
  const schemaChanges = compareSchemas(oldSpec.schemas, newSpec.schemas, summary);
  changes.push(...schemaChanges);

  // Compare security definitions
  const securityChanges = compareSecurity(oldSpec.security, newSpec.security);
  changes.push(...securityChanges);

  // Extract breaking changes
  for (const change of changes) {
    if (change.severity === 'BREAKING') {
      breakingChanges.push(createBreakingChange(change));
    }
  }

  // Calculate risk score
  const riskScore = calculateRiskScore(breakingChanges, changes);
  const riskLevel = calculateRiskLevel(riskScore);
  const semverRecommendation = getSemverRecommendation(breakingChanges, changes);

  return {
    apiName: newSpec.name,
    fromVersion: oldSpec.version,
    toVersion: newSpec.version,
    changes,
    breakingChanges,
    totalChanges: changes.length,
    riskScore,
    riskLevel,
    semverRecommendation,
    summary,
  };
}

/**
 * Compares endpoint definitions
 */
function compareEndpoints(
  oldEndpoints: Endpoint[],
  newEndpoints: Endpoint[],
  summary: ComparisonSummary
): Change[] {
  const changes: Change[] = [];
  const oldMap = new Map(oldEndpoints.map((e) => [e.id, e]));
  const newMap = new Map(newEndpoints.map((e) => [e.id, e]));

  // Removed endpoints (BREAKING)
  for (const [id, oldEndpoint] of oldMap) {
    if (!newMap.has(id)) {
      summary.endpointsRemoved++;
      changes.push({
        type: 'REMOVED',
        category: 'ENDPOINT',
        severity: 'BREAKING',
        path: `${oldEndpoint.method} ${oldEndpoint.path}`,
        description: `Endpoint removed: ${oldEndpoint.method} ${oldEndpoint.path}`,
        oldValue: id,
      });
    }
  }

  // Added endpoints (INFO)
  for (const [id, newEndpoint] of newMap) {
    if (!oldMap.has(id)) {
      summary.endpointsAdded++;
      changes.push({
        type: 'ADDED',
        category: 'ENDPOINT',
        severity: 'INFO',
        path: `${newEndpoint.method} ${newEndpoint.path}`,
        description: `New endpoint: ${newEndpoint.method} ${newEndpoint.path}`,
        newValue: id,
      });
    }
  }

  // Modified endpoints
  for (const [id, newEndpoint] of newMap) {
    const oldEndpoint = oldMap.get(id);
    if (oldEndpoint) {
      const endpointChanges = compareEndpointDetails(oldEndpoint, newEndpoint, summary);
      if (endpointChanges.length > 0) {
        summary.endpointsModified++;
      }
      changes.push(...endpointChanges);
    }
  }

  return changes;
}

/**
 * Compares details of two endpoints
 */
function compareEndpointDetails(
  oldEndpoint: Endpoint,
  newEndpoint: Endpoint,
  summary: ComparisonSummary
): Change[] {
  const changes: Change[] = [];
  const basePath = `${newEndpoint.method} ${newEndpoint.path}`;

  // Check deprecation
  if (!oldEndpoint.deprecated && newEndpoint.deprecated) {
    summary.endpointsDeprecated++;
    changes.push({
      type: 'DEPRECATED',
      category: 'ENDPOINT',
      severity: 'WARNING',
      path: basePath,
      description: `Endpoint deprecated: ${basePath}`,
    });
  }

  // Compare parameters
  const paramChanges = compareParameters(
    oldEndpoint.parameters,
    newEndpoint.parameters,
    basePath,
    summary
  );
  changes.push(...paramChanges);

  // Compare request body
  if (oldEndpoint.requestBody || newEndpoint.requestBody) {
    const reqBodyChanges = compareRequestBody(
      oldEndpoint.requestBody,
      newEndpoint.requestBody,
      basePath
    );
    changes.push(...reqBodyChanges);
  }

  // Compare responses
  const responseChanges = compareResponses(oldEndpoint.responses, newEndpoint.responses, basePath);
  changes.push(...responseChanges);

  return changes;
}

/**
 * Compares parameters between endpoints
 */
function compareParameters(
  oldParams: Parameter[],
  newParams: Parameter[],
  basePath: string,
  summary: ComparisonSummary
): Change[] {
  const changes: Change[] = [];
  const oldMap = new Map(oldParams.map((p) => [`${p.location}:${p.name}`, p]));
  const newMap = new Map(newParams.map((p) => [`${p.location}:${p.name}`, p]));

  // Removed parameters
  for (const [key, oldParam] of oldMap) {
    if (!newMap.has(key)) {
      summary.parametersRemoved++;
      const severity: ChangeSeverity = oldParam.required ? 'BREAKING' : 'WARNING';
      changes.push({
        type: 'REMOVED',
        category: 'PARAMETER',
        severity,
        path: `${basePath} -> ${oldParam.location}:${oldParam.name}`,
        description: `Parameter removed: ${oldParam.name} (${oldParam.location})`,
        oldValue: JSON.stringify(oldParam),
      });
    }
  }

  // Added parameters
  for (const [key, newParam] of newMap) {
    if (!oldMap.has(key)) {
      summary.parametersAdded++;
      const severity: ChangeSeverity = newParam.required ? 'BREAKING' : 'INFO';
      changes.push({
        type: 'ADDED',
        category: 'PARAMETER',
        severity,
        path: `${basePath} -> ${newParam.location}:${newParam.name}`,
        description: `New ${newParam.required ? 'required ' : ''}parameter: ${newParam.name} (${newParam.location})`,
        newValue: JSON.stringify(newParam),
      });
    }
  }

  // Modified parameters
  for (const [key, newParam] of newMap) {
    const oldParam = oldMap.get(key);
    if (oldParam) {
      const paramChanges = compareParameterDetails(oldParam, newParam, basePath);
      if (paramChanges.length > 0) {
        summary.parametersModified++;
      }
      changes.push(...paramChanges);
    }
  }

  return changes;
}

/**
 * Compares details of two parameters
 */
function compareParameterDetails(
  oldParam: Parameter,
  newParam: Parameter,
  basePath: string
): Change[] {
  const changes: Change[] = [];
  const paramPath = `${basePath} -> ${newParam.location}:${newParam.name}`;

  // Required changed from false to true (BREAKING)
  if (!oldParam.required && newParam.required) {
    changes.push({
      type: 'MODIFIED',
      category: 'PARAMETER',
      severity: 'BREAKING',
      path: paramPath,
      description: `Parameter is now required: ${newParam.name}`,
      oldValue: 'optional',
      newValue: 'required',
    });
  }

  // Type changed (potentially BREAKING)
  if (oldParam.type !== newParam.type) {
    const isCompatible = isTypeCompatible(oldParam.type, newParam.type);
    changes.push({
      type: 'MODIFIED',
      category: 'PARAMETER',
      severity: isCompatible ? 'WARNING' : 'BREAKING',
      path: paramPath,
      description: `Parameter type changed: ${oldParam.type} -> ${newParam.type}`,
      oldValue: oldParam.type,
      newValue: newParam.type,
    });
  }

  return changes;
}

/**
 * Compares request body definitions
 */
function compareRequestBody(
  oldBody: Endpoint['requestBody'],
  newBody: Endpoint['requestBody'],
  basePath: string
): Change[] {
  const changes: Change[] = [];

  if (!oldBody && newBody) {
    changes.push({
      type: 'ADDED',
      category: 'REQUEST_BODY',
      severity: newBody.required ? 'BREAKING' : 'INFO',
      path: `${basePath} -> requestBody`,
      description: `Request body ${newBody.required ? 'required' : 'added'}`,
    });
  }

  if (oldBody && !newBody) {
    changes.push({
      type: 'REMOVED',
      category: 'REQUEST_BODY',
      severity: 'INFO',
      path: `${basePath} -> requestBody`,
      description: 'Request body removed',
    });
  }

  if (oldBody && newBody) {
    if (!oldBody.required && newBody.required) {
      changes.push({
        type: 'MODIFIED',
        category: 'REQUEST_BODY',
        severity: 'BREAKING',
        path: `${basePath} -> requestBody`,
        description: 'Request body is now required',
        oldValue: 'optional',
        newValue: 'required',
      });
    }

    const oldTypes = new Set(oldBody.contentTypes);
    const newTypes = new Set(newBody.contentTypes);
    for (const type of oldTypes) {
      if (!newTypes.has(type)) {
        changes.push({
          type: 'REMOVED',
          category: 'REQUEST_BODY',
          severity: 'BREAKING',
          path: `${basePath} -> requestBody`,
          description: `Content type removed: ${type}`,
          oldValue: type,
        });
      }
    }
  }

  return changes;
}

/**
 * Compares response definitions
 */
function compareResponses(
  oldResponses: Endpoint['responses'],
  newResponses: Endpoint['responses'],
  basePath: string
): Change[] {
  const changes: Change[] = [];
  const oldMap = new Map(oldResponses.map((r) => [r.statusCode, r]));
  const newMap = new Map(newResponses.map((r) => [r.statusCode, r]));

  for (const [code, _oldResp] of oldMap) {
    if (!newMap.has(code)) {
      changes.push({
        type: 'REMOVED',
        category: 'RESPONSE',
        severity: 'WARNING',
        path: `${basePath} -> response:${code}`,
        description: `Response removed: ${code}`,
        oldValue: code,
      });
    }
  }

  for (const [code, newResp] of newMap) {
    if (!oldMap.has(code)) {
      changes.push({
        type: 'ADDED',
        category: 'RESPONSE',
        severity: 'INFO',
        path: `${basePath} -> response:${code}`,
        description: `New response: ${code} - ${newResp.description}`,
        newValue: code,
      });
    }
  }

  return changes;
}

/**
 * Compares schema definitions
 */
function compareSchemas(
  oldSchemas: Schema[],
  newSchemas: Schema[],
  summary: ComparisonSummary
): Change[] {
  const changes: Change[] = [];
  const oldMap = new Map(oldSchemas.map((s) => [s.name, s]));
  const newMap = new Map(newSchemas.map((s) => [s.name, s]));

  for (const [name, _oldSchema] of oldMap) {
    if (!newMap.has(name)) {
      summary.schemasRemoved++;
      changes.push({
        type: 'REMOVED',
        category: 'SCHEMA',
        severity: 'DANGEROUS',
        path: `#/components/schemas/${name}`,
        description: `Schema removed: ${name}`,
        oldValue: name,
      });
    }
  }

  for (const [name, _newSchema] of newMap) {
    if (!oldMap.has(name)) {
      summary.schemasAdded++;
      changes.push({
        type: 'ADDED',
        category: 'SCHEMA',
        severity: 'INFO',
        path: `#/components/schemas/${name}`,
        description: `New schema: ${name}`,
        newValue: name,
      });
    }
  }

  for (const [name, newSchema] of newMap) {
    const oldSchema = oldMap.get(name);
    if (oldSchema) {
      const schemaChanges = compareSchemaDetails(oldSchema, newSchema);
      if (schemaChanges.length > 0) {
        summary.schemasModified++;
      }
      changes.push(...schemaChanges);
    }
  }

  return changes;
}

/**
 * Compares details of two schemas
 */
function compareSchemaDetails(oldSchema: Schema, newSchema: Schema): Change[] {
  const changes: Change[] = [];
  const basePath = `#/components/schemas/${newSchema.name}`;

  const oldProps = new Map(oldSchema.properties.map((p) => [p.name, p]));
  const newProps = new Map(newSchema.properties.map((p) => [p.name, p]));

  for (const [propName, _oldProp] of oldProps) {
    if (!newProps.has(propName)) {
      changes.push({
        type: 'REMOVED',
        category: 'SCHEMA_PROPERTY',
        severity: 'DANGEROUS',
        path: `${basePath}/${propName}`,
        description: `Property removed from schema: ${propName}`,
      });
    }
  }

  for (const [propName, newProp] of newProps) {
    if (!oldProps.has(propName)) {
      const severity: ChangeSeverity = newProp.required ? 'WARNING' : 'INFO';
      changes.push({
        type: 'ADDED',
        category: 'SCHEMA_PROPERTY',
        severity,
        path: `${basePath}/${propName}`,
        description: `New property in schema: ${propName}${newProp.required ? ' (required)' : ''}`,
      });
    }
  }

  for (const [propName, newProp] of newProps) {
    const oldProp = oldProps.get(propName);
    if (oldProp) {
      if (oldProp.type !== newProp.type) {
        changes.push({
          type: 'MODIFIED',
          category: 'SCHEMA_PROPERTY',
          severity: 'BREAKING',
          path: `${basePath}/${propName}`,
          description: `Property type changed: ${oldProp.type} -> ${newProp.type}`,
          oldValue: oldProp.type,
          newValue: newProp.type,
        });
      }

      if (!oldProp.required && newProp.required) {
        changes.push({
          type: 'MODIFIED',
          category: 'SCHEMA_PROPERTY',
          severity: 'BREAKING',
          path: `${basePath}/${propName}`,
          description: `Property is now required: ${propName}`,
          oldValue: 'optional',
          newValue: 'required',
        });
      }
    }
  }

  return changes;
}

/**
 * Compares security definitions
 */
function compareSecurity(
  oldSecurity: ApiSpec['security'],
  newSecurity: ApiSpec['security']
): Change[] {
  const changes: Change[] = [];
  const oldMap = new Map(oldSecurity.map((s) => [s.name, s]));
  const newMap = new Map(newSecurity.map((s) => [s.name, s]));

  for (const [name] of oldMap) {
    if (!newMap.has(name)) {
      changes.push({
        type: 'REMOVED',
        category: 'SECURITY',
        severity: 'BREAKING',
        path: `#/components/securitySchemes/${name}`,
        description: `Security scheme removed: ${name}`,
        oldValue: name,
      });
    }
  }

  for (const [name] of newMap) {
    if (!oldMap.has(name)) {
      changes.push({
        type: 'ADDED',
        category: 'SECURITY',
        severity: 'INFO',
        path: `#/components/securitySchemes/${name}`,
        description: `New security scheme: ${name}`,
        newValue: name,
      });
    }
  }

  return changes;
}

/**
 * Checks if two types are compatible
 */
function isTypeCompatible(oldType: string, newType: string): boolean {
  if (oldType === newType) return true;
  if (oldType === 'number' && newType === 'integer') return true;
  if (newType === 'string') return true;
  return false;
}

/**
 * Creates a breaking change with migration suggestion
 */
function createBreakingChange(change: Change): BreakingChange {
  return {
    ...change,
    migrationSuggestion: generateMigrationSuggestion(change),
    impactScore: calculateImpactScore(change),
  };
}

/**
 * Generates a migration suggestion based on the change
 */
function generateMigrationSuggestion(change: Change): string {
  switch (change.category) {
    case 'ENDPOINT':
      if (change.type === 'REMOVED') {
        return 'Remove all references to this endpoint from client code';
      }
      break;
    case 'PARAMETER':
      if (change.type === 'REMOVED') {
        return 'Remove this parameter from API calls';
      }
      if (change.type === 'ADDED' && change.description.includes('required')) {
        return 'Add this required parameter to all API calls';
      }
      if (change.type === 'MODIFIED') {
        return 'Update the parameter type/format in API calls';
      }
      break;
    case 'SCHEMA':
    case 'SCHEMA_PROPERTY':
      if (change.type === 'REMOVED') {
        return 'Remove references to this schema/property in client code';
      }
      if (change.type === 'MODIFIED') {
        return 'Update client code to handle the new type/format';
      }
      break;
    case 'SECURITY':
      return 'Update authentication configuration';
  }
  return 'Review and update client code accordingly';
}

/**
 * Calculates the impact score for a breaking change
 */
function calculateImpactScore(change: Change): number {
  let score = 50;
  if (change.category === 'ENDPOINT') score += 30;
  if (change.type === 'REMOVED') score += 20;
  if (change.category === 'SECURITY') score += 25;
  return Math.min(100, score);
}

/**
 * Calculates overall risk score
 */
function calculateRiskScore(breakingChanges: BreakingChange[], allChanges: Change[]): number {
  if (allChanges.length === 0) return 0;

  let totalScore = 0;
  for (const change of breakingChanges) {
    totalScore += change.impactScore;
  }

  const dangerousChanges = allChanges.filter((c) => c.severity === 'DANGEROUS');
  totalScore += dangerousChanges.length * 15;

  const maxPossible = breakingChanges.length * 100 + dangerousChanges.length * 15;
  return Math.min(100, Math.round((totalScore / Math.max(maxPossible, 100)) * 100));
}

/**
 * Determines the risk level based on score
 */
function calculateRiskLevel(score: number): RiskLevel {
  if (score >= 75) return 'CRITICAL';
  if (score >= 50) return 'HIGH';
  if (score >= 25) return 'MEDIUM';
  return 'LOW';
}

/**
 * Determines the recommended semantic version bump
 */
function getSemverRecommendation(
  breakingChanges: BreakingChange[],
  changes: Change[]
): SemverRecommendation {
  if (breakingChanges.length > 0) return 'MAJOR';
  if (changes.some((c) => c.type === 'ADDED')) return 'MINOR';
  return 'PATCH';
}

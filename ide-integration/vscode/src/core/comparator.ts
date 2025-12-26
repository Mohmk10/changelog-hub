import {
  ApiSpec,
  Endpoint,
  Schema,
  Change,
  BreakingChange,
  ComparisonResult,
  ComparisonSummary,
  RiskLevel,
  SemverRecommendation,
} from '../types';

/**
 * Compares two API specifications and identifies all changes.
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

  // Compare security
  const securityChanges = compareSecurity(oldSpec.security, newSpec.security);
  changes.push(...securityChanges);

  // Extract breaking changes
  for (const change of changes) {
    if (change.severity === 'BREAKING') {
      breakingChanges.push(createBreakingChange(change));
    }
  }

  // Calculate risk
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

function compareEndpoints(
  oldEndpoints: Endpoint[],
  newEndpoints: Endpoint[],
  summary: ComparisonSummary
): Change[] {
  const changes: Change[] = [];
  const oldMap = new Map(oldEndpoints.map((e) => [e.id, e]));
  const newMap = new Map(newEndpoints.map((e) => [e.id, e]));

  // Removed endpoints
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

  // Added endpoints
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
  const oldParams = new Map(oldEndpoint.parameters.map((p) => [p.name, p]));
  const newParams = new Map(newEndpoint.parameters.map((p) => [p.name, p]));

  // Removed parameters
  for (const [name, oldParam] of oldParams) {
    if (!newParams.has(name)) {
      summary.parametersRemoved++;
      changes.push({
        type: 'REMOVED',
        category: 'PARAMETER',
        severity: oldParam.required ? 'BREAKING' : 'WARNING',
        path: `${basePath} > ${name}`,
        description: `Parameter removed: ${name}`,
        oldValue: oldParam,
      });
    }
  }

  // Added parameters
  for (const [name, newParam] of newParams) {
    if (!oldParams.has(name)) {
      summary.parametersAdded++;
      changes.push({
        type: 'ADDED',
        category: 'PARAMETER',
        severity: newParam.required ? 'BREAKING' : 'INFO',
        path: `${basePath} > ${name}`,
        description: `New parameter: ${name} (${newParam.location})`,
        newValue: newParam,
      });
    }
  }

  // Modified parameters
  for (const [name, newParam] of newParams) {
    const oldParam = oldParams.get(name);
    if (oldParam) {
      // Type change
      if (oldParam.type !== newParam.type) {
        summary.parametersModified++;
        changes.push({
          type: 'MODIFIED',
          category: 'PARAMETER',
          severity: 'BREAKING',
          path: `${basePath} > ${name}`,
          description: `Parameter type changed: ${name} (${oldParam.type} → ${newParam.type})`,
          oldValue: oldParam.type,
          newValue: newParam.type,
        });
      }

      // Required change
      if (!oldParam.required && newParam.required) {
        summary.parametersModified++;
        changes.push({
          type: 'MODIFIED',
          category: 'PARAMETER',
          severity: 'BREAKING',
          path: `${basePath} > ${name}`,
          description: `Parameter now required: ${name}`,
          oldValue: false,
          newValue: true,
        });
      }
    }
  }

  return changes;
}

function compareSchemas(
  oldSchemas: Schema[],
  newSchemas: Schema[],
  summary: ComparisonSummary
): Change[] {
  const changes: Change[] = [];
  const oldMap = new Map(oldSchemas.map((s) => [s.name, s]));
  const newMap = new Map(newSchemas.map((s) => [s.name, s]));

  // Removed schemas
  for (const [name] of oldMap) {
    if (!newMap.has(name)) {
      summary.schemasRemoved++;
      changes.push({
        type: 'REMOVED',
        category: 'SCHEMA',
        severity: 'BREAKING',
        path: `#/schemas/${name}`,
        description: `Schema removed: ${name}`,
        oldValue: name,
      });
    }
  }

  // Added schemas
  for (const [name] of newMap) {
    if (!oldMap.has(name)) {
      summary.schemasAdded++;
      changes.push({
        type: 'ADDED',
        category: 'SCHEMA',
        severity: 'INFO',
        path: `#/schemas/${name}`,
        description: `New schema: ${name}`,
        newValue: name,
      });
    }
  }

  // Modified schemas
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

function compareSchemaDetails(oldSchema: Schema, newSchema: Schema): Change[] {
  const changes: Change[] = [];
  const basePath = `#/schemas/${newSchema.name}`;

  const oldProps = new Map(oldSchema.properties.map((p) => [p.name, p]));
  const newProps = new Map(newSchema.properties.map((p) => [p.name, p]));

  // Removed properties
  for (const [name, oldProp] of oldProps) {
    if (!newProps.has(name)) {
      changes.push({
        type: 'REMOVED',
        category: 'SCHEMA',
        severity: 'BREAKING',
        path: `${basePath}/${name}`,
        description: `Property removed from ${newSchema.name}: ${name}`,
        oldValue: oldProp,
      });
    }
  }

  // Added properties
  for (const [name, newProp] of newProps) {
    if (!oldProps.has(name)) {
      const isRequired = newSchema.required.includes(name);
      changes.push({
        type: 'ADDED',
        category: 'SCHEMA',
        severity: isRequired ? 'BREAKING' : 'INFO',
        path: `${basePath}/${name}`,
        description: `New property in schema: ${name}`,
        newValue: newProp,
      });
    }
  }

  // Modified properties
  for (const [name, newProp] of newProps) {
    const oldProp = oldProps.get(name);
    if (oldProp) {
      if (oldProp.type !== newProp.type) {
        changes.push({
          type: 'MODIFIED',
          category: 'SCHEMA',
          severity: 'BREAKING',
          path: `${basePath}/${name}`,
          description: `Property type changed in ${newSchema.name}: ${name} (${oldProp.type} → ${newProp.type})`,
          oldValue: oldProp.type,
          newValue: newProp.type,
        });
      }

      const wasRequired = oldSchema.required.includes(name);
      const isRequired = newSchema.required.includes(name);
      if (!wasRequired && isRequired) {
        changes.push({
          type: 'MODIFIED',
          category: 'SCHEMA',
          severity: 'BREAKING',
          path: `${basePath}/${name}`,
          description: `Property now required in ${newSchema.name}: ${name}`,
          oldValue: false,
          newValue: true,
        });
      }
    }
  }

  return changes;
}

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
        path: `#/security/${name}`,
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
        severity: 'WARNING',
        path: `#/security/${name}`,
        description: `New security scheme: ${name}`,
        newValue: name,
      });
    }
  }

  return changes;
}

function createBreakingChange(change: Change): BreakingChange {
  return {
    type: change.type,
    category: change.category,
    path: change.path,
    description: change.description,
    migrationSuggestion: getMigrationSuggestion(change),
    impactScore: getImpactScore(change),
  };
}

function getMigrationSuggestion(change: Change): string {
  switch (change.type) {
    case 'REMOVED':
      if (change.category === 'ENDPOINT') {
        return 'Update client code to use an alternative endpoint or remove the call.';
      }
      if (change.category === 'PARAMETER') {
        return 'Remove the parameter from API calls.';
      }
      if (change.category === 'SCHEMA') {
        return 'Update data models to reflect the removed property.';
      }
      return 'Update code to handle the removal.';
    case 'MODIFIED':
      if (change.category === 'PARAMETER') {
        return 'Update the parameter type or ensure it is always provided.';
      }
      return 'Update code to handle the new type or requirement.';
    default:
      return 'Review the change and update code accordingly.';
  }
}

function getImpactScore(change: Change): number {
  let score = 0;
  if (change.category === 'ENDPOINT') score += 40;
  else if (change.category === 'SCHEMA') score += 30;
  else if (change.category === 'PARAMETER') score += 20;
  else if (change.category === 'SECURITY') score += 35;

  if (change.type === 'REMOVED') score += 30;
  else if (change.type === 'MODIFIED') score += 20;

  return Math.min(100, score);
}

function calculateRiskScore(breakingChanges: BreakingChange[], changes: Change[]): number {
  if (breakingChanges.length === 0) return 0;
  const avgImpact =
    breakingChanges.reduce((sum, bc) => sum + bc.impactScore, 0) / breakingChanges.length;
  const multiplier = Math.min(2, 1 + breakingChanges.length * 0.1);
  return Math.min(100, Math.round(avgImpact * multiplier));
}

function calculateRiskLevel(score: number): RiskLevel {
  if (score >= 75) return 'CRITICAL';
  if (score >= 50) return 'HIGH';
  if (score >= 25) return 'MEDIUM';
  return 'LOW';
}

function getSemverRecommendation(
  breakingChanges: BreakingChange[],
  changes: Change[]
): SemverRecommendation {
  if (breakingChanges.length > 0) return 'MAJOR';
  if (changes.some((c) => c.type === 'ADDED')) return 'MINOR';
  return 'PATCH';
}

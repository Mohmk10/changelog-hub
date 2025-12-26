package io.github.mohmk10.changeloghub.intellij.model

/**
 * API specification model.
 */
data class ApiSpec(
    val name: String,
    val version: String,
    val type: ApiType,
    val endpoints: List<Endpoint>,
    val schemas: List<Schema> = emptyList()
)

/**
 * API type enumeration.
 */
enum class ApiType {
    REST,
    GRAPHQL,
    GRPC,
    ASYNCAPI
}

/**
 * Endpoint model.
 */
data class Endpoint(
    val method: String,
    val path: String,
    val operationId: String? = null,
    val summary: String? = null,
    val description: String? = null,
    val parameters: List<Parameter> = emptyList(),
    val deprecated: Boolean = false
)

/**
 * Parameter model.
 */
data class Parameter(
    val name: String,
    val location: String,
    val required: Boolean = false,
    val type: String? = null,
    val description: String? = null
)

/**
 * Schema model.
 */
data class Schema(
    val name: String,
    val type: String,
    val properties: List<Property> = emptyList()
)

/**
 * Property model.
 */
data class Property(
    val name: String,
    val type: String,
    val required: Boolean = false
)

/**
 * Changelog model.
 */
data class Changelog(
    val changes: List<Change>,
    val breakingChanges: List<Change> = changes.filter { it.severity == Severity.BREAKING }
)

/**
 * Change model.
 */
data class Change(
    val path: String,
    val changeType: ChangeType,
    val category: ChangeCategory,
    val severity: Severity,
    val description: String,
    val migrationSuggestion: String? = null,
    val impactScore: Int = 0
)

/**
 * Change type enumeration.
 */
enum class ChangeType {
    ADDED,
    REMOVED,
    MODIFIED,
    DEPRECATED
}

/**
 * Change category enumeration.
 */
enum class ChangeCategory {
    ENDPOINT,
    PARAMETER,
    SCHEMA,
    RESPONSE,
    SECURITY
}

/**
 * Severity enumeration.
 */
enum class Severity {
    SAFE,
    INFO,
    WARNING,
    DANGEROUS,
    BREAKING
}

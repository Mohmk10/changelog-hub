package io.github.mohmk10.changeloghub.intellij.model

data class ApiSpec(
    val name: String,
    val version: String,
    val type: ApiType,
    val endpoints: List<Endpoint>,
    val schemas: List<Schema> = emptyList()
)

enum class ApiType {
    REST,
    GRAPHQL,
    GRPC,
    ASYNCAPI
}

data class Endpoint(
    val method: String,
    val path: String,
    val operationId: String? = null,
    val summary: String? = null,
    val description: String? = null,
    val parameters: List<Parameter> = emptyList(),
    val deprecated: Boolean = false
)

data class Parameter(
    val name: String,
    val location: String,
    val required: Boolean = false,
    val type: String? = null,
    val description: String? = null
)

data class Schema(
    val name: String,
    val type: String,
    val properties: List<Property> = emptyList()
)

data class Property(
    val name: String,
    val type: String,
    val required: Boolean = false
)

data class Changelog(
    val changes: List<Change>,
    val breakingChanges: List<Change> = changes.filter { it.severity == Severity.BREAKING }
)

data class Change(
    val path: String,
    val changeType: ChangeType,
    val category: ChangeCategory,
    val severity: Severity,
    val description: String,
    val migrationSuggestion: String? = null,
    val impactScore: Int = 0
)

enum class ChangeType {
    ADDED,
    REMOVED,
    MODIFIED,
    DEPRECATED
}

enum class ChangeCategory {
    ENDPOINT,
    PARAMETER,
    SCHEMA,
    RESPONSE,
    SECURITY
}

enum class Severity {
    SAFE,
    INFO,
    WARNING,
    DANGEROUS,
    BREAKING
}

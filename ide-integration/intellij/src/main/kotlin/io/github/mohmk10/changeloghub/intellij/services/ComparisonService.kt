package io.github.mohmk10.changeloghub.intellij.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import io.github.mohmk10.changeloghub.intellij.model.*
import io.github.mohmk10.changeloghub.intellij.util.FileUtils
import io.github.mohmk10.changeloghub.intellij.util.Logger

@Service(Service.Level.PROJECT)
class ComparisonService(private val project: Project) {

    private val parserService: ParserService
        get() = project.getService(ParserService::class.java)

    fun compare(oldFile: VirtualFile, newFile: VirtualFile): ComparisonResult {
        val oldContent = FileUtils.readContent(oldFile)
        val newContent = FileUtils.readContent(newFile)
        return compareContents(oldContent, oldFile.name, newContent, newFile.name)
    }

    fun compareWithContent(oldContent: String, newFile: VirtualFile): ComparisonResult {
        val newContent = FileUtils.readContent(newFile)
        return compareContents(oldContent, "old", newContent, newFile.name)
    }

    private fun compareContents(
        oldContent: String,
        oldFileName: String,
        newContent: String,
        newFileName: String
    ): ComparisonResult {
        Logger.info("Comparing $oldFileName with $newFileName")

        val oldSpec = parserService.parse(oldContent, oldFileName)
        val newSpec = parserService.parse(newContent, newFileName)

        val changes = compareSpecs(oldSpec, newSpec)
        val breakingChanges = changes.filter { it.severity == Severity.BREAKING }

        val changeInfos = changes.map { change ->
            ChangeInfo(
                path = change.path,
                type = change.changeType.name,
                category = change.category.name,
                severity = change.severity.name,
                description = change.description,
                migrationSuggestion = change.migrationSuggestion ?: ""
            )
        }

        val breakingChangeInfos = breakingChanges.map { change ->
            BreakingChangeInfo(
                path = change.path,
                type = change.changeType.name,
                category = change.category.name,
                description = change.description,
                migrationSuggestion = change.migrationSuggestion ?: "Review the change and update clients accordingly",
                impactScore = change.impactScore
            )
        }

        return ComparisonResult(
            changes = changeInfos,
            breakingChanges = breakingChangeInfos,
            breakingChangesCount = breakingChanges.size,
            totalChangesCount = changes.size,
            riskLevel = calculateRiskLevel(breakingChanges.size, changes.size),
            semverRecommendation = getSemverRecommendation(breakingChanges.isNotEmpty(), changeInfos)
        )
    }

    private fun compareSpecs(oldSpec: ApiSpec, newSpec: ApiSpec): List<Change> {
        val changes = mutableListOf<Change>()

        val oldEndpoints = oldSpec.endpoints.associateBy { "${it.method}:${it.path}" }
        val newEndpoints = newSpec.endpoints.associateBy { "${it.method}:${it.path}" }

        oldEndpoints.forEach { (key, oldEndpoint) ->
            if (!newEndpoints.containsKey(key)) {
                changes.add(Change(
                    path = "${oldEndpoint.method} ${oldEndpoint.path}",
                    changeType = ChangeType.REMOVED,
                    category = ChangeCategory.ENDPOINT,
                    severity = Severity.BREAKING,
                    description = "Endpoint removed: ${oldEndpoint.method} ${oldEndpoint.path}",
                    migrationSuggestion = "Remove all client calls to this endpoint",
                    impactScore = 10
                ))
            }
        }

        newEndpoints.forEach { (key, newEndpoint) ->
            if (!oldEndpoints.containsKey(key)) {
                changes.add(Change(
                    path = "${newEndpoint.method} ${newEndpoint.path}",
                    changeType = ChangeType.ADDED,
                    category = ChangeCategory.ENDPOINT,
                    severity = Severity.SAFE,
                    description = "Endpoint added: ${newEndpoint.method} ${newEndpoint.path}",
                    impactScore = 0
                ))
            }
        }

        oldEndpoints.forEach { (key, oldEndpoint) ->
            val newEndpoint = newEndpoints[key] ?: return@forEach

            if (!oldEndpoint.deprecated && newEndpoint.deprecated) {
                changes.add(Change(
                    path = "${oldEndpoint.method} ${oldEndpoint.path}",
                    changeType = ChangeType.DEPRECATED,
                    category = ChangeCategory.ENDPOINT,
                    severity = Severity.WARNING,
                    description = "Endpoint deprecated: ${oldEndpoint.method} ${oldEndpoint.path}",
                    migrationSuggestion = "Plan migration to alternative endpoint",
                    impactScore = 5
                ))
            }

            compareParameters(oldEndpoint, newEndpoint, changes)
        }

        compareSchemas(oldSpec.schemas, newSpec.schemas, changes)

        return changes
    }

    private fun compareParameters(oldEndpoint: Endpoint, newEndpoint: Endpoint, changes: MutableList<Change>) {
        val oldParams = oldEndpoint.parameters.associateBy { it.name }
        val newParams = newEndpoint.parameters.associateBy { it.name }
        val path = "${oldEndpoint.method} ${oldEndpoint.path}"

        oldParams.forEach { (name, _) ->
            if (!newParams.containsKey(name)) {
                changes.add(Change(
                    path = "$path parameter '$name'",
                    changeType = ChangeType.REMOVED,
                    category = ChangeCategory.PARAMETER,
                    severity = Severity.DANGEROUS,
                    description = "Parameter '$name' removed from $path",
                    migrationSuggestion = "Remove this parameter from client requests",
                    impactScore = 7
                ))
            }
        }

        newParams.forEach { (name, param) ->
            if (!oldParams.containsKey(name) && param.required) {
                changes.add(Change(
                    path = "$path parameter '$name'",
                    changeType = ChangeType.ADDED,
                    category = ChangeCategory.PARAMETER,
                    severity = Severity.BREAKING,
                    description = "Required parameter '$name' added to $path",
                    migrationSuggestion = "Add this required parameter to all client requests",
                    impactScore = 9
                ))
            } else if (!oldParams.containsKey(name)) {
                changes.add(Change(
                    path = "$path parameter '$name'",
                    changeType = ChangeType.ADDED,
                    category = ChangeCategory.PARAMETER,
                    severity = Severity.SAFE,
                    description = "Optional parameter '$name' added to $path",
                    impactScore = 0
                ))
            }
        }

        oldParams.forEach { (name, oldParam) ->
            val newParam = newParams[name] ?: return@forEach
            if (!oldParam.required && newParam.required) {
                changes.add(Change(
                    path = "$path parameter '$name'",
                    changeType = ChangeType.MODIFIED,
                    category = ChangeCategory.PARAMETER,
                    severity = Severity.BREAKING,
                    description = "Parameter '$name' changed from optional to required in $path",
                    migrationSuggestion = "Ensure this parameter is provided in all client requests",
                    impactScore = 8
                ))
            }
        }
    }

    private fun compareSchemas(oldSchemas: List<Schema>, newSchemas: List<Schema>, changes: MutableList<Change>) {
        val oldSchemaMap = oldSchemas.associateBy { it.name }
        val newSchemaMap = newSchemas.associateBy { it.name }

        oldSchemaMap.forEach { (name, _) ->
            if (!newSchemaMap.containsKey(name)) {
                changes.add(Change(
                    path = "schema '$name'",
                    changeType = ChangeType.REMOVED,
                    category = ChangeCategory.SCHEMA,
                    severity = Severity.BREAKING,
                    description = "Schema '$name' removed",
                    migrationSuggestion = "Update client code that uses this schema",
                    impactScore = 10
                ))
            }
        }

        newSchemaMap.forEach { (name, _) ->
            if (!oldSchemaMap.containsKey(name)) {
                changes.add(Change(
                    path = "schema '$name'",
                    changeType = ChangeType.ADDED,
                    category = ChangeCategory.SCHEMA,
                    severity = Severity.SAFE,
                    description = "Schema '$name' added",
                    impactScore = 0
                ))
            }
        }

        oldSchemaMap.forEach { (name, oldSchema) ->
            val newSchema = newSchemaMap[name] ?: return@forEach
            compareSchemaProperties(name, oldSchema.properties, newSchema.properties, changes)
        }
    }

    private fun compareSchemaProperties(schemaName: String, oldProps: List<Property>, newProps: List<Property>, changes: MutableList<Change>) {
        val oldPropMap = oldProps.associateBy { it.name }
        val newPropMap = newProps.associateBy { it.name }

        oldPropMap.forEach { (name, oldProp) ->
            if (!newPropMap.containsKey(name)) {
                changes.add(Change(
                    path = "schema '$schemaName' property '$name'",
                    changeType = ChangeType.REMOVED,
                    category = ChangeCategory.SCHEMA,
                    severity = if (oldProp.required) Severity.BREAKING else Severity.WARNING,
                    description = "Property '$name' removed from schema '$schemaName'",
                    migrationSuggestion = "Update client code that uses this property",
                    impactScore = if (oldProp.required) 8 else 4
                ))
            }
        }

        newPropMap.forEach { (name, newProp) ->
            if (!oldPropMap.containsKey(name) && newProp.required) {
                changes.add(Change(
                    path = "schema '$schemaName' property '$name'",
                    changeType = ChangeType.ADDED,
                    category = ChangeCategory.SCHEMA,
                    severity = Severity.BREAKING,
                    description = "Required property '$name' added to schema '$schemaName'",
                    migrationSuggestion = "Provide this required property in all requests",
                    impactScore = 8
                ))
            }
        }
    }

    private fun calculateRiskLevel(breakingCount: Int, totalCount: Int): String {
        return when {
            breakingCount >= 5 -> "CRITICAL"
            breakingCount >= 3 -> "HIGH"
            breakingCount >= 1 -> "MEDIUM"
            else -> "LOW"
        }
    }

    private fun getSemverRecommendation(hasBreaking: Boolean, changes: List<ChangeInfo>): String {
        return when {
            hasBreaking -> "MAJOR"
            changes.any { it.type == "ADDED" } -> "MINOR"
            else -> "PATCH"
        }
    }
}

data class ComparisonResult(
    val changes: List<ChangeInfo>,
    val breakingChanges: List<BreakingChangeInfo>,
    val breakingChangesCount: Int,
    val totalChangesCount: Int,
    val riskLevel: String,
    val semverRecommendation: String
)

data class ChangeInfo(
    val path: String,
    val type: String,
    val category: String,
    val severity: String,
    val description: String,
    val migrationSuggestion: String
)

data class BreakingChangeInfo(
    val path: String,
    val type: String,
    val category: String,
    val description: String,
    val migrationSuggestion: String,
    val impactScore: Int
)

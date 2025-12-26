package io.github.mohmk10.changeloghub.gradle.task

import io.github.mohmk10.changeloghub.gradle.ChangelogHubExtension
import io.github.mohmk10.changeloghub.gradle.util.ParserFactory
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File

/**
 * Gradle task for validating an API specification file.
 *
 * Usage:
 * ```
 * ./gradlew changelogValidate --spec=api/openapi.yaml
 * ```
 *
 * Or configure via extension:
 * ```
 * changelogHub {
 *     spec = "api/openapi.yaml"
 *     strict = true
 * }
 * ```
 */
abstract class ValidateTask : DefaultTask() {

    @Internal
    lateinit var extension: ChangelogHubExtension

    @get:Input
    @get:Optional
    @get:Option(option = "spec", description = "Path to the API specification to validate")
    var specPath: String? = null

    @get:Input
    @get:Optional
    @get:Option(option = "strict", description = "Enable strict validation mode")
    var strictOption: Boolean? = null

    @TaskAction
    fun validate() {
        // Check if skipped
        if (extension.skip) {
            logger.lifecycle("API validation skipped (skip=true)")
            return
        }

        // Resolve parameters
        val specFile = resolveSpec()
        val strict = strictOption ?: extension.strict

        // Validate file exists
        validateFileExists(specFile)

        logger.lifecycle("Validating API specification...")
        logger.lifecycle("  File: ${specFile.absolutePath}")
        logger.lifecycle("  Strict mode: $strict")

        try {
            // Detect spec type
            val specType = ParserFactory.detectSpecType(specFile)

            if (specType == ParserFactory.SpecType.UNKNOWN) {
                throw GradleException(
                    "Unable to determine specification type for file: ${specFile.name}. " +
                    ParserFactory.getSupportedFormats()
                )
            }

            logger.lifecycle("  Detected type: $specType")

            // Parse specification (validation happens during parsing)
            val spec = ParserFactory.parse(specFile, extension.specType)

            // Additional strict validations
            val issues = mutableListOf<String>()
            val warnings = mutableListOf<String>()

            // Check for missing metadata
            if (spec.name.isNullOrBlank()) {
                if (strict) {
                    issues.add("Missing API name/title")
                } else {
                    warnings.add("Missing API name/title")
                }
            }

            if (spec.version.isNullOrBlank()) {
                if (strict) {
                    issues.add("Missing API version")
                } else {
                    warnings.add("Missing API version")
                }
            }

            // Check for empty spec
            if (spec.endpoints.isEmpty()) {
                if (strict) {
                    issues.add("No endpoints defined in specification")
                } else {
                    warnings.add("No endpoints defined in specification")
                }
            }

            // Check for deprecated endpoints
            val deprecatedCount = spec.endpoints.count { it.isDeprecated }
            if (deprecatedCount > 0) {
                warnings.add("$deprecatedCount deprecated endpoint(s) found")
            }

            // Check for endpoints without descriptions
            val noDescriptionCount = spec.endpoints.count { it.description.isNullOrBlank() }
            if (noDescriptionCount > 0 && strict) {
                warnings.add("$noDescriptionCount endpoint(s) without description")
            }

            // Output results
            logger.lifecycle("")
            logger.lifecycle("Validation Results:")
            logger.lifecycle("=".repeat(50))

            if (issues.isEmpty() && warnings.isEmpty()) {
                logger.lifecycle("Specification is valid!")
            } else {
                if (warnings.isNotEmpty()) {
                    logger.warn("Warnings:")
                    warnings.forEach { logger.warn("  - $it") }
                }

                if (issues.isNotEmpty()) {
                    logger.error("Errors:")
                    issues.forEach { logger.error("  - $it") }
                }
            }

            logger.lifecycle("")
            logger.lifecycle("Summary:")
            logger.lifecycle("  API: ${spec.name ?: "Unknown"}")
            logger.lifecycle("  Version: ${spec.version ?: "Unknown"}")
            logger.lifecycle("  Endpoints: ${spec.endpoints.size}")
            logger.lifecycle("  Warnings: ${warnings.size}")
            logger.lifecycle("  Errors: ${issues.size}")

            // Fail if there are issues in strict mode
            if (issues.isNotEmpty()) {
                throw GradleException(
                    "Validation failed with ${issues.size} error(s). " +
                    "See details above."
                )
            }

            logger.lifecycle("")
            logger.lifecycle("Validation passed!")

        } catch (e: GradleException) {
            throw e
        } catch (e: Exception) {
            throw GradleException("Failed to validate API specification: ${e.message}", e)
        }
    }

    private fun resolveSpec(): File {
        val path = specPath ?: extension.spec ?: extension.newSpec
            ?: throw GradleException(
                "Specification path not specified. " +
                "Use --spec option or set changelogHub.spec in build.gradle"
            )
        return project.file(path)
    }

    private fun validateFileExists(file: File) {
        if (!file.exists()) {
            throw GradleException("Specification file not found: ${file.absolutePath}")
        }
        if (!file.isFile) {
            throw GradleException("Path is not a file: ${file.absolutePath}")
        }
    }
}

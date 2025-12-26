package io.github.mohmk10.changeloghub.gradle.task

import io.github.mohmk10.changeloghub.core.comparator.impl.DefaultApiComparator
import io.github.mohmk10.changeloghub.gradle.ChangelogHubExtension
import io.github.mohmk10.changeloghub.gradle.util.ParserFactory
import io.github.mohmk10.changeloghub.gradle.util.ReportWriter
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File

/**
 * Gradle task for detecting breaking changes between API specifications.
 * Optimized for CI/CD pipelines - fails build by default when breaking changes are found.
 *
 * Usage:
 * ```
 * ./gradlew changelogDetect --old-spec=api/v1.yaml --new-spec=api/v2.yaml
 * ```
 *
 * Or configure via extension:
 * ```
 * changelogHub {
 *     oldSpec = "api/v1.yaml"
 *     newSpec = "api/v2.yaml"
 * }
 * ```
 *
 * This task is similar to CompareTask but with failOnBreaking=true by default,
 * making it suitable for CI/CD integration where breaking changes should fail the build.
 */
abstract class DetectBreakingChangesTask : DefaultTask() {

    @Internal
    lateinit var extension: ChangelogHubExtension

    @get:Input
    @get:Optional
    @get:Option(option = "old-spec", description = "Path to the old/baseline API specification")
    var oldSpecPath: String? = null

    @get:Input
    @get:Optional
    @get:Option(option = "new-spec", description = "Path to the new API specification")
    var newSpecPath: String? = null

    @get:Input
    @get:Optional
    @get:Option(option = "format", description = "Output format: console, markdown, json, html")
    var formatOption: String? = null

    @get:Input
    @get:Optional
    @get:Option(option = "output-dir", description = "Output directory for reports")
    var outputDirOption: String? = null

    @get:Input
    @get:Option(option = "allow-breaking", description = "Set to true to allow breaking changes without failing")
    var allowBreaking: Boolean = false

    @TaskAction
    fun detect() {
        // Check if skipped
        if (extension.skip) {
            logger.lifecycle("Breaking change detection skipped (skip=true)")
            return
        }

        // Resolve parameters (CLI options override extension)
        val oldSpecFile = resolveOldSpec()
        val newSpecFile = resolveNewSpec()
        val format = formatOption ?: extension.format
        val outputDir = File(project.projectDir, outputDirOption ?: extension.outputDir)

        // Fail on breaking unless explicitly allowed
        val failOnBreaking = !allowBreaking

        // Validate files exist
        validateFile(oldSpecFile, "Old specification")
        validateFile(newSpecFile, "New specification")

        logger.lifecycle("Detecting breaking changes...")
        logger.lifecycle("  Old: ${oldSpecFile.absolutePath}")
        logger.lifecycle("  New: ${newSpecFile.absolutePath}")
        logger.lifecycle("  Fail on breaking: $failOnBreaking")

        try {
            // Parse specifications
            val oldSpec = ParserFactory.parse(oldSpecFile, extension.specType)
            val newSpec = ParserFactory.parse(newSpecFile, extension.specType)

            if (extension.verbose) {
                logger.lifecycle("Parsed old spec: ${oldSpec.name} v${oldSpec.version}")
                logger.lifecycle("Parsed new spec: ${newSpec.name} v${newSpec.version}")
            }

            // Create comparator
            val comparator = DefaultApiComparator()

            // Compare specifications
            val changelog = comparator.compare(oldSpec, newSpec)

            // Output summary to console
            logger.lifecycle("")
            logger.lifecycle("=".repeat(60))
            logger.lifecycle("BREAKING CHANGE DETECTION REPORT")
            logger.lifecycle("=".repeat(60))
            logger.lifecycle("")

            if (changelog.breakingChanges.isEmpty()) {
                logger.lifecycle("No breaking changes detected!")
                logger.lifecycle("")
            } else {
                logger.error("Breaking changes detected: ${changelog.breakingChanges.size}")
                logger.lifecycle("")

                changelog.breakingChanges.forEachIndexed { index, change ->
                    logger.error("  ${index + 1}. ${change.description}")
                }
                logger.lifecycle("")
            }

            // Summary
            logger.lifecycle("-".repeat(40))
            logger.lifecycle("Summary:")
            logger.lifecycle("  Total changes: ${changelog.changes.size}")
            logger.lifecycle("  Breaking changes: ${changelog.breakingChanges.size}")
            logger.lifecycle("  Non-breaking changes: ${changelog.changes.size - changelog.breakingChanges.size}")
            logger.lifecycle("-".repeat(40))

            // Write detailed report to file
            if (format != "console") {
                outputDir.mkdirs()
                val reportFile = ReportWriter.write(outputDir, changelog, format, "breaking-changes")
                logger.lifecycle("Detailed report written to: ${reportFile.absolutePath}")
            }

            // Also generate full report if verbose
            if (extension.verbose && changelog.changes.isNotEmpty()) {
                logger.lifecycle("")
                logger.lifecycle("Full changelog:")
                val report = ReportWriter.generateReport(changelog, "console")
                logger.lifecycle(report)
            }

            // Fail if breaking changes detected and failOnBreaking is true
            if (failOnBreaking && changelog.breakingChanges.isNotEmpty()) {
                throw GradleException(
                    "\n" +
                    "BUILD FAILED: Breaking API changes detected!\n" +
                    "Found ${changelog.breakingChanges.size} breaking change(s).\n" +
                    "\n" +
                    "To allow breaking changes, use one of:\n" +
                    "  ./gradlew changelogDetect --allow-breaking\n" +
                    "  changelogHub { failOnBreaking = false }"
                )
            }

            logger.lifecycle("")
            logger.lifecycle("Breaking change detection complete!")

        } catch (e: GradleException) {
            throw e
        } catch (e: Exception) {
            throw GradleException("Failed to detect breaking changes: ${e.message}", e)
        }
    }

    private fun resolveOldSpec(): File {
        val path = oldSpecPath ?: extension.oldSpec
            ?: throw GradleException(
                "Old specification path not specified. " +
                "Use --old-spec option or set changelogHub.oldSpec in build.gradle"
            )
        return project.file(path)
    }

    private fun resolveNewSpec(): File {
        val path = newSpecPath ?: extension.newSpec
            ?: throw GradleException(
                "New specification path not specified. " +
                "Use --new-spec option or set changelogHub.newSpec in build.gradle"
            )
        return project.file(path)
    }

    private fun validateFile(file: File, description: String) {
        if (!file.exists()) {
            throw GradleException("$description file not found: ${file.absolutePath}")
        }
        if (!file.isFile) {
            throw GradleException("$description is not a file: ${file.absolutePath}")
        }
    }
}

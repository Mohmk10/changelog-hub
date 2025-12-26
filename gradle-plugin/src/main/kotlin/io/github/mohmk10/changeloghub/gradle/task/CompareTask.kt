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
 * Gradle task for comparing two API specifications.
 *
 * Usage:
 * ```
 * ./gradlew changelogCompare --old-spec=api/v1.yaml --new-spec=api/v2.yaml
 * ```
 *
 * Or configure via extension:
 * ```
 * changelogHub {
 *     oldSpec = "api/v1.yaml"
 *     newSpec = "api/v2.yaml"
 *     format = "markdown"
 *     outputDir = "build/changelog"
 * }
 * ```
 */
abstract class CompareTask : DefaultTask() {

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
    @get:Option(option = "fail-on-breaking", description = "Fail build if breaking changes are detected")
    var failOnBreakingOption: Boolean? = null

    @get:Input
    @get:Optional
    @get:Option(option = "output-dir", description = "Output directory for reports")
    var outputDirOption: String? = null

    @TaskAction
    fun compare() {
        // Check if skipped
        if (extension.skip) {
            logger.lifecycle("Changelog comparison skipped (skip=true)")
            return
        }

        // Resolve parameters (CLI options override extension)
        val oldSpecFile = resolveOldSpec()
        val newSpecFile = resolveNewSpec()
        val format = formatOption ?: extension.format
        val failOnBreaking = failOnBreakingOption ?: extension.failOnBreaking
        val outputDir = File(project.projectDir, outputDirOption ?: extension.outputDir)

        // Validate files exist
        validateFile(oldSpecFile, "Old specification")
        validateFile(newSpecFile, "New specification")

        logger.lifecycle("Comparing API specifications...")
        logger.lifecycle("  Old: ${oldSpecFile.absolutePath}")
        logger.lifecycle("  New: ${newSpecFile.absolutePath}")

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

            // Generate report
            val report = ReportWriter.generateReport(changelog, format)

            // Output to console
            logger.lifecycle("")
            logger.lifecycle(report)

            // Write to file if not console format
            if (format != "console") {
                outputDir.mkdirs()
                val reportFile = ReportWriter.write(outputDir, changelog, format)
                logger.lifecycle("Report written to: ${reportFile.absolutePath}")
            }

            // Summary
            logger.lifecycle("")
            logger.lifecycle("Comparison complete:")
            logger.lifecycle("  Total changes: ${changelog.changes.size}")
            logger.lifecycle("  Breaking changes: ${changelog.breakingChanges.size}")

            // Fail if breaking changes detected and configured to fail
            if (failOnBreaking && changelog.breakingChanges.isNotEmpty()) {
                throw GradleException(
                    "Breaking changes detected! Found ${changelog.breakingChanges.size} breaking change(s). " +
                    "See report above for details."
                )
            }

        } catch (e: GradleException) {
            throw e
        } catch (e: Exception) {
            throw GradleException("Failed to compare API specifications: ${e.message}", e)
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

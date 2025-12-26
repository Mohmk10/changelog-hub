package io.github.mohmk10.changeloghub.gradle.task

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

abstract class AnalyzeTask : DefaultTask() {

    @Internal
    lateinit var extension: ChangelogHubExtension

    @get:Input
    @get:Optional
    @get:Option(option = "spec", description = "Path to the API specification to analyze")
    var specPath: String? = null

    @get:Input
    @get:Optional
    @get:Option(option = "format", description = "Output format: console, markdown, json, html")
    var formatOption: String? = null

    @get:Input
    @get:Optional
    @get:Option(option = "output-dir", description = "Output directory for reports")
    var outputDirOption: String? = null

    @TaskAction
    fun analyze() {
        
        if (extension.skip) {
            logger.lifecycle("API analysis skipped (skip=true)")
            return
        }

        val specFile = resolveSpec()
        val format = formatOption ?: extension.format
        val outputDir = File(project.projectDir, outputDirOption ?: extension.outputDir)

        validateFile(specFile)

        logger.lifecycle("Analyzing API specification...")
        logger.lifecycle("  File: ${specFile.absolutePath}")

        try {
            
            val spec = ParserFactory.parse(specFile, extension.specType)

            if (extension.verbose) {
                logger.lifecycle("Parsed spec: ${spec.name} v${spec.version}")
                logger.lifecycle("Detected type: ${ParserFactory.detectSpecType(specFile)}")
            }

            val report = ReportWriter.generateAnalysisReport(spec, format)

            logger.lifecycle("")
            logger.lifecycle(report)

            if (format != "console") {
                outputDir.mkdirs()
                val reportFile = ReportWriter.writeAnalysis(outputDir, spec, format)
                logger.lifecycle("Analysis written to: ${reportFile.absolutePath}")
            }

            logger.lifecycle("")
            logger.lifecycle("Analysis complete:")
            logger.lifecycle("  API: ${spec.name ?: "Unknown"}")
            logger.lifecycle("  Version: ${spec.version ?: "Unknown"}")
            logger.lifecycle("  Endpoints: ${spec.endpoints.size}")

            val deprecatedCount = spec.endpoints.count { it.isDeprecated }
            if (deprecatedCount > 0) {
                logger.warn("  Deprecated endpoints: $deprecatedCount")
            }

        } catch (e: Exception) {
            throw GradleException("Failed to analyze API specification: ${e.message}", e)
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

    private fun validateFile(file: File) {
        if (!file.exists()) {
            throw GradleException("Specification file not found: ${file.absolutePath}")
        }
        if (!file.isFile) {
            throw GradleException("Path is not a file: ${file.absolutePath}")
        }
    }
}

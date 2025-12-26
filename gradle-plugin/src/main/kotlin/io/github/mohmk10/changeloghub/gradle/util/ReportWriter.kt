package io.github.mohmk10.changeloghub.gradle.util

import io.github.mohmk10.changeloghub.core.model.ApiSpec
import io.github.mohmk10.changeloghub.core.model.Changelog
import io.github.mohmk10.changeloghub.core.reporter.Reporter
import io.github.mohmk10.changeloghub.core.reporter.impl.ConsoleReporter
import io.github.mohmk10.changeloghub.core.reporter.impl.HtmlReporter
import io.github.mohmk10.changeloghub.core.reporter.impl.JsonReporter
import io.github.mohmk10.changeloghub.core.reporter.impl.MarkdownReporter
import org.gradle.api.GradleException
import java.io.File

object ReportWriter {

    enum class Format(val extension: String) {
        CONSOLE("txt"),
        MARKDOWN("md"),
        JSON("json"),
        HTML("html")
    }

    fun getReporter(format: String): Reporter {
        return when (format.lowercase()) {
            "console", "text", "txt" -> ConsoleReporter()
            "markdown", "md" -> MarkdownReporter()
            "json" -> JsonReporter()
            "html" -> HtmlReporter()
            else -> throw GradleException(
                "Unsupported report format: $format. " +
                "Supported formats: console, markdown, json, html"
            )
        }
    }

    fun getExtension(format: String): String {
        return when (format.lowercase()) {
            "console", "text", "txt" -> "txt"
            "markdown", "md" -> "md"
            "json" -> "json"
            "html" -> "html"
            else -> "txt"
        }
    }

    fun write(
        outputDir: File,
        changelog: Changelog,
        format: String,
        filename: String = "changelog"
    ): File {
        outputDir.mkdirs()

        val extension = getExtension(format)
        val file = File(outputDir, "$filename.$extension")

        val reporter = getReporter(format)
        val content = reporter.report(changelog)

        file.writeText(content)
        return file
    }

    fun generateReport(changelog: Changelog, format: String): String {
        val reporter = getReporter(format)
        return reporter.report(changelog)
    }

    fun writeAnalysis(
        outputDir: File,
        spec: ApiSpec,
        format: String,
        filename: String = "analysis"
    ): File {
        outputDir.mkdirs()

        val extension = getExtension(format)
        val file = File(outputDir, "$filename.$extension")

        val content = generateAnalysisReport(spec, format)
        file.writeText(content)
        return file
    }

    fun generateAnalysisReport(spec: ApiSpec, format: String): String {
        return when (format.lowercase()) {
            "json" -> generateJsonAnalysis(spec)
            "html" -> generateHtmlAnalysis(spec)
            "markdown", "md" -> generateMarkdownAnalysis(spec)
            else -> generateConsoleAnalysis(spec)
        }
    }

    private fun generateConsoleAnalysis(spec: ApiSpec): String {
        val sb = StringBuilder()
        sb.appendLine("=".repeat(60))
        sb.appendLine("API ANALYSIS REPORT")
        sb.appendLine("=".repeat(60))
        sb.appendLine()
        sb.appendLine("Name: ${spec.name ?: "Unknown"}")
        sb.appendLine("Version: ${spec.version ?: "Unknown"}")
        sb.appendLine("Type: ${spec.type ?: "Unknown"}")
        sb.appendLine()
        sb.appendLine("-".repeat(40))
        sb.appendLine("STATISTICS")
        sb.appendLine("-".repeat(40))
        sb.appendLine("Total Endpoints: ${spec.endpoints.size}")

        val deprecatedCount = spec.endpoints.count { it.isDeprecated }
        sb.appendLine("Deprecated Endpoints: $deprecatedCount")

        val methodCounts = spec.endpoints.groupBy { it.method?.name ?: "UNKNOWN" }.mapValues { it.value.size }
        sb.appendLine()
        sb.appendLine("Endpoints by Method:")
        methodCounts.forEach { (method, count) ->
            sb.appendLine("  $method: $count")
        }

        sb.appendLine()
        sb.appendLine("=".repeat(60))
        return sb.toString()
    }

    private fun generateMarkdownAnalysis(spec: ApiSpec): String {
        val sb = StringBuilder()
        sb.appendLine("# API Analysis Report")
        sb.appendLine()
        sb.appendLine("## Overview")
        sb.appendLine()
        sb.appendLine("| Property | Value |")
        sb.appendLine("|----------|-------|")
        sb.appendLine("| Name | ${spec.name ?: "Unknown"} |")
        sb.appendLine("| Version | ${spec.version ?: "Unknown"} |")
        sb.appendLine("| Type | ${spec.type ?: "Unknown"} |")
        sb.appendLine()
        sb.appendLine("## Statistics")
        sb.appendLine()
        sb.appendLine("| Metric | Count |")
        sb.appendLine("|--------|-------|")
        sb.appendLine("| Total Endpoints | ${spec.endpoints.size} |")

        val deprecatedCount = spec.endpoints.count { it.isDeprecated }
        sb.appendLine("| Deprecated Endpoints | $deprecatedCount |")

        sb.appendLine()
        sb.appendLine("## Endpoints by Method")
        sb.appendLine()
        val methodCounts = spec.endpoints.groupBy { it.method?.name ?: "UNKNOWN" }.mapValues { it.value.size }
        sb.appendLine("| Method | Count |")
        sb.appendLine("|--------|-------|")
        methodCounts.forEach { (method, count) ->
            sb.appendLine("| $method | $count |")
        }

        return sb.toString()
    }

    private fun generateJsonAnalysis(spec: ApiSpec): String {
        val methodCounts = spec.endpoints.groupBy { it.method?.name ?: "UNKNOWN" }.mapValues { it.value.size }
        val deprecatedCount = spec.endpoints.count { it.isDeprecated }

        return """
{
  "name": "${spec.name ?: "Unknown"}",
  "version": "${spec.version ?: "Unknown"}",
  "type": "${spec.type ?: "Unknown"}",
  "statistics": {
    "totalEndpoints": ${spec.endpoints.size},
    "deprecatedEndpoints": $deprecatedCount,
    "methodCounts": {
      ${methodCounts.entries.joinToString(",\n      ") { "\"${it.key}\": ${it.value}" }}
    }
  }
}
        """.trimIndent()
    }

    private fun generateHtmlAnalysis(spec: ApiSpec): String {
        val deprecatedCount = spec.endpoints.count { it.isDeprecated }
        val methodCounts = spec.endpoints.groupBy { it.method?.name ?: "UNKNOWN" }.mapValues { it.value.size }

        return """
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>API Analysis Report</title>
    <style>
        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; margin: 40px; }
        h1 { color: #333; }
        table { border-collapse: collapse; width: 100%; max-width: 600px; margin: 20px 0; }
        th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }
        th { background-color: #4CAF50; color: white; }
        tr:nth-child(even) { background-color: #f2f2f2; }
    </style>
</head>
<body>
    <h1>API Analysis Report</h1>

    <h2>Overview</h2>
    <table>
        <tr><th>Property</th><th>Value</th></tr>
        <tr><td>Name</td><td>${spec.name ?: "Unknown"}</td></tr>
        <tr><td>Version</td><td>${spec.version ?: "Unknown"}</td></tr>
        <tr><td>Type</td><td>${spec.type ?: "Unknown"}</td></tr>
    </table>

    <h2>Statistics</h2>
    <table>
        <tr><th>Metric</th><th>Count</th></tr>
        <tr><td>Total Endpoints</td><td>${spec.endpoints.size}</td></tr>
        <tr><td>Deprecated Endpoints</td><td>$deprecatedCount</td></tr>
    </table>

    <h2>Endpoints by Method</h2>
    <table>
        <tr><th>Method</th><th>Count</th></tr>
        ${methodCounts.entries.joinToString("\n        ") { "<tr><td>${it.key}</td><td>${it.value}</td></tr>" }}
    </table>
</body>
</html>
        """.trimIndent()
    }
}

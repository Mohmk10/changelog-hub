package io.github.mohmk10.changeloghub.intellij.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import io.github.mohmk10.changeloghub.intellij.settings.ChangelogHubSettings
import io.github.mohmk10.changeloghub.intellij.util.Logger

@Service(Service.Level.PROJECT)
class ChangelogService(private val project: Project) {

    fun generateChangelog(result: ComparisonResult, format: String? = null): String {
        val settings = project.getService(ChangelogHubSettings::class.java)
        val outputFormat = format ?: settings.state.defaultFormat

        Logger.info("Generating changelog in $outputFormat format")

        return when (outputFormat.lowercase()) {
            "markdown", "md" -> generateMarkdown(result)
            "json" -> generateJson(result)
            "html" -> generateHtml(result)
            else -> generateMarkdown(result)
        }
    }

    private fun generateMarkdown(result: ComparisonResult): String {
        return buildString {
            appendLine("# API Changelog")
            appendLine()
            appendLine("## Summary")
            appendLine("- Total changes: ${result.totalChangesCount}")
            appendLine("- Breaking changes: ${result.breakingChangesCount}")
            appendLine("- Risk level: ${result.riskLevel}")
            appendLine("- Recommended version bump: ${result.semverRecommendation}")
            appendLine()

            if (result.breakingChanges.isNotEmpty()) {
                appendLine("## Breaking Changes")
                appendLine()
                result.breakingChanges.forEach { change ->
                    appendLine("### ${change.path}")
                    appendLine("- **Type:** ${change.type}")
                    appendLine("- **Description:** ${change.description}")
                    appendLine("- **Migration:** ${change.migrationSuggestion}")
                    appendLine()
                }
            }

            if (result.changes.isNotEmpty()) {
                appendLine("## All Changes")
                appendLine()
                result.changes.groupBy { it.category }.forEach { (category, changes) ->
                    appendLine("### $category")
                    changes.forEach { change ->
                        val icon = when (change.severity) {
                            "BREAKING" -> "🔴"
                            "DANGEROUS" -> "🟠"
                            "WARNING" -> "🟡"
                            else -> "🟢"
                        }
                        appendLine("- $icon **${change.type}** ${change.path}: ${change.description}")
                    }
                    appendLine()
                }
            }
        }
    }

    private fun generateJson(result: ComparisonResult): String {
        return buildString {
            appendLine("{")
            appendLine("  \"summary\": {")
            appendLine("    \"totalChanges\": ${result.totalChangesCount},")
            appendLine("    \"breakingChanges\": ${result.breakingChangesCount},")
            appendLine("    \"riskLevel\": \"${result.riskLevel}\",")
            appendLine("    \"semverRecommendation\": \"${result.semverRecommendation}\"")
            appendLine("  },")
            appendLine("  \"breakingChanges\": [")
            result.breakingChanges.forEachIndexed { index, change ->
                val comma = if (index < result.breakingChanges.size - 1) "," else ""
                appendLine("    {")
                appendLine("      \"path\": \"${change.path}\",")
                appendLine("      \"type\": \"${change.type}\",")
                appendLine("      \"description\": \"${change.description}\",")
                appendLine("      \"migration\": \"${change.migrationSuggestion}\"")
                appendLine("    }$comma")
            }
            appendLine("  ],")
            appendLine("  \"changes\": [")
            result.changes.forEachIndexed { index, change ->
                val comma = if (index < result.changes.size - 1) "," else ""
                appendLine("    {")
                appendLine("      \"path\": \"${change.path}\",")
                appendLine("      \"type\": \"${change.type}\",")
                appendLine("      \"category\": \"${change.category}\",")
                appendLine("      \"severity\": \"${change.severity}\",")
                appendLine("      \"description\": \"${change.description}\"")
                appendLine("    }$comma")
            }
            appendLine("  ]")
            appendLine("}")
        }
    }

    private fun generateHtml(result: ComparisonResult): String {
        return buildString {
            appendLine("<!DOCTYPE html>")
            appendLine("<html><head><title>API Changelog</title>")
            appendLine("<style>")
            appendLine("body { font-family: Arial, sans-serif; margin: 20px; }")
            appendLine(".breaking { color: #d32f2f; }")
            appendLine(".warning { color: #f57c00; }")
            appendLine(".info { color: #1976d2; }")
            appendLine("</style></head><body>")
            appendLine("<h1>API Changelog</h1>")
            appendLine("<h2>Summary</h2>")
            appendLine("<ul>")
            appendLine("<li>Total changes: ${result.totalChangesCount}</li>")
            appendLine("<li>Breaking changes: ${result.breakingChangesCount}</li>")
            appendLine("<li>Risk level: ${result.riskLevel}</li>")
            appendLine("<li>Recommended version bump: ${result.semverRecommendation}</li>")
            appendLine("</ul>")

            if (result.breakingChanges.isNotEmpty()) {
                appendLine("<h2 class=\"breaking\">Breaking Changes</h2>")
                appendLine("<ul>")
                result.breakingChanges.forEach { change ->
                    appendLine("<li><strong>${change.path}</strong>: ${change.description}</li>")
                }
                appendLine("</ul>")
            }

            appendLine("</body></html>")
        }
    }
}

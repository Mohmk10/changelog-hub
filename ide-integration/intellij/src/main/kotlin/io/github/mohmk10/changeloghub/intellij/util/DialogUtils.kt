package io.github.mohmk10.changeloghub.intellij.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.util.ui.JBUI
import io.github.mohmk10.changeloghub.intellij.services.ComparisonResult
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Show a dialog with comparison results.
 */
fun showResultDialog(project: Project, result: ComparisonResult, title: String = "Comparison Results") {
    ResultDialog(project, result, title).show()
}

/**
 * Dialog for displaying comparison results.
 */
class ResultDialog(
    project: Project,
    private val result: ComparisonResult,
    title: String
) : DialogWrapper(project) {

    init {
        this.title = title
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        panel.preferredSize = Dimension(700, 500)

        // Summary
        val summaryText = buildString {
            appendLine("${"═".repeat(60)}")
            appendLine("SUMMARY")
            appendLine("${"─".repeat(60)}")
            appendLine("Total changes: ${result.totalChangesCount}")
            appendLine("Breaking changes: ${result.breakingChangesCount}")
            appendLine("Risk level: ${result.riskLevel}")
            appendLine("Recommended version bump: ${result.semverRecommendation}")
            appendLine()

            if (result.breakingChangesCount > 0) {
                appendLine("${"═".repeat(60)}")
                appendLine("BREAKING CHANGES")
                appendLine("${"─".repeat(60)}")
                result.breakingChanges.forEach { change ->
                    appendLine("✗ ${change.path}")
                    appendLine("  Type: ${change.type}")
                    appendLine("  ${change.description}")
                    appendLine("  Migration: ${change.migrationSuggestion}")
                    appendLine()
                }
            }

            if (result.changes.isNotEmpty()) {
                appendLine("${"═".repeat(60)}")
                appendLine("ALL CHANGES")
                appendLine("${"─".repeat(60)}")
                result.changes.forEach { change ->
                    val symbol = when (change.severity) {
                        "BREAKING" -> "✗"
                        "DANGEROUS" -> "⚠"
                        "WARNING" -> "⚡"
                        else -> "✓"
                    }
                    appendLine("$symbol [${change.severity}] ${change.path}: ${change.description}")
                }
            }
        }

        val textArea = JBTextArea(summaryText)
        textArea.isEditable = false
        textArea.font = JBUI.Fonts.create("Monospaced", 12)

        panel.add(JBScrollPane(textArea), BorderLayout.CENTER)

        // Status label
        val statusLabel = JBLabel(
            if (result.breakingChangesCount > 0)
                "⚠ ${result.breakingChangesCount} breaking change(s) detected!"
            else
                "✓ No breaking changes detected"
        )
        statusLabel.border = JBUI.Borders.empty(10)
        panel.add(statusLabel, BorderLayout.NORTH)

        return panel
    }
}

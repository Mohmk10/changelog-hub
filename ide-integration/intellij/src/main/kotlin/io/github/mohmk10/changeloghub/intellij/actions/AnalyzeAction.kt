package io.github.mohmk10.changeloghub.intellij.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.util.ui.JBUI
import io.github.mohmk10.changeloghub.intellij.services.ParserService
import io.github.mohmk10.changeloghub.intellij.util.FileUtils
import io.github.mohmk10.changeloghub.intellij.util.Logger
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Action for analyzing an API specification.
 */
class AnalyzeAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        if (!FileUtils.isApiSpec(file)) {
            Messages.showWarningDialog(project, "Please select an API spec file", "Changelog Hub")
            return
        }

        analyzeInBackground(project, file)
    }

    private fun analyzeInBackground(project: Project, file: VirtualFile) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Analyzing API Spec...", true) {
            override fun run(indicator: ProgressIndicator) {
                try {
                    indicator.text = "Reading file..."
                    indicator.fraction = 0.2

                    val content = FileUtils.readContent(file)

                    indicator.text = "Parsing specification..."
                    indicator.fraction = 0.5

                    val parserService = project.getService(ParserService::class.java)
                    val apiSpec = parserService.parse(content, file.name)

                    indicator.fraction = 1.0

                    val analysisReport = buildString {
                        appendLine("${"═".repeat(60)}")
                        appendLine("API SPECIFICATION ANALYSIS")
                        appendLine("${"═".repeat(60)}")
                        appendLine()
                        appendLine("File: ${file.name}")
                        appendLine("Type: ${apiSpec.type}")
                        appendLine("Name: ${apiSpec.name}")
                        appendLine("Version: ${apiSpec.version}")
                        appendLine()
                        appendLine("${"─".repeat(60)}")
                        appendLine("ENDPOINTS (${apiSpec.endpoints.size})")
                        appendLine("${"─".repeat(60)}")
                        apiSpec.endpoints.forEach { endpoint ->
                            appendLine("${endpoint.method} ${endpoint.path}")
                            endpoint.summary?.let { appendLine("  Summary: $it") }
                            endpoint.description?.let { appendLine("  Description: $it") }
                            if (endpoint.parameters.isNotEmpty()) {
                                appendLine("  Parameters: ${endpoint.parameters.size}")
                            }
                        }
                        appendLine()
                        appendLine("${"─".repeat(60)}")
                        appendLine("STATISTICS")
                        appendLine("${"─".repeat(60)}")
                        appendLine("Total endpoints: ${apiSpec.endpoints.size}")
                        val totalParams = apiSpec.endpoints.sumOf { it.parameters.size }
                        appendLine("Total parameters: $totalParams")
                    }

                    ApplicationManager.getApplication().invokeLater {
                        AnalysisDialog(project, analysisReport, file.name).show()
                    }

                } catch (ex: Exception) {
                    Logger.error("Analysis failed: ${ex.message}", ex)
                    ApplicationManager.getApplication().invokeLater {
                        Messages.showErrorDialog(project, "Analysis failed: ${ex.message}", "Changelog Hub")
                    }
                }
            }
        })
    }

    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        e.presentation.isEnabledAndVisible = e.project != null && file != null && FileUtils.isApiSpec(file)
    }
}

private class AnalysisDialog(
    project: Project,
    private val report: String,
    fileName: String
) : DialogWrapper(project) {

    init {
        title = "Analysis: $fileName"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        panel.preferredSize = Dimension(700, 500)

        val textArea = JBTextArea(report)
        textArea.isEditable = false
        textArea.font = JBUI.Fonts.create("Monospaced", 12)

        panel.add(JBScrollPane(textArea), BorderLayout.CENTER)
        return panel
    }
}

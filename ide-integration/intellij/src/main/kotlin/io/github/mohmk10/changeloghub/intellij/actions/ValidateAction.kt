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
import io.github.mohmk10.changeloghub.intellij.services.NotificationService
import io.github.mohmk10.changeloghub.intellij.services.ParserService
import io.github.mohmk10.changeloghub.intellij.util.FileUtils
import io.github.mohmk10.changeloghub.intellij.util.Logger
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JPanel

class ValidateAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        if (!FileUtils.isApiSpec(file)) {
            Messages.showWarningDialog(project, "Please select an API spec file", "Changelog Hub")
            return
        }

        validateInBackground(project, file)
    }

    private fun validateInBackground(project: Project, file: VirtualFile) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Validating API Spec...", true) {
            override fun run(indicator: ProgressIndicator) {
                val issues = mutableListOf<ValidationIssue>()

                try {
                    indicator.text = "Reading file..."
                    indicator.fraction = 0.2

                    val content = FileUtils.readContent(file)

                    indicator.text = "Parsing specification..."
                    indicator.fraction = 0.4

                    val parserService = project.getService(ParserService::class.java)
                    val apiSpec = parserService.parse(content, file.name)

                    indicator.text = "Validating..."
                    indicator.fraction = 0.6

                    apiSpec.endpoints.forEach { endpoint ->
                        if (endpoint.summary.isNullOrBlank() && endpoint.description.isNullOrBlank()) {
                            issues.add(ValidationIssue(
                                level = "WARNING",
                                path = "${endpoint.method} ${endpoint.path}",
                                message = "Endpoint is missing description/summary"
                            ))
                        }

                        endpoint.parameters.forEach { param ->
                            if (param.description.isNullOrBlank()) {
                                issues.add(ValidationIssue(
                                    level = "INFO",
                                    path = "${endpoint.method} ${endpoint.path} - ${param.name}",
                                    message = "Parameter '${param.name}' is missing description"
                                ))
                            }
                        }
                    }

                    indicator.fraction = 1.0

                    val report = buildValidationReport(file.name, issues)

                    ApplicationManager.getApplication().invokeLater {
                        ValidationDialog(project, report, file.name, issues.isEmpty()).show()

                        val notificationService = project.getService(NotificationService::class.java)
                        if (issues.isEmpty()) {
                            notificationService.info("Validation Passed", "No issues found in ${file.name}")
                        } else {
                            notificationService.warn("Validation Complete", "${issues.size} issue(s) found in ${file.name}")
                        }
                    }

                } catch (ex: Exception) {
                    Logger.error("Validation failed: ${ex.message}", ex)
                    ApplicationManager.getApplication().invokeLater {
                        Messages.showErrorDialog(project, "Validation failed: ${ex.message}", "Changelog Hub")
                    }
                }
            }
        })
    }

    private fun buildValidationReport(fileName: String, issues: List<ValidationIssue>): String {
        return buildString {
            appendLine("${"═".repeat(60)}")
            appendLine("VALIDATION REPORT")
            appendLine("${"═".repeat(60)}")
            appendLine()
            appendLine("File: $fileName")
            appendLine("Issues found: ${issues.size}")
            appendLine()

            if (issues.isEmpty()) {
                appendLine("✓ No issues found!")
            } else {
                val grouped = issues.groupBy { it.level }

                grouped["ERROR"]?.let { errors ->
                    appendLine("${"─".repeat(60)}")
                    appendLine("ERRORS (${errors.size})")
                    appendLine("${"─".repeat(60)}")
                    errors.forEach { issue ->
                        appendLine("✗ ${issue.path}")
                        appendLine("  ${issue.message}")
                    }
                    appendLine()
                }

                grouped["WARNING"]?.let { warnings ->
                    appendLine("${"─".repeat(60)}")
                    appendLine("WARNINGS (${warnings.size})")
                    appendLine("${"─".repeat(60)}")
                    warnings.forEach { issue ->
                        appendLine("⚠ ${issue.path}")
                        appendLine("  ${issue.message}")
                    }
                    appendLine()
                }

                grouped["INFO"]?.let { infos ->
                    appendLine("${"─".repeat(60)}")
                    appendLine("INFO (${infos.size})")
                    appendLine("${"─".repeat(60)}")
                    infos.forEach { issue ->
                        appendLine("ℹ ${issue.path}")
                        appendLine("  ${issue.message}")
                    }
                }
            }
        }
    }

    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        e.presentation.isEnabledAndVisible = e.project != null && file != null && FileUtils.isApiSpec(file)
    }
}

private data class ValidationIssue(
    val level: String,
    val path: String,
    val message: String
)

private class ValidationDialog(
    project: Project,
    private val report: String,
    fileName: String,
    private val passed: Boolean
) : DialogWrapper(project) {

    init {
        title = if (passed) "Validation Passed: $fileName" else "Validation Results: $fileName"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        panel.preferredSize = Dimension(700, 400)

        val textArea = JBTextArea(report)
        textArea.isEditable = false
        textArea.font = JBUI.Fonts.create("Monospaced", 12)

        panel.add(JBScrollPane(textArea), BorderLayout.CENTER)
        return panel
    }
}

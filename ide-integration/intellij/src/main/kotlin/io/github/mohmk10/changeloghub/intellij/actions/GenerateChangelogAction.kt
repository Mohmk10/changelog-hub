package io.github.mohmk10.changeloghub.intellij.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBUI
import io.github.mohmk10.changeloghub.intellij.services.ChangelogService
import io.github.mohmk10.changeloghub.intellij.services.ComparisonService
import io.github.mohmk10.changeloghub.intellij.services.NotificationService
import io.github.mohmk10.changeloghub.intellij.settings.ChangelogHubSettings
import io.github.mohmk10.changeloghub.intellij.util.FileUtils
import io.github.mohmk10.changeloghub.intellij.util.Logger
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import javax.swing.JComponent
import javax.swing.JPanel

class GenerateChangelogAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val settings = project.getService(ChangelogHubSettings::class.java)
        val formatDialog = FormatSelectionDialog(project, settings.state.defaultFormat)

        if (!formatDialog.showAndGet()) {
            return
        }

        val selectedFormat = formatDialog.selectedFormat

        val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor()
            .withTitle("Select OLD API Spec")
            .withFileFilter { FileUtils.isApiSpec(it) }

        val oldFile = FileChooser.chooseFile(descriptor, project, null) ?: return

        val newFile = FileChooser.chooseFile(
            FileChooserDescriptorFactory.createSingleFileDescriptor()
                .withTitle("Select NEW API Spec")
                .withFileFilter { FileUtils.isApiSpec(it) },
            project,
            null
        ) ?: return

        generateChangelogInBackground(project, oldFile, newFile, selectedFormat)
    }

    private fun generateChangelogInBackground(
        project: Project,
        oldFile: VirtualFile,
        newFile: VirtualFile,
        format: String
    ) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Generating Changelog...", true) {
            override fun run(indicator: ProgressIndicator) {
                try {
                    indicator.text = "Comparing specifications..."
                    indicator.fraction = 0.3

                    val comparisonService = project.getService(ComparisonService::class.java)
                    val result = comparisonService.compare(oldFile, newFile)

                    indicator.text = "Generating changelog..."
                    indicator.fraction = 0.6

                    val changelogService = project.getService(ChangelogService::class.java)
                    val changelog = changelogService.generateChangelog(result, format)

                    indicator.fraction = 1.0

                    ApplicationManager.getApplication().invokeLater {
                        ChangelogDialog(project, changelog, format).show()

                        val notificationService = project.getService(NotificationService::class.java)
                        notificationService.info("Changelog Generated", "Changelog generated in $format format")
                    }

                } catch (ex: Exception) {
                    Logger.error("Changelog generation failed: ${ex.message}", ex)
                    ApplicationManager.getApplication().invokeLater {
                        Messages.showErrorDialog(project, "Failed to generate changelog: ${ex.message}", "Changelog Hub")
                    }
                }
            }
        })
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }
}

private class FormatSelectionDialog(
    project: Project,
    defaultFormat: String
) : DialogWrapper(project) {

    var selectedFormat: String = defaultFormat

    init {
        title = "Generate Changelog"
        init()
    }

    override fun createCenterPanel(): JComponent {
        return panel {
            row("Output format:") {
                comboBox(listOf("markdown", "json", "html"))
                    .bindItem({ selectedFormat }, { selectedFormat = it ?: "markdown" })
            }
        }
    }
}

private class ChangelogDialog(
    project: Project,
    private val changelog: String,
    private val format: String
) : DialogWrapper(project) {

    init {
        title = "Generated Changelog ($format)"
        setOKButtonText("Copy to Clipboard")
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        panel.preferredSize = Dimension(800, 600)

        val textArea = JBTextArea(changelog)
        textArea.isEditable = false
        textArea.font = JBUI.Fonts.create("Monospaced", 12)

        panel.add(JBScrollPane(textArea), BorderLayout.CENTER)
        return panel
    }

    override fun doOKAction() {
        val selection = StringSelection(changelog)
        Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, selection)
        super.doOKAction()
    }
}

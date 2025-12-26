package io.github.mohmk10.changeloghub.intellij.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import io.github.mohmk10.changeloghub.intellij.services.ComparisonService
import io.github.mohmk10.changeloghub.intellij.services.NotificationService
import io.github.mohmk10.changeloghub.intellij.util.FileUtils
import io.github.mohmk10.changeloghub.intellij.util.Logger
import io.github.mohmk10.changeloghub.intellij.util.showResultDialog

/**
 * Action for comparing two API specification files.
 */
class CompareAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val currentFile = e.getData(CommonDataKeys.VIRTUAL_FILE)

        // Select old spec file
        val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor()
            .withTitle("Select OLD API Spec")
            .withFileFilter { FileUtils.isApiSpec(it) }

        val oldFile = FileChooser.chooseFile(descriptor, project, null) ?: return

        // Select new spec file (or use current)
        val newFile: VirtualFile = if (currentFile != null && FileUtils.isApiSpec(currentFile)) {
            currentFile
        } else {
            FileChooser.chooseFile(
                FileChooserDescriptorFactory.createSingleFileDescriptor()
                    .withTitle("Select NEW API Spec")
                    .withFileFilter { FileUtils.isApiSpec(it) },
                project,
                null
            ) ?: return
        }

        // Compare in background
        compareInBackground(project, oldFile, newFile)
    }

    private fun compareInBackground(project: Project, oldFile: VirtualFile, newFile: VirtualFile) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Comparing API Specs...", true) {
            override fun run(indicator: ProgressIndicator) {
                try {
                    indicator.text = "Parsing old specification..."
                    indicator.fraction = 0.2

                    val comparisonService = project.getService(ComparisonService::class.java)

                    indicator.text = "Parsing new specification..."
                    indicator.fraction = 0.4

                    indicator.text = "Comparing specifications..."
                    indicator.fraction = 0.6

                    val result = comparisonService.compare(oldFile, newFile)

                    indicator.text = "Generating report..."
                    indicator.fraction = 0.8

                    indicator.fraction = 1.0

                    // Show results on EDT
                    ApplicationManager.getApplication().invokeLater {
                        showResultDialog(project, result)

                        // Also show notification
                        val notificationService = project.getService(NotificationService::class.java)
                        notificationService.showComparisonResult(result)
                    }

                } catch (ex: Exception) {
                    Logger.error("Comparison failed: ${ex.message}", ex)
                    ApplicationManager.getApplication().invokeLater {
                        Messages.showErrorDialog(project, "Comparison failed: ${ex.message}", "Changelog Hub")
                    }
                }
            }
        })
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }
}

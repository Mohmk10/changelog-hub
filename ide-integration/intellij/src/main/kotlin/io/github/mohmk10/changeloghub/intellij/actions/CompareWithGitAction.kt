package io.github.mohmk10.changeloghub.intellij.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import git4idea.GitUtil
import git4idea.commands.Git
import git4idea.commands.GitCommand
import git4idea.commands.GitLineHandler
import io.github.mohmk10.changeloghub.intellij.services.ComparisonService
import io.github.mohmk10.changeloghub.intellij.services.NotificationService
import io.github.mohmk10.changeloghub.intellij.settings.ChangelogHubSettings
import io.github.mohmk10.changeloghub.intellij.util.FileUtils
import io.github.mohmk10.changeloghub.intellij.util.Logger
import io.github.mohmk10.changeloghub.intellij.util.showResultDialog

class CompareWithGitAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        if (!FileUtils.isApiSpec(file)) {
            Messages.showWarningDialog(project, "Please select an API spec file", "Changelog Hub")
            return
        }

        val settings = project.getService(ChangelogHubSettings::class.java)
        val gitRef = Messages.showInputDialog(
            project,
            "Enter Git ref to compare with (branch, tag, or commit):",
            "Compare with Git",
            null,
            settings.state.defaultGitRef,
            null
        ) ?: return

        compareWithGitInBackground(project, file, gitRef)
    }

    private fun compareWithGitInBackground(project: Project, file: VirtualFile, gitRef: String) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Comparing with $gitRef...", true) {
            override fun run(indicator: ProgressIndicator) {
                try {
                    indicator.text = "Getting file from Git..."
                    indicator.fraction = 0.2

                    val oldContent = getFileFromGit(project, file, gitRef)

                    indicator.text = "Parsing specifications..."
                    indicator.fraction = 0.5

                    val comparisonService = project.getService(ComparisonService::class.java)
                    val result = comparisonService.compareWithContent(oldContent, file)

                    indicator.fraction = 1.0

                    ApplicationManager.getApplication().invokeLater {
                        showResultDialog(project, result, "Changes since $gitRef")

                        val notificationService = project.getService(NotificationService::class.java)
                        notificationService.showComparisonResult(result)
                    }

                } catch (ex: Exception) {
                    Logger.error("Git comparison failed: ${ex.message}", ex)
                    ApplicationManager.getApplication().invokeLater {
                        Messages.showErrorDialog(project, "Git comparison failed: ${ex.message}", "Changelog Hub")
                    }
                }
            }
        })
    }

    private fun getFileFromGit(project: Project, file: VirtualFile, ref: String): String {
        val repository = GitUtil.getRepositoryManager(project).getRepositoryForFile(file)
            ?: throw IllegalStateException("File is not in a Git repository")

        val relativePath = file.path.removePrefix(repository.root.path + "/")

        val handler = GitLineHandler(project, repository.root, GitCommand.SHOW)
        handler.addParameters("$ref:$relativePath")

        val result = Git.getInstance().runCommand(handler)
        if (!result.success()) {
            throw IllegalStateException("Could not get file from Git: ${result.errorOutputAsJoinedString}")
        }

        return result.output.joinToString("\n")
    }

    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        e.presentation.isEnabledAndVisible = e.project != null && file != null && FileUtils.isApiSpec(file)
    }
}

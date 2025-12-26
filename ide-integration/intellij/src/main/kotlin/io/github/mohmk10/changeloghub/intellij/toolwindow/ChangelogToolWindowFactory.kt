package io.github.mohmk10.changeloghub.intellij.toolwindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

/**
 * Factory for creating the Changelog Hub tool window.
 */
class ChangelogToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.getInstance()

        // Breaking Changes tab
        val breakingChangesPanel = BreakingChangesPanel(project)
        val breakingContent = contentFactory.createContent(
            breakingChangesPanel.component,
            "Breaking Changes",
            false
        )
        toolWindow.contentManager.addContent(breakingContent)

        // API Explorer tab
        val apiExplorerPanel = ApiExplorerPanel(project)
        val explorerContent = contentFactory.createContent(
            apiExplorerPanel.component,
            "API Explorer",
            false
        )
        toolWindow.contentManager.addContent(explorerContent)

        // Changelog tab
        val changelogPanel = ChangelogPanel(project)
        val changelogContent = contentFactory.createContent(
            changelogPanel.component,
            "Changelog",
            false
        )
        toolWindow.contentManager.addContent(changelogContent)
    }

    override fun shouldBeAvailable(project: Project): Boolean = true
}

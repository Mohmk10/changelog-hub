package io.github.mohmk10.changeloghub.intellij

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import io.github.mohmk10.changeloghub.intellij.util.Logger

/**
 * Plugin startup activity that initializes Changelog Hub when a project is opened.
 */
class ChangelogHubPlugin : ProjectActivity {
    override suspend fun execute(project: Project) {
        Logger.info("Changelog Hub plugin initialized for project: ${project.name}")
    }
}

package io.github.mohmk10.changeloghub.gradle

import io.github.mohmk10.changeloghub.gradle.task.AnalyzeTask
import io.github.mohmk10.changeloghub.gradle.task.CompareTask
import io.github.mohmk10.changeloghub.gradle.task.DetectBreakingChangesTask
import io.github.mohmk10.changeloghub.gradle.task.ValidateTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class ChangelogHubPlugin : Plugin<Project> {

    companion object {
        const val EXTENSION_NAME = "changelogHub"
        const val TASK_GROUP = "changelog"
    }

    override fun apply(project: Project) {
        
        val extension = project.extensions.create(
            EXTENSION_NAME,
            ChangelogHubExtension::class.java
        )

        project.tasks.register("changelogCompare", CompareTask::class.java) { task ->
            task.group = TASK_GROUP
            task.description = "Compare two API specifications and generate a changelog"
            task.extension = extension
        }

        project.tasks.register("changelogAnalyze", AnalyzeTask::class.java) { task ->
            task.group = TASK_GROUP
            task.description = "Analyze a single API specification"
            task.extension = extension
        }

        project.tasks.register("changelogValidate", ValidateTask::class.java) { task ->
            task.group = TASK_GROUP
            task.description = "Validate an API specification file"
            task.extension = extension
        }

        project.tasks.register("changelogDetect", DetectBreakingChangesTask::class.java) { task ->
            task.group = TASK_GROUP
            task.description = "Detect breaking changes and fail build if found (CI-friendly)"
            task.extension = extension
        }

        project.afterEvaluate {
            if (extension.failOnBreaking) {
                project.tasks.findByName("check")?.dependsOn("changelogDetect")
            }
        }
    }
}

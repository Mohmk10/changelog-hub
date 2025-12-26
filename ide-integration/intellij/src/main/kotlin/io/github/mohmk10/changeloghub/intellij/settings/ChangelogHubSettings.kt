package io.github.mohmk10.changeloghub.intellij.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@Service(Service.Level.PROJECT)
@State(
    name = "ChangelogHubSettings",
    storages = [Storage("changelogHub.xml")]
)
class ChangelogHubSettings : PersistentStateComponent<ChangelogHubSettings.State> {

    private var myState = State()

    data class State(
        var defaultFormat: String = "markdown",
        var defaultGitRef: String = "main",
        var showInlineWarnings: Boolean = true,
        var severityThreshold: String = "INFO",
        var autoDetectSpecs: Boolean = true,
        var specPatterns: List<String> = listOf(
            "**/api*.yaml",
            "**/api*.yml",
            "**/api*.json",
            "**/openapi*.yaml",
            "**/openapi*.yml",
            "**/swagger*.yaml",
            "**/swagger*.yml",
            "**/*.graphql",
            "**/*.proto"
        )
    )

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    companion object {
        val SEVERITY_OPTIONS = listOf("INFO", "WARNING", "DANGEROUS", "BREAKING")
        val FORMAT_OPTIONS = listOf("markdown", "json", "html")
    }
}

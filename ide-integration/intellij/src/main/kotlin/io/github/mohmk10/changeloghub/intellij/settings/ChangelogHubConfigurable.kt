package io.github.mohmk10.changeloghub.intellij.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

/**
 * Settings UI for the Changelog Hub plugin.
 */
class ChangelogHubConfigurable(private val project: Project) : Configurable {

    private val settings = project.getService(ChangelogHubSettings::class.java)
    private var formatField: String = settings.state.defaultFormat
    private var gitRefField: String = settings.state.defaultGitRef
    private var showWarningsField: Boolean = settings.state.showInlineWarnings
    private var severityField: String = settings.state.severityThreshold
    private var autoDetectField: Boolean = settings.state.autoDetectSpecs

    override fun getDisplayName(): String = "Changelog Hub"

    override fun createComponent(): JComponent {
        return panel {
            group("Output") {
                row("Default format:") {
                    comboBox(ChangelogHubSettings.FORMAT_OPTIONS)
                        .bindItem({ formatField }, { formatField = it ?: "markdown" })
                        .comment("Format used when generating changelogs")
                }
            }

            group("Git Integration") {
                row("Default Git ref:") {
                    textField()
                        .bindText({ gitRefField }, { gitRefField = it })
                        .comment("Default branch, tag, or commit for comparison (e.g., main, develop)")
                }
            }

            group("Editor") {
                row {
                    checkBox("Show inline warnings")
                        .bindSelected({ showWarningsField }, { showWarningsField = it })
                        .comment("Display warnings for deprecated endpoints and issues in the editor")
                }
                row("Minimum severity:") {
                    comboBox(ChangelogHubSettings.SEVERITY_OPTIONS)
                        .bindItem({ severityField }, { severityField = it ?: "INFO" })
                        .comment("Only show changes at or above this severity level")
                }
            }

            group("Detection") {
                row {
                    checkBox("Auto-detect API specifications")
                        .bindSelected({ autoDetectField }, { autoDetectField = it })
                        .comment("Automatically scan project for API specification files")
                }
            }
        }
    }

    override fun isModified(): Boolean {
        return formatField != settings.state.defaultFormat ||
               gitRefField != settings.state.defaultGitRef ||
               showWarningsField != settings.state.showInlineWarnings ||
               severityField != settings.state.severityThreshold ||
               autoDetectField != settings.state.autoDetectSpecs
    }

    override fun apply() {
        settings.state.defaultFormat = formatField
        settings.state.defaultGitRef = gitRefField
        settings.state.showInlineWarnings = showWarningsField
        settings.state.severityThreshold = severityField
        settings.state.autoDetectSpecs = autoDetectField
    }

    override fun reset() {
        formatField = settings.state.defaultFormat
        gitRefField = settings.state.defaultGitRef
        showWarningsField = settings.state.showInlineWarnings
        severityField = settings.state.severityThreshold
        autoDetectField = settings.state.autoDetectSpecs
    }
}

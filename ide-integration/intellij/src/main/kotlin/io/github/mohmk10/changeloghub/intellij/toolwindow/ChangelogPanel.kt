package io.github.mohmk10.changeloghub.intellij.toolwindow

import com.intellij.openapi.project.Project
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import io.github.mohmk10.changeloghub.intellij.services.ChangeInfo
import io.github.mohmk10.changeloghub.intellij.util.Icons
import java.awt.BorderLayout
import javax.swing.DefaultListModel
import javax.swing.JList
import javax.swing.JPanel

/**
 * Panel displaying all changes in the tool window.
 */
class ChangelogPanel(private val project: Project) {

    val component: JPanel = JPanel(BorderLayout())
    private val listModel = DefaultListModel<ChangelogItem>()
    private val list = JBList(listModel)

    init {
        val header = JBLabel("Changelog")
        header.icon = Icons.CHANGELOG_HUB
        header.border = JBUI.Borders.empty(10)

        list.cellRenderer = ChangelogCellRenderer()

        component.add(header, BorderLayout.NORTH)
        component.add(JBScrollPane(list), BorderLayout.CENTER)

        // Initial message
        listModel.addElement(ChangelogItem(
            path = "Run 'Compare API Specs' to see changes",
            type = "",
            severity = "INFO",
            description = "",
            isPlaceholder = true
        ))
    }

    fun setChanges(changes: List<ChangeInfo>) {
        listModel.clear()
        if (changes.isEmpty()) {
            listModel.addElement(ChangelogItem(
                path = "No changes detected",
                type = "",
                severity = "INFO",
                description = "",
                isPlaceholder = true
            ))
        } else {
            // Group by severity
            val grouped = changes.groupBy { it.severity }

            grouped["BREAKING"]?.forEach { change ->
                listModel.addElement(ChangelogItem(
                    path = change.path,
                    type = change.type,
                    severity = change.severity,
                    description = change.description,
                    isPlaceholder = false
                ))
            }

            grouped["DANGEROUS"]?.forEach { change ->
                listModel.addElement(ChangelogItem(
                    path = change.path,
                    type = change.type,
                    severity = change.severity,
                    description = change.description,
                    isPlaceholder = false
                ))
            }

            grouped["WARNING"]?.forEach { change ->
                listModel.addElement(ChangelogItem(
                    path = change.path,
                    type = change.type,
                    severity = change.severity,
                    description = change.description,
                    isPlaceholder = false
                ))
            }

            grouped["INFO"]?.forEach { change ->
                listModel.addElement(ChangelogItem(
                    path = change.path,
                    type = change.type,
                    severity = change.severity,
                    description = change.description,
                    isPlaceholder = false
                ))
            }

            grouped["SAFE"]?.forEach { change ->
                listModel.addElement(ChangelogItem(
                    path = change.path,
                    type = change.type,
                    severity = change.severity,
                    description = change.description,
                    isPlaceholder = false
                ))
            }
        }
    }

    fun clear() {
        listModel.clear()
        listModel.addElement(ChangelogItem(
            path = "Run 'Compare API Specs' to see changes",
            type = "",
            severity = "INFO",
            description = "",
            isPlaceholder = true
        ))
    }
}

data class ChangelogItem(
    val path: String,
    val type: String,
    val severity: String,
    val description: String,
    val isPlaceholder: Boolean = false
)

private class ChangelogCellRenderer : ColoredListCellRenderer<ChangelogItem>() {
    override fun customizeCellRenderer(
        list: JList<out ChangelogItem>,
        value: ChangelogItem,
        index: Int,
        selected: Boolean,
        hasFocus: Boolean
    ) {
        if (value.isPlaceholder) {
            icon = Icons.INFO
            append(value.path, SimpleTextAttributes.GRAYED_ATTRIBUTES)
        } else {
            val (textAttrs, itemIcon) = when (value.severity) {
                "BREAKING" -> SimpleTextAttributes.ERROR_ATTRIBUTES to Icons.BREAKING
                "DANGEROUS" -> SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD, java.awt.Color.ORANGE) to Icons.WARNING
                "WARNING" -> SimpleTextAttributes.REGULAR_ATTRIBUTES to Icons.WARNING
                else -> SimpleTextAttributes.REGULAR_ATTRIBUTES to Icons.INFO
            }

            icon = itemIcon
            append("[${value.type}] ", SimpleTextAttributes.GRAYED_ATTRIBUTES)
            append(value.path, textAttrs)
        }
    }
}

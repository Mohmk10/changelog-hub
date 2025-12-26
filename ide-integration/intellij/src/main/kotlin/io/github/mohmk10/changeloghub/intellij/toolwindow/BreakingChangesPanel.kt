package io.github.mohmk10.changeloghub.intellij.toolwindow

import com.intellij.openapi.project.Project
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import io.github.mohmk10.changeloghub.intellij.services.BreakingChangeInfo
import io.github.mohmk10.changeloghub.intellij.util.Icons
import java.awt.BorderLayout
import javax.swing.DefaultListModel
import javax.swing.JList
import javax.swing.JPanel

/**
 * Panel displaying breaking changes in the tool window.
 */
class BreakingChangesPanel(private val project: Project) {

    val component: JPanel = JPanel(BorderLayout())
    private val listModel = DefaultListModel<BreakingChangeItem>()
    private val list = JBList(listModel)

    init {
        val header = JBLabel("Breaking Changes")
        header.icon = Icons.BREAKING
        header.border = JBUI.Borders.empty(10)

        list.cellRenderer = BreakingChangeCellRenderer()

        component.add(header, BorderLayout.NORTH)
        component.add(JBScrollPane(list), BorderLayout.CENTER)

        // Initial message
        listModel.addElement(BreakingChangeItem(
            path = "Run 'Compare API Specs' to detect breaking changes",
            type = "",
            description = "",
            isPlaceholder = true
        ))
    }

    fun setBreakingChanges(changes: List<BreakingChangeInfo>) {
        listModel.clear()
        if (changes.isEmpty()) {
            listModel.addElement(BreakingChangeItem(
                path = "No breaking changes detected",
                type = "",
                description = "",
                isPlaceholder = true
            ))
        } else {
            changes.forEach { change ->
                listModel.addElement(BreakingChangeItem(
                    path = change.path,
                    type = change.type,
                    description = change.description,
                    isPlaceholder = false
                ))
            }
        }
    }

    fun clear() {
        listModel.clear()
        listModel.addElement(BreakingChangeItem(
            path = "Run 'Compare API Specs' to detect breaking changes",
            type = "",
            description = "",
            isPlaceholder = true
        ))
    }
}

data class BreakingChangeItem(
    val path: String,
    val type: String,
    val description: String,
    val isPlaceholder: Boolean = false
)

private class BreakingChangeCellRenderer : ColoredListCellRenderer<BreakingChangeItem>() {
    override fun customizeCellRenderer(
        list: JList<out BreakingChangeItem>,
        value: BreakingChangeItem,
        index: Int,
        selected: Boolean,
        hasFocus: Boolean
    ) {
        if (value.isPlaceholder) {
            icon = Icons.INFO
            append(value.path, SimpleTextAttributes.GRAYED_ATTRIBUTES)
        } else {
            icon = Icons.BREAKING
            append(value.path, SimpleTextAttributes.ERROR_ATTRIBUTES)
            append(" - ${value.type}", SimpleTextAttributes.GRAYED_ATTRIBUTES)
        }
    }
}

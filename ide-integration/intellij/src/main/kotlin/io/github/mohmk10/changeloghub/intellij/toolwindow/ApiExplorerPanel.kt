package io.github.mohmk10.changeloghub.intellij.toolwindow

import com.intellij.icons.AllIcons
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.JBUI
import io.github.mohmk10.changeloghub.intellij.util.FileUtils
import io.github.mohmk10.changeloghub.intellij.util.Icons
import java.awt.BorderLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JPanel
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

class ApiExplorerPanel(private val project: Project) {

    val component: JPanel = JPanel(BorderLayout())
    private val rootNode = DefaultMutableTreeNode("API Specifications")
    private val treeModel = DefaultTreeModel(rootNode)
    private val tree = Tree(treeModel)

    init {
        val header = JBLabel("API Explorer")
        header.icon = Icons.CHANGELOG_HUB
        header.border = JBUI.Borders.empty(10)

        tree.cellRenderer = ApiSpecTreeCellRenderer()
        tree.isRootVisible = true

        tree.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    val node = tree.lastSelectedPathComponent as? DefaultMutableTreeNode ?: return
                    val userObject = node.userObject
                    if (userObject is ApiSpecNode) {
                        openFile(userObject.file)
                    }
                }
            }
        })

        component.add(header, BorderLayout.NORTH)
        component.add(JBScrollPane(tree), BorderLayout.CENTER)

        refreshApiSpecs()
    }

    fun refreshApiSpecs() {
        rootNode.removeAllChildren()

        val extensions = listOf("yaml", "yml", "json", "graphql", "gql", "proto")

        extensions.forEach { ext ->
            val files = FilenameIndex.getAllFilesByExt(project, ext, GlobalSearchScope.projectScope(project))
            files.filter { FileUtils.isApiSpec(it) && isLikelyApiSpec(it) }.forEach { file ->
                rootNode.add(DefaultMutableTreeNode(ApiSpecNode(file)))
            }
        }

        if (rootNode.childCount == 0) {
            rootNode.add(DefaultMutableTreeNode("No API specs found"))
        }

        treeModel.reload()
        tree.expandRow(0)
    }

    private fun isLikelyApiSpec(file: VirtualFile): Boolean {
        val content = try {
            String(file.contentsToByteArray()).take(500)
        } catch (e: Exception) {
            return false
        }

        return content.contains("openapi:") ||
               content.contains("swagger:") ||
               content.contains("asyncapi:") ||
               content.contains("type Query") ||
               content.contains("syntax = \"proto") ||
               file.name.contains("api", ignoreCase = true) ||
               file.name.contains("spec", ignoreCase = true) ||
               file.name.contains("schema", ignoreCase = true)
    }

    private fun openFile(file: VirtualFile) {
        FileEditorManager.getInstance(project).openFile(file, true)
    }
}

data class ApiSpecNode(val file: VirtualFile) {
    override fun toString(): String = file.name
}

private class ApiSpecTreeCellRenderer : ColoredTreeCellRenderer() {
    override fun customizeCellRenderer(
        tree: javax.swing.JTree,
        value: Any?,
        selected: Boolean,
        expanded: Boolean,
        leaf: Boolean,
        row: Int,
        hasFocus: Boolean
    ) {
        val node = (value as? DefaultMutableTreeNode)?.userObject

        when (node) {
            is ApiSpecNode -> {
                icon = getIconForFile(node.file)
                append(node.file.name)
                append(" - ${node.file.parent?.path ?: ""}", SimpleTextAttributes.GRAYED_ATTRIBUTES)
            }
            is String -> {
                icon = if (node == "API Specifications") Icons.CHANGELOG_HUB else AllIcons.General.Information
                append(node)
            }
        }
    }

    private fun getIconForFile(file: VirtualFile): javax.swing.Icon {
        return when (file.extension?.lowercase()) {
            "yaml", "yml" -> AllIcons.FileTypes.Yaml
            "json" -> AllIcons.FileTypes.Json
            "graphql", "gql" -> AllIcons.FileTypes.Any_type
            "proto" -> AllIcons.FileTypes.Any_type
            else -> AllIcons.FileTypes.Any_type
        }
    }
}

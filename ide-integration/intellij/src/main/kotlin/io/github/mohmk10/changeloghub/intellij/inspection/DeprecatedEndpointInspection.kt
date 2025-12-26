package io.github.mohmk10.changeloghub.intellij.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YamlPsiElementVisitor

/**
 * Inspection for detecting deprecated endpoints in API specifications.
 */
class DeprecatedEndpointInspection : LocalInspectionTool() {

    override fun getDisplayName(): String = "Deprecated endpoint"

    override fun getGroupDisplayName(): String = "Changelog Hub"

    override fun getShortName(): String = "DeprecatedEndpoint"

    override fun isEnabledByDefault(): Boolean = true

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val file = holder.file
        if (file !is YAMLFile) {
            return PsiElementVisitor.EMPTY_VISITOR
        }

        // Check if this is an API spec file
        val content = file.text
        if (!isApiSpec(content)) {
            return PsiElementVisitor.EMPTY_VISITOR
        }

        return object : YamlPsiElementVisitor() {
            override fun visitKeyValue(keyValue: YAMLKeyValue) {
                super.visitKeyValue(keyValue)

                val key = keyValue.keyText
                if (key == "deprecated" && keyValue.valueText == "true") {
                    // Find the parent path/operation
                    val parent = findOperationParent(keyValue)
                    if (parent != null) {
                        holder.registerProblem(
                            keyValue,
                            "This endpoint is marked as deprecated. Consider providing migration guidance."
                        )
                    }
                }
            }
        }
    }

    private fun isApiSpec(content: String): Boolean {
        return content.contains("openapi:") ||
               content.contains("swagger:") ||
               content.contains("asyncapi:")
    }

    private fun findOperationParent(element: YAMLKeyValue): YAMLKeyValue? {
        var current = element.parent
        while (current != null) {
            if (current is YAMLKeyValue) {
                val key = current.keyText.lowercase()
                if (key in listOf("get", "post", "put", "delete", "patch", "options", "head")) {
                    return current
                }
            }
            current = current.parent
        }
        return null
    }
}

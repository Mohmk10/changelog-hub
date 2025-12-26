package io.github.mohmk10.changeloghub.intellij.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YamlPsiElementVisitor

/**
 * Inspection for detecting missing descriptions in API specifications.
 */
class MissingDescriptionInspection : LocalInspectionTool() {

    override fun getDisplayName(): String = "Missing description"

    override fun getGroupDisplayName(): String = "Changelog Hub"

    override fun getShortName(): String = "MissingDescription"

    override fun isEnabledByDefault(): Boolean = true

    private val httpMethods = setOf("get", "post", "put", "delete", "patch", "options", "head")

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val file = holder.file
        if (file !is YAMLFile) {
            return PsiElementVisitor.EMPTY_VISITOR
        }

        val content = file.text
        if (!isApiSpec(content)) {
            return PsiElementVisitor.EMPTY_VISITOR
        }

        return object : YamlPsiElementVisitor() {
            override fun visitKeyValue(keyValue: YAMLKeyValue) {
                super.visitKeyValue(keyValue)

                val key = keyValue.keyText.lowercase()

                // Check if this is an HTTP method (operation)
                if (key in httpMethods) {
                    val value = keyValue.value
                    if (value is YAMLMapping) {
                        val hasDescription = value.keyValues.any {
                            it.keyText in listOf("description", "summary")
                        }

                        if (!hasDescription) {
                            holder.registerProblem(
                                keyValue.key ?: keyValue,
                                "Operation '${key.uppercase()}' is missing a description or summary"
                            )
                        }
                    }
                }

                // Check parameters
                if (key == "parameters") {
                    val value = keyValue.value
                    if (value is org.jetbrains.yaml.psi.YAMLSequence) {
                        value.items.forEach { item ->
                            val mapping = item.value as? YAMLMapping
                            if (mapping != null) {
                                val hasDescription = mapping.keyValues.any { it.keyText == "description" }
                                val paramName = mapping.keyValues.find { it.keyText == "name" }?.valueText ?: "unknown"

                                if (!hasDescription) {
                                    holder.registerProblem(
                                        item,
                                        "Parameter '$paramName' is missing a description"
                                    )
                                }
                            }
                        }
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
}

package io.github.mohmk10.changeloghub.intellij.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.psi.PsiElement
import org.jetbrains.yaml.psi.YAMLKeyValue
import java.awt.Color
import java.awt.Font

/**
 * Annotator for highlighting API specification elements.
 */
class ApiSpecAnnotator : Annotator {

    private val httpMethodColors = mapOf(
        "get" to Color(0x61, 0xAF, 0xEF),      // Blue
        "post" to Color(0x98, 0xC3, 0x79),     // Green
        "put" to Color(0xE5, 0xC0, 0x7B),      // Yellow
        "delete" to Color(0xE0, 0x6C, 0x75),   // Red
        "patch" to Color(0xC6, 0x78, 0xDD),    // Purple
        "options" to Color(0x56, 0xB6, 0xC2),  // Cyan
        "head" to Color(0xAB, 0xB2, 0xBF)      // Gray
    )

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is YAMLKeyValue) return

        val file = element.containingFile?.text ?: return
        if (!isApiSpec(file)) return

        val key = element.keyText.lowercase()

        // Highlight HTTP methods
        if (key in httpMethodColors) {
            val color = httpMethodColors[key] ?: return
            val keyElement = element.key ?: return

            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(keyElement)
                .textAttributes(TextAttributesKey.createTextAttributesKey(
                    "CHANGELOG_HUB_HTTP_METHOD_$key",
                    TextAttributes(color, null, null, null, Font.BOLD)
                ))
                .create()
        }

        // Highlight deprecated
        if (key == "deprecated" && element.valueText == "true") {
            holder.newAnnotation(HighlightSeverity.WARNING, "This element is deprecated")
                .range(element)
                .textAttributes(TextAttributesKey.createTextAttributesKey(
                    "CHANGELOG_HUB_DEPRECATED",
                    TextAttributes(Color(0xE0, 0x6C, 0x75), null, null, null, Font.ITALIC)
                ))
                .create()
        }

        // Highlight paths
        if (element.parent?.parent is YAMLKeyValue) {
            val parentKey = (element.parent?.parent as? YAMLKeyValue)?.keyText
            if (parentKey == "paths" && key.startsWith("/")) {
                val keyElement = element.key ?: return

                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(keyElement)
                    .textAttributes(TextAttributesKey.createTextAttributesKey(
                        "CHANGELOG_HUB_PATH",
                        TextAttributes(Color(0x98, 0xC3, 0x79), null, null, null, Font.PLAIN)
                    ))
                    .create()
            }
        }
    }

    private fun isApiSpec(content: String): Boolean {
        return content.contains("openapi:") ||
               content.contains("swagger:") ||
               content.contains("asyncapi:")
    }
}

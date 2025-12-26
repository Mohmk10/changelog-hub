package io.github.mohmk10.changeloghub.intellij.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import io.github.mohmk10.changeloghub.intellij.util.Icons

class ApiSpecCompletionContributor : CompletionContributor() {

    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement(),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    val file = parameters.originalFile.text
                    if (!isOpenApiSpec(file)) return

                    val prefix = result.prefixMatcher.prefix.lowercase()

                    val httpMethods = listOf("get", "post", "put", "delete", "patch", "options", "head")
                    httpMethods.filter { it.startsWith(prefix) }.forEach { method ->
                        result.addElement(
                            LookupElementBuilder.create(method)
                                .withIcon(Icons.CHANGELOG_HUB)
                                .withTypeText("HTTP Method")
                                .withBoldness(true)
                        )
                    }

                    val openApiKeys = listOf(
                        "openapi" to "OpenAPI version",
                        "info" to "API information",
                        "paths" to "API paths",
                        "components" to "Reusable components",
                        "schemas" to "Schema definitions",
                        "parameters" to "Parameter definitions",
                        "responses" to "Response definitions",
                        "requestBody" to "Request body",
                        "security" to "Security requirements",
                        "servers" to "Server definitions",
                        "tags" to "API tags",
                        "description" to "Description",
                        "summary" to "Summary",
                        "operationId" to "Operation ID",
                        "deprecated" to "Deprecation flag"
                    )

                    openApiKeys.filter { it.first.startsWith(prefix) }.forEach { (key, description) ->
                        result.addElement(
                            LookupElementBuilder.create(key)
                                .withIcon(Icons.INFO)
                                .withTypeText(description)
                        )
                    }
                }
            }
        )
    }

    private fun isOpenApiSpec(content: String): Boolean {
        return content.contains("openapi:") || content.contains("swagger:")
    }
}

package io.github.mohmk10.changeloghub.intellij.util

import com.intellij.openapi.vfs.VirtualFile

object FileUtils {

    private val API_SPEC_EXTENSIONS = setOf("yaml", "yml", "json", "graphql", "gql", "proto")

    fun isApiSpec(file: VirtualFile): Boolean {
        val ext = file.extension?.lowercase() ?: return false
        return ext in API_SPEC_EXTENSIONS
    }

    fun getSpecType(file: VirtualFile): SpecType {
        val ext = file.extension?.lowercase() ?: return SpecType.UNKNOWN
        return when (ext) {
            "yaml", "yml", "json" -> SpecType.YAML_JSON
            "graphql", "gql" -> SpecType.GRAPHQL
            "proto" -> SpecType.PROTOBUF
            else -> SpecType.UNKNOWN
        }
    }

    fun readContent(file: VirtualFile): String {
        return String(file.contentsToByteArray(), Charsets.UTF_8)
    }
}

enum class SpecType {
    YAML_JSON,
    GRAPHQL,
    PROTOBUF,
    UNKNOWN
}

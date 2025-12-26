package io.github.mohmk10.changeloghub.intellij.util

import com.intellij.openapi.vfs.VirtualFile

/**
 * File utility functions for API spec detection and handling.
 */
object FileUtils {

    private val API_SPEC_EXTENSIONS = setOf("yaml", "yml", "json", "graphql", "gql", "proto")

    /**
     * Check if a file is an API specification file.
     */
    fun isApiSpec(file: VirtualFile): Boolean {
        val ext = file.extension?.lowercase() ?: return false
        return ext in API_SPEC_EXTENSIONS
    }

    /**
     * Get the API spec type based on file extension.
     */
    fun getSpecType(file: VirtualFile): SpecType {
        val ext = file.extension?.lowercase() ?: return SpecType.UNKNOWN
        return when (ext) {
            "yaml", "yml", "json" -> SpecType.YAML_JSON
            "graphql", "gql" -> SpecType.GRAPHQL
            "proto" -> SpecType.PROTOBUF
            else -> SpecType.UNKNOWN
        }
    }

    /**
     * Read the content of a virtual file as a string.
     */
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

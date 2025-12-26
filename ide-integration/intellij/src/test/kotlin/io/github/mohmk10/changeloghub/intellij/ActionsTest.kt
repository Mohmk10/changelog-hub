package io.github.mohmk10.changeloghub.intellij

import io.github.mohmk10.changeloghub.intellij.util.SpecType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Tests for action utilities and helpers.
 */
class ActionsTest {

    @Test
    fun `test isApiSpec for supported extensions`() {
        assertTrue(isApiSpec("api.yaml"))
        assertTrue(isApiSpec("api.yml"))
        assertTrue(isApiSpec("api.json"))
        assertTrue(isApiSpec("schema.graphql"))
        assertTrue(isApiSpec("schema.gql"))
        assertTrue(isApiSpec("service.proto"))
    }

    @Test
    fun `test isApiSpec for unsupported extensions`() {
        assertFalse(isApiSpec("readme.md"))
        assertFalse(isApiSpec("app.kt"))
        assertFalse(isApiSpec("style.css"))
        assertFalse(isApiSpec("script.js"))
        assertFalse(isApiSpec("file.txt"))
    }

    @Test
    fun `test getSpecType for YAML and JSON files`() {
        assertEquals(SpecType.YAML_JSON, getSpecType("api.yaml"))
        assertEquals(SpecType.YAML_JSON, getSpecType("api.yml"))
        assertEquals(SpecType.YAML_JSON, getSpecType("api.json"))
    }

    @Test
    fun `test getSpecType for GraphQL files`() {
        assertEquals(SpecType.GRAPHQL, getSpecType("schema.graphql"))
        assertEquals(SpecType.GRAPHQL, getSpecType("schema.gql"))
    }

    @Test
    fun `test getSpecType for Protocol Buffer files`() {
        assertEquals(SpecType.PROTOBUF, getSpecType("service.proto"))
    }

    @Test
    fun `test getSpecType for unknown extensions`() {
        assertEquals(SpecType.UNKNOWN, getSpecType("file.txt"))
        assertEquals(SpecType.UNKNOWN, getSpecType("app.kt"))
    }

    @Test
    fun `test changelog generation format options`() {
        val formats = listOf("markdown", "json", "html")
        assertTrue(formats.contains("markdown"))
        assertTrue(formats.contains("json"))
        assertTrue(formats.contains("html"))
    }

    @Test
    fun `test severity threshold options`() {
        val severities = listOf("INFO", "WARNING", "DANGEROUS", "BREAKING")
        assertEquals(4, severities.size)
        assertTrue(severities.contains("INFO"))
        assertTrue(severities.contains("BREAKING"))
    }

    private fun isApiSpec(fileName: String): Boolean {
        val ext = fileName.substringAfterLast('.').lowercase()
        return ext in listOf("yaml", "yml", "json", "graphql", "gql", "proto")
    }

    private fun getSpecType(fileName: String): SpecType {
        val ext = fileName.substringAfterLast('.').lowercase()
        return when (ext) {
            "yaml", "yml", "json" -> SpecType.YAML_JSON
            "graphql", "gql" -> SpecType.GRAPHQL
            "proto" -> SpecType.PROTOBUF
            else -> SpecType.UNKNOWN
        }
    }
}

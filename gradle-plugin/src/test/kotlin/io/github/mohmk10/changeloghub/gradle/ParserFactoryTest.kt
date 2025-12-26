package io.github.mohmk10.changeloghub.gradle

import io.github.mohmk10.changeloghub.gradle.util.ParserFactory
import org.gradle.api.GradleException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for ParserFactory.
 */
class ParserFactoryTest {

    @TempDir
    lateinit var tempDir: File

    @Test
    fun `detectSpecType returns GRPC for proto files`() {
        val file = File(tempDir, "api.proto")
        file.writeText("syntax = \"proto3\";")

        assertEquals(ParserFactory.SpecType.GRPC, ParserFactory.detectSpecType(file))
    }

    @Test
    fun `detectSpecType returns GRAPHQL for graphql files`() {
        val file = File(tempDir, "schema.graphql")
        file.writeText("type Query { hello: String }")

        assertEquals(ParserFactory.SpecType.GRAPHQL, ParserFactory.detectSpecType(file))
    }

    @Test
    fun `detectSpecType returns GRAPHQL for gql files`() {
        val file = File(tempDir, "schema.gql")
        file.writeText("type Query { hello: String }")

        assertEquals(ParserFactory.SpecType.GRAPHQL, ParserFactory.detectSpecType(file))
    }

    @Test
    fun `detectSpecType returns OPENAPI for yaml with openapi keyword`() {
        val file = File(tempDir, "api.yaml")
        file.writeText("openapi: 3.0.0\ninfo:\n  title: Test API")

        assertEquals(ParserFactory.SpecType.OPENAPI, ParserFactory.detectSpecType(file))
    }

    @Test
    fun `detectSpecType returns OPENAPI for yaml with swagger keyword`() {
        val file = File(tempDir, "api.yaml")
        file.writeText("swagger: \"2.0\"\ninfo:\n  title: Test API")

        assertEquals(ParserFactory.SpecType.OPENAPI, ParserFactory.detectSpecType(file))
    }

    @Test
    fun `detectSpecType returns ASYNCAPI for yaml with asyncapi keyword`() {
        val file = File(tempDir, "api.yaml")
        file.writeText("asyncapi: 2.0.0\ninfo:\n  title: Test API")

        assertEquals(ParserFactory.SpecType.ASYNCAPI, ParserFactory.detectSpecType(file))
    }

    @Test
    fun `detectSpecType returns OPENAPI for json with openapi keyword`() {
        val file = File(tempDir, "api.json")
        file.writeText("""{"openapi": "3.0.0", "info": {"title": "Test"}}""")

        assertEquals(ParserFactory.SpecType.OPENAPI, ParserFactory.detectSpecType(file))
    }

    @Test
    fun `detectSpecType returns UNKNOWN for unrecognized yaml`() {
        val file = File(tempDir, "data.yaml")
        file.writeText("some: data\nother: value")

        assertEquals(ParserFactory.SpecType.UNKNOWN, ParserFactory.detectSpecType(file))
    }

    @Test
    fun `detectSpecType returns UNKNOWN for unknown extension`() {
        val file = File(tempDir, "file.txt")
        file.writeText("some text content")

        assertEquals(ParserFactory.SpecType.UNKNOWN, ParserFactory.detectSpecType(file))
    }

    @Test
    fun `parse throws for unknown spec type`() {
        val file = File(tempDir, "unknown.txt")
        file.writeText("some content")

        val exception = assertFailsWith<GradleException> {
            ParserFactory.parse(file)
        }

        assertTrue(exception.message!!.contains("Unable to determine specification type"))
    }

    @Test
    fun `parse with explicit specType openapi`() {
        val file = File(tempDir, "api.yaml")
        file.writeText("""
            openapi: "3.0.0"
            info:
              title: Test API
              version: "1.0.0"
            paths: {}
        """.trimIndent())

        val spec = ParserFactory.parse(file, "openapi")

        assertEquals("Test API", spec.name)
        assertEquals("1.0.0", spec.version)
    }

    @Test
    fun `parse with explicit specType graphql`() {
        val file = File(tempDir, "schema.graphql")
        file.writeText("""
            type Query {
                hello: String
            }
        """.trimIndent())

        val spec = ParserFactory.parse(file, "graphql")

        // GraphQL parser should work
        assertNotNull(spec)
    }

    @Test
    fun `getSupportedFormats returns non-empty string`() {
        val formats = ParserFactory.getSupportedFormats()

        assertTrue(formats.contains("OpenAPI"))
        assertTrue(formats.contains("AsyncAPI"))
        assertTrue(formats.contains("GraphQL"))
        assertTrue(formats.contains("Protocol Buffers"))
    }
}

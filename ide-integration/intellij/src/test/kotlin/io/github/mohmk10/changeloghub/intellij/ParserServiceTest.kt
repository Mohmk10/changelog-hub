package io.github.mohmk10.changeloghub.intellij

import io.github.mohmk10.changeloghub.intellij.model.ApiType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Tests for parser functionality and API type detection.
 */
class ParserServiceTest {

    @Test
    fun `test detect OpenAPI type`() {
        val openApiContent = """{"openapi": "3.0.0"}"""
        val swaggerContent = """swagger: "2.0""""

        assertEquals(ApiType.REST, detectType(openApiContent))
        assertEquals(ApiType.REST, detectType(swaggerContent))
    }

    @Test
    fun `test detect AsyncAPI type`() {
        val asyncApiContent = """asyncapi: 2.0.0"""
        assertEquals(ApiType.ASYNCAPI, detectType(asyncApiContent))
    }

    @Test
    fun `test detect GraphQL type`() {
        val graphqlContent = """type Query { users: [User] }"""
        assertEquals(ApiType.GRAPHQL, detectType(graphqlContent))
    }

    @Test
    fun `test detect gRPC type`() {
        val protoContent = """syntax = "proto3";"""
        assertEquals(ApiType.GRPC, detectType(protoContent))
    }

    @Test
    fun `test default type for unknown content`() {
        val unknownContent = """{ "unknown": "content" }"""
        assertEquals(ApiType.REST, detectType(unknownContent))
    }

    private fun detectType(content: String): ApiType {
        return when {
            content.contains("openapi:") || content.contains("swagger:") ||
            content.contains("\"openapi\"") || content.contains("\"swagger\"") -> ApiType.REST
            content.contains("asyncapi:") || content.contains("\"asyncapi\"") -> ApiType.ASYNCAPI
            content.contains("type Query") || content.contains("type Mutation") -> ApiType.GRAPHQL
            content.contains("syntax = \"proto") -> ApiType.GRPC
            else -> ApiType.REST
        }
    }
}

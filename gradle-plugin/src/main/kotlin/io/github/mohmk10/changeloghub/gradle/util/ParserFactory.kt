package io.github.mohmk10.changeloghub.gradle.util

import io.github.mohmk10.changeloghub.core.model.ApiSpec
import io.github.mohmk10.changeloghub.parser.asyncapi.DefaultAsyncApiParser
import io.github.mohmk10.changeloghub.parser.asyncapi.mapper.AsyncApiModelMapper
import io.github.mohmk10.changeloghub.parser.graphql.DefaultGraphQLParser
import io.github.mohmk10.changeloghub.parser.grpc.DefaultGrpcParser
import io.github.mohmk10.changeloghub.parser.openapi.impl.DefaultOpenApiParser
import org.gradle.api.GradleException
import java.io.File

object ParserFactory {

    enum class SpecType {
        OPENAPI,
        ASYNCAPI,
        GRAPHQL,
        GRPC,
        UNKNOWN
    }

    fun detectSpecType(file: File): SpecType {
        val extension = file.extension.lowercase()

        return when {
            extension == "proto" -> SpecType.GRPC
            extension == "graphql" || extension == "gql" -> SpecType.GRAPHQL
            extension in listOf("yaml", "yml", "json") -> {
                val content = file.readText()
                when {
                    content.contains("openapi:") || content.contains("\"openapi\"") ||
                    content.contains("swagger:") || content.contains("\"swagger\"") -> SpecType.OPENAPI
                    content.contains("asyncapi:") || content.contains("\"asyncapi\"") -> SpecType.ASYNCAPI
                    else -> SpecType.UNKNOWN
                }
            }
            else -> SpecType.UNKNOWN
        }
    }

    fun parse(file: File, specType: String = "auto"): ApiSpec {
        val type = if (specType == "auto") {
            detectSpecType(file)
        } else {
            when (specType.lowercase()) {
                "openapi" -> SpecType.OPENAPI
                "asyncapi" -> SpecType.ASYNCAPI
                "graphql" -> SpecType.GRAPHQL
                "grpc", "proto" -> SpecType.GRPC
                else -> SpecType.UNKNOWN
            }
        }

        return when (type) {
            SpecType.OPENAPI -> parseOpenApi(file)
            SpecType.ASYNCAPI -> parseAsyncApi(file)
            SpecType.GRAPHQL -> parseGraphQL(file)
            SpecType.GRPC -> parseGrpc(file)
            SpecType.UNKNOWN -> throw GradleException(
                "Unable to determine specification type for file: ${file.name}. " +
                "Supported types: OpenAPI, AsyncAPI, GraphQL, gRPC (proto)"
            )
        }
    }

    private fun parseOpenApi(file: File): ApiSpec {
        val parser = DefaultOpenApiParser()
        return parser.parseFile(file.toPath())
    }

    private fun parseAsyncApi(file: File): ApiSpec {
        val parser = DefaultAsyncApiParser()
        val asyncApiSpec = parser.parseFile(file)
        val mapper = AsyncApiModelMapper()
        return mapper.map(asyncApiSpec)
    }

    private fun parseGraphQL(file: File): ApiSpec {
        val parser = DefaultGraphQLParser()
        return parser.parseFileToApiSpec(file)
    }

    private fun parseGrpc(file: File): ApiSpec {
        val parser = DefaultGrpcParser()
        val protoFile = parser.parseFile(file)
        return parser.toApiSpec(protoFile)
    }

    fun getSupportedFormats(): String {
        return """
            Supported API specification formats:
            - OpenAPI 3.x / Swagger 2.x (.yaml, .yml, .json)
            - AsyncAPI 2.x / 3.x (.yaml, .yml, .json)
            - GraphQL Schema (.graphql, .gql)
            - Protocol Buffers / gRPC (.proto)
        """.trimIndent()
    }
}

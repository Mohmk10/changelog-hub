package io.github.mohmk10.changeloghub.intellij.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import io.github.mohmk10.changeloghub.intellij.model.*
import io.github.mohmk10.changeloghub.intellij.util.Logger
import org.yaml.snakeyaml.Yaml

@Service(Service.Level.PROJECT)
class ParserService(private val project: Project) {

    private val yaml = Yaml()

    fun parse(content: String, fileName: String): ApiSpec {
        val extension = fileName.substringAfterLast('.').lowercase()

        Logger.info("Parsing $fileName as $extension")

        return when (extension) {
            "yaml", "yml", "json" -> parseYamlOrJson(content, fileName)
            "graphql", "gql" -> parseGraphQL(content, fileName)
            "proto" -> parseProto(content, fileName)
            else -> throw IllegalArgumentException("Unsupported file format: $extension")
        }
    }

    private fun parseYamlOrJson(content: String, fileName: String): ApiSpec {
        return when {
            content.contains("openapi:") || content.contains("swagger:") ||
            content.contains("\"openapi\"") || content.contains("\"swagger\"") ->
                parseOpenApi(content, fileName)

            content.contains("asyncapi:") || content.contains("\"asyncapi\"") ->
                parseAsyncApi(content, fileName)

            else ->
                parseOpenApi(content, fileName)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseOpenApi(content: String, fileName: String): ApiSpec {
        val data = yaml.load<Map<String, Any>>(content) ?: emptyMap()

        val info = data["info"] as? Map<String, Any> ?: emptyMap()
        val name = info["title"] as? String ?: fileName
        val version = info["version"] as? String ?: "1.0.0"

        val paths = data["paths"] as? Map<String, Any> ?: emptyMap()
        val endpoints = mutableListOf<Endpoint>()

        paths.forEach { (path, pathItem) ->
            val pathData = pathItem as? Map<String, Any> ?: return@forEach
            listOf("get", "post", "put", "delete", "patch", "options", "head").forEach { method ->
                val operation = pathData[method] as? Map<String, Any>
                if (operation != null) {
                    val params = (operation["parameters"] as? List<Map<String, Any>>)?.map { p ->
                        Parameter(
                            name = p["name"] as? String ?: "",
                            location = p["in"] as? String ?: "query",
                            required = p["required"] as? Boolean ?: false,
                            type = (p["schema"] as? Map<String, Any>)?.get("type") as? String,
                            description = p["description"] as? String
                        )
                    } ?: emptyList()

                    endpoints.add(Endpoint(
                        method = method.uppercase(),
                        path = path,
                        operationId = operation["operationId"] as? String,
                        summary = operation["summary"] as? String,
                        description = operation["description"] as? String,
                        parameters = params,
                        deprecated = operation["deprecated"] as? Boolean ?: false
                    ))
                }
            }
        }

        val components = data["components"] as? Map<String, Any> ?: emptyMap()
        val schemasData = components["schemas"] as? Map<String, Any> ?: emptyMap()
        val schemas = schemasData.map { (schemaName, schemaData) ->
            val schemaMap = schemaData as? Map<String, Any> ?: emptyMap()
            val props = (schemaMap["properties"] as? Map<String, Any>)?.map { (propName, propData) ->
                val propMap = propData as? Map<String, Any> ?: emptyMap()
                val required = (schemaMap["required"] as? List<String>)?.contains(propName) ?: false
                Property(
                    name = propName,
                    type = propMap["type"] as? String ?: "object",
                    required = required
                )
            } ?: emptyList()
            Schema(
                name = schemaName,
                type = schemaMap["type"] as? String ?: "object",
                properties = props
            )
        }

        return ApiSpec(name, version, ApiType.REST, endpoints, schemas)
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseAsyncApi(content: String, fileName: String): ApiSpec {
        val data = yaml.load<Map<String, Any>>(content) ?: emptyMap()

        val info = data["info"] as? Map<String, Any> ?: emptyMap()
        val name = info["title"] as? String ?: fileName
        val version = info["version"] as? String ?: "1.0.0"

        val channels = data["channels"] as? Map<String, Any> ?: emptyMap()
        val endpoints = channels.flatMap { (channelName, channelData) ->
            val channel = channelData as? Map<String, Any> ?: return@flatMap emptyList()
            val ops = mutableListOf<Endpoint>()

            if (channel.containsKey("publish")) {
                ops.add(Endpoint(method = "PUBLISH", path = channelName))
            }
            if (channel.containsKey("subscribe")) {
                ops.add(Endpoint(method = "SUBSCRIBE", path = channelName))
            }
            ops
        }

        return ApiSpec(name, version, ApiType.ASYNCAPI, endpoints)
    }

    private fun parseGraphQL(content: String, fileName: String): ApiSpec {
        val endpoints = mutableListOf<Endpoint>()

        val queryMatch = Regex("""type\s+Query\s*\{([^}]+)\}""").find(content)
        queryMatch?.groupValues?.getOrNull(1)?.let { queryBody ->
            Regex("""(\w+)""").findAll(queryBody).forEach { match ->
                endpoints.add(Endpoint(method = "QUERY", path = match.value))
            }
        }

        val mutationMatch = Regex("""type\s+Mutation\s*\{([^}]+)\}""").find(content)
        mutationMatch?.groupValues?.getOrNull(1)?.let { mutationBody ->
            Regex("""(\w+)""").findAll(mutationBody).forEach { match ->
                endpoints.add(Endpoint(method = "MUTATION", path = match.value))
            }
        }

        return ApiSpec(fileName, "1.0.0", ApiType.GRAPHQL, endpoints)
    }

    private fun parseProto(content: String, fileName: String): ApiSpec {
        val endpoints = mutableListOf<Endpoint>()

        val serviceMatch = Regex("""service\s+(\w+)\s*\{([^}]+)\}""").findAll(content)
        serviceMatch.forEach { match ->
            val serviceName = match.groupValues[1]
            val serviceBody = match.groupValues[2]

            Regex("""rpc\s+(\w+)""").findAll(serviceBody).forEach { rpcMatch ->
                endpoints.add(Endpoint(method = "RPC", path = "$serviceName/${rpcMatch.groupValues[1]}"))
            }
        }

        return ApiSpec(fileName, "1.0.0", ApiType.GRPC, endpoints)
    }

    fun detectType(content: String): ApiType {
        return when {
            content.contains("openapi:") || content.contains("swagger:") ||
            content.contains("\"openapi\"") || content.contains("\"swagger\"") -> ApiType.REST

            content.contains("asyncapi:") || content.contains("\"asyncapi\"") -> ApiType.ASYNCAPI

            content.contains("type Query") || content.contains("type Mutation") -> ApiType.GRAPHQL

            content.contains("syntax = \"proto") -> ApiType.GRPC

            else -> ApiType.REST
        }
    }

    fun isValidSpec(content: String): Boolean {
        return try {
            detectType(content)
            true
        } catch (e: Exception) {
            false
        }
    }
}

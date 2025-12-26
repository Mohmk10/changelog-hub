package io.github.mohmk10.changeloghub.gradle

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GradleTestKitTest {

    @TempDir
    lateinit var tempDir: File

    private lateinit var buildFile: File
    private lateinit var settingsFile: File

    @BeforeEach
    fun setup() {
        buildFile = File(tempDir, "build.gradle.kts")
        settingsFile = File(tempDir, "settings.gradle.kts")

        settingsFile.writeText("""
            rootProject.name = "test-project"
        """.trimIndent())
    }

    @Test
    fun `plugin can be applied`() {
        buildFile.writeText("""
            plugins {
                id("io.github.mohmk10.changelog-hub")
            }
        """.trimIndent())

        val result = GradleRunner.create()
            .withProjectDir(tempDir)
            .withArguments("tasks", "--group=changelog")
            .withPluginClasspath()
            .build()

        assertTrue(result.output.contains("changelogCompare"))
        assertTrue(result.output.contains("changelogAnalyze"))
        assertTrue(result.output.contains("changelogValidate"))
        assertTrue(result.output.contains("changelogDetect"))
    }

    @Test
    fun `compare task skips when configured`() {
        buildFile.writeText("""
            plugins {
                id("io.github.mohmk10.changelog-hub")
            }

            changelogHub {
                skip = true
            }
        """.trimIndent())

        val result = GradleRunner.create()
            .withProjectDir(tempDir)
            .withArguments("changelogCompare")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":changelogCompare")?.outcome)
        assertTrue(result.output.contains("skipped"))
    }

    @Test
    fun `analyze task skips when configured`() {
        buildFile.writeText("""
            plugins {
                id("io.github.mohmk10.changelog-hub")
            }

            changelogHub {
                skip = true
            }
        """.trimIndent())

        val result = GradleRunner.create()
            .withProjectDir(tempDir)
            .withArguments("changelogAnalyze")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":changelogAnalyze")?.outcome)
        assertTrue(result.output.contains("skipped"))
    }

    @Test
    fun `validate task skips when configured`() {
        buildFile.writeText("""
            plugins {
                id("io.github.mohmk10.changelog-hub")
            }

            changelogHub {
                skip = true
            }
        """.trimIndent())

        val result = GradleRunner.create()
            .withProjectDir(tempDir)
            .withArguments("changelogValidate")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":changelogValidate")?.outcome)
        assertTrue(result.output.contains("skipped"))
    }

    @Test
    fun `detect task skips when configured`() {
        buildFile.writeText("""
            plugins {
                id("io.github.mohmk10.changelog-hub")
            }

            changelogHub {
                skip = true
            }
        """.trimIndent())

        val result = GradleRunner.create()
            .withProjectDir(tempDir)
            .withArguments("changelogDetect")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":changelogDetect")?.outcome)
        assertTrue(result.output.contains("skipped"))
    }

    @Test
    fun `analyze task analyzes OpenAPI spec`() {
        val apiFile = File(tempDir, "api.yaml")
        apiFile.writeText("""
            openapi: "3.0.0"
            info:
              title: "Test API"
              version: "1.0.0"
              description: "A test API"
            paths:
              /users:
                get:
                  operationId: getUsers
                  summary: Get all users
                  responses:
                    "200":
                      description: Success
        """.trimIndent())

        buildFile.writeText("""
            plugins {
                id("io.github.mohmk10.changelog-hub")
            }

            changelogHub {
                spec = "api.yaml"
                format = "console"
            }
        """.trimIndent())

        val result = GradleRunner.create()
            .withProjectDir(tempDir)
            .withArguments("changelogAnalyze")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":changelogAnalyze")?.outcome)
        assertTrue(result.output.contains("Test API"))
        assertTrue(result.output.contains("Endpoints: 1"))
    }

    @Test
    fun `validate task validates OpenAPI spec`() {
        val apiFile = File(tempDir, "api.yaml")
        apiFile.writeText("""
            openapi: "3.0.0"
            info:
              title: "Test API"
              version: "1.0.0"
            paths:
              /users:
                get:
                  operationId: getUsers
                  responses:
                    "200":
                      description: Success
        """.trimIndent())

        buildFile.writeText("""
            plugins {
                id("io.github.mohmk10.changelog-hub")
            }

            changelogHub {
                spec = "api.yaml"
            }
        """.trimIndent())

        val result = GradleRunner.create()
            .withProjectDir(tempDir)
            .withArguments("changelogValidate")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":changelogValidate")?.outcome)
        assertTrue(result.output.contains("Validation passed"))
    }

    @Test
    fun `compare task compares two specs`() {
        val oldApiFile = File(tempDir, "api-v1.yaml")
        oldApiFile.writeText("""
            openapi: "3.0.0"
            info:
              title: "Test API"
              version: "1.0.0"
            paths:
              /users:
                get:
                  operationId: getUsers
                  responses:
                    "200":
                      description: Success
        """.trimIndent())

        val newApiFile = File(tempDir, "api-v2.yaml")
        newApiFile.writeText("""
            openapi: "3.0.0"
            info:
              title: "Test API"
              version: "2.0.0"
            paths:
              /users:
                get:
                  operationId: getUsers
                  responses:
                    "200":
                      description: Success
              /posts:
                get:
                  operationId: getPosts
                  responses:
                    "200":
                      description: Success
        """.trimIndent())

        buildFile.writeText("""
            plugins {
                id("io.github.mohmk10.changelog-hub")
            }

            changelogHub {
                oldSpec = "api-v1.yaml"
                newSpec = "api-v2.yaml"
                format = "console"
            }
        """.trimIndent())

        val result = GradleRunner.create()
            .withProjectDir(tempDir)
            .withArguments("changelogCompare")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":changelogCompare")?.outcome)
        assertTrue(result.output.contains("Comparison complete"))
    }

    @Test
    fun `detect task detects changes between specs`() {
        val oldApiFile = File(tempDir, "api-v1.yaml")
        oldApiFile.writeText("""
            openapi: "3.0.0"
            info:
              title: "Test API"
              version: "1.0.0"
            paths:
              /users:
                get:
                  operationId: getUsers
                  responses:
                    "200":
                      description: Success
        """.trimIndent())

        val newApiFile = File(tempDir, "api-v2.yaml")
        newApiFile.writeText("""
            openapi: "3.0.0"
            info:
              title: "Test API"
              version: "2.0.0"
            paths:
              /members:
                get:
                  operationId: getMembers
                  responses:
                    "200":
                      description: Success
        """.trimIndent())

        buildFile.writeText("""
            plugins {
                id("io.github.mohmk10.changelog-hub")
            }

            changelogHub {
                oldSpec = "api-v1.yaml"
                newSpec = "api-v2.yaml"
            }
        """.trimIndent())

        val result = GradleRunner.create()
            .withProjectDir(tempDir)
            .withArguments("changelogDetect")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":changelogDetect")?.outcome)
        assertTrue(result.output.contains("BREAKING CHANGE DETECTION REPORT"))
        assertTrue(result.output.contains("Total changes:"))
    }

    @Test
    fun `detect task succeeds with allow-breaking flag`() {
        val oldApiFile = File(tempDir, "api-v1.yaml")
        oldApiFile.writeText("""
            openapi: "3.0.0"
            info:
              title: "Test API"
              version: "1.0.0"
            paths:
              /users:
                get:
                  operationId: getUsers
                  responses:
                    "200":
                      description: Success
        """.trimIndent())

        val newApiFile = File(tempDir, "api-v2.yaml")
        newApiFile.writeText("""
            openapi: "3.0.0"
            info:
              title: "Test API"
              version: "2.0.0"
            paths:
              /members:
                get:
                  operationId: getMembers
                  responses:
                    "200":
                      description: Success
        """.trimIndent())

        buildFile.writeText("""
            plugins {
                id("io.github.mohmk10.changelog-hub")
            }

            changelogHub {
                oldSpec = "api-v1.yaml"
                newSpec = "api-v2.yaml"
            }
        """.trimIndent())

        val result = GradleRunner.create()
            .withProjectDir(tempDir)
            .withArguments("changelogDetect", "--allow-breaking")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":changelogDetect")?.outcome)
    }

    @Test
    fun `extension configuration is applied`() {
        val apiFile = File(tempDir, "api.yaml")
        apiFile.writeText("""
            openapi: "3.0.0"
            info:
              title: "Test API"
              version: "1.0.0"
            paths: {}
        """.trimIndent())

        buildFile.writeText("""
            plugins {
                id("io.github.mohmk10.changelog-hub")
            }

            changelogHub {
                spec = "api.yaml"
                format = "markdown"
                outputDir = "build/reports"
                verbose = true
            }
        """.trimIndent())

        val result = GradleRunner.create()
            .withProjectDir(tempDir)
            .withArguments("changelogAnalyze")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":changelogAnalyze")?.outcome)
    }
}

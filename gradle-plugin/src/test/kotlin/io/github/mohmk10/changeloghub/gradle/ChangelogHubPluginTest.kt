package io.github.mohmk10.changeloghub.gradle

import io.github.mohmk10.changeloghub.gradle.task.AnalyzeTask
import io.github.mohmk10.changeloghub.gradle.task.CompareTask
import io.github.mohmk10.changeloghub.gradle.task.DetectBreakingChangesTask
import io.github.mohmk10.changeloghub.gradle.task.ValidateTask
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for ChangelogHubPlugin.
 */
class ChangelogHubPluginTest {

    @TempDir
    lateinit var tempDir: File

    private lateinit var project: Project

    @BeforeEach
    fun setup() {
        project = ProjectBuilder.builder()
            .withProjectDir(tempDir)
            .build()
        project.plugins.apply("io.github.mohmk10.changelog-hub")
    }

    @Test
    fun `plugin registers extension`() {
        val extension = project.extensions.findByName("changelogHub")
        assertNotNull(extension)
        assertTrue(extension is ChangelogHubExtension)
    }

    @Test
    fun `plugin registers compare task`() {
        val task = project.tasks.findByName("changelogCompare")
        assertNotNull(task)
        assertTrue(task is CompareTask)
        assertEquals("changelog", task.group)
        assertEquals("Compare two API specifications and generate a changelog", task.description)
    }

    @Test
    fun `plugin registers analyze task`() {
        val task = project.tasks.findByName("changelogAnalyze")
        assertNotNull(task)
        assertTrue(task is AnalyzeTask)
        assertEquals("changelog", task.group)
        assertEquals("Analyze a single API specification", task.description)
    }

    @Test
    fun `plugin registers validate task`() {
        val task = project.tasks.findByName("changelogValidate")
        assertNotNull(task)
        assertTrue(task is ValidateTask)
        assertEquals("changelog", task.group)
        assertEquals("Validate an API specification file", task.description)
    }

    @Test
    fun `plugin registers detect task`() {
        val task = project.tasks.findByName("changelogDetect")
        assertNotNull(task)
        assertTrue(task is DetectBreakingChangesTask)
        assertEquals("changelog", task.group)
        assertEquals("Detect breaking changes and fail build if found (CI-friendly)", task.description)
    }

    @Test
    fun `extension has default values`() {
        val extension = project.extensions.getByType(ChangelogHubExtension::class.java)

        assertEquals("build/changelog", extension.outputDir)
        assertEquals("console", extension.format)
        assertEquals(false, extension.failOnBreaking)
        assertEquals(false, extension.verbose)
        assertEquals(false, extension.skip)
        assertEquals(false, extension.strict)
        assertEquals("auto", extension.specType)
    }

    @Test
    fun `extension properties can be configured`() {
        val extension = project.extensions.getByType(ChangelogHubExtension::class.java)

        extension.oldSpec = "api/v1.yaml"
        extension.newSpec = "api/v2.yaml"
        extension.format = "markdown"
        extension.failOnBreaking = true
        extension.verbose = true

        assertEquals("api/v1.yaml", extension.oldSpec)
        assertEquals("api/v2.yaml", extension.newSpec)
        assertEquals("markdown", extension.format)
        assertEquals(true, extension.failOnBreaking)
        assertEquals(true, extension.verbose)
    }

    @Test
    fun `compare task receives extension`() {
        val task = project.tasks.getByName("changelogCompare") as CompareTask
        assertNotNull(task.extension)
    }

    @Test
    fun `analyze task receives extension`() {
        val task = project.tasks.getByName("changelogAnalyze") as AnalyzeTask
        assertNotNull(task.extension)
    }

    @Test
    fun `validate task receives extension`() {
        val task = project.tasks.getByName("changelogValidate") as ValidateTask
        assertNotNull(task.extension)
    }

    @Test
    fun `detect task receives extension`() {
        val task = project.tasks.getByName("changelogDetect") as DetectBreakingChangesTask
        assertNotNull(task.extension)
    }

    @Test
    fun `all tasks are in changelog group`() {
        val taskNames = listOf("changelogCompare", "changelogAnalyze", "changelogValidate", "changelogDetect")

        taskNames.forEach { taskName ->
            val task = project.tasks.getByName(taskName)
            assertEquals("changelog", task.group, "Task $taskName should be in 'changelog' group")
        }
    }
}

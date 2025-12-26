package io.github.mohmk10.changeloghub.gradle

import io.github.mohmk10.changeloghub.gradle.task.AnalyzeTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AnalyzeTaskTest {

    @TempDir
    lateinit var tempDir: File

    private lateinit var project: Project
    private lateinit var task: AnalyzeTask

    @BeforeEach
    fun setup() {
        project = ProjectBuilder.builder()
            .withProjectDir(tempDir)
            .build()
        project.plugins.apply("io.github.mohmk10.changelog-hub")
        task = project.tasks.getByName("changelogAnalyze") as AnalyzeTask
    }

    @Test
    fun `task has correct default values`() {
        assertNull(task.specPath)
        assertNull(task.formatOption)
        assertNull(task.outputDirOption)
    }

    @Test
    fun `task skips when skip is true`() {
        task.extension.skip = true

        task.analyze()
    }

    @Test
    fun `task fails when spec not specified`() {
        val exception = assertFailsWith<GradleException> {
            task.analyze()
        }

        assertTrue(exception.message!!.contains("Specification path not specified"))
    }

    @Test
    fun `task fails when spec file not found`() {
        task.extension.spec = "nonexistent/api.yaml"

        val exception = assertFailsWith<GradleException> {
            task.analyze()
        }

        assertTrue(exception.message!!.contains("file not found"))
    }

    @Test
    fun `CLI options override extension values`() {
        task.extension.spec = "default/api.yaml"
        task.extension.format = "console"

        task.specPath = "cli/api.yaml"
        task.formatOption = "json"

        assertEquals("cli/api.yaml", task.specPath)
        assertEquals("json", task.formatOption)
    }

    @Test
    fun `task uses newSpec as fallback for spec`() {
        task.extension.newSpec = "api/new.yaml"

        assertEquals("api/new.yaml", task.extension.newSpec)
        assertNull(task.extension.spec)
    }

    @Test
    fun `extension has extension reference`() {
        assertNotNull(task.extension)
        assertTrue(task.extension is ChangelogHubExtension)
    }

    @Test
    fun `task description is set`() {
        assertEquals("Analyze a single API specification", task.description)
    }

    @Test
    fun `task group is set`() {
        assertEquals("changelog", task.group)
    }
}

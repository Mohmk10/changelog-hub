package io.github.mohmk10.changeloghub.gradle

import io.github.mohmk10.changeloghub.gradle.task.ValidateTask
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

/**
 * Unit tests for ValidateTask.
 */
class ValidateTaskTest {

    @TempDir
    lateinit var tempDir: File

    private lateinit var project: Project
    private lateinit var task: ValidateTask

    @BeforeEach
    fun setup() {
        project = ProjectBuilder.builder()
            .withProjectDir(tempDir)
            .build()
        project.plugins.apply("io.github.mohmk10.changelog-hub")
        task = project.tasks.getByName("changelogValidate") as ValidateTask
    }

    @Test
    fun `task has correct default values`() {
        assertNull(task.specPath)
        assertNull(task.strictOption)
    }

    @Test
    fun `task skips when skip is true`() {
        task.extension.skip = true

        // Should not throw even without spec file
        task.validate()
    }

    @Test
    fun `task fails when spec not specified`() {
        val exception = assertFailsWith<GradleException> {
            task.validate()
        }

        assertTrue(exception.message!!.contains("Specification path not specified"))
    }

    @Test
    fun `task fails when spec file not found`() {
        task.extension.spec = "nonexistent/api.yaml"

        val exception = assertFailsWith<GradleException> {
            task.validate()
        }

        assertTrue(exception.message!!.contains("file not found"))
    }

    @Test
    fun `CLI options override extension values`() {
        task.extension.spec = "default/api.yaml"
        task.extension.strict = false

        task.specPath = "cli/api.yaml"
        task.strictOption = true

        assertEquals("cli/api.yaml", task.specPath)
        assertEquals(true, task.strictOption)
    }

    @Test
    fun `strict mode defaults to false`() {
        assertEquals(false, task.extension.strict)
    }

    @Test
    fun `extension has extension reference`() {
        assertNotNull(task.extension)
        assertTrue(task.extension is ChangelogHubExtension)
    }

    @Test
    fun `task description is set`() {
        assertEquals("Validate an API specification file", task.description)
    }

    @Test
    fun `task group is set`() {
        assertEquals("changelog", task.group)
    }
}

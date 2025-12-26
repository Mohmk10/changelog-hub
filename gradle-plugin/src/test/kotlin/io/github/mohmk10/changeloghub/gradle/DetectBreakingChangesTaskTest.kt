package io.github.mohmk10.changeloghub.gradle

import io.github.mohmk10.changeloghub.gradle.task.DetectBreakingChangesTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for DetectBreakingChangesTask.
 */
class DetectBreakingChangesTaskTest {

    @TempDir
    lateinit var tempDir: File

    private lateinit var project: Project
    private lateinit var task: DetectBreakingChangesTask

    @BeforeEach
    fun setup() {
        project = ProjectBuilder.builder()
            .withProjectDir(tempDir)
            .build()
        project.plugins.apply("io.github.mohmk10.changelog-hub")
        task = project.tasks.getByName("changelogDetect") as DetectBreakingChangesTask
    }

    @Test
    fun `task has correct default values`() {
        assertNull(task.oldSpecPath)
        assertNull(task.newSpecPath)
        assertNull(task.formatOption)
        assertNull(task.outputDirOption)
        assertFalse(task.allowBreaking)
    }

    @Test
    fun `task skips when skip is true`() {
        task.extension.skip = true

        // Should not throw even without spec files
        task.detect()
    }

    @Test
    fun `task fails when old spec not specified`() {
        task.extension.newSpec = "api/new.yaml"

        val exception = assertFailsWith<GradleException> {
            task.detect()
        }

        assertTrue(exception.message!!.contains("Old specification path not specified"))
    }

    @Test
    fun `task fails when new spec not specified`() {
        task.extension.oldSpec = "api/old.yaml"

        val exception = assertFailsWith<GradleException> {
            task.detect()
        }

        assertTrue(exception.message!!.contains("New specification path not specified"))
    }

    @Test
    fun `task fails when spec file not found`() {
        task.extension.oldSpec = "nonexistent/old.yaml"
        task.extension.newSpec = "api/new.yaml"

        val exception = assertFailsWith<GradleException> {
            task.detect()
        }

        assertTrue(exception.message!!.contains("file not found"))
    }

    @Test
    fun `allowBreaking defaults to false`() {
        assertFalse(task.allowBreaking)
    }

    @Test
    fun `allowBreaking can be set to true`() {
        task.allowBreaking = true
        assertTrue(task.allowBreaking)
    }

    @Test
    fun `CLI options override extension values`() {
        task.extension.oldSpec = "default/old.yaml"
        task.extension.newSpec = "default/new.yaml"
        task.extension.format = "console"

        task.oldSpecPath = "cli/old.yaml"
        task.newSpecPath = "cli/new.yaml"
        task.formatOption = "json"

        assertEquals("cli/old.yaml", task.oldSpecPath)
        assertEquals("cli/new.yaml", task.newSpecPath)
        assertEquals("json", task.formatOption)
    }

    @Test
    fun `extension has extension reference`() {
        assertNotNull(task.extension)
        assertTrue(task.extension is ChangelogHubExtension)
    }

    @Test
    fun `task description is set`() {
        assertEquals("Detect breaking changes and fail build if found (CI-friendly)", task.description)
    }

    @Test
    fun `task group is set`() {
        assertEquals("changelog", task.group)
    }

    @Test
    fun `task is CI-friendly by defaulting failOnBreaking to true`() {
        // allowBreaking = false means failOnBreaking = true
        assertFalse(task.allowBreaking)
    }
}

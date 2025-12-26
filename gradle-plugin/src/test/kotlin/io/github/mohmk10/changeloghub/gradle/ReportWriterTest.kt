package io.github.mohmk10.changeloghub.gradle

import io.github.mohmk10.changeloghub.core.model.ApiSpec
import io.github.mohmk10.changeloghub.core.model.ApiType
import io.github.mohmk10.changeloghub.core.model.Changelog
import io.github.mohmk10.changeloghub.core.model.Endpoint
import io.github.mohmk10.changeloghub.core.model.HttpMethod
import io.github.mohmk10.changeloghub.core.reporter.impl.ConsoleReporter
import io.github.mohmk10.changeloghub.core.reporter.impl.HtmlReporter
import io.github.mohmk10.changeloghub.core.reporter.impl.JsonReporter
import io.github.mohmk10.changeloghub.core.reporter.impl.MarkdownReporter
import io.github.mohmk10.changeloghub.gradle.util.ReportWriter
import org.gradle.api.GradleException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ReportWriterTest {

    @TempDir
    lateinit var tempDir: File

    @Test
    fun `getReporter returns ConsoleReporter for console format`() {
        assertTrue(ReportWriter.getReporter("console") is ConsoleReporter)
        assertTrue(ReportWriter.getReporter("text") is ConsoleReporter)
        assertTrue(ReportWriter.getReporter("txt") is ConsoleReporter)
    }

    @Test
    fun `getReporter returns MarkdownReporter for markdown format`() {
        assertTrue(ReportWriter.getReporter("markdown") is MarkdownReporter)
        assertTrue(ReportWriter.getReporter("md") is MarkdownReporter)
    }

    @Test
    fun `getReporter returns JsonReporter for json format`() {
        assertTrue(ReportWriter.getReporter("json") is JsonReporter)
    }

    @Test
    fun `getReporter returns HtmlReporter for html format`() {
        assertTrue(ReportWriter.getReporter("html") is HtmlReporter)
    }

    @Test
    fun `getReporter throws for unsupported format`() {
        val exception = assertFailsWith<GradleException> {
            ReportWriter.getReporter("pdf")
        }

        assertTrue(exception.message!!.contains("Unsupported report format"))
    }

    @Test
    fun `getExtension returns correct extensions`() {
        assertEquals("txt", ReportWriter.getExtension("console"))
        assertEquals("txt", ReportWriter.getExtension("text"))
        assertEquals("md", ReportWriter.getExtension("markdown"))
        assertEquals("json", ReportWriter.getExtension("json"))
        assertEquals("html", ReportWriter.getExtension("html"))
    }

    @Test
    fun `getExtension returns txt for unknown format`() {
        assertEquals("txt", ReportWriter.getExtension("unknown"))
    }

    @Test
    fun `write creates file in output directory`() {
        val changelog = createTestChangelog()

        val file = ReportWriter.write(tempDir, changelog, "markdown")

        assertTrue(file.exists())
        assertEquals("changelog.md", file.name)
        assertTrue(file.readText().isNotEmpty())
    }

    @Test
    fun `write creates output directory if not exists`() {
        val outputDir = File(tempDir, "nested/output/dir")
        val changelog = createTestChangelog()

        val file = ReportWriter.write(outputDir, changelog, "json")

        assertTrue(outputDir.exists())
        assertTrue(file.exists())
    }

    @Test
    fun `write uses custom filename`() {
        val changelog = createTestChangelog()

        val file = ReportWriter.write(tempDir, changelog, "html", "custom-report")

        assertEquals("custom-report.html", file.name)
    }

    @Test
    fun `generateReport returns report string`() {
        val changelog = createTestChangelog()

        val report = ReportWriter.generateReport(changelog, "console")

        assertTrue(report.isNotEmpty())
    }

    @Test
    fun `writeAnalysis creates analysis report`() {
        val spec = createTestSpec()

        val file = ReportWriter.writeAnalysis(tempDir, spec, "markdown")

        assertTrue(file.exists())
        assertEquals("analysis.md", file.name)
        assertTrue(file.readText().contains("API Analysis Report"))
    }

    @Test
    fun `generateAnalysisReport returns console format`() {
        val spec = createTestSpec()

        val report = ReportWriter.generateAnalysisReport(spec, "console")

        assertTrue(report.contains("API ANALYSIS REPORT"))
        assertTrue(report.contains("Total Endpoints"))
    }

    @Test
    fun `generateAnalysisReport returns markdown format`() {
        val spec = createTestSpec()

        val report = ReportWriter.generateAnalysisReport(spec, "markdown")

        assertTrue(report.contains("# API Analysis Report"))
        assertTrue(report.contains("| Property | Value |"))
    }

    @Test
    fun `generateAnalysisReport returns json format`() {
        val spec = createTestSpec()

        val report = ReportWriter.generateAnalysisReport(spec, "json")

        assertTrue(report.contains("\"name\""))
        assertTrue(report.contains("\"version\""))
        assertTrue(report.contains("\"statistics\""))
    }

    @Test
    fun `generateAnalysisReport returns html format`() {
        val spec = createTestSpec()

        val report = ReportWriter.generateAnalysisReport(spec, "html")

        assertTrue(report.contains("<!DOCTYPE html>"))
        assertTrue(report.contains("API Analysis Report"))
        assertTrue(report.contains("<table>"))
    }

    @Test
    fun `generateAnalysisReport includes endpoint statistics`() {
        val spec = createTestSpec()

        val report = ReportWriter.generateAnalysisReport(spec, "console")

        assertTrue(report.contains("Total Endpoints: 2"))
        assertTrue(report.contains("Deprecated Endpoints: 1"))
    }

    @Test
    fun `generateAnalysisReport includes method counts`() {
        val spec = createTestSpec()

        val report = ReportWriter.generateAnalysisReport(spec, "console")

        assertTrue(report.contains("Endpoints by Method"))
        assertTrue(report.contains("GET"))
        assertTrue(report.contains("POST"))
    }

    private fun createTestChangelog(): Changelog {
        return Changelog.builder()
            .fromVersion("1.0.0")
            .toVersion("2.0.0")
            .changes(emptyList())
            .breakingChanges(emptyList())
            .build()
    }

    private fun createTestSpec(): ApiSpec {
        val endpoint1 = Endpoint.builder()
            .path("/users")
            .method(HttpMethod.GET)
            .operationId("getUsers")
            .description("Get all users")
            .deprecated(false)
            .build()

        val endpoint2 = Endpoint.builder()
            .path("/users")
            .method(HttpMethod.POST)
            .operationId("createUser")
            .description("Create a user")
            .deprecated(true)
            .build()

        return ApiSpec.builder()
            .name("Test API")
            .version("1.0.0")
            .type(ApiType.REST)
            .endpoints(listOf(endpoint1, endpoint2))
            .build()
    }
}

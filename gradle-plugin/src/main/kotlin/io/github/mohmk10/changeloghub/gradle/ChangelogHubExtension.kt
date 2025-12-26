package io.github.mohmk10.changeloghub.gradle

import org.gradle.api.provider.Property
import org.gradle.api.file.DirectoryProperty

/**
 * Extension for configuring the Changelog Hub plugin.
 *
 * Usage in build.gradle.kts:
 * ```
 * changelogHub {
 *     oldSpec = "api/v1/openapi.yaml"
 *     newSpec = "api/v2/openapi.yaml"
 *     outputDir = "build/reports/changelog"
 *     format = "markdown"
 *     failOnBreaking = true
 * }
 * ```
 */
open class ChangelogHubExtension {

    /**
     * Path to the old/baseline API specification file.
     */
    var oldSpec: String? = null

    /**
     * Path to the new API specification file to compare against.
     */
    var newSpec: String? = null

    /**
     * Path to a single API specification file (for analyze/validate tasks).
     */
    var spec: String? = null

    /**
     * Output directory for generated reports.
     * Default: build/changelog
     */
    var outputDir: String = "build/changelog"

    /**
     * Output format: console, markdown, json, html
     * Default: console
     */
    var format: String = "console"

    /**
     * Whether to fail the build when breaking changes are detected.
     * Default: false
     */
    var failOnBreaking: Boolean = false

    /**
     * Enable verbose output.
     * Default: false
     */
    var verbose: Boolean = false

    /**
     * Skip changelog detection entirely.
     * Default: false
     */
    var skip: Boolean = false

    /**
     * Strict validation mode.
     * Default: false
     */
    var strict: Boolean = false

    /**
     * API specification type: auto, openapi, asyncapi, graphql, grpc
     * Default: auto (auto-detect from file)
     */
    var specType: String = "auto"

    override fun toString(): String {
        return "ChangelogHubExtension(oldSpec=$oldSpec, newSpec=$newSpec, spec=$spec, " +
                "outputDir=$outputDir, format=$format, failOnBreaking=$failOnBreaking, " +
                "verbose=$verbose, skip=$skip, strict=$strict, specType=$specType)"
    }
}

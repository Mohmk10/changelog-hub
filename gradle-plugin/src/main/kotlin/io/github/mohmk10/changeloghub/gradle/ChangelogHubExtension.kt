package io.github.mohmk10.changeloghub.gradle

import org.gradle.api.provider.Property
import org.gradle.api.file.DirectoryProperty

open class ChangelogHubExtension {

    var oldSpec: String? = null

    var newSpec: String? = null

    var spec: String? = null

    var outputDir: String = "build/changelog"

    var format: String = "console"

    var failOnBreaking: Boolean = false

    var verbose: Boolean = false

    var skip: Boolean = false

    var strict: Boolean = false

    var specType: String = "auto"

    override fun toString(): String {
        return "ChangelogHubExtension(oldSpec=$oldSpec, newSpec=$newSpec, spec=$spec, " +
                "outputDir=$outputDir, format=$format, failOnBreaking=$failOnBreaking, " +
                "verbose=$verbose, skip=$skip, strict=$strict, specType=$specType)"
    }
}

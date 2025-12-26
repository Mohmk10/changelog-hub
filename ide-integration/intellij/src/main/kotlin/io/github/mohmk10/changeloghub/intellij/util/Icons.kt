package io.github.mohmk10.changeloghub.intellij.util

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

/**
 * Icon resources for the Changelog Hub plugin.
 */
object Icons {
    @JvmField
    val CHANGELOG_HUB: Icon = IconLoader.getIcon("/icons/changelog-hub.svg", Icons::class.java)

    @JvmField
    val BREAKING: Icon = IconLoader.getIcon("/icons/breaking.svg", Icons::class.java)

    @JvmField
    val WARNING: Icon = IconLoader.getIcon("/icons/warning.svg", Icons::class.java)

    @JvmField
    val INFO: Icon = IconLoader.getIcon("/icons/info.svg", Icons::class.java)
}

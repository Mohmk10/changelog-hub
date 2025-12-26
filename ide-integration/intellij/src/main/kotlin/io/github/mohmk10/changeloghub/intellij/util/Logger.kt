package io.github.mohmk10.changeloghub.intellij.util

import com.intellij.openapi.diagnostic.Logger as IdeaLogger

object Logger {
    private val LOG = IdeaLogger.getInstance("ChangelogHub")

    fun info(message: String) = LOG.info(message)
    fun warn(message: String) = LOG.warn(message)
    fun error(message: String) = LOG.error(message)
    fun error(message: String, throwable: Throwable) = LOG.error(message, throwable)
    fun debug(message: String) = LOG.debug(message)
}

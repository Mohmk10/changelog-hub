package io.github.mohmk10.changeloghub.intellij.services

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

/**
 * Service for displaying notifications to the user.
 */
@Service(Service.Level.PROJECT)
class NotificationService(private val project: Project) {

    private val notificationGroup = NotificationGroupManager.getInstance()
        .getNotificationGroup("Changelog Hub")

    /**
     * Show an info notification.
     */
    fun info(title: String, content: String) {
        notificationGroup.createNotification(title, content, NotificationType.INFORMATION)
            .notify(project)
    }

    /**
     * Show a warning notification.
     */
    fun warn(title: String, content: String) {
        notificationGroup.createNotification(title, content, NotificationType.WARNING)
            .notify(project)
    }

    /**
     * Show an error notification.
     */
    fun error(title: String, content: String) {
        notificationGroup.createNotification(title, content, NotificationType.ERROR)
            .notify(project)
    }

    /**
     * Show a comparison result notification.
     */
    fun showComparisonResult(result: ComparisonResult) {
        val type = if (result.breakingChangesCount > 0) NotificationType.WARNING else NotificationType.INFORMATION
        val title = if (result.breakingChangesCount > 0)
            "${result.breakingChangesCount} Breaking Change(s) Detected"
        else
            "No Breaking Changes"

        val content = buildString {
            appendLine("Total changes: ${result.totalChangesCount}")
            appendLine("Risk level: ${result.riskLevel}")
            appendLine("Recommended: ${result.semverRecommendation} version bump")
        }

        notificationGroup.createNotification(title, content, type)
            .notify(project)
    }
}

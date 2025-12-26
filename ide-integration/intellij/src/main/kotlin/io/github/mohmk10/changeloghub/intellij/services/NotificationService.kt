package io.github.mohmk10.changeloghub.intellij.services

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class NotificationService(private val project: Project) {

    private val notificationGroup = NotificationGroupManager.getInstance()
        .getNotificationGroup("Changelog Hub")

    fun info(title: String, content: String) {
        notificationGroup.createNotification(title, content, NotificationType.INFORMATION)
            .notify(project)
    }

    fun warn(title: String, content: String) {
        notificationGroup.createNotification(title, content, NotificationType.WARNING)
            .notify(project)
    }

    fun error(title: String, content: String) {
        notificationGroup.createNotification(title, content, NotificationType.ERROR)
            .notify(project)
    }

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

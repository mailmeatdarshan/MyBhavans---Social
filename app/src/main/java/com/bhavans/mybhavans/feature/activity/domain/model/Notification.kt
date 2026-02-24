package com.bhavans.mybhavans.feature.activity.domain.model

data class Notification(
    val id: String = "",
    val type: NotificationType = NotificationType.LIKE,
    val actorId: String = "",
    val actorName: String = "",
    val actorPhotoUrl: String = "",
    val postId: String? = null,
    val message: String = "",
    val createdAt: Long = 0,
    val isRead: Boolean = false
)

enum class NotificationType {
    LIKE,
    COMMENT,
    FOLLOW,
    SAFEWALK_ACCEPTED
}

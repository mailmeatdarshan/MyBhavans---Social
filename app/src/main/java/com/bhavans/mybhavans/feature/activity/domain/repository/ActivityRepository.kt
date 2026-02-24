package com.bhavans.mybhavans.feature.activity.domain.repository

import com.bhavans.mybhavans.core.util.Resource
import com.bhavans.mybhavans.feature.activity.domain.model.Notification
import kotlinx.coroutines.flow.Flow

interface ActivityRepository {
    fun getNotifications(): Flow<Resource<List<Notification>>>
    suspend fun markAsRead(notificationId: String): Resource<Unit>
    suspend fun markAllAsRead(): Resource<Unit>
    suspend fun createNotification(
        targetUserId: String,
        type: String,
        actorName: String,
        actorPhotoUrl: String,
        postId: String?,
        message: String
    ): Resource<Unit>
}

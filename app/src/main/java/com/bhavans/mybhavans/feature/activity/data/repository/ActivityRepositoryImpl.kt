package com.bhavans.mybhavans.feature.activity.data.repository

import com.bhavans.mybhavans.core.util.Resource
import com.bhavans.mybhavans.feature.activity.domain.model.Notification
import com.bhavans.mybhavans.feature.activity.domain.model.NotificationType
import com.bhavans.mybhavans.feature.activity.domain.repository.ActivityRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ActivityRepository {

    override fun getNotifications(): Flow<Resource<List<Notification>>> = callbackFlow {
        trySend(Resource.Loading())

        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(Resource.Error("User not authenticated"))
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("users")
            .document(currentUser.uid)
            .collection("notifications")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to load notifications"))
                    return@addSnapshotListener
                }

                val notifications = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(NotificationDto::class.java)?.toDomain(doc.id)
                } ?: emptyList()

                trySend(Resource.Success(notifications))
            }

        awaitClose { listener.remove() }
    }

    override suspend fun markAsRead(notificationId: String): Resource<Unit> {
        return try {
            val currentUser = auth.currentUser ?: return Resource.Error("Not logged in")
            firestore.collection("users")
                .document(currentUser.uid)
                .collection("notifications")
                .document(notificationId)
                .update("isRead", true)
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to mark notification as read")
        }
    }

    override suspend fun markAllAsRead(): Resource<Unit> {
        return try {
            val currentUser = auth.currentUser ?: return Resource.Error("Not logged in")
            val notificationsRef = firestore.collection("users")
                .document(currentUser.uid)
                .collection("notifications")
            val unread = notificationsRef
                .whereEqualTo("isRead", false)
                .get()
                .await()

            val batch = firestore.batch()
            unread.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
            }
            batch.commit().await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to mark all as read")
        }
    }

    override suspend fun createNotification(
        targetUserId: String,
        type: String,
        actorName: String,
        actorPhotoUrl: String,
        postId: String?,
        message: String
    ): Resource<Unit> {
        return try {
            val currentUser = auth.currentUser ?: return Resource.Error("Not logged in")
            // Don't notify yourself
            if (currentUser.uid == targetUserId) return Resource.Success(Unit)

            val notificationData = hashMapOf(
                "type" to type,
                "actorId" to currentUser.uid,
                "actorName" to actorName,
                "actorPhotoUrl" to actorPhotoUrl,
                "postId" to postId,
                "message" to message,
                "createdAt" to System.currentTimeMillis(),
                "isRead" to false
            )

            firestore.collection("users")
                .document(targetUserId)
                .collection("notifications")
                .add(notificationData)
                .await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create notification")
        }
    }
}

data class NotificationDto(
    val type: String = "",
    val actorId: String = "",
    val actorName: String = "",
    val actorPhotoUrl: String = "",
    val postId: String? = null,
    val message: String = "",
    val createdAt: Long = 0,
    val isRead: Boolean = false
) {
    fun toDomain(id: String): Notification {
        return Notification(
            id = id,
            type = try { NotificationType.valueOf(type) } catch (e: Exception) { NotificationType.LIKE },
            actorId = actorId,
            actorName = actorName,
            actorPhotoUrl = actorPhotoUrl,
            postId = postId,
            message = message,
            createdAt = createdAt,
            isRead = isRead
        )
    }
}

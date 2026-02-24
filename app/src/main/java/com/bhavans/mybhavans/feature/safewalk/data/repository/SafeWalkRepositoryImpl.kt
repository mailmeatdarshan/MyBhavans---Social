package com.bhavans.mybhavans.feature.safewalk.data.repository

import com.bhavans.mybhavans.core.util.Resource
import com.bhavans.mybhavans.feature.safewalk.domain.model.WalkRequest
import com.bhavans.mybhavans.feature.safewalk.domain.model.WalkRequestStatus
import com.bhavans.mybhavans.feature.safewalk.domain.repository.SafeWalkRepository
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
class SafeWalkRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : SafeWalkRepository {

    private val requestsCollection = firestore.collection("walk_requests")

    override fun getPendingRequests(): Flow<Resource<List<WalkRequest>>> = callbackFlow {
        trySend(Resource.Loading())
        
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(Resource.Error("User not authenticated"))
            close()
            return@callbackFlow
        }
        
        // Load all pending requests, then filter client-side to exclude own user
        // This avoids compound query + composite index requirement
        val listener = requestsCollection
            .whereEqualTo("status", WalkRequestStatus.PENDING.name)
            .orderBy("scheduledTime", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to load requests"))
                    return@addSnapshotListener
                }
                
                val requests = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(WalkRequestDto::class.java)?.toDomain(doc.id)
                }?.filter { it.requesterId != currentUser.uid } ?: emptyList()
                
                trySend(Resource.Success(requests))
            }
        
        awaitClose { listener.remove() }
    }

    override fun getMyRequests(): Flow<Resource<List<WalkRequest>>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(Resource.Error("User not authenticated"))
            close()
            return@callbackFlow
        }
        
        trySend(Resource.Loading())
        
        val listener = requestsCollection
            .whereEqualTo("requesterId", currentUser.uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to load requests"))
                    return@addSnapshotListener
                }
                
                val requests = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(WalkRequestDto::class.java)?.toDomain(doc.id)
                } ?: emptyList()
                
                trySend(Resource.Success(requests))
            }
        
        awaitClose { listener.remove() }
    }

    override fun getMyBuddyRequests(): Flow<Resource<List<WalkRequest>>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(Resource.Error("User not authenticated"))
            close()
            return@callbackFlow
        }
        
        trySend(Resource.Loading())
        
        val listener = requestsCollection
            .whereEqualTo("buddyId", currentUser.uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to load requests"))
                    return@addSnapshotListener
                }
                
                val requests = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(WalkRequestDto::class.java)?.toDomain(doc.id)
                } ?: emptyList()
                
                trySend(Resource.Success(requests))
            }
        
        awaitClose { listener.remove() }
    }

    override suspend fun getRequest(requestId: String): Resource<WalkRequest> {
        return try {
            val doc = requestsCollection.document(requestId).get().await()
            val request = doc.toObject(WalkRequestDto::class.java)?.toDomain(doc.id)
            if (request != null) {
                Resource.Success(request)
            } else {
                Resource.Error("Request not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get request")
        }
    }

    override suspend fun createRequest(
        fromLocation: String,
        toLocation: String,
        scheduledTime: Long,
        message: String,
        phoneNumber: String?
    ): Resource<WalkRequest> {
        return try {
            val currentUser = auth.currentUser ?: return Resource.Error("User not authenticated")
            
            val now = System.currentTimeMillis()
            val requestDto = WalkRequestDto(
                requesterId = currentUser.uid,
                requesterName = currentUser.displayName ?: "Anonymous",
                requesterEmail = currentUser.email ?: "",
                requesterPhone = phoneNumber,
                fromLocation = fromLocation,
                toLocation = toLocation,
                scheduledTime = scheduledTime,
                message = message,
                status = WalkRequestStatus.PENDING.name,
                createdAt = now,
                updatedAt = now
            )
            
            val docRef = requestsCollection.add(requestDto).await()
            val request = requestDto.toDomain(docRef.id)
            
            Resource.Success(request)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create request")
        }
    }

    override suspend fun acceptRequest(requestId: String): Resource<WalkRequest> {
        return try {
            val currentUser = auth.currentUser ?: return Resource.Error("User not authenticated")
            
            val doc = requestsCollection.document(requestId).get().await()
            val request = doc.toObject(WalkRequestDto::class.java) 
                ?: return Resource.Error("Request not found")
            
            if (request.requesterId == currentUser.uid) {
                return Resource.Error("You cannot accept your own request")
            }
            
            if (request.status != WalkRequestStatus.PENDING.name) {
                return Resource.Error("This request is no longer available")
            }
            
            val updates = mapOf(
                "buddyId" to currentUser.uid,
                "buddyName" to (currentUser.displayName ?: "Anonymous"),
                "buddyEmail" to (currentUser.email ?: ""),
                "buddyPhone" to currentUser.phoneNumber,
                "status" to WalkRequestStatus.ACCEPTED.name,
                "updatedAt" to System.currentTimeMillis()
            )
            
            requestsCollection.document(requestId).update(updates).await()
            
            val updatedRequest = request.toDomain(requestId).copy(
                buddyId = currentUser.uid,
                buddyName = currentUser.displayName,
                buddyEmail = currentUser.email,
                status = WalkRequestStatus.ACCEPTED
            )
            
            Resource.Success(updatedRequest)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to accept request")
        }
    }

    override suspend fun updateRequestStatus(requestId: String, status: WalkRequestStatus): Resource<Unit> {
        return try {
            requestsCollection.document(requestId).update(
                mapOf(
                    "status" to status.name,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update status")
        }
    }

    override suspend fun cancelRequest(requestId: String): Resource<Unit> {
        return try {
            val currentUser = auth.currentUser ?: return Resource.Error("User not authenticated")
            
            val doc = requestsCollection.document(requestId).get().await()
            val request = doc.toObject(WalkRequestDto::class.java) 
                ?: return Resource.Error("Request not found")
            
            if (request.requesterId != currentUser.uid && request.buddyId != currentUser.uid) {
                return Resource.Error("You can only cancel your own requests")
            }
            
            requestsCollection.document(requestId).update(
                mapOf(
                    "status" to WalkRequestStatus.CANCELLED.name,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
            
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to cancel request")
        }
    }

    override suspend fun completeWalk(requestId: String): Resource<Unit> {
        return try {
            requestsCollection.document(requestId).update(
                mapOf(
                    "status" to WalkRequestStatus.COMPLETED.name,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to complete walk")
        }
    }
}

// DTO class
data class WalkRequestDto(
    val requesterId: String = "",
    val requesterName: String = "",
    val requesterEmail: String = "",
    val requesterPhone: String? = null,
    val buddyId: String? = null,
    val buddyName: String? = null,
    val buddyEmail: String? = null,
    val buddyPhone: String? = null,
    val fromLocation: String = "",
    val toLocation: String = "",
    val scheduledTime: Long = 0,
    val message: String = "",
    val status: String = "PENDING",
    val createdAt: Long = 0,
    val updatedAt: Long = 0
) {
    fun toDomain(id: String): WalkRequest {
        return WalkRequest(
            id = id,
            requesterId = requesterId,
            requesterName = requesterName,
            requesterEmail = requesterEmail,
            requesterPhone = requesterPhone,
            buddyId = buddyId,
            buddyName = buddyName,
            buddyEmail = buddyEmail,
            buddyPhone = buddyPhone,
            fromLocation = fromLocation,
            toLocation = toLocation,
            scheduledTime = scheduledTime,
            message = message,
            status = try { WalkRequestStatus.valueOf(status) } catch (e: Exception) { WalkRequestStatus.PENDING },
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}

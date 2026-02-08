package com.bhavans.mybhavans.feature.safewalk.domain.repository

import com.bhavans.mybhavans.core.util.Resource
import com.bhavans.mybhavans.feature.safewalk.domain.model.WalkRequest
import com.bhavans.mybhavans.feature.safewalk.domain.model.WalkRequestStatus
import kotlinx.coroutines.flow.Flow

interface SafeWalkRepository {
    
    fun getPendingRequests(): Flow<Resource<List<WalkRequest>>>
    
    fun getMyRequests(): Flow<Resource<List<WalkRequest>>>
    
    fun getMyBuddyRequests(): Flow<Resource<List<WalkRequest>>>
    
    suspend fun getRequest(requestId: String): Resource<WalkRequest>
    
    suspend fun createRequest(
        fromLocation: String,
        toLocation: String,
        scheduledTime: Long,
        message: String,
        phoneNumber: String?
    ): Resource<WalkRequest>
    
    suspend fun acceptRequest(requestId: String): Resource<WalkRequest>
    
    suspend fun updateRequestStatus(requestId: String, status: WalkRequestStatus): Resource<Unit>
    
    suspend fun cancelRequest(requestId: String): Resource<Unit>
    
    suspend fun completeWalk(requestId: String): Resource<Unit>
}

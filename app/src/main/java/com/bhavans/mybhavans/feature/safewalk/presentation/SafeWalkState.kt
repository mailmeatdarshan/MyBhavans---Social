package com.bhavans.mybhavans.feature.safewalk.presentation

import com.bhavans.mybhavans.feature.safewalk.domain.model.WalkRequest

data class SafeWalkState(
    val pendingRequests: List<WalkRequest> = emptyList(),
    val myRequests: List<WalkRequest> = emptyList(),
    val myBuddyRequests: List<WalkRequest> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedTab: Int = 0 // 0 = Available, 1 = My Requests, 2 = As Buddy
)

data class CreateWalkRequestState(
    val fromLocation: String = "",
    val toLocation: String = "",
    val scheduledTime: Long = System.currentTimeMillis() + (30 * 60 * 1000), // 30 min from now
    val message: String = "",
    val phoneNumber: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

data class WalkRequestDetailState(
    val request: WalkRequest? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val actionSuccess: Boolean = false
)

sealed class SafeWalkEvent {
    data object LoadRequests : SafeWalkEvent()
    data class SelectTab(val index: Int) : SafeWalkEvent()
    data class AcceptRequest(val requestId: String) : SafeWalkEvent()
    data class CancelRequest(val requestId: String) : SafeWalkEvent()
    data class CompleteWalk(val requestId: String) : SafeWalkEvent()
    data class LoadRequestDetail(val requestId: String) : SafeWalkEvent()
    
    // Create events
    data class UpdateFromLocation(val location: String) : SafeWalkEvent()
    data class UpdateToLocation(val location: String) : SafeWalkEvent()
    data class UpdateScheduledTime(val time: Long) : SafeWalkEvent()
    data class UpdateMessage(val message: String) : SafeWalkEvent()
    data class UpdatePhoneNumber(val phone: String) : SafeWalkEvent()
    data object CreateRequest : SafeWalkEvent()
    data object ClearCreateState : SafeWalkEvent()
    
    data object ClearError : SafeWalkEvent()
    data object ClearActionSuccess : SafeWalkEvent()
}

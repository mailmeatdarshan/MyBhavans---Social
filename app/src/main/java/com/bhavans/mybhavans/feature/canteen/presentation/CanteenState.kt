package com.bhavans.mybhavans.feature.canteen.presentation

import com.bhavans.mybhavans.feature.canteen.domain.model.Canteen
import com.bhavans.mybhavans.feature.canteen.domain.model.CheckIn
import com.bhavans.mybhavans.feature.canteen.domain.model.CrowdLevel

data class CanteenState(
    val canteens: List<Canteen> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class CanteenDetailState(
    val canteen: Canteen? = null,
    val recentCheckIns: List<CheckIn> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class CheckInState(
    val selectedCrowdLevel: CrowdLevel = CrowdLevel.MODERATE,
    val waitTime: Int = 10,
    val comment: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

sealed class CanteenEvent {
    data object LoadCanteens : CanteenEvent()
    data class LoadCanteenDetail(val canteenId: String) : CanteenEvent()
    
    // Check-in events
    data class UpdateCrowdLevel(val level: CrowdLevel) : CanteenEvent()
    data class UpdateWaitTime(val minutes: Int) : CanteenEvent()
    data class UpdateComment(val comment: String) : CanteenEvent()
    data class SubmitCheckIn(val canteenId: String) : CanteenEvent()
    data object ClearCheckInState : CanteenEvent()
    data object ClearError : CanteenEvent()
}

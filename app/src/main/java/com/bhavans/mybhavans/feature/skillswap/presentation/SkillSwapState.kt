package com.bhavans.mybhavans.feature.skillswap.presentation

import com.bhavans.mybhavans.feature.skillswap.domain.model.Skill
import com.bhavans.mybhavans.feature.skillswap.domain.model.SkillCategory
import com.bhavans.mybhavans.feature.skillswap.domain.model.SkillLevel
import com.bhavans.mybhavans.feature.skillswap.domain.model.SkillMatch

data class SkillSwapState(
    val skills: List<Skill> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCategory: SkillCategory? = null,
    val showTeaching: Boolean? = null // null = all, true = teaching, false = learning
)

data class SkillDetailState(
    val skill: Skill? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val matchRequestSent: Boolean = false
)

data class CreateSkillState(
    val title: String = "",
    val description: String = "",
    val category: SkillCategory = SkillCategory.OTHER,
    val level: SkillLevel = SkillLevel.INTERMEDIATE,
    val isTeaching: Boolean = true,
    val lookingFor: List<SkillCategory> = emptyList(),
    val availability: String = "",
    val contactPreference: String = "email",
    val phoneNumber: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

data class MatchRequestsState(
    val sentRequests: List<SkillMatch> = emptyList(),
    val receivedRequests: List<SkillMatch> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class SkillSwapEvent {
    data object LoadSkills : SkillSwapEvent()
    data class FilterByCategory(val category: SkillCategory?) : SkillSwapEvent()
    data class FilterByType(val isTeaching: Boolean?) : SkillSwapEvent()
    data class LoadSkillDetail(val skillId: String) : SkillSwapEvent()
    data class SendMatchRequest(val skillId: String, val message: String) : SkillSwapEvent()
    
    // Create skill events
    data class UpdateTitle(val title: String) : SkillSwapEvent()
    data class UpdateDescription(val description: String) : SkillSwapEvent()
    data class UpdateCategory(val category: SkillCategory) : SkillSwapEvent()
    data class UpdateLevel(val level: SkillLevel) : SkillSwapEvent()
    data class UpdateIsTeaching(val isTeaching: Boolean) : SkillSwapEvent()
    data class ToggleLookingFor(val category: SkillCategory) : SkillSwapEvent()
    data class UpdateAvailability(val availability: String) : SkillSwapEvent()
    data class UpdateContactPreference(val preference: String) : SkillSwapEvent()
    data class UpdatePhoneNumber(val phone: String) : SkillSwapEvent()
    data object CreateSkill : SkillSwapEvent()
    data object ClearCreateState : SkillSwapEvent()
    
    // Match events
    data object LoadMatchRequests : SkillSwapEvent()
    data class UpdateMatchStatus(val matchId: String, val accepted: Boolean) : SkillSwapEvent()
    
    data object ClearError : SkillSwapEvent()
    data object ClearDetail : SkillSwapEvent()
}

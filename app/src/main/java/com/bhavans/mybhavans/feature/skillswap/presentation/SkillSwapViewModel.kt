package com.bhavans.mybhavans.feature.skillswap.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhavans.mybhavans.core.util.Resource
import com.bhavans.mybhavans.feature.skillswap.domain.model.MatchStatus
import com.bhavans.mybhavans.feature.skillswap.domain.model.SkillCategory
import com.bhavans.mybhavans.feature.skillswap.domain.model.SkillLevel
import com.bhavans.mybhavans.feature.skillswap.domain.repository.SkillSwapRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SkillSwapViewModel @Inject constructor(
    private val repository: SkillSwapRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SkillSwapState())
    val state = _state.asStateFlow()

    private val _detailState = MutableStateFlow(SkillDetailState())
    val detailState = _detailState.asStateFlow()

    private val _createState = MutableStateFlow(CreateSkillState())
    val createState = _createState.asStateFlow()

    private val _matchState = MutableStateFlow(MatchRequestsState())
    val matchState = _matchState.asStateFlow()

    init {
        loadSkills()
    }

    fun onEvent(event: SkillSwapEvent) {
        when (event) {
            is SkillSwapEvent.LoadSkills -> loadSkills()
            is SkillSwapEvent.FilterByCategory -> filterByCategory(event.category)
            is SkillSwapEvent.FilterByType -> filterByType(event.isTeaching)
            is SkillSwapEvent.LoadSkillDetail -> loadSkillDetail(event.skillId)
            is SkillSwapEvent.SendMatchRequest -> sendMatchRequest(event.skillId, event.message)
            is SkillSwapEvent.UpdateTitle -> _createState.update { it.copy(title = event.title) }
            is SkillSwapEvent.UpdateDescription -> _createState.update { it.copy(description = event.description) }
            is SkillSwapEvent.UpdateCategory -> _createState.update { it.copy(category = event.category) }
            is SkillSwapEvent.UpdateLevel -> _createState.update { it.copy(level = event.level) }
            is SkillSwapEvent.UpdateIsTeaching -> _createState.update { it.copy(isTeaching = event.isTeaching) }
            is SkillSwapEvent.ToggleLookingFor -> toggleLookingFor(event.category)
            is SkillSwapEvent.UpdateAvailability -> _createState.update { it.copy(availability = event.availability) }
            is SkillSwapEvent.UpdateContactPreference -> _createState.update { it.copy(contactPreference = event.preference) }
            is SkillSwapEvent.UpdatePhoneNumber -> _createState.update { it.copy(phoneNumber = event.phone) }
            is SkillSwapEvent.CreateSkill -> createSkill()
            is SkillSwapEvent.ClearCreateState -> _createState.value = CreateSkillState()
            is SkillSwapEvent.LoadMatchRequests -> loadMatchRequests()
            is SkillSwapEvent.UpdateMatchStatus -> updateMatchStatus(event.matchId, event.accepted)
            is SkillSwapEvent.ClearError -> clearError()
            is SkillSwapEvent.ClearDetail -> _detailState.value = SkillDetailState()
        }
    }

    private fun loadSkills() {
        val category = _state.value.selectedCategory
        val isTeaching = _state.value.showTeaching
        
        repository.getSkills(category, isTeaching).onEach { result ->
            when (result) {
                is Resource.Loading -> _state.update { it.copy(isLoading = true, error = null) }
                is Resource.Success -> _state.update { 
                    it.copy(isLoading = false, skills = result.data ?: emptyList())
                }
                is Resource.Error -> _state.update { 
                    it.copy(isLoading = false, error = result.message)
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun filterByCategory(category: SkillCategory?) {
        _state.update { it.copy(selectedCategory = category) }
        loadSkills()
    }

    private fun filterByType(isTeaching: Boolean?) {
        _state.update { it.copy(showTeaching = isTeaching) }
        loadSkills()
    }

    private fun loadSkillDetail(skillId: String) {
        viewModelScope.launch {
            _detailState.update { it.copy(isLoading = true, error = null, matchRequestSent = false) }
            
            when (val result = repository.getSkill(skillId)) {
                is Resource.Success -> {
                    _detailState.update { it.copy(isLoading = false, skill = result.data) }
                }
                is Resource.Error -> {
                    _detailState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun sendMatchRequest(skillId: String, message: String) {
        viewModelScope.launch {
            _detailState.update { it.copy(isLoading = true) }
            
            when (val result = repository.sendMatchRequest(skillId, message)) {
                is Resource.Success -> {
                    _detailState.update { it.copy(isLoading = false, matchRequestSent = true) }
                }
                is Resource.Error -> {
                    _detailState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun toggleLookingFor(category: SkillCategory) {
        _createState.update { state ->
            val current = state.lookingFor.toMutableList()
            if (current.contains(category)) {
                current.remove(category)
            } else {
                current.add(category)
            }
            state.copy(lookingFor = current)
        }
    }

    private fun createSkill() {
        val state = _createState.value
        
        if (state.title.isBlank()) {
            _createState.update { it.copy(error = "Title is required") }
            return
        }
        
        if (state.description.isBlank()) {
            _createState.update { it.copy(error = "Description is required") }
            return
        }
        
        viewModelScope.launch {
            _createState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = repository.createSkill(
                title = state.title,
                description = state.description,
                category = state.category,
                level = state.level,
                isTeaching = state.isTeaching,
                lookingFor = state.lookingFor,
                availability = state.availability,
                contactPreference = state.contactPreference,
                phoneNumber = state.phoneNumber.takeIf { it.isNotBlank() }
            )) {
                is Resource.Success -> {
                    _createState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                is Resource.Error -> {
                    _createState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun loadMatchRequests() {
        _matchState.update { it.copy(isLoading = true) }
        
        repository.getMyMatchRequests().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _matchState.update { it.copy(sentRequests = result.data ?: emptyList()) }
                }
                is Resource.Error -> {
                    _matchState.update { it.copy(error = result.message) }
                }
                is Resource.Loading -> {}
            }
        }.launchIn(viewModelScope)

        repository.getReceivedMatchRequests().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _matchState.update { it.copy(isLoading = false, receivedRequests = result.data ?: emptyList()) }
                }
                is Resource.Error -> {
                    _matchState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Resource.Loading -> {}
            }
        }.launchIn(viewModelScope)
    }

    private fun updateMatchStatus(matchId: String, accepted: Boolean) {
        viewModelScope.launch {
            val status = if (accepted) MatchStatus.ACCEPTED else MatchStatus.DECLINED
            when (val result = repository.updateMatchStatus(matchId, status)) {
                is Resource.Error -> {
                    _matchState.update { it.copy(error = result.message) }
                }
                else -> {}
            }
        }
    }

    private fun clearError() {
        _state.update { it.copy(error = null) }
        _detailState.update { it.copy(error = null) }
        _createState.update { it.copy(error = null) }
        _matchState.update { it.copy(error = null) }
    }
}

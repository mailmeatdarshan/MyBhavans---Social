package com.bhavans.mybhavans.feature.canteen.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhavans.mybhavans.core.util.Resource
import com.bhavans.mybhavans.feature.canteen.domain.model.CrowdLevel
import com.bhavans.mybhavans.feature.canteen.domain.repository.CanteenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CanteenViewModel @Inject constructor(
    private val repository: CanteenRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CanteenState())
    val state = _state.asStateFlow()

    private val _detailState = MutableStateFlow(CanteenDetailState())
    val detailState = _detailState.asStateFlow()

    private val _checkInState = MutableStateFlow(CheckInState())
    val checkInState = _checkInState.asStateFlow()

    init {
        loadCanteens()
    }

    fun onEvent(event: CanteenEvent) {
        when (event) {
            is CanteenEvent.LoadCanteens -> loadCanteens()
            is CanteenEvent.LoadCanteenDetail -> loadCanteenDetail(event.canteenId)
            is CanteenEvent.UpdateCrowdLevel -> updateCrowdLevel(event.level)
            is CanteenEvent.UpdateWaitTime -> updateWaitTime(event.minutes)
            is CanteenEvent.UpdateComment -> updateComment(event.comment)
            is CanteenEvent.SubmitCheckIn -> submitCheckIn(event.canteenId)
            is CanteenEvent.ClearCheckInState -> clearCheckInState()
            is CanteenEvent.ClearError -> clearError()
        }
    }

    private fun loadCanteens() {
        repository.getCanteens().onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _state.update { it.copy(isLoading = true, error = null) }
                }
                is Resource.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            canteens = result.data ?: emptyList(),
                            error = null
                        )
                    }
                }
                is Resource.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun loadCanteenDetail(canteenId: String) {
        viewModelScope.launch {
            _detailState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = repository.getCanteen(canteenId)) {
                is Resource.Success -> {
                    _detailState.update {
                        it.copy(
                            isLoading = false,
                            canteen = result.data
                        )
                    }
                    // Load recent check-ins
                    loadRecentCheckIns(canteenId)
                }
                is Resource.Error -> {
                    _detailState.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun loadRecentCheckIns(canteenId: String) {
        repository.getRecentCheckIns(canteenId).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _detailState.update {
                        it.copy(recentCheckIns = result.data ?: emptyList())
                    }
                }
                is Resource.Error -> {
                    // Just log, don't show error for check-ins
                }
                is Resource.Loading -> {}
            }
        }.launchIn(viewModelScope)
    }

    private fun updateCrowdLevel(level: CrowdLevel) {
        _checkInState.update { it.copy(selectedCrowdLevel = level) }
    }

    private fun updateWaitTime(minutes: Int) {
        _checkInState.update { it.copy(waitTime = minutes.coerceIn(0, 60)) }
    }

    private fun updateComment(comment: String) {
        _checkInState.update { it.copy(comment = comment) }
    }

    private fun submitCheckIn(canteenId: String) {
        val state = _checkInState.value
        
        viewModelScope.launch {
            _checkInState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = repository.checkIn(
                canteenId = canteenId,
                crowdLevel = state.selectedCrowdLevel,
                waitTime = state.waitTime,
                comment = state.comment
            )) {
                is Resource.Success -> {
                    _checkInState.update { 
                        it.copy(isLoading = false, isSuccess = true)
                    }
                }
                is Resource.Error -> {
                    _checkInState.update { 
                        it.copy(isLoading = false, error = result.message)
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun clearCheckInState() {
        _checkInState.value = CheckInState()
    }

    private fun clearError() {
        _state.update { it.copy(error = null) }
        _detailState.update { it.copy(error = null) }
        _checkInState.update { it.copy(error = null) }
    }
}

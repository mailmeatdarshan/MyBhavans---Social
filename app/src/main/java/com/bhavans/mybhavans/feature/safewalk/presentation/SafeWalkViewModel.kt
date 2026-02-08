package com.bhavans.mybhavans.feature.safewalk.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhavans.mybhavans.core.util.Resource
import com.bhavans.mybhavans.feature.safewalk.domain.repository.SafeWalkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SafeWalkViewModel @Inject constructor(
    private val repository: SafeWalkRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SafeWalkState())
    val state = _state.asStateFlow()

    private val _createState = MutableStateFlow(CreateWalkRequestState())
    val createState = _createState.asStateFlow()

    private val _detailState = MutableStateFlow(WalkRequestDetailState())
    val detailState = _detailState.asStateFlow()

    init {
        loadRequests()
    }

    fun onEvent(event: SafeWalkEvent) {
        when (event) {
            is SafeWalkEvent.LoadRequests -> loadRequests()
            is SafeWalkEvent.SelectTab -> _state.update { it.copy(selectedTab = event.index) }
            is SafeWalkEvent.AcceptRequest -> acceptRequest(event.requestId)
            is SafeWalkEvent.CancelRequest -> cancelRequest(event.requestId)
            is SafeWalkEvent.CompleteWalk -> completeWalk(event.requestId)
            is SafeWalkEvent.LoadRequestDetail -> loadRequestDetail(event.requestId)
            
            is SafeWalkEvent.UpdateFromLocation -> _createState.update { it.copy(fromLocation = event.location) }
            is SafeWalkEvent.UpdateToLocation -> _createState.update { it.copy(toLocation = event.location) }
            is SafeWalkEvent.UpdateScheduledTime -> _createState.update { it.copy(scheduledTime = event.time) }
            is SafeWalkEvent.UpdateMessage -> _createState.update { it.copy(message = event.message) }
            is SafeWalkEvent.UpdatePhoneNumber -> _createState.update { it.copy(phoneNumber = event.phone) }
            is SafeWalkEvent.CreateRequest -> createRequest()
            is SafeWalkEvent.ClearCreateState -> _createState.value = CreateWalkRequestState()
            
            is SafeWalkEvent.ClearError -> clearError()
            is SafeWalkEvent.ClearActionSuccess -> _detailState.update { it.copy(actionSuccess = false) }
        }
    }

    private fun loadRequests() {
        _state.update { it.copy(isLoading = true) }
        
        repository.getPendingRequests().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _state.update { it.copy(pendingRequests = result.data ?: emptyList()) }
                }
                is Resource.Error -> {
                    _state.update { it.copy(error = result.message) }
                }
                is Resource.Loading -> {}
            }
        }.launchIn(viewModelScope)

        repository.getMyRequests().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _state.update { it.copy(myRequests = result.data ?: emptyList()) }
                }
                is Resource.Error -> {}
                is Resource.Loading -> {}
            }
        }.launchIn(viewModelScope)

        repository.getMyBuddyRequests().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _state.update { it.copy(isLoading = false, myBuddyRequests = result.data ?: emptyList()) }
                }
                is Resource.Error -> {
                    _state.update { it.copy(isLoading = false) }
                }
                is Resource.Loading -> {}
            }
        }.launchIn(viewModelScope)
    }

    private fun loadRequestDetail(requestId: String) {
        viewModelScope.launch {
            _detailState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = repository.getRequest(requestId)) {
                is Resource.Success -> {
                    _detailState.update { it.copy(isLoading = false, request = result.data) }
                }
                is Resource.Error -> {
                    _detailState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun createRequest() {
        val state = _createState.value
        
        if (state.fromLocation.isBlank()) {
            _createState.update { it.copy(error = "Please enter starting location") }
            return
        }
        
        if (state.toLocation.isBlank()) {
            _createState.update { it.copy(error = "Please enter destination") }
            return
        }
        
        viewModelScope.launch {
            _createState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = repository.createRequest(
                fromLocation = state.fromLocation,
                toLocation = state.toLocation,
                scheduledTime = state.scheduledTime,
                message = state.message,
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

    private fun acceptRequest(requestId: String) {
        viewModelScope.launch {
            _detailState.update { it.copy(isLoading = true) }
            
            when (val result = repository.acceptRequest(requestId)) {
                is Resource.Success -> {
                    _detailState.update { 
                        it.copy(isLoading = false, request = result.data, actionSuccess = true) 
                    }
                }
                is Resource.Error -> {
                    _detailState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun cancelRequest(requestId: String) {
        viewModelScope.launch {
            when (val result = repository.cancelRequest(requestId)) {
                is Resource.Success -> {
                    _detailState.update { it.copy(actionSuccess = true) }
                }
                is Resource.Error -> {
                    _state.update { it.copy(error = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun completeWalk(requestId: String) {
        viewModelScope.launch {
            when (val result = repository.completeWalk(requestId)) {
                is Resource.Success -> {
                    _detailState.update { it.copy(actionSuccess = true) }
                }
                is Resource.Error -> {
                    _state.update { it.copy(error = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun clearError() {
        _state.update { it.copy(error = null) }
        _createState.update { it.copy(error = null) }
        _detailState.update { it.copy(error = null) }
    }
}

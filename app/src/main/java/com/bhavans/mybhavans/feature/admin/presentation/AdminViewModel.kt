package com.bhavans.mybhavans.feature.admin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhavans.mybhavans.core.util.Resource
import com.bhavans.mybhavans.feature.admin.domain.repository.AdminRepository
import com.bhavans.mybhavans.feature.admin.domain.repository.AdminStats
import com.bhavans.mybhavans.feature.auth.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminState(
    val stats: AdminStats = AdminStats(),
    val users: List<User> = emptyList(),
    val posts: List<Map<String, Any?>> = emptyList(),
    val lostFoundItems: List<Map<String, Any?>> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AdminState())
    val state: StateFlow<AdminState> = _state.asStateFlow()

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = adminRepository.getStats()) {
                is Resource.Success -> {
                    _state.update { it.copy(stats = result.data ?: AdminStats(), isLoading = false) }
                }
                is Resource.Error -> {
                    _state.update { it.copy(error = result.message, isLoading = false) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = adminRepository.getAllUsers()) {
                is Resource.Success -> {
                    _state.update { it.copy(users = result.data ?: emptyList(), isLoading = false) }
                }
                is Resource.Error -> {
                    _state.update { it.copy(error = result.message, isLoading = false) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun updateUserRole(uid: String, role: String) {
        viewModelScope.launch {
            when (val result = adminRepository.updateUserRole(uid, role)) {
                is Resource.Success -> loadUsers()
                is Resource.Error -> {
                    _state.update { it.copy(error = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun deleteUser(uid: String) {
        viewModelScope.launch {
            when (val result = adminRepository.deleteUser(uid)) {
                is Resource.Success -> loadUsers()
                is Resource.Error -> {
                    _state.update { it.copy(error = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun loadPosts() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = adminRepository.getAllPosts()) {
                is Resource.Success -> {
                    _state.update { it.copy(posts = result.data ?: emptyList(), isLoading = false) }
                }
                is Resource.Error -> {
                    _state.update { it.copy(error = result.message, isLoading = false) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun loadLostFoundItems() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = adminRepository.getAllLostFoundItems()) {
                is Resource.Success -> {
                    _state.update { it.copy(lostFoundItems = result.data ?: emptyList(), isLoading = false) }
                }
                is Resource.Error -> {
                    _state.update { it.copy(error = result.message, isLoading = false) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            when (val result = adminRepository.deletePost(postId)) {
                is Resource.Success -> loadPosts()
                is Resource.Error -> {
                    _state.update { it.copy(error = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun deleteLostFoundItem(itemId: String) {
        viewModelScope.launch {
            when (val result = adminRepository.deleteLostFoundItem(itemId)) {
                is Resource.Success -> loadLostFoundItems()
                is Resource.Error -> {
                    _state.update { it.copy(error = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

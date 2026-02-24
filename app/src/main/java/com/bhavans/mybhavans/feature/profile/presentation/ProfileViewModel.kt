package com.bhavans.mybhavans.feature.profile.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhavans.mybhavans.core.util.Resource
import com.bhavans.mybhavans.feature.auth.domain.model.User
import com.bhavans.mybhavans.feature.auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isUploadingPhoto: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false,
    val editDisplayName: String = "",
    val editBio: String = "",
    val editGender: String = "",
    val editDepartment: String = "",
    val editYear: String = "",
    val editInstagram: String = "",
    val editTwitter: String = "",
    val editLinkedIn: String = "",
    val editGitHub: String = "",
    val selectedPhotoUri: Uri? = null
)

sealed class ProfileEvent {
    data class DisplayNameChanged(val name: String) : ProfileEvent()
    data class BioChanged(val bio: String) : ProfileEvent()
    data class GenderChanged(val gender: String) : ProfileEvent()
    data class DepartmentChanged(val department: String) : ProfileEvent()
    data class YearChanged(val year: String) : ProfileEvent()
    data class PhotoSelected(val uri: Uri) : ProfileEvent()
    data class InstagramChanged(val url: String) : ProfileEvent()
    data class TwitterChanged(val url: String) : ProfileEvent()
    data class LinkedInChanged(val url: String) : ProfileEvent()
    data class GitHubChanged(val url: String) : ProfileEvent()
    data object SaveProfile : ProfileEvent()
    data object ClearError : ProfileEvent()
    data object ResetSaveSuccess : ProfileEvent()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private val _userProfileState = MutableStateFlow(UserProfileState())
    val userProfileState: StateFlow<UserProfileState> = _userProfileState.asStateFlow()

    init {
        observeUser()
    }

    private fun observeUser() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _state.update {
                    it.copy(
                        user = user,
                        editDisplayName = if (it.editDisplayName.isEmpty()) user?.displayName ?: "" else it.editDisplayName,
                        editBio = if (it.editBio.isEmpty()) user?.bio ?: "" else it.editBio,
                        editGender = if (it.editGender.isEmpty()) user?.gender ?: "" else it.editGender,
                        editDepartment = if (it.editDepartment.isEmpty()) user?.department ?: "" else it.editDepartment,
                        editYear = if (it.editYear.isEmpty()) (user?.year?.toString() ?: "") else it.editYear,
                        editInstagram = if (it.editInstagram.isEmpty()) user?.socialLinks?.get("instagram") ?: "" else it.editInstagram,
                        editTwitter = if (it.editTwitter.isEmpty()) user?.socialLinks?.get("twitter") ?: "" else it.editTwitter,
                        editLinkedIn = if (it.editLinkedIn.isEmpty()) user?.socialLinks?.get("linkedin") ?: "" else it.editLinkedIn,
                        editGitHub = if (it.editGitHub.isEmpty()) user?.socialLinks?.get("github") ?: "" else it.editGitHub
                    )
                }
            }
        }
    }

    fun initEditFields() {
        val user = _state.value.user
        _state.update {
            it.copy(
                editDisplayName = user?.displayName ?: "",
                editBio = user?.bio ?: "",
                editGender = user?.gender ?: "",
                editDepartment = user?.department ?: "",
                editYear = user?.year?.toString() ?: "",
                editInstagram = user?.socialLinks?.get("instagram") ?: "",
                editTwitter = user?.socialLinks?.get("twitter") ?: "",
                editLinkedIn = user?.socialLinks?.get("linkedin") ?: "",
                editGitHub = user?.socialLinks?.get("github") ?: "",
                selectedPhotoUri = null,
                saveSuccess = false
            )
        }
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.DisplayNameChanged -> {
                _state.update { it.copy(editDisplayName = event.name) }
            }
            is ProfileEvent.BioChanged -> {
                _state.update { it.copy(editBio = event.bio) }
            }
            is ProfileEvent.GenderChanged -> {
                _state.update { it.copy(editGender = event.gender) }
            }
            is ProfileEvent.DepartmentChanged -> {
                _state.update { it.copy(editDepartment = event.department) }
            }
            is ProfileEvent.YearChanged -> {
                _state.update { it.copy(editYear = event.year) }
            }
            is ProfileEvent.PhotoSelected -> {
                _state.update { it.copy(selectedPhotoUri = event.uri) }
            }
            is ProfileEvent.InstagramChanged -> {
                _state.update { it.copy(editInstagram = event.url) }
            }
            is ProfileEvent.TwitterChanged -> {
                _state.update { it.copy(editTwitter = event.url) }
            }
            is ProfileEvent.LinkedInChanged -> {
                _state.update { it.copy(editLinkedIn = event.url) }
            }
            is ProfileEvent.GitHubChanged -> {
                _state.update { it.copy(editGitHub = event.url) }
            }
            is ProfileEvent.SaveProfile -> saveProfile()
            is ProfileEvent.ClearError -> {
                _state.update { it.copy(error = null) }
            }
            is ProfileEvent.ResetSaveSuccess -> {
                _state.update { it.copy(saveSuccess = false) }
            }
        }
    }

    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            _userProfileState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.getUserProfile(userId)) {
                is Resource.Success -> {
                    val isFollowing = authRepository.isFollowing(userId)
                    _userProfileState.update {
                        it.copy(
                            isLoading = false,
                            user = result.data,
                            isFollowing = isFollowing
                        )
                    }
                }
                is Resource.Error -> {
                    _userProfileState.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun followUser(userId: String) {
        viewModelScope.launch {
            _userProfileState.update { it.copy(isFollowLoading = true) }
            when (authRepository.followUser(userId)) {
                is Resource.Success -> {
                    // Refresh profile to get updated counts
                    loadUserProfile(userId)
                    _userProfileState.update { it.copy(isFollowLoading = false, isFollowing = true) }
                }
                is Resource.Error -> {
                    _userProfileState.update { it.copy(isFollowLoading = false) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun unfollowUser(userId: String) {
        viewModelScope.launch {
            _userProfileState.update { it.copy(isFollowLoading = true) }
            when (authRepository.unfollowUser(userId)) {
                is Resource.Success -> {
                    loadUserProfile(userId)
                    _userProfileState.update { it.copy(isFollowLoading = false, isFollowing = false) }
                }
                is Resource.Error -> {
                    _userProfileState.update { it.copy(isFollowLoading = false) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun saveProfile() {
        val currentUser = _state.value.user ?: return
        val state = _state.value

        if (state.editDisplayName.isBlank()) {
            _state.update { it.copy(error = "Display name cannot be empty") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }

            // Upload photo first if selected
            var photoUrl = currentUser.photoUrl
            state.selectedPhotoUri?.let { uri ->
                _state.update { it.copy(isUploadingPhoto = true) }
                when (val uploadResult = authRepository.uploadProfilePhoto(uri)) {
                    is Resource.Success -> {
                        photoUrl = uploadResult.data ?: photoUrl
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isSaving = false,
                                isUploadingPhoto = false,
                                error = uploadResult.message
                            )
                        }
                        return@launch
                    }
                    is Resource.Loading -> {}
                }
                _state.update { it.copy(isUploadingPhoto = false) }
            }

            // Build social links map
            val socialLinks = mutableMapOf<String, String>()
            if (state.editInstagram.isNotBlank()) socialLinks["instagram"] = state.editInstagram.trim()
            if (state.editTwitter.isNotBlank()) socialLinks["twitter"] = state.editTwitter.trim()
            if (state.editLinkedIn.isNotBlank()) socialLinks["linkedin"] = state.editLinkedIn.trim()
            if (state.editGitHub.isNotBlank()) socialLinks["github"] = state.editGitHub.trim()

            // Update profile
            val updatedUser = currentUser.copy(
                displayName = state.editDisplayName.trim(),
                bio = state.editBio.trim(),
                gender = state.editGender,
                department = state.editDepartment.trim(),
                year = state.editYear.toIntOrNull(),
                photoUrl = photoUrl,
                socialLinks = socialLinks
            )

            when (val result = authRepository.updateProfile(updatedUser)) {
                is Resource.Success -> {
                    _state.update {
                        it.copy(
                            isSaving = false,
                            saveSuccess = true,
                            selectedPhotoUri = null
                        )
                    }
                }
                is Resource.Error -> {
                    _state.update {
                        it.copy(
                            isSaving = false,
                            error = result.message
                        )
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }
}

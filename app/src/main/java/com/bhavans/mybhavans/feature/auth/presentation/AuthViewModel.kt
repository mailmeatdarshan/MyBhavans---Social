package com.bhavans.mybhavans.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhavans.mybhavans.core.util.Resource
import com.bhavans.mybhavans.feature.auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState(isLoggedIn = authRepository.isUserLoggedIn()))
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _displayName = MutableStateFlow("")
    val displayName: StateFlow<String> = _displayName.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _authState.update {
                    it.copy(
                        isLoggedIn = user != null,
                        user = user
                    )
                }
            }
        }
    }

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.EmailChanged -> {
                _email.value = event.email
                _authState.update {
                    it.copy(
                        isEmailValid = authRepository.isValidCollegeEmail(event.email) || event.email.isEmpty(),
                        error = null
                    )
                }
            }
            is AuthEvent.PasswordChanged -> {
                _password.value = event.password
                _authState.update {
                    it.copy(
                        isPasswordValid = event.password.length >= 6 || event.password.isEmpty(),
                        error = null
                    )
                }
            }
            is AuthEvent.DisplayNameChanged -> {
                _displayName.value = event.name
            }
            is AuthEvent.SignIn -> signIn()
            is AuthEvent.SignUp -> signUp()
            is AuthEvent.SignOut -> signOut()
            is AuthEvent.ClearError -> {
                _authState.update { it.copy(error = null) }
            }
            is AuthEvent.SendVerificationEmail -> sendVerificationEmail()
        }
    }

    private fun signIn() {
        val currentEmail = _email.value
        val currentPassword = _password.value

        if (currentEmail.isBlank() || currentPassword.isBlank()) {
            _authState.update { it.copy(error = "Please fill in all fields") }
            return
        }

        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = authRepository.signIn(currentEmail, currentPassword)) {
                is Resource.Success -> {
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            signInSuccess = true,
                            user = result.data
                        )
                    }
                    clearFields()
                }
                is Resource.Error -> {
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
                is Resource.Loading -> {
                    _authState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    private fun signUp() {
        val currentEmail = _email.value
        val currentPassword = _password.value
        val currentDisplayName = _displayName.value

        if (currentEmail.isBlank() || currentPassword.isBlank() || currentDisplayName.isBlank()) {
            _authState.update { it.copy(error = "Please fill in all fields") }
            return
        }

        if (currentPassword.length < 6) {
            _authState.update { it.copy(error = "Password must be at least 6 characters") }
            return
        }

        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = authRepository.signUp(currentEmail, currentPassword, currentDisplayName)) {
                is Resource.Success -> {
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            signUpSuccess = true,
                            user = result.data
                        )
                    }
                    clearFields()
                }
                is Resource.Error -> {
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
                is Resource.Loading -> {
                    _authState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _authState.update {
                AuthState(isLoggedIn = false)
            }
        }
    }

    private fun sendVerificationEmail() {
        viewModelScope.launch {
            when (val result = authRepository.sendEmailVerification()) {
                is Resource.Success -> {
                    _authState.update { it.copy(error = null) }
                }
                is Resource.Error -> {
                    _authState.update { it.copy(error = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun clearFields() {
        _email.value = ""
        _password.value = ""
        _displayName.value = ""
    }
}

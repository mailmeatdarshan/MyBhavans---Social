package com.bhavans.mybhavans.feature.auth.presentation

import com.bhavans.mybhavans.feature.auth.domain.model.User

data class AuthState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val isEmailValid: Boolean = true,
    val isPasswordValid: Boolean = true,
    val signUpSuccess: Boolean = false,
    val signInSuccess: Boolean = false
)

sealed class AuthEvent {
    data class EmailChanged(val email: String) : AuthEvent()
    data class PasswordChanged(val password: String) : AuthEvent()
    data class DisplayNameChanged(val name: String) : AuthEvent()
    data object SignIn : AuthEvent()
    data object SignUp : AuthEvent()
    data object SignOut : AuthEvent()
    data object ClearError : AuthEvent()
    data object SendVerificationEmail : AuthEvent()
}

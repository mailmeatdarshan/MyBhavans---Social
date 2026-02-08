package com.bhavans.mybhavans.feature.auth.domain.repository

import com.bhavans.mybhavans.core.util.Resource
import com.bhavans.mybhavans.feature.auth.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    
    suspend fun signUp(email: String, password: String, displayName: String): Resource<User>
    
    suspend fun signIn(email: String, password: String): Resource<User>
    
    suspend fun signOut()
    
    suspend fun sendEmailVerification(): Resource<Unit>
    
    suspend fun sendPasswordResetEmail(email: String): Resource<Unit>
    
    fun isValidCollegeEmail(email: String): Boolean
    
    fun isUserLoggedIn(): Boolean
}

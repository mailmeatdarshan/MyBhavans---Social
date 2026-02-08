package com.bhavans.mybhavans.feature.auth.data.repository

import com.bhavans.mybhavans.core.util.Constants
import com.bhavans.mybhavans.core.util.Resource
import com.bhavans.mybhavans.feature.auth.domain.model.User
import com.bhavans.mybhavans.feature.auth.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override val currentUser: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                trySend(
                    User(
                        uid = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        displayName = firebaseUser.displayName ?: "",
                        photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                        isVerified = firebaseUser.isEmailVerified
                    )
                )
            } else {
                trySend(null)
            }
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signUp(
        email: String,
        password: String,
        displayName: String
    ): Resource<User> {
        return try {
            if (!isValidCollegeEmail(email)) {
                return Resource.Error("Please use your college email (${Constants.COLLEGE_EMAIL_DOMAIN})")
            }

            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Resource.Error("Sign up failed")

            // Update display name
            val profileUpdates = userProfileChangeRequest {
                this.displayName = displayName
            }
            firebaseUser.updateProfile(profileUpdates).await()

            // Create user document in Firestore
            val user = User(
                uid = firebaseUser.uid,
                email = email,
                displayName = displayName,
                role = Constants.ROLE_STUDENT,
                isVerified = false
            )
            
            firestore.collection(Constants.USERS_COLLECTION)
                .document(firebaseUser.uid)
                .set(user.toMap())
                .await()

            // Send verification email
            firebaseUser.sendEmailVerification().await()

            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Sign up failed")
        }
    }

    override suspend fun signIn(email: String, password: String): Resource<User> {
        return try {
            if (!isValidCollegeEmail(email)) {
                return Resource.Error("Please use your college email (${Constants.COLLEGE_EMAIL_DOMAIN})")
            }

            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Resource.Error("Sign in failed")

            val user = User(
                uid = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                displayName = firebaseUser.displayName ?: "",
                photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                isVerified = firebaseUser.isEmailVerified
            )

            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Sign in failed")
        }
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun sendEmailVerification(): Resource<Unit> {
        return try {
            auth.currentUser?.sendEmailVerification()?.await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to send verification email")
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Resource<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to send password reset email")
        }
    }

    override fun isValidCollegeEmail(email: String): Boolean {
        return email.lowercase().endsWith(Constants.COLLEGE_EMAIL_DOMAIN)
    }

    override fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    private fun User.toMap(): Map<String, Any?> = mapOf(
        "uid" to uid,
        "email" to email,
        "displayName" to displayName,
        "photoUrl" to photoUrl,
        "department" to department,
        "year" to year,
        "role" to role,
        "bio" to bio,
        "skills" to skills,
        "isVerified" to isVerified,
        "createdAt" to com.google.firebase.Timestamp.now(),
        "lastActiveAt" to com.google.firebase.Timestamp.now()
    )
}

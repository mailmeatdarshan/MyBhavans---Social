package com.bhavans.mybhavans.feature.auth.data.repository

import android.net.Uri
import com.bhavans.mybhavans.core.util.Constants
import com.bhavans.mybhavans.core.util.Resource
import com.bhavans.mybhavans.feature.auth.domain.model.User
import com.bhavans.mybhavans.feature.auth.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : AuthRepository {

    override val currentUser: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                firestore.collection(Constants.USERS_COLLECTION)
                    .document(firebaseUser.uid)
                    .addSnapshotListener { snapshot, _ ->
                        if (snapshot != null && snapshot.exists()) {
                            val user = User(
                                uid = firebaseUser.uid,
                                email = snapshot.getString("email") ?: firebaseUser.email ?: "",
                                displayName = snapshot.getString("displayName") ?: firebaseUser.displayName ?: "",
                                photoUrl = snapshot.getString("photoUrl") ?: firebaseUser.photoUrl?.toString() ?: "",
                                department = snapshot.getString("department") ?: "",
                                year = snapshot.getLong("year")?.toInt(),
                                role = snapshot.getString("role") ?: Constants.ROLE_STUDENT,
                                bio = snapshot.getString("bio") ?: "",
                                gender = snapshot.getString("gender") ?: "",
                                skills = (snapshot.get("skills") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                                isVerified = snapshot.getBoolean("isVerified") ?: firebaseUser.isEmailVerified,
                                postsCount = snapshot.getLong("postsCount")?.toInt() ?: 0,
                                followersCount = snapshot.getLong("followersCount")?.toInt() ?: 0,
                                followingCount = snapshot.getLong("followingCount")?.toInt() ?: 0
                            )
                            trySend(user)
                        } else {
                            trySend(
                                User(
                                    uid = firebaseUser.uid,
                                    email = firebaseUser.email ?: "",
                                    displayName = firebaseUser.displayName ?: "",
                                    photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                                    isVerified = firebaseUser.isEmailVerified
                                )
                            )
                        }
                    }
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

            val profileUpdates = userProfileChangeRequest {
                this.displayName = displayName
            }
            firebaseUser.updateProfile(profileUpdates).await()

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

    override suspend fun updateProfile(user: User): Resource<Unit> {
        return try {
            val currentUser = auth.currentUser ?: return Resource.Error("Not logged in")
            
            // Update Firebase Auth profile
            val profileUpdates = userProfileChangeRequest {
                displayName = user.displayName
                if (user.photoUrl.isNotEmpty()) {
                    photoUri = Uri.parse(user.photoUrl)
                }
            }
            currentUser.updateProfile(profileUpdates).await()

            // Update Firestore document
            firestore.collection(Constants.USERS_COLLECTION)
                .document(currentUser.uid)
                .update(user.toUpdateMap())
                .await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update profile")
        }
    }

    override suspend fun uploadProfilePhoto(imageUri: Uri): Resource<String> {
        return try {
            val currentUser = auth.currentUser ?: return Resource.Error("Not logged in")
            val ref = storage.reference
                .child(Constants.PROFILE_IMAGES_PATH)
                .child("${currentUser.uid}.jpg")

            ref.putFile(imageUri).await()
            val downloadUrl = ref.downloadUrl.await().toString()

            // Update the user's photoUrl in Firestore
            firestore.collection(Constants.USERS_COLLECTION)
                .document(currentUser.uid)
                .update("photoUrl", downloadUrl)
                .await()

            // Update Firebase Auth photo
            val profileUpdates = userProfileChangeRequest {
                photoUri = Uri.parse(downloadUrl)
            }
            currentUser.updateProfile(profileUpdates).await()

            Resource.Success(downloadUrl)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to upload photo")
        }
    }

    override suspend fun getUserProfile(uid: String): Resource<User> {
        return try {
            val snapshot = firestore.collection(Constants.USERS_COLLECTION)
                .document(uid)
                .get()
                .await()

            if (snapshot.exists()) {
                val user = User(
                    uid = uid,
                    email = snapshot.getString("email") ?: "",
                    displayName = snapshot.getString("displayName") ?: "",
                    photoUrl = snapshot.getString("photoUrl") ?: "",
                    department = snapshot.getString("department") ?: "",
                    year = snapshot.getLong("year")?.toInt(),
                    role = snapshot.getString("role") ?: Constants.ROLE_STUDENT,
                    bio = snapshot.getString("bio") ?: "",
                    gender = snapshot.getString("gender") ?: "",
                    skills = (snapshot.get("skills") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                    isVerified = snapshot.getBoolean("isVerified") ?: false,
                    postsCount = snapshot.getLong("postsCount")?.toInt() ?: 0,
                    followersCount = snapshot.getLong("followersCount")?.toInt() ?: 0,
                    followingCount = snapshot.getLong("followingCount")?.toInt() ?: 0
                )
                Resource.Success(user)
            } else {
                Resource.Error("User not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get user profile")
        }
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
        "gender" to gender,
        "skills" to skills,
        "isVerified" to isVerified,
        "postsCount" to postsCount,
        "followersCount" to followersCount,
        "followingCount" to followingCount,
        "createdAt" to com.google.firebase.Timestamp.now(),
        "lastActiveAt" to com.google.firebase.Timestamp.now()
    )

    private fun User.toUpdateMap(): Map<String, Any?> = mapOf(
        "displayName" to displayName,
        "photoUrl" to photoUrl,
        "department" to department,
        "year" to year,
        "bio" to bio,
        "gender" to gender,
        "skills" to skills,
        "lastActiveAt" to com.google.firebase.Timestamp.now()
    )
}

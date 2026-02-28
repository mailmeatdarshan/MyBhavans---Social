package com.bhavans.mybhavans.feature.admin.data.repository

import com.bhavans.mybhavans.core.util.Constants
import com.bhavans.mybhavans.core.util.Resource
import com.bhavans.mybhavans.feature.admin.domain.repository.AdminRepository
import com.bhavans.mybhavans.feature.admin.domain.repository.AdminStats
import com.bhavans.mybhavans.feature.auth.domain.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AdminRepository {

    override suspend fun getStats(): Resource<AdminStats> {
        return try {
            val users = firestore.collection(Constants.USERS_COLLECTION).get().await()
            val posts = firestore.collection(Constants.POSTS_COLLECTION).get().await()
            val lostFound = firestore.collection("lostfound").get().await()
            val skills = firestore.collection("skills").get().await()
            val safeWalk = firestore.collection(Constants.SAFE_WALK_COLLECTION).get().await()

            Resource.Success(
                AdminStats(
                    totalUsers = users.size(),
                    totalPosts = posts.size(),
                    totalLostFoundItems = lostFound.size(),
                    totalSkillListings = skills.size(),
                    totalSafeWalkRequests = safeWalk.size()
                )
            )
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get stats")
        }
    }

    override suspend fun getAllUsers(): Resource<List<User>> {
        return try {
            val snapshot = firestore.collection(Constants.USERS_COLLECTION)
                .get()
                .await()

            val users = snapshot.documents.mapNotNull { doc ->
                User(
                    uid = doc.id,
                    email = doc.getString("email") ?: "",
                    displayName = doc.getString("displayName") ?: "",
                    photoUrl = doc.getString("photoUrl") ?: "",
                    department = doc.getString("department") ?: "",
                    year = doc.getLong("year")?.toInt(),
                    role = doc.getString("role") ?: Constants.ROLE_STUDENT,
                    bio = doc.getString("bio") ?: "",
                    gender = doc.getString("gender") ?: "",
                    isVerified = doc.getBoolean("isVerified") ?: false
                )
            }
            Resource.Success(users)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get users")
        }
    }

    override suspend fun updateUserRole(uid: String, role: String): Resource<Unit> {
        return try {
            firestore.collection(Constants.USERS_COLLECTION)
                .document(uid)
                .update("role", role)
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update user role")
        }
    }

    override suspend fun updateUserVerification(uid: String, isVerified: Boolean): Resource<Unit> {
        return try {
            firestore.collection(Constants.USERS_COLLECTION)
                .document(uid)
                .update("isVerified", isVerified)
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update verification status")
        }
    }

    override suspend fun deleteUser(uid: String): Resource<Unit> {
        return try {
            firestore.collection(Constants.USERS_COLLECTION)
                .document(uid)
                .delete()
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete user")
        }
    }

    override suspend fun deletePost(postId: String): Resource<Unit> {
        return try {
            firestore.collection(Constants.POSTS_COLLECTION)
                .document(postId)
                .delete()
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete post")
        }
    }

    override suspend fun deleteLostFoundItem(itemId: String): Resource<Unit> {
        return try {
            firestore.collection(Constants.LOST_FOUND_COLLECTION)
                .document(itemId)
                .delete()
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete item")
        }
    }

    override suspend fun getAllPosts(): Resource<List<Map<String, Any?>>> {
        return try {
            val snapshot = firestore.collection(Constants.POSTS_COLLECTION)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            val posts = snapshot.documents.map { doc ->
                val data = doc.data?.toMutableMap() ?: mutableMapOf()
                data["id"] = doc.id
                data
            }
            Resource.Success(posts)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get posts")
        }
    }

    override suspend fun getAllLostFoundItems(): Resource<List<Map<String, Any?>>> {
        return try {
            val snapshot = firestore.collection(Constants.LOST_FOUND_COLLECTION)
                .get()
                .await()

            val items = snapshot.documents.map { doc ->
                val data = doc.data?.toMutableMap() ?: mutableMapOf()
                data["id"] = doc.id
                data
            }
            Resource.Success(items)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get items")
        }
    }
}

package com.bhavans.mybhavans.feature.admin.domain.repository

import com.bhavans.mybhavans.core.util.Resource
import com.bhavans.mybhavans.feature.auth.domain.model.User

data class AdminStats(
    val totalUsers: Int = 0,
    val totalPosts: Int = 0,
    val totalLostFoundItems: Int = 0,
    val totalSkillListings: Int = 0,
    val totalSafeWalkRequests: Int = 0
)

interface AdminRepository {
    suspend fun getStats(): Resource<AdminStats>
    suspend fun getAllUsers(): Resource<List<User>>
    suspend fun updateUserRole(uid: String, role: String): Resource<Unit>
    suspend fun updateUserVerification(uid: String, isVerified: Boolean): Resource<Unit>
    suspend fun deleteUser(uid: String): Resource<Unit>
    suspend fun deletePost(postId: String): Resource<Unit>
    suspend fun deleteLostFoundItem(itemId: String): Resource<Unit>
    suspend fun getAllPosts(): Resource<List<Map<String, Any?>>>
    suspend fun getAllLostFoundItems(): Resource<List<Map<String, Any?>>>
}

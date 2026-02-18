package com.bhavans.mybhavans.feature.auth.domain.model

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val department: String = "",
    val year: Int? = null,
    val role: String = "student",
    val bio: String = "",
    val gender: String = "",
    val skills: List<String> = emptyList(),
    val isVerified: Boolean = false,
    val postsCount: Int = 0,
    val followersCount: Int = 0,
    val followingCount: Int = 0
)

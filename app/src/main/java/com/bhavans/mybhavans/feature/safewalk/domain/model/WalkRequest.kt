package com.bhavans.mybhavans.feature.safewalk.domain.model

enum class WalkRequestStatus {
    PENDING,
    ACCEPTED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

data class WalkRequest(
    val id: String = "",
    val requesterId: String = "",
    val requesterName: String = "",
    val requesterEmail: String = "",
    val requesterPhone: String? = null,
    val buddyId: String? = null,
    val buddyName: String? = null,
    val buddyEmail: String? = null,
    val buddyPhone: String? = null,
    val fromLocation: String = "",
    val toLocation: String = "",
    val scheduledTime: Long = System.currentTimeMillis(),
    val message: String = "",
    val status: WalkRequestStatus = WalkRequestStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class SafeWalkStats(
    val totalWalks: Int = 0,
    val walksAsRequester: Int = 0,
    val walksAsBuddy: Int = 0
)

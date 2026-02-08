package com.bhavans.mybhavans.feature.skillswap.domain.model

enum class SkillCategory {
    PROGRAMMING,
    DESIGN,
    MUSIC,
    LANGUAGES,
    ACADEMICS,
    SPORTS,
    ART,
    PHOTOGRAPHY,
    COOKING,
    OTHER
}

enum class SkillLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
    EXPERT
}

data class Skill(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val userProfileUrl: String? = null,
    val title: String = "",
    val description: String = "",
    val category: SkillCategory = SkillCategory.OTHER,
    val level: SkillLevel = SkillLevel.INTERMEDIATE,
    val isTeaching: Boolean = true, // true = offering to teach, false = wanting to learn
    val lookingFor: List<SkillCategory> = emptyList(), // Skills the user wants to learn in exchange
    val availability: String = "", // e.g., "Weekends", "Evenings after 6PM"
    val isActive: Boolean = true,
    val contactPreference: String = "email", // email, phone, both
    val phoneNumber: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class SkillMatch(
    val id: String = "",
    val requesterId: String = "",
    val requesterName: String = "",
    val providerId: String = "",
    val providerName: String = "",
    val skillId: String = "",
    val skillTitle: String = "",
    val message: String = "",
    val status: MatchStatus = MatchStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)

enum class MatchStatus {
    PENDING,
    ACCEPTED,
    DECLINED
}

package com.bhavans.mybhavans.feature.canteen.domain.model

enum class CrowdLevel {
    EMPTY,      // 0-20%
    LOW,        // 20-40%
    MODERATE,   // 40-60%
    BUSY,       // 60-80%
    CROWDED     // 80-100%
}

data class Canteen(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val imageUrl: String? = null,
    val currentCrowdLevel: CrowdLevel = CrowdLevel.MODERATE,
    val crowdPercentage: Int = 50,
    val checkInsLast30Min: Int = 0,
    val isOpen: Boolean = true,
    val openTime: String = "08:00",
    val closeTime: String = "18:00",
    val specialItems: List<String> = emptyList(),
    val avgWaitTime: Int = 10, // in minutes
    val lastUpdated: Long = System.currentTimeMillis(),
    val menuItems: List<MenuItem> = emptyList(),
    val totalSeats: Int = 100,
    val occupiedSeats: Int = 0
)

data class MenuItem(
    val name: String = "",
    val price: Double = 0.0,
    val category: String = "General",
    val isAvailable: Boolean = true
)

data class CheckIn(
    val id: String = "",
    val canteenId: String = "",
    val userId: String = "",
    val userName: String = "",
    val crowdLevel: CrowdLevel = CrowdLevel.MODERATE,
    val waitTime: Int = 10, // in minutes
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

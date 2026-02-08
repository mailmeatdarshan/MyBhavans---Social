package com.bhavans.mybhavans.feature.lostfound.domain.model

enum class LostFoundType {
    LOST,
    FOUND
}

enum class LostFoundCategory {
    ELECTRONICS,
    DOCUMENTS,
    ACCESSORIES,
    BOOKS,
    CLOTHING,
    KEYS,
    WALLET,
    OTHER
}

data class LostFoundItem(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val type: LostFoundType = LostFoundType.LOST,
    val category: LostFoundCategory = LostFoundCategory.OTHER,
    val location: String = "",
    val imageUrl: String? = null,
    val authorId: String = "",
    val authorName: String = "",
    val authorEmail: String = "",
    val contactNumber: String = "",
    val isResolved: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

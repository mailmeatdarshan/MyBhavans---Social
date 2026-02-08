package com.bhavans.mybhavans.feature.feed.domain.model

data class Post(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorPhotoUrl: String = "",
    val content: String = "",
    val imageUrl: String = "",
    val category: PostCategory = PostCategory.GENERAL,
    val likes: List<String> = emptyList(),
    val commentCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val likeCount: Int get() = likes.size
    
    fun isLikedBy(userId: String): Boolean = likes.contains(userId)
}

enum class PostCategory(val displayName: String) {
    GENERAL("General"),
    ANNOUNCEMENT("Announcement"),
    EVENT("Event"),
    QUESTION("Question"),
    CLUB("Club Activity"),
    MEME("Meme/Fun")
}

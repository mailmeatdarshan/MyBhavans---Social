package com.bhavans.mybhavans.feature.feed.domain.model

data class Comment(
    val id: String = "",
    val postId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorPhotoUrl: String = "",
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

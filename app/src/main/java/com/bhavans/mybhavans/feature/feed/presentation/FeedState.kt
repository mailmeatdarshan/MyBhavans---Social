package com.bhavans.mybhavans.feature.feed.presentation

import android.net.Uri
import com.bhavans.mybhavans.feature.feed.domain.model.Comment
import com.bhavans.mybhavans.feature.feed.domain.model.Post
import com.bhavans.mybhavans.feature.feed.domain.model.PostCategory

data class FeedState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCategory: PostCategory? = null,
    
    // Create/Edit Post State
    val isCreatingPost: Boolean = false,
    val createPostContent: String = "",
    val createPostCategory: PostCategory = PostCategory.GENERAL,
    val createPostImageUri: Uri? = null,
    val createPostError: String? = null,
    val createPostSuccess: Boolean = false,
    
    // Post Detail State
    val selectedPost: Post? = null,
    val comments: List<Comment> = emptyList(),
    val isLoadingComments: Boolean = false,
    val commentText: String = "",
    val isAddingComment: Boolean = false
)

sealed class FeedEvent {
    data class SelectCategory(val category: PostCategory?) : FeedEvent()
    data class LikePost(val postId: String) : FeedEvent()
    data class UnlikePost(val postId: String) : FeedEvent()
    data class DeletePost(val postId: String) : FeedEvent()
    
    // Create Post Events
    data class UpdatePostContent(val content: String) : FeedEvent()
    data class UpdatePostCategory(val category: PostCategory) : FeedEvent()
    data class UpdatePostImage(val uri: Uri?) : FeedEvent()
    data object CreatePost : FeedEvent()
    data object ClearCreatePostState : FeedEvent()
    
    // Post Detail Events
    data class LoadPostDetail(val postId: String) : FeedEvent()
    data class UpdateCommentText(val text: String) : FeedEvent()
    data object AddComment : FeedEvent()
    data class DeleteComment(val commentId: String) : FeedEvent()
    data object ClearPostDetail : FeedEvent()
    
    data object ClearError : FeedEvent()
}

package com.bhavans.mybhavans.feature.feed.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhavans.mybhavans.core.util.Resource
import com.bhavans.mybhavans.feature.activity.domain.repository.ActivityRepository
import com.bhavans.mybhavans.feature.feed.domain.repository.FeedRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val feedRepository: FeedRepository,
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FeedState())
    val state: StateFlow<FeedState> = _state.asStateFlow()

    init {
        loadFeed()
    }

    fun onEvent(event: FeedEvent) {
        when (event) {
            is FeedEvent.SelectCategory -> selectCategory(event.category)
            is FeedEvent.LikePost -> likePost(event.postId)
            is FeedEvent.UnlikePost -> unlikePost(event.postId)
            is FeedEvent.DeletePost -> deletePost(event.postId)
            
            is FeedEvent.UpdatePostContent -> updatePostContent(event.content)
            is FeedEvent.UpdatePostCategory -> updatePostCategory(event.category)
            is FeedEvent.UpdatePostImage -> updatePostImage(event.uri)
            FeedEvent.CreatePost -> createPost()
            FeedEvent.ClearCreatePostState -> clearCreatePostState()
            
            is FeedEvent.LoadPostDetail -> loadPostDetail(event.postId)
            is FeedEvent.UpdateCommentText -> updateCommentText(event.text)
            FeedEvent.AddComment -> addComment()
            is FeedEvent.DeleteComment -> deleteComment(event.commentId)
            FeedEvent.ClearPostDetail -> clearPostDetail()
            
            FeedEvent.ClearError -> clearError()
        }
    }

    private fun loadFeed() {
        feedRepository.getFeed(_state.value.selectedCategory)
            .onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                posts = result.data ?: emptyList(),
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun selectCategory(category: com.bhavans.mybhavans.feature.feed.domain.model.PostCategory?) {
        _state.update { it.copy(selectedCategory = category) }
        loadFeed()
    }

    private fun likePost(postId: String) {
        viewModelScope.launch {
            feedRepository.likePost(postId)
            // Create notification for post author
            val post = _state.value.posts.find { it.id == postId }
            if (post != null) {
                val user = FirebaseAuth.getInstance().currentUser
                activityRepository.createNotification(
                    targetUserId = post.authorId,
                    type = "LIKE",
                    actorName = user?.displayName ?: "Someone",
                    actorPhotoUrl = user?.photoUrl?.toString() ?: "",
                    postId = postId,
                    message = "liked your post"
                )
            }
        }
    }

    private fun unlikePost(postId: String) {
        viewModelScope.launch {
            feedRepository.unlikePost(postId)
        }
    }

    private fun deletePost(postId: String) {
        viewModelScope.launch {
            when (val result = feedRepository.deletePost(postId)) {
                is Resource.Error -> {
                    _state.update { it.copy(error = result.message) }
                }
                else -> { /* Success is handled by real-time listener */ }
            }
        }
    }

    private fun updatePostContent(content: String) {
        _state.update { it.copy(createPostContent = content) }
    }

    private fun updatePostCategory(category: com.bhavans.mybhavans.feature.feed.domain.model.PostCategory) {
        _state.update { it.copy(createPostCategory = category) }
    }

    private fun updatePostImage(uri: android.net.Uri?) {
        _state.update { it.copy(createPostImageUri = uri) }
    }

    private fun createPost() {
        val content = _state.value.createPostContent.trim()
        if (content.isEmpty()) {
            _state.update { it.copy(createPostError = "Post content cannot be empty") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isCreatingPost = true, createPostError = null) }
            
            when (val result = feedRepository.createPost(
                content = content,
                category = _state.value.createPostCategory,
                imageUri = _state.value.createPostImageUri
            )) {
                is Resource.Success -> {
                    _state.update { 
                        it.copy(
                            isCreatingPost = false,
                            createPostSuccess = true,
                            createPostContent = "",
                            createPostCategory = com.bhavans.mybhavans.feature.feed.domain.model.PostCategory.GENERAL,
                            createPostImageUri = null
                        )
                    }
                }
                is Resource.Error -> {
                    _state.update { 
                        it.copy(
                            isCreatingPost = false,
                            createPostError = result.message
                        )
                    }
                }
                is Resource.Loading -> { /* Handled above */ }
            }
        }
    }

    private fun clearCreatePostState() {
        _state.update { 
            it.copy(
                createPostContent = "",
                createPostCategory = com.bhavans.mybhavans.feature.feed.domain.model.PostCategory.GENERAL,
                createPostImageUri = null,
                createPostError = null,
                createPostSuccess = false,
                isCreatingPost = false
            )
        }
    }

    private fun loadPostDetail(postId: String) {
        viewModelScope.launch {
            when (val result = feedRepository.getPost(postId)) {
                is Resource.Success -> {
                    _state.update { it.copy(selectedPost = result.data) }
                }
                is Resource.Error -> {
                    _state.update { it.copy(error = result.message) }
                }
                is Resource.Loading -> { /* Not expected for single fetch */ }
            }
        }
        
        // Load comments
        feedRepository.getComments(postId)
            .onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoadingComments = true) }
                    }
                    is Resource.Success -> {
                        _state.update { 
                            it.copy(
                                isLoadingComments = false,
                                comments = result.data ?: emptyList()
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update { 
                            it.copy(
                                isLoadingComments = false,
                                error = result.message
                            )
                        }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun updateCommentText(text: String) {
        _state.update { it.copy(commentText = text) }
    }

    private fun addComment() {
        val postId = _state.value.selectedPost?.id ?: return
        val content = _state.value.commentText.trim()
        
        if (content.isEmpty()) return

        viewModelScope.launch {
            _state.update { it.copy(isAddingComment = true) }
            
            when (val result = feedRepository.addComment(postId, content)) {
                is Resource.Success -> {
                    _state.update { 
                        it.copy(
                            isAddingComment = false,
                            commentText = ""
                        )
                    }
                    // Create notification for post author
                    val post = _state.value.selectedPost
                    if (post != null) {
                        val user = FirebaseAuth.getInstance().currentUser
                        activityRepository.createNotification(
                            targetUserId = post.authorId,
                            type = "COMMENT",
                            actorName = user?.displayName ?: "Someone",
                            actorPhotoUrl = user?.photoUrl?.toString() ?: "",
                            postId = postId,
                            message = "commented on your post"
                        )
                    }
                }
                is Resource.Error -> {
                    _state.update { 
                        it.copy(
                            isAddingComment = false,
                            error = result.message
                        )
                    }
                }
                is Resource.Loading -> { /* Not expected */ }
            }
        }
    }

    private fun deleteComment(commentId: String) {
        val postId = _state.value.selectedPost?.id ?: return
        
        viewModelScope.launch {
            when (val result = feedRepository.deleteComment(postId, commentId)) {
                is Resource.Error -> {
                    _state.update { it.copy(error = result.message) }
                }
                else -> { /* Success is handled by real-time listener */ }
            }
        }
    }

    private fun clearPostDetail() {
        _state.update { 
            it.copy(
                selectedPost = null,
                comments = emptyList(),
                commentText = "",
                isLoadingComments = false
            )
        }
    }

    private fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

package com.bhavans.mybhavans.feature.feed.domain.repository

import android.net.Uri
import com.bhavans.mybhavans.core.util.Resource
import com.bhavans.mybhavans.feature.feed.domain.model.Comment
import com.bhavans.mybhavans.feature.feed.domain.model.Post
import com.bhavans.mybhavans.feature.feed.domain.model.PostCategory
import kotlinx.coroutines.flow.Flow

interface FeedRepository {
    
    fun getFeed(category: PostCategory? = null): Flow<Resource<List<Post>>>
    
    suspend fun getPost(postId: String): Resource<Post>
    
    suspend fun createPost(
        content: String,
        category: PostCategory,
        imageUri: Uri? = null
    ): Resource<Post>
    
    suspend fun updatePost(
        postId: String,
        content: String,
        category: PostCategory
    ): Resource<Post>
    
    suspend fun deletePost(postId: String): Resource<Unit>
    
    suspend fun likePost(postId: String): Resource<Unit>
    
    suspend fun unlikePost(postId: String): Resource<Unit>
    
    fun getComments(postId: String): Flow<Resource<List<Comment>>>
    
    suspend fun addComment(postId: String, content: String): Resource<Comment>
    
    suspend fun deleteComment(postId: String, commentId: String): Resource<Unit>
    
    suspend fun uploadImage(uri: Uri): Resource<String>
}

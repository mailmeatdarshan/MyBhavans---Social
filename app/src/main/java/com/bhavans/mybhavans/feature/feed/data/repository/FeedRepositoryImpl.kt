package com.bhavans.mybhavans.feature.feed.data.repository

import android.net.Uri
import com.bhavans.mybhavans.core.util.Resource
import com.bhavans.mybhavans.core.util.uploadToCloudinary
import com.bhavans.mybhavans.core.util.Constants
import com.bhavans.mybhavans.feature.feed.domain.model.Comment
import com.bhavans.mybhavans.feature.feed.domain.model.Post
import com.bhavans.mybhavans.feature.feed.domain.model.PostCategory
import com.bhavans.mybhavans.feature.feed.domain.repository.FeedRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : FeedRepository {

    private val postsCollection = firestore.collection("posts")
    private val usersCollection = firestore.collection("users")

    override fun getFeed(category: PostCategory?): Flow<Resource<List<Post>>> = callbackFlow {
        trySend(Resource.Loading())
        
        var query: Query = postsCollection.orderBy("createdAt", Query.Direction.DESCENDING)
        
        if (category != null) {
            query = query.whereEqualTo("category", category.name)
        }
        
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.message ?: "Failed to load feed"))
                return@addSnapshotListener
            }
            
            val posts = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(PostDto::class.java)?.toDomain(doc.id)
            } ?: emptyList()
            
            trySend(Resource.Success(posts))
        }
        
        awaitClose { listener.remove() }
    }

    override suspend fun getPost(postId: String): Resource<Post> {
        return try {
            val doc = postsCollection.document(postId).get().await()
            val postDto = doc.toObject(PostDto::class.java)
            
            if (postDto != null) {
                Resource.Success(postDto.toDomain(doc.id))
            } else {
                Resource.Error("Post not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get post")
        }
    }

    override suspend fun createPost(
        content: String,
        category: PostCategory,
        imageUri: Uri?
    ): Resource<Post> {
        return try {
            val currentUser = auth.currentUser 
                ?: return Resource.Error("User not authenticated")
            
            val userDoc = usersCollection.document(currentUser.uid).get().await()
            val userName = userDoc.getString("displayName") ?: "Unknown"
            val userPhoto = userDoc.getString("photoUrl") ?: ""
            
            var imageUrl = ""
            if (imageUri != null) {
                val uploadResult = uploadImage(imageUri)
                if (uploadResult is Resource.Success) {
                    imageUrl = uploadResult.data ?: ""
                } else if (uploadResult is Resource.Error) {
                    return Resource.Error(uploadResult.message ?: "Failed to upload image")
                }
            }
            
            val postDto = PostDto(
                authorId = currentUser.uid,
                authorName = userName,
                authorPhotoUrl = userPhoto,
                content = content,
                imageUrl = imageUrl,
                category = category.name,
                likes = emptyList(),
                commentCount = 0,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            val docRef = postsCollection.add(postDto).await()
            Resource.Success(postDto.toDomain(docRef.id))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create post")
        }
    }

    override suspend fun updatePost(
        postId: String,
        content: String,
        category: PostCategory
    ): Resource<Post> {
        return try {
            val currentUser = auth.currentUser 
                ?: return Resource.Error("User not authenticated")
            
            val postDoc = postsCollection.document(postId).get().await()
            val existingPost = postDoc.toObject(PostDto::class.java)
            
            if (existingPost?.authorId != currentUser.uid) {
                return Resource.Error("You can only edit your own posts")
            }
            
            val updates = mapOf(
                "content" to content,
                "category" to category.name,
                "updatedAt" to System.currentTimeMillis()
            )
            
            postsCollection.document(postId).update(updates).await()
            getPost(postId)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update post")
        }
    }

    override suspend fun deletePost(postId: String): Resource<Unit> {
        return try {
            val currentUser = auth.currentUser 
                ?: return Resource.Error("User not authenticated")
            
            val postDoc = postsCollection.document(postId).get().await()
            val existingPost = postDoc.toObject(PostDto::class.java)
            
            if (existingPost?.authorId != currentUser.uid) {
                return Resource.Error("You can only delete your own posts")
            }
            
            // Delete all comments first
            val comments = postsCollection.document(postId)
                .collection("comments").get().await()
            comments.documents.forEach { it.reference.delete().await() }
            
            // Delete the post
            postsCollection.document(postId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete post")
        }
    }

    override suspend fun likePost(postId: String): Resource<Unit> {
        return try {
            val currentUser = auth.currentUser 
                ?: return Resource.Error("User not authenticated")
            
            val postRef = postsCollection.document(postId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(postRef)
                val currentLikes = snapshot.get("likes") as? List<*> ?: emptyList<String>()
                
                if (!currentLikes.contains(currentUser.uid)) {
                    val updatedLikes = currentLikes.toMutableList().apply { add(currentUser.uid) }
                    transaction.update(postRef, "likes", updatedLikes)
                }
            }.await()
            
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to like post")
        }
    }

    override suspend fun unlikePost(postId: String): Resource<Unit> {
        return try {
            val currentUser = auth.currentUser 
                ?: return Resource.Error("User not authenticated")
            
            val postRef = postsCollection.document(postId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(postRef)
                val currentLikes = snapshot.get("likes") as? List<*> ?: emptyList<String>()
                
                if (currentLikes.contains(currentUser.uid)) {
                    val updatedLikes = currentLikes.toMutableList().apply { remove(currentUser.uid) }
                    transaction.update(postRef, "likes", updatedLikes)
                }
            }.await()
            
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to unlike post")
        }
    }

    override fun getComments(postId: String): Flow<Resource<List<Comment>>> = callbackFlow {
        trySend(Resource.Loading())
        
        val listener = postsCollection.document(postId)
            .collection("comments")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to load comments"))
                    return@addSnapshotListener
                }
                
                val comments = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(CommentDto::class.java)?.toDomain(doc.id, postId)
                } ?: emptyList()
                
                trySend(Resource.Success(comments))
            }
        
        awaitClose { listener.remove() }
    }

    override suspend fun addComment(postId: String, content: String): Resource<Comment> {
        return try {
            val currentUser = auth.currentUser 
                ?: return Resource.Error("User not authenticated")
            
            val userDoc = usersCollection.document(currentUser.uid).get().await()
            val userName = userDoc.getString("displayName") ?: "Unknown"
            val userPhoto = userDoc.getString("photoUrl") ?: ""
            
            val commentDto = CommentDto(
                authorId = currentUser.uid,
                authorName = userName,
                authorPhotoUrl = userPhoto,
                content = content,
                createdAt = System.currentTimeMillis()
            )
            
            val docRef = postsCollection.document(postId)
                .collection("comments")
                .add(commentDto).await()
            
            // Update comment count
            val postRef = postsCollection.document(postId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(postRef)
                val currentCount = snapshot.getLong("commentCount") ?: 0
                transaction.update(postRef, "commentCount", currentCount + 1)
            }.await()
            
            Resource.Success(commentDto.toDomain(docRef.id, postId))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to add comment")
        }
    }

    override suspend fun deleteComment(postId: String, commentId: String): Resource<Unit> {
        return try {
            val currentUser = auth.currentUser 
                ?: return Resource.Error("User not authenticated")
            
            val commentDoc = postsCollection.document(postId)
                .collection("comments")
                .document(commentId).get().await()
            val comment = commentDoc.toObject(CommentDto::class.java)
            
            if (comment?.authorId != currentUser.uid) {
                return Resource.Error("You can only delete your own comments")
            }
            
            commentDoc.reference.delete().await()
            
            // Update comment count
            val postRef = postsCollection.document(postId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(postRef)
                val currentCount = snapshot.getLong("commentCount") ?: 0
                transaction.update(postRef, "commentCount", maxOf(0, currentCount - 1))
            }.await()
            
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete comment")
        }
    }

    override suspend fun uploadImage(uri: Uri): Resource<String> {
        return try {
            val downloadUrl = uploadToCloudinary(
                uri = uri,
                folder = Constants.CLOUDINARY_POST_IMAGES_FOLDER
            )
            Resource.Success(downloadUrl)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to upload image")
        }
    }
}

// DTO classes for Firebase serialization
private data class PostDto(
    val authorId: String = "",
    val authorName: String = "",
    val authorPhotoUrl: String = "",
    val content: String = "",
    val imageUrl: String = "",
    val category: String = "GENERAL",
    val likes: List<String> = emptyList(),
    val commentCount: Int = 0,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
) {
    fun toDomain(id: String) = Post(
        id = id,
        authorId = authorId,
        authorName = authorName,
        authorPhotoUrl = authorPhotoUrl,
        content = content,
        imageUrl = imageUrl,
        category = try { PostCategory.valueOf(category) } catch (e: Exception) { PostCategory.GENERAL },
        likes = likes,
        commentCount = commentCount,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

private data class CommentDto(
    val authorId: String = "",
    val authorName: String = "",
    val authorPhotoUrl: String = "",
    val content: String = "",
    val createdAt: Long = 0
) {
    fun toDomain(id: String, postId: String) = Comment(
        id = id,
        postId = postId,
        authorId = authorId,
        authorName = authorName,
        authorPhotoUrl = authorPhotoUrl,
        content = content,
        createdAt = createdAt
    )
}

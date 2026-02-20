package com.bhavans.mybhavans.feature.lostfound.data.repository

import android.net.Uri
import com.bhavans.mybhavans.core.util.Resource
import com.bhavans.mybhavans.core.util.uploadToCloudinary
import com.bhavans.mybhavans.core.util.Constants
import com.bhavans.mybhavans.feature.lostfound.domain.model.LostFoundCategory
import com.bhavans.mybhavans.feature.lostfound.domain.model.LostFoundItem
import com.bhavans.mybhavans.feature.lostfound.domain.model.LostFoundType
import com.bhavans.mybhavans.feature.lostfound.domain.repository.LostFoundRepository
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
class LostFoundRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : LostFoundRepository {

    private val itemsCollection = firestore.collection("lostfound")

    override fun getItems(
        type: LostFoundType?,
        category: LostFoundCategory?
    ): Flow<Resource<List<LostFoundItem>>> = callbackFlow {
        trySend(Resource.Loading())
        
        var query: Query = itemsCollection.orderBy("createdAt", Query.Direction.DESCENDING)
        
        if (type != null) {
            query = query.whereEqualTo("type", type.name)
        }
        
        if (category != null) {
            query = query.whereEqualTo("category", category.name)
        }
        
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.message ?: "Failed to load items"))
                return@addSnapshotListener
            }
            
            val items = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(LostFoundItemDto::class.java)?.toDomain(doc.id)
            } ?: emptyList()
            
            trySend(Resource.Success(items))
        }
        
        awaitClose { listener.remove() }
    }

    override suspend fun getItem(itemId: String): Resource<LostFoundItem> {
        return try {
            val doc = itemsCollection.document(itemId).get().await()
            val item = doc.toObject(LostFoundItemDto::class.java)?.toDomain(doc.id)
            if (item != null) {
                Resource.Success(item)
            } else {
                Resource.Error("Item not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get item")
        }
    }

    override suspend fun createItem(
        title: String,
        description: String,
        type: LostFoundType,
        category: LostFoundCategory,
        location: String,
        contactNumber: String,
        imageUri: Uri?
    ): Resource<LostFoundItem> {
        return try {
            val currentUser = auth.currentUser ?: return Resource.Error("User not authenticated")
            
            val imageUrl = if (imageUri != null) {
                when (val uploadResult = uploadImage(imageUri)) {
                    is Resource.Success -> uploadResult.data
                    is Resource.Error -> null
                    is Resource.Loading -> null
                }
            } else null
            
            val itemDto = LostFoundItemDto(
                title = title,
                description = description,
                type = type.name,
                category = category.name,
                location = location,
                imageUrl = imageUrl,
                authorId = currentUser.uid,
                authorName = currentUser.displayName ?: "Anonymous",
                authorEmail = currentUser.email ?: "",
                contactNumber = contactNumber,
                isResolved = false,
                createdAt = System.currentTimeMillis()
            )
            
            val docRef = itemsCollection.add(itemDto).await()
            val createdItem = itemDto.toDomain(docRef.id)
            
            Resource.Success(createdItem)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create item")
        }
    }

    override suspend fun updateItem(
        itemId: String,
        title: String,
        description: String,
        category: LostFoundCategory,
        location: String,
        contactNumber: String
    ): Resource<LostFoundItem> {
        return try {
            val updates = mapOf(
                "title" to title,
                "description" to description,
                "category" to category.name,
                "location" to location,
                "contactNumber" to contactNumber
            )
            
            itemsCollection.document(itemId).update(updates).await()
            getItem(itemId)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update item")
        }
    }

    override suspend fun deleteItem(itemId: String): Resource<Unit> {
        return try {
            val currentUser = auth.currentUser ?: return Resource.Error("User not authenticated")
            val doc = itemsCollection.document(itemId).get().await()
            val item = doc.toObject(LostFoundItemDto::class.java)
            
            if (item?.authorId != currentUser.uid) {
                return Resource.Error("Not authorized to delete this item")
            }
            
            // Note: Cloudinary doesn't need explicit deletion here; the Cloudinary URL stored
            // in Firestore is just a reference. Use Cloudinary dashboard or Admin API to purge
            // if needed. For now, we simply delete the Firestore document.
            itemsCollection.document(itemId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete item")
        }
    }

    override suspend fun markAsResolved(itemId: String): Resource<Unit> {
        return try {
            val currentUser = auth.currentUser ?: return Resource.Error("User not authenticated")
            val doc = itemsCollection.document(itemId).get().await()
            val item = doc.toObject(LostFoundItemDto::class.java)
            
            if (item?.authorId != currentUser.uid) {
                return Resource.Error("Not authorized to update this item")
            }
            
            itemsCollection.document(itemId).update("isResolved", true).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to mark as resolved")
        }
    }

    override suspend fun uploadImage(uri: Uri): Resource<String> {
        return try {
            val downloadUrl = uploadToCloudinary(
                uri = uri,
                folder = Constants.CLOUDINARY_LOST_FOUND_FOLDER
            )
            Resource.Success(downloadUrl)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to upload image")
        }
    }
}

// DTO class for Firebase serialization
data class LostFoundItemDto(
    val title: String = "",
    val description: String = "",
    val type: String = "",
    val category: String = "",
    val location: String = "",
    val imageUrl: String? = null,
    val authorId: String = "",
    val authorName: String = "",
    val authorEmail: String = "",
    val contactNumber: String = "",
    val isResolved: Boolean = false,
    val createdAt: Long = 0
) {
    fun toDomain(id: String): LostFoundItem {
        return LostFoundItem(
            id = id,
            title = title,
            description = description,
            type = try { LostFoundType.valueOf(type) } catch (e: Exception) { LostFoundType.LOST },
            category = try { LostFoundCategory.valueOf(category) } catch (e: Exception) { LostFoundCategory.OTHER },
            location = location,
            imageUrl = imageUrl,
            authorId = authorId,
            authorName = authorName,
            authorEmail = authorEmail,
            contactNumber = contactNumber,
            isResolved = isResolved,
            createdAt = createdAt
        )
    }
}

package com.bhavans.mybhavans.feature.lostfound.domain.repository

import android.net.Uri
import com.bhavans.mybhavans.core.util.Resource
import com.bhavans.mybhavans.feature.lostfound.domain.model.LostFoundCategory
import com.bhavans.mybhavans.feature.lostfound.domain.model.LostFoundItem
import com.bhavans.mybhavans.feature.lostfound.domain.model.LostFoundType
import kotlinx.coroutines.flow.Flow

interface LostFoundRepository {
    
    fun getItems(
        type: LostFoundType? = null,
        category: LostFoundCategory? = null
    ): Flow<Resource<List<LostFoundItem>>>
    
    suspend fun getItem(itemId: String): Resource<LostFoundItem>
    
    suspend fun createItem(
        title: String,
        description: String,
        type: LostFoundType,
        category: LostFoundCategory,
        location: String,
        contactNumber: String,
        imageUri: Uri? = null
    ): Resource<LostFoundItem>
    
    suspend fun updateItem(
        itemId: String,
        title: String,
        description: String,
        category: LostFoundCategory,
        location: String,
        contactNumber: String
    ): Resource<LostFoundItem>
    
    suspend fun deleteItem(itemId: String): Resource<Unit>
    
    suspend fun markAsResolved(itemId: String): Resource<Unit>
    
    suspend fun uploadImage(uri: Uri): Resource<String>
}

package com.bhavans.mybhavans.feature.lostfound.presentation

import android.net.Uri
import com.bhavans.mybhavans.feature.lostfound.domain.model.LostFoundCategory
import com.bhavans.mybhavans.feature.lostfound.domain.model.LostFoundItem
import com.bhavans.mybhavans.feature.lostfound.domain.model.LostFoundType

data class LostFoundState(
    val items: List<LostFoundItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedType: LostFoundType? = null,
    val selectedCategory: LostFoundCategory? = null
)

data class CreateLostFoundState(
    val title: String = "",
    val description: String = "",
    val type: LostFoundType = LostFoundType.LOST,
    val category: LostFoundCategory = LostFoundCategory.OTHER,
    val location: String = "",
    val contactNumber: String = "",
    val imageUri: Uri? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

data class LostFoundDetailState(
    val item: LostFoundItem? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isDeleting: Boolean = false,
    val isResolved: Boolean = false
)

sealed class LostFoundEvent {
    data object LoadItems : LostFoundEvent()
    data class SelectType(val type: LostFoundType?) : LostFoundEvent()
    data class SelectCategory(val category: LostFoundCategory?) : LostFoundEvent()
    data class DeleteItem(val itemId: String) : LostFoundEvent()
    data class MarkAsResolved(val itemId: String) : LostFoundEvent()
    
    // Create item events
    data class UpdateTitle(val title: String) : LostFoundEvent()
    data class UpdateDescription(val description: String) : LostFoundEvent()
    data class UpdateType(val type: LostFoundType) : LostFoundEvent()
    data class UpdateCategory(val category: LostFoundCategory) : LostFoundEvent()
    data class UpdateLocation(val location: String) : LostFoundEvent()
    data class UpdateContactNumber(val contactNumber: String) : LostFoundEvent()
    data class UpdateImage(val uri: Uri?) : LostFoundEvent()
    data object CreateItem : LostFoundEvent()
    data object ClearCreateState : LostFoundEvent()
    
    // Detail events
    data class LoadItemDetail(val itemId: String) : LostFoundEvent()
    data object ClearDetail : LostFoundEvent()
    data object ClearError : LostFoundEvent()
}

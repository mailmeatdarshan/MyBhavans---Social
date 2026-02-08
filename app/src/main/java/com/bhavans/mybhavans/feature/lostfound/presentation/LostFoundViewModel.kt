package com.bhavans.mybhavans.feature.lostfound.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhavans.mybhavans.core.util.Resource
import com.bhavans.mybhavans.feature.lostfound.domain.model.LostFoundCategory
import com.bhavans.mybhavans.feature.lostfound.domain.model.LostFoundType
import com.bhavans.mybhavans.feature.lostfound.domain.repository.LostFoundRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LostFoundViewModel @Inject constructor(
    private val repository: LostFoundRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LostFoundState())
    val state = _state.asStateFlow()

    private val _createState = MutableStateFlow(CreateLostFoundState())
    val createState = _createState.asStateFlow()

    private val _detailState = MutableStateFlow(LostFoundDetailState())
    val detailState = _detailState.asStateFlow()

    init {
        loadItems()
    }

    fun onEvent(event: LostFoundEvent) {
        when (event) {
            is LostFoundEvent.LoadItems -> loadItems()
            is LostFoundEvent.SelectType -> selectType(event.type)
            is LostFoundEvent.SelectCategory -> selectCategory(event.category)
            is LostFoundEvent.DeleteItem -> deleteItem(event.itemId)
            is LostFoundEvent.MarkAsResolved -> markAsResolved(event.itemId)
            is LostFoundEvent.UpdateTitle -> updateTitle(event.title)
            is LostFoundEvent.UpdateDescription -> updateDescription(event.description)
            is LostFoundEvent.UpdateType -> updateType(event.type)
            is LostFoundEvent.UpdateCategory -> updateCategory(event.category)
            is LostFoundEvent.UpdateLocation -> updateLocation(event.location)
            is LostFoundEvent.UpdateContactNumber -> updateContactNumber(event.contactNumber)
            is LostFoundEvent.UpdateImage -> updateImage(event.uri)
            is LostFoundEvent.CreateItem -> createItem()
            is LostFoundEvent.ClearCreateState -> clearCreateState()
            is LostFoundEvent.LoadItemDetail -> loadItemDetail(event.itemId)
            is LostFoundEvent.ClearDetail -> clearDetail()
            is LostFoundEvent.ClearError -> clearError()
        }
    }

    private fun loadItems() {
        repository.getItems(
            type = _state.value.selectedType,
            category = _state.value.selectedCategory
        ).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _state.update { it.copy(isLoading = true, error = null) }
                }
                is Resource.Success -> {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            items = result.data ?: emptyList(),
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
        }.launchIn(viewModelScope)
    }

    private fun selectType(type: LostFoundType?) {
        _state.update { it.copy(selectedType = type) }
        loadItems()
    }

    private fun selectCategory(category: LostFoundCategory?) {
        _state.update { it.copy(selectedCategory = category) }
        loadItems()
    }

    private fun deleteItem(itemId: String) {
        viewModelScope.launch {
            _detailState.update { it.copy(isDeleting = true) }
            when (val result = repository.deleteItem(itemId)) {
                is Resource.Success -> {
                    _detailState.update { 
                        it.copy(isDeleting = false, item = null)
                    }
                }
                is Resource.Error -> {
                    _detailState.update { 
                        it.copy(isDeleting = false, error = result.message)
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun markAsResolved(itemId: String) {
        viewModelScope.launch {
            when (val result = repository.markAsResolved(itemId)) {
                is Resource.Success -> {
                    _detailState.update { state ->
                        state.copy(
                            item = state.item?.copy(isResolved = true),
                            isResolved = true
                        )
                    }
                }
                is Resource.Error -> {
                    _detailState.update { it.copy(error = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun updateTitle(title: String) {
        _createState.update { it.copy(title = title) }
    }

    private fun updateDescription(description: String) {
        _createState.update { it.copy(description = description) }
    }

    private fun updateType(type: LostFoundType) {
        _createState.update { it.copy(type = type) }
    }

    private fun updateCategory(category: LostFoundCategory) {
        _createState.update { it.copy(category = category) }
    }

    private fun updateLocation(location: String) {
        _createState.update { it.copy(location = location) }
    }

    private fun updateContactNumber(contactNumber: String) {
        _createState.update { it.copy(contactNumber = contactNumber) }
    }

    private fun updateImage(uri: Uri?) {
        _createState.update { it.copy(imageUri = uri) }
    }

    private fun createItem() {
        val state = _createState.value
        
        if (state.title.isBlank()) {
            _createState.update { it.copy(error = "Title is required") }
            return
        }
        
        if (state.description.isBlank()) {
            _createState.update { it.copy(error = "Description is required") }
            return
        }
        
        if (state.location.isBlank()) {
            _createState.update { it.copy(error = "Location is required") }
            return
        }
        
        if (state.contactNumber.isBlank()) {
            _createState.update { it.copy(error = "Contact number is required") }
            return
        }

        viewModelScope.launch {
            _createState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = repository.createItem(
                title = state.title,
                description = state.description,
                type = state.type,
                category = state.category,
                location = state.location,
                contactNumber = state.contactNumber,
                imageUri = state.imageUri
            )) {
                is Resource.Success -> {
                    _createState.update { 
                        it.copy(isLoading = false, isSuccess = true)
                    }
                }
                is Resource.Error -> {
                    _createState.update { 
                        it.copy(isLoading = false, error = result.message)
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun clearCreateState() {
        _createState.value = CreateLostFoundState()
    }

    private fun loadItemDetail(itemId: String) {
        viewModelScope.launch {
            _detailState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = repository.getItem(itemId)) {
                is Resource.Success -> {
                    _detailState.update { 
                        it.copy(
                            isLoading = false,
                            item = result.data,
                            isResolved = result.data?.isResolved ?: false
                        )
                    }
                }
                is Resource.Error -> {
                    _detailState.update { 
                        it.copy(isLoading = false, error = result.message)
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun clearDetail() {
        _detailState.value = LostFoundDetailState()
    }

    private fun clearError() {
        _state.update { it.copy(error = null) }
        _createState.update { it.copy(error = null) }
        _detailState.update { it.copy(error = null) }
    }
}

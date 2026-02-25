package com.bhavans.mybhavans.feature.library.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhavans.mybhavans.core.util.Resource
import com.bhavans.mybhavans.feature.library.domain.model.Library
import com.bhavans.mybhavans.feature.library.domain.model.LibraryBook
import com.bhavans.mybhavans.feature.library.domain.model.LibraryMedia
import com.bhavans.mybhavans.feature.library.domain.repository.LibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class LibraryState(
    val library: Library? = null,
    val newBooks: List<LibraryBook> = emptyList(),
    val todaysMedia: List<LibraryMedia> = emptyList(),
    val isLoading: Boolean = true,
    val isBooksLoading: Boolean = true,
    val isMediaLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: LibraryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LibraryState())
    val state: StateFlow<LibraryState> = _state.asStateFlow()

    init {
        observeLibrary()
        observeNewBooks()
        observeTodaysMedia()
    }

    private fun observeLibrary() {
        repository.getLibrary().onEach { result ->
            when (result) {
                is Resource.Loading -> _state.update { it.copy(isLoading = true) }
                is Resource.Success -> _state.update { it.copy(isLoading = false, library = result.data) }
                is Resource.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
            }
        }.launchIn(viewModelScope)
    }

    private fun observeNewBooks() {
        repository.getNewBooks().onEach { result ->
            when (result) {
                is Resource.Loading -> _state.update { it.copy(isBooksLoading = true) }
                is Resource.Success -> _state.update { it.copy(isBooksLoading = false, newBooks = result.data ?: emptyList()) }
                is Resource.Error -> _state.update { it.copy(isBooksLoading = false) }
            }
        }.launchIn(viewModelScope)
    }

    private fun observeTodaysMedia() {
        repository.getTodaysMedia().onEach { result ->
            when (result) {
                is Resource.Loading -> _state.update { it.copy(isMediaLoading = true) }
                is Resource.Success -> _state.update { it.copy(isMediaLoading = false, todaysMedia = result.data ?: emptyList()) }
                is Resource.Error -> _state.update { it.copy(isMediaLoading = false) }
            }
        }.launchIn(viewModelScope)
    }
}

package com.bhavans.mybhavans.feature.explore.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhavans.mybhavans.core.util.Resource
import com.bhavans.mybhavans.feature.auth.domain.model.User
import com.bhavans.mybhavans.feature.auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExploreState(
    val query: String = "",
    val users: List<User> = emptyList(),
    val isSearching: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ExploreState())
    val state: StateFlow<ExploreState> = _state.asStateFlow()

    // Internal query flow for debounce
    private val _queryFlow = MutableStateFlow("")

    init {
        observeQuery()
    }

    @OptIn(FlowPreview::class)
    private fun observeQuery() {
        viewModelScope.launch {
            _queryFlow
                .debounce(300) // wait 300ms after user stops typing
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isBlank()) {
                        _state.update { it.copy(users = emptyList(), isSearching = false, error = null) }
                        return@collect
                    }
                    searchUsers(query)
                }
        }
    }

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
        _queryFlow.value = query
    }

    private fun searchUsers(query: String) {
        viewModelScope.launch {
            _state.update { it.copy(isSearching = true, error = null) }
            when (val result = authRepository.searchUsers(query)) {
                is Resource.Success -> {
                    _state.update {
                        it.copy(
                            isSearching = false,
                            users = result.data ?: emptyList()
                        )
                    }
                }
                is Resource.Error -> {
                    _state.update {
                        it.copy(isSearching = false, error = result.message)
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }
}

package paige.navic.ui.viewmodels

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import paige.navic.data.repositories.SearchRepository
import paige.navic.utils.UiState

class SearchViewModel(
	private val repository: SearchRepository = SearchRepository()
) : ViewModel() {
	private val _searchState = MutableStateFlow<UiState<List<Any>>>(UiState.Success(emptyList()))
	val searchState = _searchState.asStateFlow()

	val searchQuery = TextFieldState()

	init {
		viewModelScope.launch {
			snapshotFlow { searchQuery.text }
				.collectLatest { refreshResults() }
		}
	}

	fun refreshResults() {
		viewModelScope.launch {
			_searchState.value = UiState.Loading
			try {
				val results = repository.search(searchQuery.text.toString())
				_searchState.value = UiState.Success(results)
			} catch (e: Exception) {
				_searchState.value = UiState.Error(e)
			}
		}
	}
}

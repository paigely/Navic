package paige.navic.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import paige.navic.data.repository.SearchRepository
import paige.navic.util.UiState

class SearchViewModel(
	private val repository: SearchRepository = SearchRepository()
) : ViewModel() {
	private val _searchState = MutableStateFlow<UiState<List<Any>>>(UiState.Success(emptyList()))
	val searchState = _searchState.asStateFlow()

	private val _searchQuery = MutableStateFlow<String>("")
	val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

	fun refreshResults() {
		viewModelScope.launch {
			_searchState.value = UiState.Loading
			try {
				val results = repository.search(_searchQuery.value)
				_searchState.value = UiState.Success(results)
			} catch (e: Exception) {
				_searchState.value = UiState.Error(e)
			}
		}
	}

	fun search(query: String) {
		_searchQuery.value = query
		refreshResults()
	}

	fun clearSearch() {
		_searchQuery.value = ""
		_searchState.value = UiState.Success(emptyList())
	}
}
package paige.navic.ui.viewmodels

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import paige.navic.data.repositories.SearchRepository
import paige.navic.utils.UiState

@OptIn(FlowPreview::class)
class SearchViewModel(
	private val repository: SearchRepository = SearchRepository()
) : ViewModel() {
	private val _searchState = MutableStateFlow<UiState<List<Any>>>(UiState.Success(emptyList()))
	val searchState = _searchState.asStateFlow()
	private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
	val searchHistory = _searchHistory.asStateFlow()
	val searchQuery = TextFieldState()

	init {
		viewModelScope.launch {
			snapshotFlow { searchQuery.text }
				.debounce(300)
				.collectLatest { text ->
					if (text.isBlank()) {
						_searchState.value = UiState.Success(emptyList())
					} else {
						refreshResults()
					}
				}
		}
	}

	fun refreshResults() {
		val currentQuery = searchQuery.text.toString()
		if (currentQuery.isBlank()) return

		viewModelScope.launch {
			_searchState.value = UiState.Loading
			try {
				val results = repository.search(currentQuery)
				_searchState.value = UiState.Success(results)
			} catch (e: Exception) {
				_searchState.value = UiState.Error(e)
			}
		}
	}

	fun addToSearchHistory(query: String) {
		if (query.isBlank()) return

		val currentList = _searchHistory.value.toMutableList()

		currentList.remove(query)
		currentList.add(0, query)

		_searchHistory.value = currentList.take(10)
	}
	
	fun removeFromSearchHistory(query: String) {
		_searchHistory.value = _searchHistory.value.filter { it != query }
	}
}
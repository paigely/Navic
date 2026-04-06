package paige.navic.ui.screens.search.viewmodels

import androidx.compose.foundation.lazy.grid.LazyGridState
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
import paige.navic.domain.repositories.SearchRepository
import paige.navic.managers.ConnectivityManager
import paige.navic.managers.DownloadManager
import paige.navic.utils.UiState

@OptIn(FlowPreview::class)
class SearchViewModel(
	private val repository: SearchRepository,
	connectivityManager: ConnectivityManager,
	downloadManager: DownloadManager
) : ViewModel() {
	private val _searchState = MutableStateFlow<UiState<List<Any>>>(UiState.Success(emptyList()))
	val searchState = _searchState.asStateFlow()

	private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
	val searchHistory = _searchHistory.asStateFlow()

	val searchQuery = TextFieldState()

	val isOnline = connectivityManager.isOnline
	val downloadedSongs = downloadManager.downloadedSongs

	val gridState = LazyGridState()

	init {
		viewModelScope.launch {
			snapshotFlow { searchQuery.text }
				.debounce(300)
				.collectLatest {
					refreshResults()
				}
		}
	}

	private fun refreshResults() {
		if (searchQuery.text.isBlank()) {
			_searchState.value = UiState.Success(emptyList())
			return
		}
		_searchState.value = UiState.Loading()
		viewModelScope.launch {
			_searchState.value = try {
				UiState.Success(repository.search(searchQuery.text.toString()))
			} catch (e: Exception) {
				UiState.Error(e)
			}
		}
	}

	fun addToSearchHistory(query: String) {
		if (query.isBlank()) return
		val current = _searchHistory.value.toMutableList()
		if (current.contains(query)) {
			current.remove(query)
		}
		current.add(0, query)
		_searchHistory.value = current.take(10)
	}

	fun removeFromSearchHistory(query: String) {
		val current = _searchHistory.value.toMutableList()
		current.remove(query)
		_searchHistory.value = current
	}
}

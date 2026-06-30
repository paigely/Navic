package paige.navic.ui.screens.search.viewmodels

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import paige.navic.domain.manager.ConnectivityManager
import paige.navic.domain.manager.DownloadManager
import paige.navic.domain.models.DomainSong
import paige.navic.domain.repositories.SearchRepository
import paige.navic.domain.repositories.SongRepository
import paige.navic.ui.core.UiState
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
class SearchViewModel(
	private val repository: SearchRepository,
	private val songRepository: SongRepository,
	connectivityManager: ConnectivityManager,
	downloadManager: DownloadManager
) : ViewModel() {
	val searchState: StateFlow<UiState<List<Any>>>
		field = MutableStateFlow<UiState<List<Any>>>(UiState.Success(emptyList()))

	val searchHistory: StateFlow<List<String>>
		field = MutableStateFlow<List<String>>(emptyList())

	val selectedSong: StateFlow<DomainSong?>
		field = MutableStateFlow(null)

	val selectedSongIsStarred: StateFlow<Boolean>
		field = MutableStateFlow(false)

	val selectedSongRating: StateFlow<Int>
		field = MutableStateFlow(0)

	val searchQuery = TextFieldState()

	val isOnline = connectivityManager.isOnline
	val downloadedSongs = downloadManager.downloadedSongs

	val gridState = LazyGridState()

	init {
		viewModelScope.launch {
			snapshotFlow { searchQuery.text }
				.debounce(300.milliseconds)
				.collectLatest { queryText ->
					val query = queryText.toString()
					if (query.isBlank()) {
						searchState.value = UiState.Success(emptyList())
					} else {
						searchState.value = UiState.Loading()
						try {
							searchState.value = UiState.Success(repository.search(query))
						} catch (e: Exception) {
							if (e !is CancellationException) {
								searchState.value = UiState.Error(e)
							}
						}
					}
				}
		}
	}

	fun addToSearchHistory(query: String) {
		if (query.isBlank()) return
		val current = searchHistory.value.toMutableList()
		if (current.contains(query)) {
			current.remove(query)
		}
		current.add(0, query)
		searchHistory.value = current.take(10)
	}

	fun removeFromSearchHistory(query: String) {
		val current = searchHistory.value.toMutableList()
		current.remove(query)
		searchHistory.value = current
	}

	fun selectSong(song: DomainSong) {
		viewModelScope.launch {
			selectedSong.value = song
			selectedSongIsStarred.value = songRepository.isSongStarred(song)
			selectedSongRating.value = songRepository.getSongRating(song)
		}
	}

	fun starSelectedSong(starred: Boolean) {
		viewModelScope.launch {
			val selection = selectedSong.value ?: return@launch
			runCatching {
				if (starred) {
					songRepository.starSong(selection)
				} else {
					songRepository.unstarSong(selection)
				}
				selectedSongIsStarred.value = starred
			}
		}
	}

	fun rateSelectedSong(rating: Int) {
		viewModelScope.launch {
			val selection = selectedSong.value ?: return@launch
			runCatching {
				songRepository.rateSong(selection, rating)
				selectedSongRating.value = rating
			}
		}
	}

	fun clearSelectedSong() {
		selectedSong.value = null
	}
}

package paige.navic.ui.screens.song.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import paige.navic.domain.manager.ConnectivityManager
import paige.navic.domain.manager.DownloadManager
import paige.navic.domain.manager.SessionManager
import paige.navic.domain.models.DomainSong
import paige.navic.domain.models.DomainSongListType
import paige.navic.domain.repositories.SongRepository
import paige.navic.ui.core.UiState

class SongListViewModel(
	initialListType: DomainSongListType = DomainSongListType.FrequentlyPlayed,
	private val artistId: String? = null,
	private val repository: SongRepository,
	private val downloadManager: DownloadManager,
	private val sessionManager: SessionManager,
	connectivityManager: ConnectivityManager
) : ViewModel() {
	val songsState: StateFlow<UiState<ImmutableList<DomainSong>>>
		field = MutableStateFlow<UiState<ImmutableList<DomainSong>>>(UiState.Loading())

	val allDownloads = downloadManager.allDownloads
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.Lazily,
			initialValue = persistentListOf()
		)

	val selectedSong: StateFlow<DomainSong?>
		field = MutableStateFlow(null)

	val starred: StateFlow<Boolean>
		field = MutableStateFlow(false)

	val selectedSongRating: StateFlow<Int>
		field = MutableStateFlow(0)

	val selectedSorting: StateFlow<DomainSongListType>
		field = MutableStateFlow(initialListType)

	val selectedReversed: StateFlow<Boolean>
		field = MutableStateFlow(false)

	val isOnline = connectivityManager.isOnline

	init {
		viewModelScope.launch {
			sessionManager.isLoggedIn.collect { if (it) refreshSongs(false) }
		}
	}

	fun selectSong(song: DomainSong) {
		viewModelScope.launch {
			selectedSong.value = song
			starred.value = repository.isSongStarred(song)
			selectedSongRating.value = repository.getSongRating(song)
		}
	}

	fun clearSelection() {
		selectedSong.value = null
	}

	fun refreshSongs(fullRefresh: Boolean) {
		viewModelScope.launch {
			repository.getSongsFlow(
				fullRefresh,
				selectedSorting.value,
				selectedReversed.value,
				artistId
			).collect {
				songsState.value = it
			}
		}
	}

	fun starSong(isStarred: Boolean) {
		viewModelScope.launch {
			val selection = selectedSong.value ?: return@launch
			runCatching {
				if (isStarred) {
					repository.starSong(selection)
				} else {
					repository.unstarSong(selection)
				}
				starred.value = isStarred
				refreshSongs(false)
			}
		}
	}

	fun rateSelectedSong(rating: Int) {
		viewModelScope.launch {
			val selection = selectedSong.value ?: return@launch
			runCatching {
				repository.rateSong(selection, rating)
				selectedSongRating.value = rating
			}
		}
	}

	fun setSorting(sorting: DomainSongListType) {
		selectedSorting.value = sorting
		refreshSongs(false)
	}

	fun setReversed(reversed: Boolean) {
		selectedReversed.value = reversed
		refreshSongs(false)
	}

	fun clearError() {
		songsState.value = UiState.Success(songsState.value.data ?: persistentListOf())
	}

	fun downloadSong(song: DomainSong) {
		downloadManager.downloadSong(song)
	}

	fun cancelDownload(songId: String) {
		downloadManager.cancelDownload(songId)
	}

	fun deleteDownload(songId: String) {
		downloadManager.deleteDownload(songId)
	}
}

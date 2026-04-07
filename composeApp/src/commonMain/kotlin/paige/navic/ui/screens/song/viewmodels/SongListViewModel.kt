package paige.navic.ui.screens.song.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import paige.navic.data.session.SessionManager
import paige.navic.domain.models.DomainSong
import paige.navic.domain.models.DomainSongListType
import paige.navic.domain.repositories.SongRepository
import paige.navic.utils.UiState

class SongListViewModel(
	private val artistId: String? = null,
	private val repository: SongRepository
) : ViewModel() {
	private val _songsState = MutableStateFlow<UiState<ImmutableList<DomainSong>>>(UiState.Loading())
	val songsState = _songsState.asStateFlow()

	private val _selectedSong = MutableStateFlow<DomainSong?>(null)
	val selectedSong = _selectedSong.asStateFlow()

	private val _starred = MutableStateFlow(false)
	val starred = _starred.asStateFlow()

	private val _selectedSorting = MutableStateFlow(DomainSongListType.FrequentlyPlayed)
	val selectedSorting = _selectedSorting.asStateFlow()

	private val _selectedReversed = MutableStateFlow(false)
	val selectedReversed = _selectedReversed.asStateFlow()

	init {
		viewModelScope.launch {
			SessionManager.isLoggedIn.collect { if (it) refreshSongs(false) }
		}
	}

	fun selectSong(song: DomainSong) {
		viewModelScope.launch {
			_selectedSong.value = song
			_starred.value = repository.isSongStarred(song)
		}
	}

	fun clearSelection() {
		_selectedSong.value = null
	}

	fun refreshSongs(fullRefresh: Boolean) {
		viewModelScope.launch {
			repository.getSongsFlow(fullRefresh, _selectedSorting.value, _selectedReversed.value, artistId).collect {
				_songsState.value = it
			}
		}
	}

	fun starSong(starred: Boolean) {
		viewModelScope.launch {
			val selection = _selectedSong.value ?: return@launch
			runCatching {
				if (starred) {
					repository.starSong(selection)
				} else {
					repository.unstarSong(selection)
				}
				_starred.value = starred
			}
		}
	}

	fun setSorting(sorting: DomainSongListType) {
		_selectedSorting.value = sorting
		refreshSongs(false)
	}

	fun setReversed(reversed: Boolean) {
		_selectedReversed.value = reversed
		refreshSongs(false)
	}

	fun clearError() {
		_songsState.value = UiState.Success(_songsState.value.data ?: persistentListOf())
	}
}

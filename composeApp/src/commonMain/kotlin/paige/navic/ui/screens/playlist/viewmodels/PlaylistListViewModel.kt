package paige.navic.ui.screens.playlist.viewmodels

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import paige.navic.domain.repositories.PlaylistRepository
import paige.navic.data.session.SessionManager
import paige.navic.domain.models.DomainPlaylist
import paige.navic.domain.models.DomainPlaylistListType
import paige.navic.utils.UiState

class PlaylistListViewModel(
	private val repository: PlaylistRepository
) : ViewModel() {
	private val _playlistsState = MutableStateFlow<UiState<ImmutableList<DomainPlaylist>>>(UiState.Loading())
	val playlistsState = _playlistsState.asStateFlow()

	private val _selectedPlaylist = MutableStateFlow<DomainPlaylist?>(null)
	val selectedPlaylist = _selectedPlaylist.asStateFlow()

	private val _selectedSorting = MutableStateFlow(DomainPlaylistListType.DateAdded)
	val selectedSorting = _selectedSorting.asStateFlow()

	private val _selectedReversed = MutableStateFlow(false)
	val selectedReversed = _selectedReversed.asStateFlow()

	val gridState = LazyGridState()

	init {
		viewModelScope.launch {
			SessionManager.isLoggedIn.collect { if (it) refreshPlaylists(false) }
		}
	}

	fun selectPlaylist(playlist: DomainPlaylist) {
		_selectedPlaylist.value = playlist
	}

	fun clearSelection() {
		_selectedPlaylist.value = null
	}

	fun refreshPlaylists(fullRefresh: Boolean) {
		viewModelScope.launch {
			repository.getPlaylistsFlow(fullRefresh, _selectedSorting.value, _selectedReversed.value).collect {
				_playlistsState.value = it
			}
		}
	}

	fun setSorting(sorting: DomainPlaylistListType) {
		_selectedSorting.value = sorting
		refreshPlaylists(false)
	}

	fun setReversed(reversed: Boolean) {
		_selectedReversed.value = reversed
		refreshPlaylists(false)
	}

	fun clearError() {
		_playlistsState.value = UiState.Success(_playlistsState.value.data ?: persistentListOf())
	}
}
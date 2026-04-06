package paige.navic.ui.screens.playlist.viewmodels

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import paige.navic.data.models.settings.Settings
import paige.navic.domain.repositories.PlaylistRepository
import paige.navic.data.session.SessionManager
import paige.navic.domain.models.DomainPlaylist
import paige.navic.domain.models.DomainPlaylistListType
import paige.navic.utils.UiState
import paige.navic.utils.sortedByMode

class PlaylistListViewModel(
	private val repository: PlaylistRepository
) : ViewModel() {
	private val _playlistsState = MutableStateFlow<UiState<ImmutableList<DomainPlaylist>>>(UiState.Loading())
	val playlistsState = _playlistsState.asStateFlow()
	private val _selectedPlaylist = MutableStateFlow<DomainPlaylist?>(null)
	val selectedPlaylist = _selectedPlaylist.asStateFlow()
	private val _sortMode = MutableStateFlow(Settings.shared.playlistSortMode)
	val sortMode = _sortMode.asStateFlow()

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
			repository.getPlaylistsFlow(fullRefresh).collect {
				_playlistsState.value = it
				sortPlaylists()
			}
		}
	}

	fun setSortMode(mode: DomainPlaylistListType) {
		_sortMode.value = mode
		sortPlaylists()
	}

	fun sortPlaylists() {
		_playlistsState.value.data?.sortedByMode(
			_sortMode.value,
			Settings.shared.playlistsReversed
		)?.let {
			_playlistsState.value = when (val state = _playlistsState.value) {
				is UiState.Loading -> UiState.Loading(data = it.toPersistentList())
				is UiState.Success -> UiState.Success(data = it.toPersistentList())
				is UiState.Error -> UiState.Error(error = state.error, data = it.toPersistentList())
			}
		}
	}

	fun clearError() {
		_playlistsState.value = UiState.Success(_playlistsState.value.data ?: persistentListOf())
	}
}
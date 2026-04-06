package paige.navic.ui.screens.playlist.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import paige.navic.data.models.settings.Settings
import paige.navic.domain.repositories.PlaylistRepository
import paige.navic.data.session.SessionManager
import paige.navic.domain.models.DomainPlaylist
import paige.navic.utils.UiState
import paige.navic.utils.sortedByMode

class PlaylistListViewModel(
	private val repository: PlaylistRepository
) : ViewModel() {
	private val _playlistsState = MutableStateFlow<UiState<List<DomainPlaylist>>>(UiState.Loading())
	val playlistsState = _playlistsState.asStateFlow()
	private val _selectedPlaylist = MutableStateFlow<DomainPlaylist?>(null)
	val selectedPlaylist = _selectedPlaylist.asStateFlow()

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

	fun sortPlaylists() {
		_playlistsState.value.data?.sortedByMode(
			Settings.shared.playlistSortMode,
			Settings.shared.playlistsReversed
		)?.let {
			_playlistsState.value = when (val state = _playlistsState.value) {
				is UiState.Loading -> UiState.Loading(data = it)
				is UiState.Success -> UiState.Success(data = it)
				is UiState.Error -> UiState.Error(error = state.error, data = it)
			}
		}
	}

	fun clearError() {
		_playlistsState.value = UiState.Success(_playlistsState.value.data.orEmpty())
	}
}
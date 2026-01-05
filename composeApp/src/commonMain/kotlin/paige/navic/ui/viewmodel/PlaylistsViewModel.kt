package paige.navic.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import paige.navic.data.repository.PlaylistsRepository
import paige.navic.data.session.SessionManager
import paige.navic.util.UiState
import paige.subsonic.api.model.Playlist

class PlaylistsViewModel(
	private val repository: PlaylistsRepository = PlaylistsRepository()
) : ViewModel() {
	private val _playlistsState = MutableStateFlow<UiState<List<Playlist>>>(UiState.Loading)
	val playlistsState = _playlistsState.asStateFlow()

	private val _selectedPlaylist = MutableStateFlow<Playlist?>(null)
	val selectedPlaylist: StateFlow<Playlist?> = _selectedPlaylist.asStateFlow()

	init {
		viewModelScope.launch {
			SessionManager.isLoggedIn.collect {
				refreshPlaylists()
			}
		}
	}

	fun selectPlaylist(playlist: Playlist) {
		_selectedPlaylist.value = playlist
	}

	fun clearSelection() {
		_selectedPlaylist.value = null
	}

	fun refreshPlaylists() {
		viewModelScope.launch {
			_playlistsState.value = UiState.Loading
			try {
				val playlists = repository.getPlaylists()
				_playlistsState.value = UiState.Success(playlists)
			} catch (e: Exception) {
				_playlistsState.value = UiState.Error(e)
			}
		}
	}
}

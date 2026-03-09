package paige.navic.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zt64.subsonic.api.model.Playlist
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import paige.navic.data.repositories.PlaylistsRepository
import paige.navic.data.session.SessionManager
import paige.navic.utils.UiState

class PlaylistsViewModel(
	private val repository: PlaylistsRepository = PlaylistsRepository()
) : ViewModel() {
	private val _playlistsState = MutableStateFlow<UiState<List<Playlist>>>(UiState.Loading)
	val playlistsState = _playlistsState.asStateFlow()

	private val _isRefreshing = MutableStateFlow(false)
	val isRefreshing = _isRefreshing.asStateFlow()

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
			val currentState = _playlistsState.value
			val hasData = currentState is UiState.Success && currentState.data.isNotEmpty()

			if (hasData) {
				_isRefreshing.value = true
			} else {
				_playlistsState.value = UiState.Loading
			}

			try {
				val playlists = repository.getPlaylists()
				_playlistsState.value = UiState.Success(playlists)
			} catch (e: Exception) {
				if (!hasData) {
					_playlistsState.value = UiState.Error(e)
				}
			} finally {
				_isRefreshing.value = false
			}
		}
	}
}
package paige.navic.ui.viewmodel

import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString
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
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

class PlaylistsViewModel(
	private val repository: PlaylistsRepository = PlaylistsRepository()
) : ViewModel() {
	private val _playlistsState = MutableStateFlow<UiState<List<Playlist>>>(UiState.Loading)
	val playlistsState = _playlistsState.asStateFlow()

	private val _selectedPlaylist = MutableStateFlow<Playlist?>(null)
	val selectedPlaylist: StateFlow<Playlist?> = _selectedPlaylist.asStateFlow()

	private val _error = MutableStateFlow<Exception?>(null)
	val error = _error.asStateFlow()

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

	fun clearError() {
		_error.value = null
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

	fun deleteSelectedPlaylist() {
		val playlist = _selectedPlaylist.value ?: return
		viewModelScope.launch {
			try {
				repository.deletePlaylist(playlist.id)
				refreshPlaylists()
			} catch (e: Exception) {
				_error.value = e
			} finally {
				clearSelection()
			}
		}
	}

	fun shareSelectedPlaylist(clipboard: ClipboardManager) {
		viewModelScope.launch {
			try {
				SessionManager.api.createShare(
					_selectedPlaylist.value?.id,
					"${Clock.System.now()
						.plus(1.hours)
						.toEpochMilliseconds()}"
				).data.shares.values.firstOrNull()?.firstOrNull()?.url?.let {
					clipboard.setText(
						AnnotatedString(it)
					)
				}
			} catch (e: Exception) {
				_error.value = e
			}
		}
	}
}

package paige.navic.ui.screens.playlist.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import paige.navic.domain.manager.SessionManager
import paige.navic.domain.models.DomainSong
import paige.navic.ui.core.UiState
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.notice_added_to_multiple_playlists
import navic.composeapp.generated.resources.notice_added_to_playlist
import paige.navic.domain.manager.SnackBarManager
import dev.zt64.subsonic.api.model.Playlist as ApiPlaylist

class PlaylistUpdateDialogViewModel(
	private val songs: List<DomainSong>,
	private val playlistToExclude: String?,
	private val sessionManager: SessionManager,
	private val snackBarManager: SnackBarManager
) : ViewModel() {
	private val _playlistsState = MutableStateFlow<UiState<List<ApiPlaylist>>>(UiState.Loading())
	val playlistsState = _playlistsState.asStateFlow()

	private val _confirmState = MutableStateFlow<UiState<Nothing?>>(UiState.Success(null))
	val confirmState = _confirmState.asStateFlow()

	private val _selectedPlaylists = MutableStateFlow<Set<ApiPlaylist>>(emptySet())
	val selectedPlaylists = _selectedPlaylists.asStateFlow()

	private val _events = Channel<Event>()
	val events = _events.receiveAsFlow()

	init {
		refreshResults()
	}

	fun refreshResults() {
		viewModelScope.launch {
			_selectedPlaylists.value = emptySet()
			_playlistsState.value = UiState.Loading()
			try {
				val results =
					sessionManager.api.getPlaylists()
				_playlistsState.value =
					UiState.Success(results.filter { it.id != playlistToExclude })
			} catch (e: Exception) {
				_playlistsState.value = UiState.Error(e)
			}
		}
	}

	fun togglePlaylistSelection(playlist: ApiPlaylist) {
		_selectedPlaylists.value = if (playlist in _selectedPlaylists.value) {
			_selectedPlaylists.value - playlist
		} else {
			_selectedPlaylists.value + playlist
		}
	}

	fun confirm() {
		viewModelScope.launch {
			_confirmState.value = UiState.Loading()
			try {
				val selected = _selectedPlaylists.value
				selected.forEach { playlist ->
					sessionManager.api.updatePlaylist(
						playlist.id,
						songIdsToAdd = songs.map { it.id }
					)
				}
				_confirmState.value = UiState.Success(null)
				if (selected.size == 1) {
					snackBarManager.notify(Res.string.notice_added_to_playlist, selected.first().name)
				} else if (selected.size > 1) {
					snackBarManager.notify(Res.string.notice_added_to_multiple_playlists)
				}
				_events.send(Event.Dismiss)
			} catch (e: Exception) {
				_confirmState.value = UiState.Error(e)
			}
		}
	}

	enum class Event {
		Dismiss
	}
}

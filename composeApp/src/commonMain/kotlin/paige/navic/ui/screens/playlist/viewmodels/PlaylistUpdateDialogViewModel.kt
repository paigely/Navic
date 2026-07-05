package paige.navic.ui.screens.playlist.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.notice_added_to_multiple_playlists
import navic.composeapp.generated.resources.notice_added_to_playlist
import paige.navic.domain.manager.SessionManager
import paige.navic.domain.manager.SnackBarManager
import paige.navic.domain.models.DomainSong
import paige.navic.ui.core.UiState
import dev.zt64.subsonic.api.model.Playlist as ApiPlaylist

class PlaylistUpdateDialogViewModel(
	private val songs: List<DomainSong>,
	private val playlistToExclude: String?,
	private val sessionManager: SessionManager,
	private val snackBarManager: SnackBarManager
) : ViewModel() {
	val playlistsState: StateFlow<UiState<List<ApiPlaylist>>>
		field = MutableStateFlow<UiState<List<ApiPlaylist>>>(UiState.Loading())

	val confirmState: StateFlow<UiState<Nothing?>>
		field = MutableStateFlow<UiState<Nothing?>>(UiState.Success(null))

	val selectedPlaylists: StateFlow<Set<ApiPlaylist>>
		field = MutableStateFlow<Set<ApiPlaylist>>(emptySet())

	private val _events = Channel<Event>()
	val events = _events.receiveAsFlow()

	init {
		refreshResults()
	}

	fun refreshResults() {
		viewModelScope.launch {
			selectedPlaylists.value = emptySet()
			playlistsState.value = UiState.Loading()
			try {
				val results =
					sessionManager.api.getPlaylists()
				playlistsState.value =
					UiState.Success(results.filter { it.id != playlistToExclude })
			} catch (e: Exception) {
				playlistsState.value = UiState.Error(e)
			}
		}
	}

	fun togglePlaylistSelection(playlist: ApiPlaylist) {
		selectedPlaylists.value = if (playlist in selectedPlaylists.value) {
			selectedPlaylists.value - playlist
		} else {
			selectedPlaylists.value + playlist
		}
	}

	fun confirm() {
		viewModelScope.launch {
			confirmState.value = UiState.Loading()
			try {
				val selected = selectedPlaylists.value
				selected.forEach { playlist ->
					sessionManager.api.updatePlaylist(
						playlist.id,
						songIdsToAdd = songs.map { it.id }
					)
				}
				confirmState.value = UiState.Success(null)
				if (selected.size == 1) {
					snackBarManager.notify(
						Res.string.notice_added_to_playlist,
						selected.first().name
					)
				} else if (selected.size > 1) {
					snackBarManager.notify(Res.string.notice_added_to_multiple_playlists)
				}
				_events.send(Event.Dismiss)
			} catch (e: Exception) {
				confirmState.value = UiState.Error(e)
			}
		}
	}

	enum class Event {
		Dismiss
	}
}

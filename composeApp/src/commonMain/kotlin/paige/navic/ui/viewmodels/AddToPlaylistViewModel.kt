package paige.navic.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import paige.navic.data.session.SessionManager
import paige.navic.utils.UiState
import paige.subsonic.api.models.Playlist
import paige.subsonic.api.models.Track

class AddToPlaylistViewModel(
	private val tracks: List<Track>,
	private val playlistToExclude: String?
) : ViewModel() {
	private val _playlistsState = MutableStateFlow<UiState<List<Playlist>>>(UiState.Loading)
	val playlistsState = _playlistsState.asStateFlow()

	private val _confirmState = MutableStateFlow<UiState<Nothing?>>(UiState.Success(null))
	val confirmState = _confirmState.asStateFlow()

	private val _selectedPlaylist = MutableStateFlow<Playlist?>(null)
	val selectedPlaylist = _selectedPlaylist.asStateFlow()

	private val _events = Channel<Event>()
	val events = _events.receiveAsFlow()

	init {
		refreshResults()
	}

	fun refreshResults() {
		viewModelScope.launch {
			_selectedPlaylist.value = null
			_playlistsState.value = UiState.Loading
			try {
				val results =
					SessionManager.api.getPlaylists().data.playlists.playlist ?: emptyList()
				_playlistsState.value = UiState.Success(results.filter { it.id != playlistToExclude })
			} catch (e: Exception) {
				_playlistsState.value = UiState.Error(e)
			}
		}
	}

	fun selectPlaylist(playlist: Playlist) {
		_selectedPlaylist.value = playlist
	}

	fun confirm() {
		viewModelScope.launch {
			_confirmState.value = UiState.Loading
			try {
				SessionManager.api.updatePlaylist(
					_selectedPlaylist.value!!.id,
					tracksToAdd = tracks
				)
				_confirmState.value = UiState.Success(null)
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
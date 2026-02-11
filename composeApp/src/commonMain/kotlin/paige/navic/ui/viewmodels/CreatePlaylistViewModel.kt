package paige.navic.ui.viewmodels

import androidx.compose.foundation.text.input.TextFieldState
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

class CreatePlaylistViewModel(
	private val tracks: List<Track>
) : ViewModel() {
	private val _creationState = MutableStateFlow<UiState<Nothing?>>(UiState.Success(null))
	val creationState = _creationState.asStateFlow()

	private val _events = Channel<Event>()
	val events = _events.receiveAsFlow()

	val name = TextFieldState()

	fun create() {
		viewModelScope.launch {
			_creationState.value = UiState.Loading
			try {
				val playlist = SessionManager.api.createPlaylist(
					name = name.text.toString(),
					tracks = tracks
				)
				_events.send(Event.Dismiss(playlist.data.playlist))
			} catch (e: Exception) {
				_creationState.value = UiState.Error(e)
			}
		}
	}

	sealed class Event {
		data class Dismiss(val playlist: Playlist) : Event()
	}
}
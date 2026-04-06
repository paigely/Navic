package paige.navic.ui.screens.playlist.viewmodels

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import paige.navic.data.database.dao.PlaylistDao
import paige.navic.data.database.mappers.toDomainModel
import paige.navic.data.database.mappers.toEntity
import paige.navic.data.session.SessionManager
import paige.navic.domain.models.DomainPlaylist
import paige.navic.domain.models.DomainSong
import paige.navic.utils.UiState

class PlaylistCreateDialogViewModel(
	private val tracks: List<DomainSong>,
	private val playlistDao: PlaylistDao
) : ViewModel() {
	private val _creationState = MutableStateFlow<UiState<Nothing?>>(UiState.Success(null))
	val creationState = _creationState.asStateFlow()

	private val _events = Channel<Event>()
	val events = _events.receiveAsFlow()

	val name = TextFieldState()

	fun create() {
		viewModelScope.launch {
			_creationState.value = UiState.Loading()
			try {
				val playlist = SessionManager.api.createPlaylist(
					name = name.text.toString(),
					songIds = tracks.map { it.id }
				)
				playlistDao.insertPlaylist(playlist.toEntity())
				_events.send(Event.Dismiss(playlistDao.getPlaylistById(playlist.id)!!.toDomainModel()))
				_creationState.value = UiState.Success(null)
			} catch (e: Exception) {
				_creationState.value = UiState.Error(e)
			}
		}
	}

	sealed class Event {
		data class Dismiss(val playlist: DomainPlaylist) : Event()
	}
}
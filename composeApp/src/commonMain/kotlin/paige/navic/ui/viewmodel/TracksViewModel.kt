package paige.navic.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import paige.navic.data.repository.TracksRepository
import paige.navic.data.session.SessionManager
import paige.navic.util.UiState
import paige.subsonic.api.model.Album
import paige.subsonic.api.model.AnyTrack
import paige.subsonic.api.model.AnyTracks
import paige.subsonic.api.model.Playlist

class TracksViewModel(
	private val partialTracks: Any,
	private val repository: TracksRepository = TracksRepository()
) : ViewModel() {
	private val _tracksState = MutableStateFlow<UiState<AnyTracks>>(UiState.Loading)
	val tracksState: StateFlow<UiState<AnyTracks>> = _tracksState.asStateFlow()

	private val _selectedTrack = MutableStateFlow<AnyTrack?>(null)
	val selectedTrack: StateFlow<AnyTrack?> = _selectedTrack.asStateFlow()

	init {
		viewModelScope.launch {
			SessionManager.isLoggedIn.collect {
				refreshTracks()
			}
		}
	}

	fun refreshTracks() {
		viewModelScope.launch {
			_tracksState.value = UiState.Loading
			try {
				val albums = when (partialTracks) {
					is Album -> repository.getTracks(partialTracks)
					is Playlist -> repository.getTracks(partialTracks)
					else -> error("Invalid partialTracks")
				}
				_tracksState.value = UiState.Success(albums)
			} catch (e: Exception) {
				_tracksState.value = UiState.Error(e)
			}
		}
	}

	fun selectTrack(track: AnyTrack) {
		_selectedTrack.value = track
	}

	fun clearSelection() {
		_selectedTrack.value = null
	}
}
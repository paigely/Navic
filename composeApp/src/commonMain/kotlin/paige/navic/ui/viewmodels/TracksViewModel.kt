package paige.navic.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import paige.navic.data.repositories.TracksRepository
import paige.navic.data.session.SessionManager
import paige.navic.utils.UiState
import paige.subsonic.api.models.Album
import paige.subsonic.api.models.AlbumInfo
import paige.subsonic.api.models.Track
import paige.subsonic.api.models.TrackCollection

class TracksViewModel(
	private val partialCollection: TrackCollection,
	private val repository: TracksRepository = TracksRepository()
) : ViewModel() {
	private val _tracksState = MutableStateFlow<UiState<TrackCollection>>(UiState.Loading)
	val tracksState: StateFlow<UiState<TrackCollection>> = _tracksState.asStateFlow()

	private val _selectedTrack = MutableStateFlow<Track?>(null)
	val selectedTrack: StateFlow<Track?> = _selectedTrack.asStateFlow()

	private val _selectedIndex = MutableStateFlow<Int?>(null)
	val selectedIndex: StateFlow<Int?> = _selectedIndex.asStateFlow()

	private val _albumInfoState = MutableStateFlow<UiState<AlbumInfo>>(UiState.Loading)
	val albumInfoState = _albumInfoState.asStateFlow()

	private val _starredState = MutableStateFlow<UiState<Boolean>>(UiState.Success(false))
	val starredState = _starredState.asStateFlow()

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
				_tracksState.value = UiState.Success(
					repository.fetchWithAllTracks(partialCollection)
				)
			} catch (e: Exception) {
				_tracksState.value = UiState.Error(e)
			}
			try {
				val albumInfo = repository.getAlbumInfo(
					(_tracksState.value as UiState.Success).data as Album
				)
				_albumInfoState.value = UiState.Success(albumInfo)
			} catch (e: Exception) {
				e.printStackTrace()
				_albumInfoState.value = UiState.Error(e)
			}
		}
	}

	fun selectTrack(track: Track, index: Int) {
		viewModelScope.launch {
			_selectedTrack.value = track
			_selectedIndex.value = index
			_starredState.value = UiState.Loading
			_albumInfoState.value = UiState.Loading
			try {
				val isStarred = repository.isTrackStarred(track)
				_starredState.value = UiState.Success(isStarred ?: false)
			} catch(e: Exception) {
				_starredState.value = UiState.Error(e)
			}
		}
	}

	fun clearSelection() {
		_selectedTrack.value = null
		_selectedIndex.value = null
	}

	fun removeFromPlaylist() {
		val selection = _selectedIndex.value ?: return
		clearSelection()
		viewModelScope.launch {
			try {
				SessionManager.api.updatePlaylist(
					playlistId = partialCollection.id,
					indexesToRemove = listOf(selection)
				)
				refreshTracks()
			} catch (_: Exception) { }
		}
	}

	fun starSelectedTrack() {
		viewModelScope.launch {
			try {
				repository.starTrack(_selectedTrack.value!!)
			} catch(_: Exception) { }
		}
	}

	fun unstarSelectedTrack() {
		viewModelScope.launch {
			try {
				repository.unstarTrack(_selectedTrack.value!!)
			} catch(_: Exception) { }
		}
	}
}
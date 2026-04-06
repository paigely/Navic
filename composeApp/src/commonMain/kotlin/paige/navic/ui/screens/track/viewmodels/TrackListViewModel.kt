package paige.navic.ui.screens.track.viewmodels

import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import paige.navic.data.database.entities.DownloadStatus
import paige.navic.data.database.mappers.toDomainModel
import paige.navic.domain.models.DomainSongCollection
import paige.navic.domain.repositories.TrackRepository
import paige.navic.data.session.SessionManager
import paige.navic.managers.DownloadManager
import paige.navic.managers.ConnectivityManager
import paige.navic.domain.models.DomainAlbum
import paige.navic.domain.models.DomainAlbumInfo
import paige.navic.domain.models.DomainSong
import paige.navic.shared.Logger
import paige.navic.utils.UiState

class TrackListViewModel(
	private val partialCollection: DomainSongCollection,
	private val repository: TrackRepository,
	private val downloadManager: DownloadManager,
	connectivityManager: ConnectivityManager
) : ViewModel() {
	private val _tracksState = MutableStateFlow<UiState<DomainSongCollection>>(UiState.Loading())
	val tracksState: StateFlow<UiState<DomainSongCollection>> = _tracksState.asStateFlow()

	val isOnline = connectivityManager.isOnline

	val allDownloads = downloadManager.allDownloads
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.Lazily,
			initialValue = emptyList()
		)

	val otherAlbums = repository
		.getOtherAlbums((partialCollection as? DomainAlbum)?.artistId.orEmpty(), partialCollection.id)
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.Lazily,
			initialValue = emptyList()
		)

	private val _selectedTrack = MutableStateFlow<DomainSong?>(null)
	val selectedTrack: StateFlow<DomainSong?> = _selectedTrack.asStateFlow()

	private val _selectedIndex = MutableStateFlow<Int?>(null)
	val selectedIndex: StateFlow<Int?> = _selectedIndex.asStateFlow()

	private val _albumInfoState = MutableStateFlow<UiState<DomainAlbumInfo>>(UiState.Loading())
	val albumInfoState = _albumInfoState.asStateFlow()

	private val _starredState = MutableStateFlow<UiState<Boolean>>(UiState.Success(false))
	val starredState = _starredState.asStateFlow()

	val listState = LazyListState()

	init {
		viewModelScope.launch {
			SessionManager.isLoggedIn.collect {
				refreshTracks()
			}
		}
	}

	fun refreshTracks() {
		viewModelScope.launch {
			_tracksState.value = UiState.Loading()
			try {
				val localCollection = repository.fetchWithAllTracks(partialCollection)
				_tracksState.value = UiState.Success(localCollection)

				if (localCollection is DomainAlbum) {
					try {
						val albumInfo = repository.getAlbumInfo(localCollection.id)
						_albumInfoState.value = UiState.Success(albumInfo.toDomainModel())
					} catch (e: Exception) {
						_albumInfoState.value = UiState.Error(e)
					}
				} else {
					_albumInfoState.value = UiState.Error(Exception("No album info for playlists"))
				}
			} catch (e: Exception) {
				_tracksState.value = UiState.Error(e)
			}
		}
	}

	fun selectTrack(track: DomainSong, index: Int) {
		viewModelScope.launch {
			_selectedTrack.value = track
			_selectedIndex.value = index
			_starredState.value = UiState.Loading()
			_albumInfoState.value = UiState.Loading()
			try {
				val isStarred = repository.isTrackStarred(track.id)
				_starredState.value = UiState.Success(isStarred)
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
					id = partialCollection.id,
					songIndicesToRemove = listOf(selection)
				)
				refreshTracks()
			} catch(e: Exception) {
				Logger.e("TrackListViewModel", "Failed to remove song from playlist", e)
			}
		}
	}

	fun starSelectedTrack() {
		viewModelScope.launch {
			try {
				repository.starTrack(_selectedTrack.value!!)
			} catch(e: Exception) {
				Logger.e("TrackListViewModel", "Failed to star song", e)
			}
		}
	}

	fun unstarSelectedTrack() {
		viewModelScope.launch {
			try {
				repository.unstarTrack(_selectedTrack.value!!)
			} catch(e: Exception) {
				Logger.e("TrackListViewModel", "Failed to unstar song", e)
			}
		}
	}

	fun downloadTrack(track: DomainSong) {
		downloadManager.downloadSong(track)
	}

	fun cancelDownload(trackId: String) {
		downloadManager.cancelDownload(trackId)
	}

	fun deleteDownload(trackId: String) {
		downloadManager.deleteDownload(trackId)
	}

	fun downloadAll() {
		val tracks = (tracksState.value as? UiState.Success)?.data ?: return
		viewModelScope.launch {
			downloadManager.downloadCollection(tracks)
		}
	}

	fun cancelDownloadAll() {
		val tracks = (tracksState.value as? UiState.Success)?.data ?: return
		tracks.songs.forEach {
			downloadManager.cancelDownload(it.id)
		}
	}

	fun collectionDownloadStatus(): Flow<DownloadStatus> {
		val songs = (tracksState.value as? UiState.Success)?.data?.songs.orEmpty()
		return downloadManager.getCollectionDownloadStatus(songs.map { it.id })
	}
}

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
import kotlinx.coroutines.runBlocking
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

/**
 * Viewmodel for the screen that shows an album/playlist and its songs.
 * Not to be confused with SongListViewModel, this just has a dumb name
 */
class TrackListViewModel(
	private val collectionId: String,
	private val repository: TrackRepository,
	private val downloadManager: DownloadManager,
	connectivityManager: ConnectivityManager
) : ViewModel() {
	private val _collectionState = MutableStateFlow<UiState<DomainSongCollection>>(
		runBlocking {
			try {
				UiState.Loading(repository.getLocalData(collectionId))
			} catch (_: Exception) {
				UiState.Loading()
			}
		}
	)
	val collectionState: StateFlow<UiState<DomainSongCollection>> = _collectionState.asStateFlow()

	val isOnline = connectivityManager.isOnline

	val allDownloads = downloadManager.allDownloads
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.Lazily,
			initialValue = emptyList()
		)

	val otherAlbums = (_collectionState.value.data as? DomainAlbum)?.let { album ->
		repository.getOtherAlbums(album.artistId, album.id)
	}?.stateIn(
		scope = viewModelScope,
		started = SharingStarted.Lazily,
		initialValue = emptyList()
	) ?: MutableStateFlow(emptyList())

	private val _selectedTrack = MutableStateFlow<DomainSong?>(null)
	val selectedTrack: StateFlow<DomainSong?> = _selectedTrack.asStateFlow()

	private val _albumInfoState = MutableStateFlow<UiState<DomainAlbumInfo>>(UiState.Loading())
	val albumInfoState = _albumInfoState.asStateFlow()

	private val _starredState = MutableStateFlow<UiState<Boolean>>(UiState.Success(false))
	val starredState = _starredState.asStateFlow()

	val listState = LazyListState()

	init {
		viewModelScope.launch {
			SessionManager.isLoggedIn.collect { if (it) refreshCollection(false) }
		}
	}

	fun refreshCollection(fullRefresh: Boolean) {
		viewModelScope.launch {
			repository.getCollectionFlow(fullRefresh, collectionId).collect {
				_collectionState.value = it
				if (it.data is DomainAlbum) {
					try {
						val albumInfo = repository.getAlbumInfo(collectionId)
						_albumInfoState.value = UiState.Success(albumInfo.toDomainModel())
					} catch (e: Exception) {
						_albumInfoState.value = UiState.Error(e)
					}
				}
			}
		}
	}

	fun selectTrack(track: DomainSong) {
		viewModelScope.launch {
			_selectedTrack.value = track
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
	}

	fun clearError() {
		_collectionState.value.data?.let {
			_collectionState.value = UiState.Success(it)
		}
	}

	fun removeFromPlaylist() {
		val track = _selectedTrack.value ?: return
		val songs = _collectionState.value.data?.songs ?: return
		viewModelScope.launch {
			try {
				SessionManager.api.updatePlaylist(
					id = collectionId,
					songIndicesToRemove = listOf(songs.indexOf(track))
				)
				refreshCollection(true)
			} catch(e: Exception) {
				Logger.e("TrackListViewModel", "Failed to remove song from playlist", e)
			}
		}
		clearSelection()
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
		val collection = _collectionState.value.data ?: return
		viewModelScope.launch {
			downloadManager.downloadCollection(collection)
		}
	}

	fun cancelDownloadAll() {
		_collectionState.value.data?.songs?.forEach {
			downloadManager.cancelDownload(it.id)
		}
	}

	fun collectionDownloadStatus(): Flow<DownloadStatus> {
		val songs = _collectionState.value.data?.songs.orEmpty()
		return downloadManager.getCollectionDownloadStatus(songs.map { it.id })
	}
}

package paige.navic.ui.screens.collection.viewmodels

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
import paige.navic.data.session.SessionManager
import paige.navic.domain.models.DomainAlbum
import paige.navic.domain.models.DomainAlbumInfo
import paige.navic.domain.models.DomainSong
import paige.navic.domain.models.DomainSongCollection
import paige.navic.domain.repositories.CollectionRepository
import paige.navic.managers.ConnectivityManager
import paige.navic.managers.DownloadManager
import paige.navic.shared.Logger
import paige.navic.utils.UiState

class CollectionDetailViewModel(
	private val collectionId: String,
	private val repository: CollectionRepository,
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

	private val _selectedSong = MutableStateFlow<DomainSong?>(null)
	val selectedSong: StateFlow<DomainSong?> = _selectedSong.asStateFlow()

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

	fun selectSong(song: DomainSong) {
		viewModelScope.launch {
			_selectedSong.value = song
			_starredState.value = UiState.Loading()
			_albumInfoState.value = UiState.Loading()
			try {
				val isStarred = repository.isSongStarred(song.id)
				_starredState.value = UiState.Success(isStarred)
			} catch (e: Exception) {
				_starredState.value = UiState.Error(e)
			}
		}
	}

	fun clearSelection() {
		_selectedSong.value = null
	}

	fun clearError() {
		_collectionState.value.data?.let {
			_collectionState.value = UiState.Success(it)
		}
	}

	fun removeFromPlaylist() {
		val song = _selectedSong.value ?: return
		val songs = _collectionState.value.data?.songs ?: return
		viewModelScope.launch {
			try {
				SessionManager.api.updatePlaylist(
					id = collectionId,
					songIndicesToRemove = listOf(songs.indexOf(song))
				)
				refreshCollection(true)
			} catch (e: Exception) {
				Logger.e("CollectionDetailViewModel", "Failed to remove song from playlist", e)
			}
		}
		clearSelection()
	}

	fun starSelectedSong() {
		viewModelScope.launch {
			try {
				repository.starSong(_selectedSong.value!!)
			} catch (e: Exception) {
				Logger.e("CollectionDetailViewModel", "Failed to star song", e)
			}
		}
	}

	fun unstarSelectedSong() {
		viewModelScope.launch {
			try {
				repository.unstarSong(_selectedSong.value!!)
			} catch (e: Exception) {
				Logger.e("CollectionDetailViewModel", "Failed to unstar song", e)
			}
		}
	}

	fun downloadSong(song: DomainSong) {
		downloadManager.downloadSong(song)
	}

	fun cancelDownload(songId: String) {
		downloadManager.cancelDownload(songId)
	}

	fun deleteDownload(songId: String) {
		downloadManager.deleteDownload(songId)
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

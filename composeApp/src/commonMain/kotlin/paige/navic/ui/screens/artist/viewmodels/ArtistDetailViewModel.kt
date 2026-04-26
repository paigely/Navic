package paige.navic.ui.screens.artist.viewmodels

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import paige.navic.data.database.dao.AlbumDao
import paige.navic.data.database.dao.ArtistDao
import paige.navic.data.database.entities.DownloadStatus
import paige.navic.data.database.mappers.toDomainModel
import paige.navic.domain.models.DomainAlbum
import paige.navic.domain.models.DomainArtist
import paige.navic.domain.models.DomainSong
import paige.navic.domain.repositories.AlbumRepository
import paige.navic.domain.repositories.DbRepository
import paige.navic.domain.repositories.SongRepository
import paige.navic.managers.ConnectivityManager
import paige.navic.managers.DownloadManager
import paige.navic.shared.Logger
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.utils.UiState

@Immutable
data class ArtistState(
	val artist: DomainArtist,
	val albums: List<DomainAlbum>,
	val topSongs: List<DomainSong>,
	val similarArtists: List<DomainArtist> = emptyList()
)

class ArtistDetailViewModel(
	private val artistId: String,
	private val repository: DbRepository,
	private val songRepository: SongRepository,
	private val albumRepository: AlbumRepository,
	private val artistDao: ArtistDao,
	private val albumDao: AlbumDao,
	private val downloadManager: DownloadManager,
	connectivityManager: ConnectivityManager
) : ViewModel() {
	private val _artistState = MutableStateFlow<UiState<ArtistState>>(UiState.Loading())
	val artistState = _artistState.asStateFlow()

	private val _selectedSong = MutableStateFlow<DomainSong?>(null)
	val selectedSong = _selectedSong.asStateFlow()

	private val _selectedSongIsStarred = MutableStateFlow(false)
	val selectedSongIsStarred = _selectedSongIsStarred.asStateFlow()

	private val _selectedSongRating = MutableStateFlow(0)
	val selectedSongRating = _selectedSongRating.asStateFlow()

	private val _selectedAlbum = MutableStateFlow<DomainAlbum?>(null)
	val selectedAlbum = _selectedAlbum.asStateFlow()

	private val _selectedAlbumIsStarred = MutableStateFlow(false)
	val selectedAlbumIsStarred = _selectedAlbumIsStarred.asStateFlow()

	private val _selectedAlbumRating = MutableStateFlow(0)
	val selectedAlbumRating = _selectedAlbumRating.asStateFlow()

	val isOnline = connectivityManager.isOnline

	val allDownloads = downloadManager.allDownloads
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.Lazily,
			initialValue = emptyList()
		)

	val scrollState = ScrollState(initial = 0)

	init {
		loadArtistData()
	}

	private fun loadArtistData() {
		viewModelScope.launch {
			try {
				val artistEntity = artistDao.getArtistById(artistId)
					?: throw Exception("Artist not found in database")
				val domainArtist = artistEntity.toDomainModel()

				val albumsWithSongs =
					albumDao.getAlbumsByArtist(artistId).firstOrNull() ?: emptyList()
				val domainAlbums = albumsWithSongs.map { it.toDomainModel() }

				val domainSongs = albumsWithSongs.flatMap { it.songs }
					.map { it.toDomainModel() }
					.sortedByDescending { it.playCount }
					.take(10)

				val initialSimilarArtists = domainArtist.similarArtistIds.mapNotNull { id ->
					artistDao.getArtistById(id)?.toDomainModel()
				}

				_artistState.value = UiState.Success(
					ArtistState(
						artist = domainArtist,
						albums = domainAlbums,
						topSongs = domainSongs,
						similarArtists = initialSimilarArtists
					)
				)

				repository.fetchArtistMetadata(artistId)
					.onSuccess { updatedArtist ->
						val currentState = (_artistState.value as? UiState.Success)?.data
						if (currentState != null) {

							val updatedSimilarArtists =
								updatedArtist.similarArtistIds.mapNotNull { id ->
									artistDao.getArtistById(id)?.toDomainModel()
								}

							_artistState.value = UiState.Success(
								currentState.copy(
									artist = updatedArtist,
									similarArtists = updatedSimilarArtists
								)
							)
						}
					}
					.onFailure { error ->
						Logger.e("ArtistDetailViewModel", "Failed to fetch artist metadata", error)
					}
			} catch (e: Exception) {
				_artistState.value = UiState.Error(e)
			}
		}
	}

	fun selectSong(song: DomainSong) {
		viewModelScope.launch {
			_selectedSong.value = song
			_selectedSongIsStarred.value = songRepository.isSongStarred(song)
			_selectedSongRating.value = songRepository.getSongRating(song)
		}
	}

	fun clearSelection() {
		_selectedSong.value = null
	}

	fun selectAlbum(album: DomainAlbum) {
		viewModelScope.launch {
			_selectedAlbum.value = album
			_selectedAlbumIsStarred.value = albumRepository.isAlbumStarred(album)
			_selectedAlbumRating.value = albumRepository.getAlbumRating(album)
		}
	}

	fun rateSelectedAlbum(rating: Int) {
		viewModelScope.launch {
			val selection = _selectedAlbum.value ?: return@launch
			runCatching {
				_selectedAlbumRating.value = rating
				albumRepository.rateAlbum(selection, rating)
			}
		}
	}

	fun clearAlbumSelection() {
		_selectedAlbum.value = null
	}

	fun starSelectedSong() {
		viewModelScope.launch {
			val selection = _selectedSong.value ?: return@launch
			runCatching {
				_selectedSongIsStarred.value = true
				songRepository.starSong(selection)
			}
		}
	}

	fun unstarSelectedSong() {
		viewModelScope.launch {
			val selection = _selectedSong.value ?: return@launch
			runCatching {
				_selectedSongIsStarred.value = false
				songRepository.unstarSong(selection)
			}
		}
	}

	fun rateSelectedSong(rating: Int) {
		viewModelScope.launch {
			val selection = _selectedSong.value ?: return@launch
			runCatching {
				_selectedSongRating.value = rating
				songRepository.rateSong(selection, rating)
			}
		}
	}

	fun starAlbum(starred: Boolean) {
		viewModelScope.launch {
			val selection = _selectedAlbum.value ?: return@launch
			runCatching {
				if (starred) {
					albumRepository.starAlbum(selection)
				} else {
					albumRepository.unstarAlbum(selection)
				}
				_selectedAlbumIsStarred.value = starred
			}
		}
	}

	fun playArtistAlbums(player: MediaPlayerViewModel) {
		(_artistState.value as? UiState.Success)?.data?.let { state ->
			player.clearQueue()
			state.albums.forEach { album ->
				player.addToQueue(album)
			}
			player.togglePlay()
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

	@OptIn(ExperimentalCoroutinesApi::class)
	fun collectionDownloadStatus(): Flow<DownloadStatus> {
		return artistState.flatMapLatest { state ->
			if (state is UiState.Success) {
				val allArtistSongIds = state.data.albums.flatMap { album ->
					album.songs.map { it.id }
				}

				if (allArtistSongIds.isEmpty()) {
					flowOf(DownloadStatus.NOT_DOWNLOADED)
				} else {
					downloadManager.getCollectionDownloadStatus(allArtistSongIds)
				}
			} else {
				flowOf(DownloadStatus.NOT_DOWNLOADED)
			}
		}
	}
}

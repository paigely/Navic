package paige.navic.ui.screens.artist.viewmodels

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import paige.navic.data.database.dao.AlbumDao
import paige.navic.data.database.dao.ArtistDao
import paige.navic.data.database.mappers.toDomainModel
import paige.navic.domain.repositories.DbRepository
import paige.navic.domain.models.DomainAlbum
import paige.navic.domain.models.DomainArtist
import paige.navic.domain.models.DomainSong
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
	private val artistDao: ArtistDao,
	private val albumDao: AlbumDao
) : ViewModel() {
	private val _artistState = MutableStateFlow<UiState<ArtistState>>(UiState.Loading())
	val artistState = _artistState.asStateFlow()

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

				val albumsWithSongs = albumDao.getAlbumsByArtist(artistId).firstOrNull() ?: emptyList()
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

							val updatedSimilarArtists = updatedArtist.similarArtistIds.mapNotNull { id ->
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

	fun playArtistAlbums(player: MediaPlayerViewModel) {
		(_artistState.value as? UiState.Success)?.data?.let { state ->
			player.clearQueue()
			state.albums.forEach { album ->
				player.addToQueue(album)
			}
			player.togglePlay()
		}
	}
}
package paige.navic.domain.repositories

import kotlinx.coroutines.CancellationException
import paige.navic.data.database.dao.AlbumDao
import paige.navic.data.database.dao.ArtistDao
import paige.navic.data.database.dao.PlaylistDao
import paige.navic.data.database.dao.SongDao
import paige.navic.data.database.mappers.toDomainModel
import paige.navic.data.database.mappers.toEntity
import paige.navic.data.session.SessionManager
import paige.navic.managers.ConnectivityManager
import paige.navic.shared.Logger

class SearchRepository(
	private val albumDao: AlbumDao,
	private val artistDao: ArtistDao,
	private val songDao: SongDao,
	private val playlistDao: PlaylistDao,
	private val connectivityManager: ConnectivityManager
) {
	suspend fun search(query: String): List<Any> {
		val isOnline = connectivityManager.isOnline.value

		return if (isOnline) {
			try {
				val data = SessionManager.api.searchID3(query)

				val existingAlbumIds = albumDao.getAllAlbumIds().toSet()
				val existingArtistIds = artistDao.getAllArtistIds().toSet()
				val existingSongIds = songDao.getAllSongIds().toSet()

				albumDao.insertAlbums(data.albums.filter { it.id !in existingAlbumIds }.map { it.toEntity() })
				artistDao.insertArtists(data.artists.filter { it.id !in existingArtistIds }.map { it.toEntity() })
				songDao.insertSongs(data.songs.filter { it.id !in existingSongIds }.map { it.toEntity() })

				val localPlaylists = playlistDao.searchPlaylistsList(query).map { it.toDomainModel() }

				listOf(
					data.albums.mapNotNull { albumDao.getAlbumById(it.id)?.toDomainModel() },
					data.artists.mapNotNull { artistDao.getArtistById(it.id)?.toDomainModel() },
					data.songs.mapNotNull { songDao.getSongById(it.id)?.toDomainModel() },
					localPlaylists
				).flatten()

			} catch (e: Exception) {
				if (e is CancellationException)
					throw e

				Logger.e("SearchRepository", "Online search failed despite network connection, falling back to local DB", e)
				performLocalSearch(query)
			}
		} else {
			Logger.i("SearchRepository", "Device offline, performing local search.")
			performLocalSearch(query)
		}
	}

	private suspend fun performLocalSearch(query: String): List<Any> {
		val localAlbums = albumDao.searchAlbumsList(query).map { it.toDomainModel() }
		val localArtists = artistDao.searchArtistsList(query).map { it.toDomainModel() }
		val localSongs = songDao.searchSongsList(query).map { it.toDomainModel() }
		val localPlaylists = playlistDao.searchPlaylistsList(query).map { it.toDomainModel() }

		return listOf(localAlbums, localArtists, localSongs, localPlaylists).flatten()
	}
}

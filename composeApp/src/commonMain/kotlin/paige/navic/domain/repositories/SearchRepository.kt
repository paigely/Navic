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
	connectivityManager: ConnectivityManager
) {
	val isOnline = connectivityManager.isOnline

	suspend fun search(query: String): List<Any> {
		val serverId = SessionManager.activeServerId.value ?: return emptyList()

		return if (isOnline.value) {
			try {
				val data = SessionManager.api.searchID3(query)

				albumDao.insertAlbumsIgnoringConflicts(data.albums.map { it.toEntity().copy(serverId = serverId) })
				artistDao.insertArtistsIgnoringConflicts(data.artists.map { it.toEntity().copy(serverId = serverId) })
				songDao.insertSongsIgnoringConflicts(data.songs.map { it.toEntity().copy(serverId = serverId) })

				val albums = albumDao.getAlbumsByIds(data.albums.map { it.id }, serverId)
				val artists = artistDao.getArtistsByIds(data.artists.map { it.id }, serverId)
				val songs = songDao.getSongsByIds(data.songs.map { it.id }, serverId)

				val ftsQuery = formatFtsQuery(query)
				val localPlaylists = if (ftsQuery.isNotEmpty()) {
					playlistDao.searchPlaylistsList(ftsQuery, serverId)
				} else {
					emptyList()
				}

				(albums.map { it.toDomainModel() }
					+ artists.map { it.toDomainModel() }
					+ songs.map { it.toDomainModel() }
					+ localPlaylists.map { it.toDomainModel() })
			} catch (e: Exception) {
				if (e is CancellationException) throw e
				Logger.e("SearchRepository", "Online search failed despite network connection, falling back to local DB", e)
				performLocalSearch(query, serverId)
			}
		} else {
			Logger.i("SearchRepository", "Device offline, performing local search.")
			performLocalSearch(query, serverId)
		}
	}

	private suspend fun performLocalSearch(query: String, serverId: String): List<Any> {
		val ftsQuery = formatFtsQuery(query)
		if (ftsQuery.isEmpty()) return emptyList()

		val localAlbums = albumDao.searchAlbumsList(ftsQuery, serverId).map { it.toDomainModel() }
		val localArtists = artistDao.searchArtistsList(ftsQuery, serverId).map { it.toDomainModel() }
		val localSongs = songDao.searchSongsList(ftsQuery, serverId).map { it.toDomainModel() }
		val localPlaylists = playlistDao.searchPlaylistsList(ftsQuery, serverId).map { it.toDomainModel() }

		return listOf(localAlbums, localArtists, localSongs, localPlaylists).flatten()
	}

	private fun formatFtsQuery(query: String): String {
		val sanitized = query.replace(Regex("[^a-zA-Z0-9\\s]"), " ").trim()
		if (sanitized.isEmpty()) return ""

		return sanitized.split(Regex("\\s+"))
			.filter { it.isNotEmpty() }
			.joinToString(" ") { "$it*" }
	}
}

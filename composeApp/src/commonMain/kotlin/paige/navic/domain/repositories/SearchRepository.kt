package paige.navic.domain.repositories

import paige.navic.data.database.dao.AlbumDao
import paige.navic.data.database.dao.ArtistDao
import paige.navic.data.database.dao.SongDao
import paige.navic.data.database.mappers.toDomainModel
import paige.navic.data.database.mappers.toEntity
import paige.navic.data.session.SessionManager

class SearchRepository(
	private val albumDao: AlbumDao,
	private val artistDao: ArtistDao,
	private val songDao: SongDao
) {
	suspend fun search(query: String): List<Any> {
		val data = SessionManager.api.searchID3(query)

		val existingAlbumIds = albumDao.getAllAlbumIds().toSet()
		val existingArtistIds = artistDao.getAllArtistIds().toSet()
		val existingSongIds = songDao.getAllSongIds().toSet()

		albumDao.insertAlbums(data.albums.filter { it.id !in existingAlbumIds }.map { it.toEntity() })
		artistDao.insertArtists(data.artists.filter { it.id !in existingArtistIds }.map { it.toEntity() })
		songDao.insertSongs(data.songs.filter { it.id !in existingSongIds }.map { it.toEntity() })

		return listOf(
			data.albums.mapNotNull { albumDao.getAlbumById(it.id)?.toDomainModel() },
			data.artists.mapNotNull { artistDao.getArtistById(it.id)?.toDomainModel() },
			data.songs.mapNotNull { songDao.getSongById(it.id)?.toDomainModel() }
		).flatten()
	}
}

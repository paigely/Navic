package paige.navic.domain.repositories

import dev.zt64.subsonic.api.model.AlbumInfo
import kotlinx.coroutines.flow.map
import paige.navic.data.database.SyncManager
import paige.navic.data.database.dao.AlbumDao
import paige.navic.data.database.dao.PlaylistDao
import paige.navic.data.database.dao.SongDao
import paige.navic.data.database.entities.SyncActionType
import paige.navic.data.database.mappers.toDomainModel
import paige.navic.data.database.mappers.toEntity
import paige.navic.domain.models.DomainSongCollection
import paige.navic.data.session.SessionManager
import paige.navic.domain.models.DomainAlbum
import paige.navic.domain.models.DomainPlaylist
import paige.navic.domain.models.DomainSong
import paige.navic.shared.Logger
import kotlin.time.Clock

class TrackRepository(
	private val albumDao: AlbumDao,
	private val playlistDao: PlaylistDao,
	private val songDao: SongDao,
	private val syncManager: SyncManager
) {
	suspend fun fetchWithAllTracks(collection: DomainSongCollection): DomainSongCollection {
		if (collection.songs.isNotEmpty()) {
			return collection
		} else {
			try {
				Logger.w("TrackRepository", "collection ${collection.name} does not have songs, refreshing")
				return when (collection) {
					is DomainAlbum -> {
						val album = SessionManager.api.getAlbum(collection.id)
						songDao.insertSongs(album.songs.map { it.toEntity() })
						albumDao.getAlbumById(album.id)!!.toDomainModel()
					}

					is DomainPlaylist -> {
						val playlist = SessionManager.api.getPlaylist(collection.id)
						songDao.insertSongs(playlist.songs.map { it.toEntity() })
						playlistDao.getPlaylistById(playlist.id)!!.toDomainModel()
					}
				}
			} catch (e: Exception) {
				Logger.w("TrackRepository", "failed to get collection with songs, returning original one from db", e)
				return collection
			}
		}
	}

	fun getOtherAlbums(artistId: String, albumId: String) = albumDao
		.getAlbumsByArtistExcluding(artistId, albumId)
		.map { it.map { album -> album.toDomainModel() } }

	suspend fun getAlbumInfo(albumId: String): AlbumInfo {
		return SessionManager.api.getAlbumInfo(albumId)
	}

	suspend fun isTrackStarred(trackId: String): Boolean {
		return songDao.isSongStarred(trackId)
	}

	suspend fun starTrack(track: DomainSong) {
		val starredEntity = track.toEntity().copy(
			starredAt = Clock.System.now()
		)
		songDao.insertSong(starredEntity)
		syncManager.enqueueAction(SyncActionType.STAR, track.id)
	}

	suspend fun unstarTrack(track: DomainSong) {
		val unstarredEntity = track.toEntity().copy(
			starredAt = null
		)
		songDao.insertSong(unstarredEntity)
		syncManager.enqueueAction(SyncActionType.UNSTAR, track.id)
	}
}
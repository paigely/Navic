package paige.navic.domain.repositories

import dev.zt64.subsonic.api.model.AlbumInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
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
import paige.navic.utils.UiState
import kotlin.time.Clock

/**
 * Repository for the screen that shows an album/playlist and its songs.
 * Not to be confused with SongRepository, this just has a dumb name
 */
class TrackRepository(
	private val albumDao: AlbumDao,
	private val playlistDao: PlaylistDao,
	private val songDao: SongDao,
	private val syncManager: SyncManager,
	private val dbRepository: DbRepository
) {
	suspend fun getLocalData(collectionId: String): DomainSongCollection {
		return albumDao.getAlbumById(collectionId)?.toDomainModel()
			?: playlistDao.getPlaylistById(collectionId)?.toDomainModel()
			?: throw Error("Collection ID $collectionId is neither a known album or playlist")
	}

	private suspend fun refreshLocalData(collectionId: String): DomainSongCollection {
		when (val collection = getLocalData(collectionId)) {
			is DomainAlbum -> {
				val album = SessionManager.api.getAlbum(collection.id)
				songDao.updateSongsByAlbumId(album.id, album.songs.map { it.toEntity() })
				albumDao.insertAlbum(album.toEntity())
				albumDao.getAlbumById(album.id)!!.toDomainModel()
			}
			is DomainPlaylist -> {
				val playlist = SessionManager.api.getPlaylist(collection.id)
				playlistDao.insertPlaylist(playlist.toEntity())
				dbRepository.syncPlaylistSongs(collection.id)
				playlistDao.getPlaylistById(playlist.id)!!.toDomainModel()
			}
		}
		return getLocalData(collectionId)
	}

	fun getCollectionFlow(
		fullRefresh: Boolean,
		collectionId: String
	): Flow<UiState<DomainSongCollection>> = flow {
		val localData = getLocalData(collectionId)
		if (fullRefresh) {
			emit(UiState.Loading(data = localData))
			try {
				emit(UiState.Success(data = refreshLocalData(collectionId)))
			} catch (error: Exception) {
				emit(UiState.Error(error = error, data = localData))
			}
		} else {
			emit(UiState.Success(data = localData))
		}
	}.flowOn(Dispatchers.IO)

	fun getOtherAlbums(artistId: String, albumId: String) = albumDao
		.getAlbumsByArtistExcluding(artistId, albumId)
		.map { it.map { album -> album.toDomainModel() } }

	suspend fun getSongById(songId: String) = songDao
		.getSongById(songId)
		?.toDomainModel()

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
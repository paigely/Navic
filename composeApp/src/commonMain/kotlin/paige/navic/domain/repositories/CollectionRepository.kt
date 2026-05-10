package paige.navic.domain.repositories

import dev.zt64.subsonic.api.model.AlbumInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import paige.navic.data.database.dao.AlbumDao
import paige.navic.data.database.dao.PlaylistDao
import paige.navic.data.database.dao.SongDao
import paige.navic.data.database.mappers.toDomainModel
import paige.navic.data.database.mappers.toEntity
import paige.navic.data.session.SessionManager
import paige.navic.domain.models.DomainAlbum
import paige.navic.domain.models.DomainPlaylist
import paige.navic.domain.models.DomainSongCollection
import paige.navic.utils.UiState

class CollectionRepository(
	private val albumDao: AlbumDao,
	private val playlistDao: PlaylistDao,
	private val songDao: SongDao,
	private val dbRepository: DbRepository
) {
	suspend fun getLocalData(collectionId: String, serverId: String): DomainSongCollection {
		return albumDao.getAlbumById(collectionId, serverId)?.toDomainModel()
			?: playlistDao.getPlaylistById(collectionId, serverId)?.toDomainModel()
			?: throw Error("Collection ID $collectionId is neither a known album or playlist for server $serverId")
	}

	private suspend fun refreshLocalData(collectionId: String, serverId: String): DomainSongCollection {
		when (val collection = getLocalData(collectionId, serverId)) {
			is DomainAlbum -> {
				val album = SessionManager.api.getAlbum(collection.id)
				songDao.updateSongsByAlbumId(album.id, serverId, album.songs.map { it.toEntity().copy(serverId = serverId) })
				albumDao.insertAlbum(album.toEntity())
				albumDao.getAlbumById(album.id, serverId)!!.toDomainModel()
			}

			is DomainPlaylist -> {
				val playlist = SessionManager.api.getPlaylist(collection.id)
				playlistDao.insertPlaylist(playlist.toEntity().copy(serverId = serverId))
				dbRepository.syncPlaylistSongs(collection.id)
			}
		}
		return getLocalData(collectionId, serverId)
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	fun getCollectionFlow(
		fullRefresh: Boolean,
		collectionId: String
	): Flow<UiState<DomainSongCollection>> = SessionManager.activeServerId.flatMapLatest { serverId ->
		flow {
			if (serverId == null) return@flow

			val localData = try {
				getLocalData(collectionId, serverId)
			} catch (_: Exception) {
				null
			}

			if (fullRefresh) {
				emit(UiState.Loading(data = localData))
				try {
					emit(UiState.Success(data = refreshLocalData(collectionId, serverId)))
				} catch (error: Exception) {
					emit(UiState.Error(error = error, data = localData))
				}
			} else {
				localData?.let {
					emit(UiState.Success(data = it))
				}
			}
		}
	}.flowOn(Dispatchers.IO)

	@OptIn(ExperimentalCoroutinesApi::class)
	fun getOtherAlbums(artistId: String, albumId: String): Flow<List<DomainAlbum>> =
		SessionManager.activeServerId.flatMapLatest { serverId ->
			if (serverId == null) return@flatMapLatest flow { emit(emptyList()) }

			albumDao.getAlbumsByArtistExcluding(artistId, albumId, serverId)
				.map { it.map { album -> album.toDomainModel() } }
		}

	suspend fun getSongById(songId: String): paige.navic.domain.models.DomainSong? {
		val serverId = SessionManager.activeServerId.value ?: return null
		return songDao.getSongById(songId, serverId)?.toDomainModel()
	}

	suspend fun getAlbumInfo(albumId: String): AlbumInfo {
		return SessionManager.api.getAlbumInfo(albumId)
	}
}

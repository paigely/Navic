package paige.navic.domain.repositories

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import paige.navic.data.database.SyncManager
import paige.navic.data.database.dao.AlbumDao
import paige.navic.data.database.dao.DownloadDao
import paige.navic.data.database.dao.SongDao
import paige.navic.data.database.entities.SyncActionType
import paige.navic.data.database.mappers.toDomainModel
import paige.navic.data.database.mappers.toEntity
import paige.navic.data.session.SessionManager
import paige.navic.domain.models.DomainSong
import paige.navic.domain.models.DomainSongListType
import paige.navic.utils.UiState
import paige.navic.utils.sortedByListType
import kotlin.time.Clock

class SongRepository(
	private val songDao: SongDao,
	private val albumDao: AlbumDao,
	private val downloadDao: DownloadDao,
	private val dbRepository: DbRepository,
	private val syncManager: SyncManager
) {
	fun getSongsPaging(artistId: String? = null): Flow<PagingData<DomainSong>> {
		return Pager(
			config = PagingConfig(
				pageSize = 50,
				enablePlaceholders = false
			),
			pagingSourceFactory = {
				if (artistId != null) {
					songDao.getSongsByArtistPaging(artistId)
				} else {
					songDao.getAllSongsPaging()
				}
			}
		).flow.map { pagingData ->
			pagingData.map { it.toDomainModel() }
		}
	}

	suspend fun syncLibrarySongs() {
		dbRepository.syncLibrarySongs().getOrThrow()
	}

	suspend fun getAllSongs(): List<DomainSong> {
		val serverId = SessionManager.activeServerId.value ?: return emptyList()
		return songDao.getAllSongs(serverId).map { it.toDomainModel() }
	}

	private suspend fun getLocalData(
		listType: DomainSongListType,
		reversed: Boolean,
		serverId: String,
		artistId: String? = null
	): ImmutableList<DomainSong> {
		val songs = songDao
			.getAllSongs(serverId)
			.map { it.toDomainModel() }

		val filtered = if (artistId != null) {
			songs.filter { it.artistId == artistId }
		} else {
			songs
		}.toImmutableList().sortedByListType(
			listType,
			downloads = downloadDao.getAllDownloadsList(serverId),
			albums = albumDao.getAllAlbumsList(serverId).map { it.toDomainModel() }
		)

		return if (reversed) {
			filtered.reversed().toImmutableList()
		} else {
			filtered
		}
	}

	private suspend fun refreshLocalData(
		listType: DomainSongListType,
		reversed: Boolean,
		serverId: String,
		artistId: String? = null
	): ImmutableList<DomainSong> {
		dbRepository.syncLibrarySongs().getOrThrow()
		return getLocalData(listType, reversed, serverId, artistId)
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	fun getSongsFlow(
		fullRefresh: Boolean,
		listType: DomainSongListType,
		reversed: Boolean,
		artistId: String? = null
	): Flow<UiState<ImmutableList<DomainSong>>> = SessionManager.activeServerId.flatMapLatest { serverId ->
		flow {
			if (serverId == null) return@flow

			val localData = getLocalData(listType, reversed, serverId, artistId)
			if (fullRefresh) {
				emit(UiState.Loading(data = localData))
				try {
					emit(UiState.Success(data = refreshLocalData(listType, reversed, serverId, artistId)))
				} catch (error: Exception) {
					emit(UiState.Error(error = error, data = localData))
				}
			} else {
				emit(UiState.Success(data = localData))
			}
		}
	}.flowOn(Dispatchers.IO)

	suspend fun isSongStarred(song: DomainSong): Boolean {
		val serverId = SessionManager.activeServerId.value ?: return false
		return songDao.isSongStarred(song.id, serverId)
	}

	suspend fun getSongRating(song: DomainSong): Int {
		val serverId = SessionManager.activeServerId.value ?: return 0
		return songDao.getSongRating(song.id, serverId) ?: 0
	}

	suspend fun starSong(song: DomainSong) {
		val serverId = SessionManager.activeServerId.value ?: return
		val starredEntity = song.toEntity().copy(
			serverId = serverId,
			starredAt = Clock.System.now()
		)
		songDao.insertSong(starredEntity)
		syncManager.enqueueAction(SyncActionType.STAR, song.id)
	}

	suspend fun unstarSong(song: DomainSong) {
		val serverId = SessionManager.activeServerId.value ?: return
		val unstarredEntity = song.toEntity().copy(
			serverId = serverId,
			starredAt = null
		)
		songDao.insertSong(unstarredEntity)
		syncManager.enqueueAction(SyncActionType.UNSTAR, song.id)
	}

	suspend fun rateSong(song: DomainSong, rating: Int) {
		val serverId = SessionManager.activeServerId.value ?: return
		val ratedEntity = song.toEntity().copy(
			serverId = serverId,
			userRating = rating
		)
		songDao.insertSong(ratedEntity)

		when (rating) {
			0 -> syncManager.enqueueAction(SyncActionType.STAR_0, song.id)
			1 -> syncManager.enqueueAction(SyncActionType.STAR_1, song.id)
			2 -> syncManager.enqueueAction(SyncActionType.STAR_2, song.id)
			3 -> syncManager.enqueueAction(SyncActionType.STAR_3, song.id)
			4 -> syncManager.enqueueAction(SyncActionType.STAR_4, song.id)
			5 -> syncManager.enqueueAction(SyncActionType.STAR_5, song.id)
		}
	}
}

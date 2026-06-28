package paige.navic.domain.repositories

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import paige.navic.domain.manager.SyncManager
import paige.navic.data.database.dao.AlbumDao
import paige.navic.data.database.dao.DownloadDao
import paige.navic.data.database.entities.DownloadStatus
import paige.navic.data.database.entities.SyncActionType
import paige.navic.data.database.mappers.toDomainModel
import paige.navic.data.database.mappers.toEntity
import paige.navic.domain.models.DomainAlbum
import paige.navic.domain.models.DomainAlbumSummary
import paige.navic.domain.models.DomainAlbumListType
import paige.navic.ui.core.UiState
import paige.navic.util.core.toSqlQuery
import paige.navic.data.database.mappers.toSummary
import kotlin.time.Clock

class AlbumRepository(
	private val albumDao: AlbumDao,
	private val downloadDao: DownloadDao,
	private val syncManager: SyncManager,
	private val dbRepository: DbRepository
) {
	private suspend fun getLocalData(
		listType: DomainAlbumListType,
		reversed: Boolean
	): ImmutableList<DomainAlbumSummary> {
		if (listType == DomainAlbumListType.Downloaded) {
			val downloadedSongIds = downloadDao.getAllDownloadsList()
				.filter { it.status == DownloadStatus.DOWNLOADED }
				.map { it.songId }
				.toSet()

			return albumDao
				.getAlbumsByQuery(listType.toSqlQuery())
				.filter { it.songs.all { song -> downloadedSongIds.contains(song.songId) } }
				.map { it.album.toSummary() }
				.let { if (reversed) it.asReversed() else it }
				.toImmutableList()
		}

		return albumDao
			.getAlbumEntitiesByQuery(listType.toSqlQuery())
			.map { it.toSummary() }
			.let { if (reversed) it.asReversed() else it }
			.toImmutableList()
	}

	private suspend fun refreshLocalData(
		listType: DomainAlbumListType,
		reversed: Boolean
	): ImmutableList<DomainAlbumSummary> {
		dbRepository.syncLibrarySongs().getOrThrow()
		return getLocalData(listType, reversed)
	}

	fun getAlbumsFlow(
		fullRefresh: Boolean,
		listType: DomainAlbumListType,
		reversed: Boolean
	): Flow<UiState<ImmutableList<DomainAlbumSummary>>> = flow {
		val localData = getLocalData(listType, reversed)
		if (fullRefresh) {
			emit(UiState.Loading(data = localData))
			try {
				emit(UiState.Success(data = refreshLocalData(listType, reversed)))
			} catch (error: Exception) {
				emit(UiState.Error(error = error, data = localData))
			}
		} else {
			emit(UiState.Success(data = localData))
		}
	}.flowOn(Dispatchers.IO)

	suspend fun getAlbumById(albumId: String): DomainAlbum? =
		albumDao.getAlbumById(albumId)?.toDomainModel()

	suspend fun isAlbumStarred(albumId: String) = albumDao.isAlbumStarred(albumId)
	suspend fun getAlbumRating(albumId: String) = albumDao.getAlbumRating(albumId) ?: 0

	suspend fun starAlbum(albumId: String) {
		val album = albumDao.getAlbumById(albumId)?.album ?: return
		val starredEntity = album.copy(
			starredAt = Clock.System.now()
		)
		albumDao.insertAlbum(starredEntity)
		syncManager.enqueueAction(SyncActionType.STAR, albumId)
	}

	suspend fun unstarAlbum(albumId: String) {
		val album = albumDao.getAlbumById(albumId)?.album ?: return
		val unstarredEntity = album.copy(
			starredAt = null
		)
		albumDao.insertAlbum(unstarredEntity)
		syncManager.enqueueAction(SyncActionType.UNSTAR, albumId)
	}

	suspend fun rateAlbum(albumId: String, rating: Int) {
		val album = albumDao.getAlbumById(albumId)?.album ?: return
		val ratedEntity = album.copy(
			userRating = rating
		)
		albumDao.insertAlbum(ratedEntity)
		when (rating) {
			0 -> syncManager.enqueueAction(SyncActionType.STAR_0, albumId)
			1 -> syncManager.enqueueAction(SyncActionType.STAR_1, albumId)
			2 -> syncManager.enqueueAction(SyncActionType.STAR_2, albumId)
			3 -> syncManager.enqueueAction(SyncActionType.STAR_3, albumId)
			4 -> syncManager.enqueueAction(SyncActionType.STAR_4, albumId)
			5 -> syncManager.enqueueAction(SyncActionType.STAR_5, albumId)
		}
	}
}

package paige.navic.domain.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import paige.navic.data.database.SyncManager
import paige.navic.data.database.dao.AlbumDao
import paige.navic.data.database.entities.SyncActionType
import paige.navic.data.database.mappers.toDomainModel
import paige.navic.data.database.mappers.toEntity
import paige.navic.domain.models.DomainAlbum
import paige.navic.domain.models.DomainAlbumListType
import paige.navic.utils.UiState
import paige.navic.utils.sortedByListType
import kotlin.time.Clock

class AlbumRepository(
	private val albumDao: AlbumDao,
	private val syncManager: SyncManager,
	private val dbRepository: DbRepository
) {
	private suspend fun getLocalData(
		listType: DomainAlbumListType
	): List<DomainAlbum> {
		return albumDao
			.getAllAlbumsList()
			.map { it.toDomainModel() }
			.sortedByListType(listType)
	}

	private suspend fun refreshLocalData(
		listType: DomainAlbumListType
	): List<DomainAlbum> {
		dbRepository.syncLibrarySongs().getOrThrow()
		return getLocalData(listType)
	}

	fun getAlbumsFlow(
		fullRefresh: Boolean,
		listType: DomainAlbumListType
	): Flow<UiState<List<DomainAlbum>>> = flow {
		val localData = getLocalData(listType)
		if (fullRefresh) {
			emit(UiState.Loading(data = localData))
			try {
				emit(UiState.Success(data = refreshLocalData(listType)))
			} catch (error: Exception) {
				emit(UiState.Error(error = error, data = localData))
			}
		} else {
			emit(UiState.Success(data = localData))
		}
	}.flowOn(Dispatchers.IO)

	suspend fun isAlbumStarred(album: DomainAlbum) = albumDao.isAlbumStarred(album.id)
	suspend fun starAlbum(album: DomainAlbum) {
		val starredEntity = album.toEntity().copy(
			starredAt = Clock.System.now()
		)
		albumDao.insertAlbum(starredEntity)
		syncManager.enqueueAction(SyncActionType.STAR, album.id)
	}

	suspend fun unstarAlbum(album: DomainAlbum) {
		val unstarredEntity = album.toEntity().copy(
			starredAt = null
		)
		albumDao.insertAlbum(unstarredEntity)
		syncManager.enqueueAction(SyncActionType.UNSTAR, album.id)
	}
}
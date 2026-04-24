package paige.navic.domain.repositories

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import paige.navic.data.database.SyncManager
import paige.navic.data.database.dao.AlbumDao
import paige.navic.data.database.entities.SyncActionType
import paige.navic.data.database.mappers.toDomainModel
import paige.navic.data.database.mappers.toEntity
import paige.navic.data.database.paging.RandomAlbumPagingSource
import paige.navic.domain.models.DomainAlbum
import paige.navic.domain.models.DomainAlbumListType
import kotlin.time.Clock

class AlbumRepository(
	private val albumDao: AlbumDao,
	private val syncManager: SyncManager,
	private val dbRepository: DbRepository
) {
	fun getPagedAlbums(
		listType: DomainAlbumListType,
		reversed: Boolean
	): Flow<PagingData<DomainAlbum>> {

		if (listType == DomainAlbumListType.Random) {
			return flow {
				val randomIds = albumDao.getRandomAlbumIds()

				val randomPager = Pager(
					config = PagingConfig(
						pageSize = 30,
						enablePlaceholders = true,
						prefetchDistance = 15
					),
					pagingSourceFactory = { RandomAlbumPagingSource(albumDao, randomIds) }
				).flow.map { pagingData ->
					pagingData.map { it.toDomainModel() }
				}

				emitAll(randomPager)
			}
		}

		return Pager(
			config = PagingConfig(
				pageSize = 30,
				enablePlaceholders = true,
				prefetchDistance = 15
			),
			pagingSourceFactory = {
				when (listType) {
					DomainAlbumListType.AlphabeticalByName -> {
						if (reversed) albumDao.getAlbumsByNameDesc() else albumDao.getAlbumsByNameAsc()
					}
					DomainAlbumListType.AlphabeticalByArtist -> {
						if (reversed) albumDao.getAlbumsByArtistDesc() else albumDao.getAlbumsByArtistAsc()
					}
					DomainAlbumListType.Newest -> {
						if (reversed) albumDao.getAlbumsOldest() else albumDao.getAlbumsNewest()
					}
					DomainAlbumListType.Frequent -> {
						if (reversed) albumDao.getAlbumsInfrequent() else albumDao.getAlbumsFrequent()
					}
					DomainAlbumListType.Recent -> {
						if (reversed) albumDao.getAlbumsStale() else albumDao.getAlbumsRecent()
					}
					DomainAlbumListType.Starred -> albumDao.getStarredAlbums()
					DomainAlbumListType.Downloaded -> albumDao.getDownloadedAlbums()
					else -> albumDao.getAlbumsByArtistAsc()
				}
			}
		).flow.map { pagingData ->
			pagingData.map { it.toDomainModel() }
		}
	}

	suspend fun syncLibrary() {
		dbRepository.syncLibrarySongs().getOrThrow()
	}

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

package paige.navic.domain.repositories

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import paige.navic.data.database.SyncManager
import paige.navic.data.database.dao.AlbumDao
import paige.navic.data.database.entities.SyncActionType
import paige.navic.data.database.mappers.toDomainModel
import paige.navic.data.database.mappers.toEntity
import paige.navic.data.database.paging.RandomAlbumPagingSource
import paige.navic.data.session.SessionManager
import paige.navic.domain.models.DomainAlbum
import paige.navic.domain.models.DomainAlbumListType
import kotlin.time.Clock

class AlbumRepository(
	private val albumDao: AlbumDao,
	private val syncManager: SyncManager,
	private val dbRepository: DbRepository
) {
	@OptIn(ExperimentalCoroutinesApi::class)
	fun getPagedAlbums(
		listType: DomainAlbumListType,
		reversed: Boolean
	): Flow<PagingData<DomainAlbum>> {

		return SessionManager.activeServerId.filterNotNull().flatMapLatest { serverId ->
			if (listType == DomainAlbumListType.Random) {
				return@flatMapLatest flow {
					val randomIds = albumDao.getRandomAlbumIds(serverId)

					val randomPager = Pager(
						config = PagingConfig(
							pageSize = 30,
							enablePlaceholders = true,
							prefetchDistance = 15
						),
						pagingSourceFactory = { RandomAlbumPagingSource(albumDao, serverId, randomIds) }
					).flow.map { pagingData ->
						pagingData.map { it.toDomainModel() }
					}

					emitAll(randomPager)
				}
			}

		Pager(
			config = PagingConfig(
				pageSize = 30,
				enablePlaceholders = true,
				prefetchDistance = 15
			),
			pagingSourceFactory = {
				when (listType) {
					DomainAlbumListType.AlphabeticalByName -> {
						if (reversed) albumDao.getAlbumsByNameDesc(serverId) else albumDao.getAlbumsByNameAsc(serverId)
					}
					DomainAlbumListType.AlphabeticalByArtist -> {
						if (reversed) albumDao.getAlbumsByArtistDesc(serverId) else albumDao.getAlbumsByArtistAsc(serverId)
					}
					DomainAlbumListType.Newest -> {
						if (reversed) albumDao.getAlbumsOldest(serverId) else albumDao.getAlbumsNewest(serverId)
					}
					DomainAlbumListType.Frequent -> {
						if (reversed) albumDao.getAlbumsInfrequent(serverId) else albumDao.getAlbumsFrequent(serverId)
					}
					DomainAlbumListType.Recent -> {
						if (reversed) albumDao.getAlbumsStale(serverId) else albumDao.getAlbumsRecent(serverId)
					}
					DomainAlbumListType.Starred -> albumDao.getStarredAlbums(serverId)
					DomainAlbumListType.Downloaded -> albumDao.getDownloadedAlbums(serverId)
					is DomainAlbumListType.ByGenre -> {
						if (reversed) albumDao.getAlbumsByGenreReversed(listType.genre) else albumDao.getAlbumsByGenre(listType.genre)
					}
					else -> albumDao.getAlbumsByArtistAsc(serverId)
				}
			}
			).flow.map { pagingData ->
				pagingData.map { it.toDomainModel() }
			}
		}
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	fun getPagedAlbumsByArtist(artistId: String): Flow<PagingData<DomainAlbum>> {
		return SessionManager.activeServerId.filterNotNull().flatMapLatest { serverId ->
			Pager(
				config = PagingConfig(
					pageSize = 20,
					enablePlaceholders = false
				),
				pagingSourceFactory = { albumDao.getAlbumsByArtistPaging(artistId, serverId) }
			).flow.map { pagingData ->
				pagingData.map { it.toDomainModel() }
			}
		}
	}

	suspend fun syncLibrary() {
		dbRepository.syncLibrarySongs().getOrThrow()
	}

	suspend fun isAlbumStarred(album: DomainAlbum): Boolean {
		val serverId = SessionManager.activeServerId.value ?: return false
		return albumDao.isAlbumStarred(album.id, serverId)
	}

	suspend fun getAlbumRating(album: DomainAlbum): Int {
		val serverId = SessionManager.activeServerId.value ?: return 0
		return albumDao.getAlbumRating(album.id, serverId) ?: 0
	}

	suspend fun starAlbum(album: DomainAlbum) {
		val serverId = SessionManager.activeServerId.value ?: return
		val starredEntity = album.toEntity().copy(
			serverId = serverId,
			starredAt = Clock.System.now()
		)
		albumDao.insertAlbum(starredEntity)
		syncManager.enqueueAction(SyncActionType.STAR, album.id)
	}

	suspend fun unstarAlbum(album: DomainAlbum) {
		val serverId = SessionManager.activeServerId.value ?: return
		val unstarredEntity = album.toEntity().copy(
			serverId = serverId,
			starredAt = null
		)
		albumDao.insertAlbum(unstarredEntity)
		syncManager.enqueueAction(SyncActionType.UNSTAR, album.id)
	}

	suspend fun rateAlbum(album: DomainAlbum, rating: Int) {
		val serverId = SessionManager.activeServerId.value ?: return
		val ratedEntity = album.toEntity().copy(
			serverId = serverId,
			userRating = rating
		)
		albumDao.insertAlbum(ratedEntity)

		when (rating) {
			0 -> syncManager.enqueueAction(SyncActionType.STAR_0, album.id)
			1 -> syncManager.enqueueAction(SyncActionType.STAR_1, album.id)
			2 -> syncManager.enqueueAction(SyncActionType.STAR_2, album.id)
			3 -> syncManager.enqueueAction(SyncActionType.STAR_3, album.id)
			4 -> syncManager.enqueueAction(SyncActionType.STAR_4, album.id)
			5 -> syncManager.enqueueAction(SyncActionType.STAR_5, album.id)
		}
	}
}

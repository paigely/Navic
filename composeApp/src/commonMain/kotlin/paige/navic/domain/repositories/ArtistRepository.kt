package paige.navic.domain.repositories

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import paige.navic.data.database.SyncManager
import paige.navic.data.database.dao.ArtistDao
import paige.navic.data.database.entities.SyncActionType
import paige.navic.data.database.mappers.toDomainModel
import paige.navic.data.database.mappers.toEntity
import paige.navic.data.session.SessionManager
import paige.navic.domain.models.DomainArtist
import paige.navic.domain.models.DomainArtistListType
import kotlin.time.Clock

class ArtistRepository(
	private val artistDao: ArtistDao,
	private val syncManager: SyncManager,
	private val dbRepository: DbRepository
) {
	@OptIn(ExperimentalCoroutinesApi::class)
	fun getArtistsCount(listType: DomainArtistListType): Flow<Int> {
		return SessionManager.activeServerId.filterNotNull().flatMapLatest { serverId ->
			when (listType) {
				DomainArtistListType.AlphabeticalByName,
				DomainArtistListType.Random -> artistDao.getArtistsCountFlow(serverId)

				DomainArtistListType.Starred -> artistDao.getStarredArtistsCountFlow(serverId)
			}
		}
	}
	@OptIn(ExperimentalCoroutinesApi::class)
	fun getArtistsPaging(
		listType: DomainArtistListType
	): Flow<PagingData<DomainArtist>> {
		return SessionManager.activeServerId.filterNotNull().flatMapLatest { serverId ->
			Pager(
				config = PagingConfig(
					pageSize = 30,
					enablePlaceholders = false,
					prefetchDistance = 15
				),
				pagingSourceFactory = {
					when (listType) {
						DomainArtistListType.AlphabeticalByName -> artistDao.getArtistsAlphabeticalByNamePaging(
							serverId
						)

						DomainArtistListType.Random -> artistDao.getArtistsRandomPaging(serverId)
						DomainArtistListType.Starred -> artistDao.getArtistsStarredPaging(serverId)
					}
				}
			).flow.map { pagingData ->
				pagingData.map { it.toDomainModel() }
			}
		}
	}

	suspend fun syncArtists() {
		dbRepository.syncArtists().getOrThrow()
	}

	suspend fun isArtistStarred(artist: DomainArtist): Boolean {
		val serverId = SessionManager.activeServerId.value ?: return false
		return artistDao.isArtistStarred(artist.id, serverId)
	}

	suspend fun starArtist(artist: DomainArtist) {
		val serverId = SessionManager.activeServerId.value ?: return
		val starredEntity = artist.toEntity().copy(
			serverId = serverId,
			starredAt = Clock.System.now()
		)
		artistDao.insertArtist(starredEntity)
		syncManager.enqueueAction(SyncActionType.STAR, artist.id)
	}

	suspend fun unstarArtist(artist: DomainArtist) {
		val serverId = SessionManager.activeServerId.value ?: return
		val unstarredEntity = artist.toEntity().copy(
			serverId = serverId,
			starredAt = null
		)
		artistDao.insertArtist(unstarredEntity)
		syncManager.enqueueAction(SyncActionType.UNSTAR, artist.id)
	}
}

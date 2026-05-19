package paige.navic.domain.repositories

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import paige.navic.data.database.SyncManager
import paige.navic.data.database.dao.ArtistDao
import paige.navic.data.database.entities.SyncActionType
import paige.navic.data.database.mappers.toDomainModel
import paige.navic.data.database.mappers.toEntity
import paige.navic.domain.models.DomainArtist
import paige.navic.domain.models.DomainArtistListType
import kotlin.time.Clock

class ArtistRepository(
	private val artistDao: ArtistDao,
	private val syncManager: SyncManager,
	private val dbRepository: DbRepository
) {
	fun getArtistsCount(listType: DomainArtistListType): Flow<Int> {
		return when (listType) {
			DomainArtistListType.AlphabeticalByName,
			DomainArtistListType.Random -> artistDao.getArtistsCountFlow()
			DomainArtistListType.Starred -> artistDao.getStarredArtistsCountFlow()
		}
	}
	fun getArtistsPaging(
		listType: DomainArtistListType
	): Flow<PagingData<DomainArtist>> {
		return Pager(
			config = PagingConfig(
				pageSize = 50,
				enablePlaceholders = false
			),
			pagingSourceFactory = {
				when (listType) {
					DomainArtistListType.AlphabeticalByName -> artistDao.getArtistsAlphabeticalByNamePaging()
					DomainArtistListType.Random -> artistDao.getArtistsRandomPaging()
					DomainArtistListType.Starred -> artistDao.getArtistsStarredPaging()
				}
			}
		).flow.map { pagingData ->
			pagingData.map { it.toDomainModel() }
		}
	}

	suspend fun syncArtists() {
		dbRepository.syncArtists().getOrThrow()
	}

	suspend fun isArtistStarred(artistId: String) = artistDao.isArtistStarred(artistId)

	suspend fun starArtist(artist: DomainArtist) {
		val starredEntity = artist.toEntity().copy(
			starredAt = Clock.System.now()
		)
		artistDao.insertArtist(starredEntity)
		syncManager.enqueueAction(SyncActionType.STAR, artist.id)
	}

	suspend fun unstarArtist(artist: DomainArtist) {
		val unstarredEntity = artist.toEntity().copy(
			starredAt = null
		)
		artistDao.insertArtist(unstarredEntity)
		syncManager.enqueueAction(SyncActionType.UNSTAR, artist.id)
	}
}

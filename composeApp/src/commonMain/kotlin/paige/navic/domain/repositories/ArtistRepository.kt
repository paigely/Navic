package paige.navic.domain.repositories

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import paige.navic.data.database.SyncManager
import paige.navic.data.database.dao.ArtistDao
import paige.navic.data.database.entities.SyncActionType
import paige.navic.data.database.mappers.toDomainModel
import paige.navic.data.database.mappers.toEntity
import paige.navic.domain.models.DomainArtist
import paige.navic.utils.UiState
import kotlin.time.Clock

enum class ArtistListType {
	AlphabeticalByName, Random, Starred
}

class ArtistRepository(
	private val artistDao: ArtistDao,
	private val syncManager: SyncManager,
	private val dbRepository: DbRepository
) {
	private suspend fun getLocalData(
		listType: ArtistListType
	): ImmutableList<DomainArtist> {
		return when (listType) {
			ArtistListType.AlphabeticalByName -> artistDao.getArtistsAlphabeticalByName()
			ArtistListType.Random -> artistDao.getArtistsRandom()
			ArtistListType.Starred -> artistDao.getArtistsStarred()
		}.map { it.toDomainModel() }.toImmutableList()
	}

	private suspend fun refreshLocalData(
		listType: ArtistListType
	): ImmutableList<DomainArtist> {
		dbRepository.syncArtists().getOrThrow()
		return getLocalData(listType)
	}

	fun getArtistsFlow(
		fullRefresh: Boolean,
		listType: ArtistListType
	): Flow<UiState<ImmutableList<DomainArtist>>> = flow {
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

	suspend fun isArtistStarred(artist: DomainArtist) = artistDao.isArtistStarred(artist.id)

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
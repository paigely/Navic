package paige.navic.domain.repositories

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import paige.navic.data.database.SyncManager
import paige.navic.data.database.dao.ArtistDao
import paige.navic.data.database.entities.SyncActionType
import paige.navic.data.database.mappers.toDomainModel
import paige.navic.data.database.mappers.toEntity
import paige.navic.data.session.SessionManager
import paige.navic.domain.models.DomainArtist
import paige.navic.domain.models.DomainArtistListType
import paige.navic.utils.UiState
import kotlin.time.Clock

class ArtistRepository(
	private val artistDao: ArtistDao,
	private val syncManager: SyncManager,
	private val dbRepository: DbRepository
) {
	private suspend fun getLocalData(
		listType: DomainArtistListType,
		serverId: String
	): ImmutableList<DomainArtist> {
		return when (listType) {
			DomainArtistListType.AlphabeticalByName -> artistDao.getArtistsAlphabeticalByName(serverId)
			DomainArtistListType.Random -> artistDao.getArtistsRandom(serverId)
			DomainArtistListType.Starred -> artistDao.getArtistsStarred(serverId)
		}.map { it.toDomainModel() }.toImmutableList()
	}

	private suspend fun refreshLocalData(
		listType: DomainArtistListType,
		serverId: String
	): ImmutableList<DomainArtist> {
		dbRepository.syncArtists().getOrThrow()
		return getLocalData(listType, serverId)
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	fun getArtistsFlow(
		fullRefresh: Boolean,
		listType: DomainArtistListType
	): Flow<UiState<ImmutableList<DomainArtist>>> = SessionManager.activeServerId.flatMapLatest { serverId ->
		flow {
			if (serverId == null) {
				emit(UiState.Success(data = emptyList<DomainArtist>().toImmutableList()))
				return@flow
			}

			val localData = getLocalData(listType, serverId)

			if (fullRefresh) {
				emit(UiState.Loading(data = localData))
				try {
					emit(UiState.Success(data = refreshLocalData(listType, serverId)))
				} catch (error: Exception) {
					emit(UiState.Error(error = error, data = localData))
				}
			} else {
				emit(UiState.Success(data = localData))
			}
		}
	}.flowOn(Dispatchers.IO)

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

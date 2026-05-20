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
import paige.navic.data.database.dao.GenreDao
import paige.navic.data.database.mappers.toDomainModel
import paige.navic.data.session.SessionManager
import paige.navic.domain.models.DomainGenre
import paige.navic.utils.UiState

class GenreRepository(
	private val genreDao: GenreDao,
	private val dbRepository: DbRepository
) {
	private suspend fun getLocalData(serverId: String): ImmutableList<DomainGenre> {
		return genreDao
			.getGenresWithAlbums(serverId)
			.map { it.toDomainModel() }
			.sortedByDescending { it.albums.count() }
			.filter { it.albums.isNotEmpty() }
			.toImmutableList()
	}

	private suspend fun refreshLocalData(serverId: String): ImmutableList<DomainGenre> {
		dbRepository.syncGenres().getOrThrow()
		return getLocalData(serverId)
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	fun getGenresFlow(
		fullRefresh: Boolean
	): Flow<UiState<ImmutableList<DomainGenre>>> = SessionManager.activeServerId.flatMapLatest { serverId ->
		flow {
			if (serverId == null) {
				emit(UiState.Success(data = emptyList<DomainGenre>().toImmutableList()))
				return@flow
			}

			val localData = getLocalData(serverId)

			if (fullRefresh) {
				emit(UiState.Loading(data = localData))
				try {
					emit(UiState.Success(data = refreshLocalData(serverId)))
				} catch (error: Exception) {
					emit(UiState.Error(error = error, data = localData))
				}
			} else {
				emit(UiState.Success(data = localData))
			}
		}
	}.flowOn(Dispatchers.IO)
}

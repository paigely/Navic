package paige.navic.domain.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import paige.navic.data.database.dao.GenreDao
import paige.navic.data.database.mappers.toDomainModel
import paige.navic.domain.models.DomainGenre
import paige.navic.utils.UiState

class GenreRepository(
	private val genreDao: GenreDao,
	private val dbRepository: DbRepository
) {
	private suspend fun getLocalData(): List<DomainGenre> {
		return genreDao
			.getGenresWithAlbums()
			.map { it.toDomainModel() }
			.sortedByDescending { it.albums.count() }
			.filter { it.albums.isNotEmpty() }
	}

	private suspend fun refreshLocalData(): List<DomainGenre> {
		dbRepository.syncGenres().getOrThrow()
		return getLocalData()
	}

	fun getGenresFlow(
		fullRefresh: Boolean
	): Flow<UiState<List<DomainGenre>>> = flow {
		val localData = getLocalData()
		if (fullRefresh) {
			emit(UiState.Loading(data = localData))
			try {
				emit(UiState.Success(data = refreshLocalData()))
			} catch (error: Exception) {
				emit(UiState.Error(error = error, data = localData))
			}
		} else {
			emit(UiState.Success(data = localData))
		}
	}.flowOn(Dispatchers.IO)
}
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
import paige.navic.data.database.dao.RadioDao
import paige.navic.data.database.mappers.toDomainModel
import paige.navic.data.session.SessionManager
import paige.navic.domain.models.DomainRadio
import paige.navic.utils.UiState

class RadioRepository(
	private val radioDao: RadioDao,
	private val dbRepository: DbRepository
) {
	private suspend fun getLocalData(serverId: String): ImmutableList<DomainRadio> {
		return radioDao
			.getRadios(serverId)
			.map { it.toDomainModel() }
			.toImmutableList()
	}

	private suspend fun refreshLocalData(serverId: String): ImmutableList<DomainRadio> {
		dbRepository.syncRadios().getOrThrow()
		return getLocalData(serverId)
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	fun getRadiosFlow(
		fullRefresh: Boolean
	): Flow<UiState<ImmutableList<DomainRadio>>> = SessionManager.activeServerId.flatMapLatest { serverId ->
		flow {
			if (serverId == null) {
				emit(UiState.Success(data = emptyList<DomainRadio>().toImmutableList()))
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

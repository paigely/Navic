package paige.navic.domain.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import paige.navic.data.database.dao.PlaylistDao
import paige.navic.data.database.mappers.toDomainModel
import paige.navic.domain.models.DomainPlaylist
import paige.navic.utils.UiState

class PlaylistRepository(
	private val playlistDao: PlaylistDao,
	private val dbRepository: DbRepository
) {
	private suspend fun getLocalData(): List<DomainPlaylist> {
		return playlistDao
			.getAllPlaylists()
			.map { it.toDomainModel() }
	}

	private suspend fun refreshLocalData(): List<DomainPlaylist> {
		dbRepository.syncPlaylists().getOrThrow().forEach { playlist ->
			dbRepository.syncPlaylistSongs(playlist.playlistId).getOrThrow()
		}
		return getLocalData()
	}

	fun getPlaylistsFlow(fullRefresh: Boolean): Flow<UiState<List<DomainPlaylist>>> = flow {
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

package paige.navic.domain.repositories

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import paige.navic.data.database.dao.PlaylistDao
import paige.navic.data.database.mappers.toDomainModel
import paige.navic.domain.models.DomainPlaylist
import paige.navic.domain.models.DomainPlaylistListType
import paige.navic.utils.UiState

class PlaylistRepository(
	private val playlistDao: PlaylistDao,
	private val dbRepository: DbRepository
) {
	private suspend fun getLocalData(
		listType: DomainPlaylistListType,
		reversed: Boolean
	): ImmutableList<DomainPlaylist> {
		val sorted = when (listType) {
			DomainPlaylistListType.Name -> playlistDao.getAllPlaylistsByName()
			DomainPlaylistListType.DateAdded -> playlistDao.getAllPlaylistsByDateAdded()
			DomainPlaylistListType.Duration -> playlistDao.getAllPlaylistsByDuration()
			DomainPlaylistListType.Random -> playlistDao.getAllPlaylistsRandom()
		}.map { it.toDomainModel() }.toImmutableList()
		return if (reversed) {
			sorted.reversed().toImmutableList()
		} else {
			sorted
		}
	}

	private suspend fun refreshLocalData(
		listType: DomainPlaylistListType,
		reversed: Boolean
	): ImmutableList<DomainPlaylist> {
		dbRepository.syncPlaylists().getOrThrow().forEach { playlist ->
			dbRepository.syncPlaylistSongs(playlist.playlistId).getOrThrow()
		}
		return getLocalData(listType, reversed)
	}

	fun getPlaylistsFlow(
		fullRefresh: Boolean,
		listType: DomainPlaylistListType,
		reversed: Boolean
	): Flow<UiState<ImmutableList<DomainPlaylist>>> = flow {
		val localData = getLocalData(listType, reversed)
		if (fullRefresh) {
			emit(UiState.Loading(data = localData))
			try {
				emit(UiState.Success(data = refreshLocalData(listType, reversed)))
			} catch (error: Exception) {
				emit(UiState.Error(error = error, data = localData))
			}
		} else {
			emit(UiState.Success(data = localData))
		}
	}.flowOn(Dispatchers.IO)
}

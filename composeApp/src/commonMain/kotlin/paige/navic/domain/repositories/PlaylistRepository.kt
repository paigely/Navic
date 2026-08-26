package paige.navic.domain.repositories

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import paige.navic.data.database.dao.DownloadDao
import paige.navic.data.database.dao.PlaylistDao
import paige.navic.data.database.entities.DownloadStatus
import paige.navic.data.database.mappers.toDomainModel
import paige.navic.domain.models.DomainFilter
import paige.navic.domain.models.DomainPlaylist
import paige.navic.domain.models.DomainPlaylistListType
import paige.navic.ui.core.UiState

class PlaylistRepository(
	private val playlistDao: PlaylistDao,
	private val dbRepository: DbRepository,
	private val downloadDao: DownloadDao
) {
	private suspend fun getLocalData(
		listType: DomainPlaylistListType,
		reversed: Boolean,
		filters: Set<DomainFilter> = emptySet()
	): ImmutableList<DomainPlaylist> {
		val playlists = when (listType) {
			DomainPlaylistListType.Name -> playlistDao.getAllPlaylistsByName()
			DomainPlaylistListType.DateAdded -> playlistDao.getAllPlaylistsByDateAdded()
			DomainPlaylistListType.Duration -> playlistDao.getAllPlaylistsByDuration()
			DomainPlaylistListType.Random -> playlistDao.getAllPlaylistsRandom()
		}

		val downloadedIds = if (filters.contains(DomainFilter.Downloaded)) {
			downloadDao.getAllDownloadsList()
				.filter { it.status == DownloadStatus.DOWNLOADED }
				.map { it.songId }
				.toSet()
		} else null

		val filtered = playlists.filter { (_, songs) ->
			filters.all { filter ->
				when (filter) {
					DomainFilter.Starred -> false // not applicable
					DomainFilter.Downloaded -> downloadedIds != null && downloadedIds.containsAll(songs.map { it.song.songId })
				}
			}
		}.map { it.toDomainModel() }

		val sorted = if (reversed) {
			filtered.reversed().toImmutableList()
		} else {
			filtered.toImmutableList()
		}
		return sorted
	}

	private suspend fun refreshLocalData(
		listType: DomainPlaylistListType,
		reversed: Boolean,
		filters: Set<DomainFilter> = emptySet()
	): ImmutableList<DomainPlaylist> {
		dbRepository.syncPlaylists().getOrThrow().forEach { playlist ->
			dbRepository.syncPlaylistSongs(playlist.playlistId).getOrThrow()
		}
		return getLocalData(listType, reversed, filters)
	}

	fun getPlaylistsFlow(
		fullRefresh: Boolean,
		listType: DomainPlaylistListType,
		reversed: Boolean,
		filters: Set<DomainFilter> = emptySet()
	): Flow<UiState<ImmutableList<DomainPlaylist>>> = flow {
		val localData = getLocalData(listType, reversed, filters)
		if (fullRefresh) {
			emit(UiState.Loading(data = localData))
			try {
				emit(UiState.Success(data = refreshLocalData(listType, reversed, filters)))
			} catch (error: Exception) {
				emit(UiState.Error(error = error, data = localData))
			}
		} else {
			emit(UiState.Success(data = localData))
		}
	}.flowOn(Dispatchers.IO)
}

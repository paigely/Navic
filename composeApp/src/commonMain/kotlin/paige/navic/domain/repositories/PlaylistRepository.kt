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
import paige.navic.data.database.dao.DownloadDao
import paige.navic.data.database.dao.PlaylistDao
import paige.navic.data.database.entities.DownloadStatus
import paige.navic.data.database.mappers.toDomainModel
import paige.navic.data.session.SessionManager
import paige.navic.domain.models.DomainPlaylist
import paige.navic.domain.models.DomainPlaylistListType
import paige.navic.utils.UiState

class PlaylistRepository(
	private val playlistDao: PlaylistDao,
	private val dbRepository: DbRepository,
	private val downloadDao: DownloadDao
) {
	private suspend fun getLocalData(
		listType: DomainPlaylistListType,
		reversed: Boolean,
		serverId: String
	): ImmutableList<DomainPlaylist> {
		val sorted = when (listType) {
			DomainPlaylistListType.Name -> playlistDao.getAllPlaylistsByName(serverId)
			DomainPlaylistListType.DateAdded -> playlistDao.getAllPlaylistsByDateAdded(serverId)
			DomainPlaylistListType.Duration -> playlistDao.getAllPlaylistsByDuration(serverId)
			DomainPlaylistListType.Random -> playlistDao.getAllPlaylistsRandom(serverId)
			DomainPlaylistListType.Downloaded -> {
				// Filter downloads by both status AND serverId
				val downloadedSongIds = downloadDao.getAllDownloadsList(serverId)
					.filter { it.status == DownloadStatus.DOWNLOADED }
					.map { it.songId }
					.toSet()

				playlistDao.getAllPlaylistsByDateAdded(serverId).filter { (_, songs) ->
					val playlistSongIds = songs.map { it.song.songId }
					playlistSongIds.isNotEmpty() && downloadedSongIds.containsAll(playlistSongIds)
				}
			}
		}.map { it.toDomainModel() }.toImmutableList()

		return if (reversed) {
			sorted.reversed().toImmutableList()
		} else {
			sorted
		}
	}

	private suspend fun refreshLocalData(
		listType: DomainPlaylistListType,
		reversed: Boolean,
		serverId: String
	): ImmutableList<DomainPlaylist> {
		dbRepository.syncPlaylists().getOrThrow().forEach { playlist ->
			dbRepository.syncPlaylistSongs(playlist.playlistId).getOrThrow()
		}
		return getLocalData(listType, reversed, serverId)
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	fun getPlaylistsFlow(
		fullRefresh: Boolean,
		listType: DomainPlaylistListType,
		reversed: Boolean
	): Flow<UiState<ImmutableList<DomainPlaylist>>> = SessionManager.activeServerId.flatMapLatest { serverId ->
		flow {
			if (serverId == null) {
				emit(UiState.Success(data = emptyList<DomainPlaylist>().toImmutableList()))
				return@flow
			}

			val localData = getLocalData(listType, reversed, serverId)
			if (fullRefresh) {
				emit(UiState.Loading(data = localData))
				try {
					emit(UiState.Success(data = refreshLocalData(listType, reversed, serverId)))
				} catch (error: Exception) {
					emit(UiState.Error(error = error, data = localData))
				}
			} else {
				emit(UiState.Success(data = localData))
			}
		}
	}.flowOn(Dispatchers.IO)
}

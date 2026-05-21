package paige.navic.domain.repositories

import kotlinx.serialization.json.Json
import paige.navic.data.database.dao.PlayerStateDao
import paige.navic.data.database.entities.PlayerStateEntity
import paige.navic.data.database.entities.QueueItemEntity
import paige.navic.domain.models.DomainSong
import paige.navic.shared.PlayerUiState

class PlayerStateRepository(
	private val playerStateDao: PlayerStateDao,
	private val json: Json
) {
	suspend fun loadState(serverId: String): PlayerUiState? {
		val stateEntity = playerStateDao.getPlayerState(serverId) ?: return null
		val queueEntities = playerStateDao.getQueue(serverId)

		return PlayerUiState(
			queue = queueEntities.map { json.decodeFromString<DomainSong>(it.songJson) },
			currentSong = stateEntity.currentSongId?.let { null },
			currentIndex = stateEntity.currentIndex,
			isShuffleEnabled = stateEntity.isShuffleEnabled,
			repeatMode = stateEntity.repeatMode,
			progress = stateEntity.progress,
			playbackSpeed = stateEntity.playbackSpeed
		)
	}

	suspend fun saveState(serverId: String, uiState: PlayerUiState) {
		val stateEntity = PlayerStateEntity(
			serverId = serverId,
			currentSongId = uiState.currentSong?.id,
			currentCollectionId = uiState.currentCollection?.id,
			currentIndex = uiState.currentIndex,
			isShuffleEnabled = uiState.isShuffleEnabled,
			repeatMode = uiState.repeatMode,
			progress = uiState.progress,
			playbackSpeed = uiState.playbackSpeed
		)

		val queueEntities = uiState.queue.mapIndexed { index, song ->
			QueueItemEntity(
				serverId = serverId,
				position = index,
				songJson = json.encodeToString(song)
			)
		}

		playerStateDao.saveFullState(serverId, stateEntity, queueEntities)
	}
}

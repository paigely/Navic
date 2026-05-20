package paige.navic.data.database

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_status_idle
import org.jetbrains.compose.resources.StringResource
import paige.navic.data.database.dao.AlbumDao
import paige.navic.data.database.dao.SyncActionDao
import paige.navic.data.database.entities.SyncActionEntity
import paige.navic.data.database.entities.SyncActionType
import paige.navic.data.models.settings.Settings
import paige.navic.data.session.SessionManager
import paige.navic.domain.repositories.DbRepository
import paige.navic.managers.ConnectivityManager
import paige.navic.managers.SyncScheduler
import paige.navic.shared.Logger
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

data class SyncState(
	val isSyncing: Boolean = false,
	val progress: Float = 0f,
	val message: StringResource = Res.string.info_status_idle
)

class SyncManager(
	private val repository: DbRepository,
	private val syncDao: SyncActionDao,
	private val albumDao: AlbumDao,
	private val connectivityManager: ConnectivityManager,
	private val scheduler: SyncScheduler,
	private val scope: CoroutineScope
) {
	private var syncJob: Job? = null
	private val syncMutex = Mutex()

	private val fullSyncThreshold = 24.hours

	private val _syncState = MutableStateFlow(SyncState())
	val syncState = _syncState.asStateFlow()

	init {
		scope.launch {
			connectivityManager.isOnline.collect { isOnline ->
				if (!syncMutex.isLocked && isOnline) {
					syncMutex.withLock { processQueue() }
				}
			}
		}
	}

	fun startPeriodicSync() {
		Logger.i("SyncManager", "Starting periodic sync cycle.")
		scheduler.schedulePeriodicSync()
		scope.launch {
			val serverId = SessionManager.activeServerId.value ?: return@launch

			if (albumDao.getAlbumCount(serverId) == 0 || Settings.shared.getLastFullSyncTime(serverId) <= 0) {
				Logger.i("SyncManager", "Syncing now because we haven't synced before")
				runSyncCycleInternal()
			}
		}
	}

	fun triggerManualSync() {
		scope.launch {
			val serverId = SessionManager.activeServerId.value ?: return@launch

			Settings.shared.setLastFullSyncTime(serverId, 0)
			runSyncCycleInternal()
		}
	}

	fun stopPeriodicSync() {
		syncJob?.cancel()
		_syncState.value = SyncState(isSyncing = false)
	}

	fun enqueueAction(actionType: SyncActionType, itemId: String) {
		val serverId = SessionManager.activeServerId.value ?: run {
			Logger.e("SyncManager", "Cannot enqueue action: No active server.")
			return
		}

		scope.launch {
			syncDao.enqueue(
				SyncActionEntity(
					actionType = actionType,
					itemId = itemId,
					serverId = serverId
				)
			)

			if (!syncMutex.isLocked) {
				syncMutex.withLock { processQueue() }
			}
		}
	}

	suspend fun runSyncCycleInternal() {
		val serverId = SessionManager.activeServerId.value ?: return

		syncMutex.withLock {
			processQueue()

			val currentTime = Clock.System.now()
			val lastSyncTimeMs = Settings.shared.getLastFullSyncTime(serverId)

			if (currentTime - Instant.fromEpochMilliseconds(lastSyncTimeMs) > fullSyncThreshold) {
				Logger.i("SyncManager", "Starting full library pull for server: $serverId...")

				_syncState.update {
					it.copy(isSyncing = true)
				}

				val result = repository.syncEverything { progress, message ->
					_syncState.update {
						it.copy(isSyncing = true, progress = progress, message = message)
					}
				}

				if (result.isSuccess) {
					Settings.shared.setLastFullSyncTime(serverId, currentTime.toEpochMilliseconds())
					Logger.i("SyncManager", "Full library sync complete for server: $serverId.")
				}

				_syncState.update {
					it.copy(isSyncing = false, message = Res.string.info_status_idle)
				}
			}
		}
	}

	private suspend fun processQueue() {
		val serverId = SessionManager.activeServerId.value ?: return

		val actions = syncDao.getPendingActions(serverId)
		if (actions.isEmpty()) return

		for (action in actions) {
			try {
				when (action.actionType) {
					SyncActionType.STAR -> SessionManager.api.star(action.itemId)
					SyncActionType.UNSTAR -> SessionManager.api.unstar(action.itemId)
					SyncActionType.DELETE_PLAYLIST -> SessionManager.api.deletePlaylist(action.itemId)
					SyncActionType.SCROBBLE -> SessionManager.api.scrobble(action.itemId, submission = true)
					SyncActionType.STAR_0 -> SessionManager.api.setRating(action.itemId, 0)
					SyncActionType.STAR_1 -> SessionManager.api.setRating(action.itemId, 1)
					SyncActionType.STAR_2 -> SessionManager.api.setRating(action.itemId, 2)
					SyncActionType.STAR_3 -> SessionManager.api.setRating(action.itemId, 3)
					SyncActionType.STAR_4 -> SessionManager.api.setRating(action.itemId, 4)
					SyncActionType.STAR_5 -> SessionManager.api.setRating(action.itemId, 5)
				}

				syncDao.removeAction(action.id, serverId)
				Logger.i(
					"SyncManager",
					"Successfully synced ${action.actionType} for ${action.itemId} on server $serverId"
				)

			} catch (e: Exception) {
				Logger.e("SyncManager", "Network failed. Action left in queue.", e)
				break
			}
		}
	}
}

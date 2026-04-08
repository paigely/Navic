package paige.navic.data.database

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import paige.navic.data.database.dao.AlbumDao
import paige.navic.data.database.dao.SyncActionDao
import paige.navic.data.database.entities.SyncActionEntity
import paige.navic.data.database.entities.SyncActionType
import paige.navic.data.models.settings.Settings
import paige.navic.data.session.SessionManager
import paige.navic.domain.repositories.DbRepository
import paige.navic.shared.Logger
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

data class SyncState(
	val isSyncing: Boolean = false,
	val progress: Float = 0f,
	val message: String = ""
)

class SyncManager(
	private val repository: DbRepository,
	private val syncDao: SyncActionDao,
	private val albumDao: AlbumDao,
	private val scope: CoroutineScope
) {
	private var syncJob: Job? = null
	private val syncMutex = Mutex()

	private val fullSyncThreshold = 1.hours

	private val _syncState = MutableStateFlow(SyncState())
	val syncState = _syncState.asStateFlow()

	fun startPeriodicSync() {
		Logger.i("SyncManager", "Starting periodic sync cicle.")
		if (syncJob?.isActive == true) return

		scope.launch {
			if (albumDao.getAllAlbumsList().isEmpty()
				|| Settings.shared.lastFullSyncTime <= 0L) {
				Logger.i("SyncManager", "Syncing now because we haven't synced before")
				runSyncCycle()
			}
		}

		syncJob = scope.launch {
			while (isActive) {
				runSyncCycle()
				delay(15.minutes)
			}
		}
	}

	fun triggerManualSync() {
		scope.launch {
			Settings.shared.lastFullSyncTime = 0
			runSyncCycle()
		}
	}

	fun stopPeriodicSync() {
		syncJob?.cancel()
		_syncState.value = SyncState(isSyncing = false)
	}

	fun enqueueAction(actionType: SyncActionType, itemId: String) {
		scope.launch {
			syncDao.enqueue(SyncActionEntity(actionType = actionType, itemId = itemId))
			if (!syncMutex.isLocked) {
				syncMutex.withLock { processQueue() }
			}
		}
	}

	private suspend fun runSyncCycle() {
		syncMutex.withLock {
			processQueue()

			val currentTime = Clock.System.now()
			if (currentTime - Instant.fromEpochMilliseconds(Settings.shared.lastFullSyncTime) > fullSyncThreshold) {
				Logger.i("SyncManager", "Starting full library pull...")

				_syncState.update {
					it.copy(isSyncing = true, message = "Starting sync...")
				}

				val result = repository.syncEverything { progress, message ->
					_syncState.update {
						it.copy(isSyncing = true, progress = progress, message = message)
					}
				}

				if (result.isSuccess) {
					Settings.shared.lastFullSyncTime = currentTime.toEpochMilliseconds()
					Logger.i("SyncManager", "Full library sync complete.")
				}

				_syncState.update {
					it.copy(isSyncing = false)
				}
			}
		}
	}

	private suspend fun processQueue() {
		val actions = syncDao.getPendingActions()
		if (actions.isEmpty()) return

		for (action in actions) {
			try {
				when (action.actionType) {
					SyncActionType.STAR -> SessionManager.api.star(action.itemId)
					SyncActionType.UNSTAR -> SessionManager.api.unstar(action.itemId)
					SyncActionType.DELETE_PLAYLIST -> SessionManager.api.deletePlaylist(action.itemId)
				}

				syncDao.removeAction(action.id)
				Logger.i(
					"SyncManager",
					"Successfully synced ${action.actionType} for ${action.itemId}"
				)

			} catch (e: Exception) {
				Logger.e("SyncManager", "Network failed. Action left in queue.", e)
				break
			}
		}
	}
}

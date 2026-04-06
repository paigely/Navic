package paige.navic.ui.screens.settings.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import paige.navic.data.database.SyncManager
import paige.navic.data.database.dao.SyncActionDao
import paige.navic.domain.repositories.DbRepository
import paige.navic.managers.DownloadManager

class SettingsDataStorageViewModel(
	private val syncManager: SyncManager,
	private val dbRepository: DbRepository,
	private val syncDao: SyncActionDao,
	private val downloadManager: DownloadManager
) : ViewModel() {

	val syncState = syncManager.syncState
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5000),
			initialValue = syncManager.syncState.value
		)

	private val _pendingActionCount = MutableStateFlow(0)
	val pendingActionCount = _pendingActionCount.asStateFlow()

	val downloadCount = downloadManager.downloadCount
	val downloadSize = downloadManager.downloadSize

	init {
		loadPendingActions()
	}

	private fun loadPendingActions() {
		viewModelScope.launch(Dispatchers.IO) {
			_pendingActionCount.value = syncDao.getPendingActions().size
		}
	}

	fun triggerManualSync() {
		syncManager.triggerManualSync()
	}

	fun rebuildDatabase() {
		viewModelScope.launch(Dispatchers.IO) {
			dbRepository.removeEverything()
			syncManager.stopPeriodicSync()
			_pendingActionCount.value = 0
		}
		triggerManualSync()
	}

	fun removeAllActions() {
		viewModelScope.launch(Dispatchers.IO) {
			syncDao.clearAllActions()
			_pendingActionCount.value = 0
		}
	}

	fun clearAllDownloads() {
		downloadManager.clearAllDownloads()
	}
}
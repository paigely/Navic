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
import paige.navic.domain.repositories.SongRepository
import paige.navic.managers.ConnectivityManager
import paige.navic.managers.DownloadManager

class SettingsDataStorageViewModel(
	private val syncManager: SyncManager,
	private val dbRepository: DbRepository,
	private val syncDao: SyncActionDao,
	private val downloadManager: DownloadManager,
	private val songRepository: SongRepository,
	connectivityManager: ConnectivityManager
) : ViewModel() {

	val syncState = syncManager.syncState
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5000),
			initialValue = syncManager.syncState.value
		)

	private val _pendingActionCount = MutableStateFlow(0)
	val pendingActionCount = _pendingActionCount.asStateFlow()

	val downloadCount = downloadManager.downloadCount.stateIn(
		viewModelScope, SharingStarted.WhileSubscribed(5000), 0
	)
	val downloadSize = downloadManager.downloadSize.stateIn(
		viewModelScope, SharingStarted.WhileSubscribed(5000), 0L
	)

	val isDownloadingLibrary = downloadManager.isDownloadingLibrary
	val libraryDownloadProgress = downloadManager.libraryDownloadProgress
	val isOnline = connectivityManager.isOnline

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

	fun downloadEntireLibrary() {
		viewModelScope.launch(Dispatchers.IO) {
			val allSongs = songRepository.getAllSongs()
			downloadManager.downloadEntireLibrary(allSongs)
		}
	}

	fun cancelLibraryDownload() {
		downloadManager.cancelAllActiveDownloads()
	}
}

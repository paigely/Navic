package paige.navic.ui.screens.queue.viewmodels

import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import paige.navic.managers.ConnectivityManager
import paige.navic.managers.DownloadManager

class QueueViewModel(
	connectivityManager: ConnectivityManager,
	downloadManager: DownloadManager
) : ViewModel() {
	val listState = LazyListState()
	val isOnline = connectivityManager.isOnline
	val downloadedSongs = downloadManager.downloadedSongs
}

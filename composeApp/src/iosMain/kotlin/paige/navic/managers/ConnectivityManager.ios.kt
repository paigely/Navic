package paige.navic.managers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import paige.navic.data.models.settings.Settings
import paige.navic.data.models.settings.enums.OfflineMode
import paige.navic.data.session.SessionManager
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.darwin.dispatch_get_main_queue

actual class ConnectivityManager(
	scope: CoroutineScope,
	dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
	@OptIn(ExperimentalCoroutinesApi::class)
	actual val isOnline: StateFlow<Boolean> = callbackFlow {
		val monitor = nw_path_monitor_create()
		nw_path_monitor_set_update_handler(monitor) { path ->
			val status = nw_path_get_status(path)
			trySend(
				// TODO: handle OfflineMode.NoWifi
				if (Settings.shared.offlineMode == OfflineMode.Forced)
					false
				else
					status == nw_path_status_satisfied
			)
		}
		nw_path_monitor_set_queue(monitor, dispatch_get_main_queue())
		nw_path_monitor_start(monitor)

		awaitClose {
			nw_path_monitor_cancel(monitor)
		}
	}
		.mapLatest { isDeviceOnline ->
			if (isDeviceOnline) {
				try {
					SessionManager.api.ping()
					true
				} catch (_: Exception) {
					false
				}
			} else {
				false
			}
		}
		.distinctUntilChanged()
		.flowOn(dispatcher)
		.stateIn(scope, SharingStarted.WhileSubscribed(5000), true)
}

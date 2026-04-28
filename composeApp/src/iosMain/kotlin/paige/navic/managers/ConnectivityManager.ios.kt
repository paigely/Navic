package paige.navic.managers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import paige.navic.data.models.settings.Settings
import paige.navic.data.models.settings.enums.OfflineMode
import platform.Network.nw_interface_type_cellular
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.Network.nw_path_uses_interface_type
import platform.darwin.dispatch_get_main_queue

private data class NetworkStatus(
	val isOnline: Boolean = false,
	val isCellular: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
actual class ConnectivityManager(
	scope: CoroutineScope,
	dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
	private val started = SharingStarted.WhileSubscribed(5000)

	private val networkStatus = callbackFlow {
		val monitor = nw_path_monitor_create()

		nw_path_monitor_set_update_handler(monitor) { path ->
			trySend(NetworkStatus(
				isOnline = nw_path_get_status(path) == nw_path_status_satisfied,
				isCellular = nw_path_uses_interface_type(path, nw_interface_type_cellular)
			))
		}
		nw_path_monitor_set_queue(monitor, dispatch_get_main_queue())
		nw_path_monitor_start(monitor)

		awaitClose { nw_path_monitor_cancel(monitor) }
	}.stateIn(scope, started, NetworkStatus())

	actual val isCellular = networkStatus
		.map { it.isCellular }
		.distinctUntilChanged()
		.flowOn(dispatcher)
		.stateIn(scope, started, false)

	actual val isOnline = networkStatus
		.mapLatest { status ->
			when (Settings.shared.offlineMode) {
				OfflineMode.Forced -> false
				OfflineMode.NoWiFi -> status.isOnline && !status.isCellular
				else -> status.isOnline
			}
		}
		.distinctUntilChanged()
		.flowOn(dispatcher)
		.stateIn(scope, started, true)
}

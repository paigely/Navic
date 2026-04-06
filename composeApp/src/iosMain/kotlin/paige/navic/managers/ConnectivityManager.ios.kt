package paige.navic.managers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import platform.Network.*
import platform.darwin.dispatch_get_main_queue

actual class ConnectivityManager(
	scope: CoroutineScope,
	dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
	actual val isOnline: StateFlow<Boolean> = callbackFlow {
		val monitor = nw_path_monitor_create()
		nw_path_monitor_set_update_handler(monitor) { path ->
			val status = nw_path_get_status(path)
			trySend(status == nw_path_status_satisfied)
		}
		nw_path_monitor_set_queue(monitor, dispatch_get_main_queue())
		nw_path_monitor_start(monitor)

		awaitClose {
			nw_path_monitor_cancel(monitor)
		}
	}
		.distinctUntilChanged()
		.flowOn(dispatcher)
		.stateIn(scope, SharingStarted.WhileSubscribed(5000), true)
}

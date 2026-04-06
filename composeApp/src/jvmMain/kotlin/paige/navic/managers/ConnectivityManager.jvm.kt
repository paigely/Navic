package paige.navic.managers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import java.net.NetworkInterface

actual class ConnectivityManager(
	scope: CoroutineScope,
	dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
	actual val isOnline: StateFlow<Boolean> = flow {
		while (true) {
			val interfaces = NetworkInterface.getNetworkInterfaces()
			var isOnline = false
			while (interfaces != null && interfaces.hasMoreElements()) {
				val iface = interfaces.nextElement()
				if (iface.isUp && !iface.isLoopback) {
					isOnline = true
					break
				}
			}
			emit(isOnline)
			delay(5000)
		}
	}
		.distinctUntilChanged()
		.flowOn(dispatcher)
		.stateIn(scope, SharingStarted.WhileSubscribed(5000), true)
}

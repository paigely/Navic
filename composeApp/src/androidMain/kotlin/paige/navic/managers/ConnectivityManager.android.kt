package paige.navic.managers

import android.content.Context
import android.net.ConnectivityManager as AndroidConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn

actual class ConnectivityManager(
	context: Context,
	scope: CoroutineScope,
	dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
	private val connectivityManager =
		context.getSystemService(Context.CONNECTIVITY_SERVICE) as AndroidConnectivityManager

	actual val isOnline: StateFlow<Boolean> = callbackFlow {
		val callback = object : AndroidConnectivityManager.NetworkCallback() {
			override fun onAvailable(network: Network) {
				trySend(true)
			}

			override fun onLost(network: Network) {
				trySend(false)
			}
		}

		val request = NetworkRequest.Builder()
			.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
			.build()

		connectivityManager.registerNetworkCallback(request, callback)

		val isCurrentlyOnline = connectivityManager.activeNetwork?.let { network ->
			connectivityManager.getNetworkCapabilities(network)
				?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
		} ?: false

		trySend(isCurrentlyOnline)

		awaitClose {
			connectivityManager.unregisterNetworkCallback(callback)
		}
	}
		.distinctUntilChanged()
		.flowOn(dispatcher)
		.stateIn(scope, SharingStarted.WhileSubscribed(5000), true)
}

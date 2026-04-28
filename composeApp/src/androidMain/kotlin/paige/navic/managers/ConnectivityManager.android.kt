package paige.navic.managers

import android.content.Context
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import paige.navic.data.session.SessionManager
import android.net.ConnectivityManager as AndroidConnectivityManager
import paige.navic.data.models.settings.Settings
import paige.navic.data.models.settings.enums.OfflineMode

@OptIn(ExperimentalCoroutinesApi::class)
actual class ConnectivityManager(
	context: Context,
	scope: CoroutineScope,
	dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
	private val connectivityManager =
		context.getSystemService(Context.CONNECTIVITY_SERVICE) as AndroidConnectivityManager

	val isOnCellular = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)?.hasTransport(
		NetworkCapabilities.TRANSPORT_CELLULAR
	) ?: false

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

		val isCurrentlyOnline = 
			if (Settings.shared.offlineMode == OfflineMode.Forced) false
			else connectivityManager.activeNetwork?.let { network ->
				if (Settings.shared.offlineMode == OfflineMode.NoWiFi)
					connectivityManager.getNetworkCapabilities(network)
						?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
					else connectivityManager.getNetworkCapabilities(network)
						?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
			} ?: false

		trySend(isCurrentlyOnline)

		awaitClose {
			connectivityManager.unregisterNetworkCallback(callback)
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

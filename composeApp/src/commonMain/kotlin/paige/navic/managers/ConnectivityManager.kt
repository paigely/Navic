package paige.navic.managers

import kotlinx.coroutines.flow.StateFlow

expect class ConnectivityManager {
	val isOnline: StateFlow<Boolean>
}

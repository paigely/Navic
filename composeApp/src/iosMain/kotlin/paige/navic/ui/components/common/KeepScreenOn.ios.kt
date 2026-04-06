package paige.navic.ui.components.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import platform.UIKit.UIApplication

@Composable
actual fun KeepScreenOn() {
	DisposableEffect(Unit) {
		UIApplication.sharedApplication.idleTimerDisabled = true
		onDispose {
			UIApplication.sharedApplication.idleTimerDisabled = false
		}
	}
}

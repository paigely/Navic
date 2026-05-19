package paige.navic

import androidx.compose.ui.window.ComposeUIViewController
import org.koin.mp.KoinPlatformTools
import paige.navic.managers.IOSSyncScheduler

@Suppress("FunctionName", "unused")
fun MainViewController() = ComposeUIViewController {
	KoinPlatformTools.defaultContext().getOrNull()?.getOrNull<IOSSyncScheduler>()?.schedulePeriodicSync()

	App()
}

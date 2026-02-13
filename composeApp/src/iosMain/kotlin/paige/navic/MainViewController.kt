package paige.navic

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.ui.window.ComposeUIViewController
import paige.navic.shared.ShareServiceProvider

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
fun MainViewController() = ComposeUIViewController {
	ShareServiceProvider.initialize(IosShareService())
	App()
}

annotation class IosShareService

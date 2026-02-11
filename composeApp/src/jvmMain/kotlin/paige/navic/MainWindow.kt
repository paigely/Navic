package paige.navic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.app_name
import navic.composeapp.generated.resources.icon
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import paige.navic.data.models.Settings
import java.awt.Dimension

@Composable
fun MainWindow(
	window: ComposeWindow?,
	onSetWindow: (ComposeWindow?) -> Unit
) {
	Window(
		title = stringResource(Res.string.app_name),
		icon = painterResource(Res.drawable.icon),
		state = rememberSavedWindowState(),
		onCloseRequest = {
			window?.isVisible = false
		}
	) {
		this@Window.window.minimumSize = Dimension(800, 600)
		App()

		DisposableEffect(Unit) {
			onSetWindow(this@Window.window)
			onDispose {
				onSetWindow(null)
			}
		}

		SideEffect {
			with(this@Window.window.rootPane) {
				putClientProperty("apple.awt.fullWindowContent", true)
				putClientProperty("apple.awt.transparentTitleBar", true)
				putClientProperty("apple.awt.windowTitleVisible", false)
			}
		}
	}
}

@Composable
private fun rememberSavedWindowState(): WindowState {
	val windowState = rememberWindowState(
		placement = WindowPlacement.entries
			.getOrNull(Settings.shared.windowPlacement) ?: WindowPlacement.Floating,
		position = WindowPosition(
			x = Settings.shared.windowPositionX.dp,
			y = Settings.shared.windowPositionY.dp
		),
		size = DpSize(
			width = Settings.shared.windowSizeX.dp,
			height = Settings.shared.windowSizeY.dp
		)
	)

	LaunchedEffect(windowState) {
		snapshotFlow { windowState.placement }
			.collect { placement ->
				Settings.shared.windowPlacement = placement.ordinal
			}
		snapshotFlow { windowState.position }
			.collect { position ->
				if (position is WindowPosition.Absolute) {
					Settings.shared.windowPositionX = position.x.value
					Settings.shared.windowPositionY = position.y.value
				}
			}
		snapshotFlow { windowState.size }
			.collect { size ->
				Settings.shared.windowSizeX = size.width.value
				Settings.shared.windowSizeY = size.height.value
			}
	}

	return windowState
}

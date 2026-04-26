package paige.navic.ui.components.layouts

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import paige.navic.data.models.settings.Settings
import paige.navic.data.models.settings.enums.ToolbarPosition

@Composable
fun SheetScaffold(
	toolbar: @Composable (windowInsets: WindowInsets) -> Unit,
	toolbarPosition: ToolbarPosition = Settings.shared.nowPlayingToolbarPosition,
	floatingActionButton: @Composable () -> Unit = {},
	content: @Composable (contentPadding: PaddingValues) -> Unit
) {
	Scaffold(
		topBar = {
			if (toolbarPosition == ToolbarPosition.Top) {
				toolbar(WindowInsets.systemBars.only(
					WindowInsetsSides.Horizontal + WindowInsetsSides.Top
				))
			}
		},
		bottomBar = {
			if (toolbarPosition == ToolbarPosition.Bottom) {
				toolbar(WindowInsets.systemBars.only(
					WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
				))
			}
		},
		floatingActionButton = floatingActionButton,
		containerColor = Color.Transparent,
		content = content
	)
}

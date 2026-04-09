package paige.navic.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.option_custom_headers
import navic.composeapp.generated.resources.title_developer
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.data.models.Screen
import paige.navic.icons.Icons
import paige.navic.icons.outlined.ChevronForward
import paige.navic.ui.components.common.Form
import paige.navic.ui.components.common.FormRow
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.utils.fadeFromTop

@Composable
fun SettingsDeveloperScreen() {
	val ctx = LocalCtx.current
	val backStack = LocalNavStack.current

	Scaffold(
		topBar = {
			NestedTopBar(
				{ Text(stringResource(Res.string.title_developer)) },
				hideBack = ctx.sizeClass.widthSizeClass >= WindowWidthSizeClass.Medium
			)
		}
	) { innerPadding ->
		CompositionLocalProvider(
			LocalMinimumInteractiveComponentSize provides 0.dp
		) {
			Column(
				Modifier
					.padding(innerPadding)
					.verticalScroll(rememberScrollState())
					.padding(top = 16.dp, end = 16.dp, start = 16.dp)
					.fadeFromTop()
			) {
				Form {
					FormRow(
						onClick = {
							backStack.lastOrNull()?.let {
								if (it is Screen.Settings.Developer) {
									backStack.add(Screen.Settings.CustomHeaders)
								}
							}
						}
					) {
						Text(stringResource(Res.string.option_custom_headers))
						Icon(Icons.Outlined.ChevronForward, null)
					}
				}
			}
		}
	}
}

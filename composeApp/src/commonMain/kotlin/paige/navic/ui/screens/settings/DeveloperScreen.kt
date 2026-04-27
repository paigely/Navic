package paige.navic.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_cancel
import navic.composeapp.generated.resources.action_ok
import navic.composeapp.generated.resources.action_test_exception_handler
import navic.composeapp.generated.resources.info_exception_handler
import navic.composeapp.generated.resources.option_check_for_updates
import navic.composeapp.generated.resources.option_custom_headers
import navic.composeapp.generated.resources.subtitle_check_for_updates
import navic.composeapp.generated.resources.title_confirm
import navic.composeapp.generated.resources.title_developer
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.data.models.Screen
import paige.navic.data.models.settings.Settings
import paige.navic.icons.Icons
import paige.navic.icons.outlined.ChevronForward
import paige.navic.ui.components.common.Form
import paige.navic.ui.components.common.FormButton
import paige.navic.ui.components.common.FormRow
import paige.navic.ui.components.dialogs.FormDialog
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.screens.settings.components.SettingSwitchRow

@Composable
fun SettingsDeveloperScreen() {
	val ctx = LocalCtx.current
	val backStack = LocalNavStack.current
	var exceptionConfirmationShown by rememberSaveable { mutableStateOf(false) }

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
			) {
				Form {
					SettingSwitchRow(
						title = { Text(stringResource(Res.string.option_check_for_updates)) },
						subtitle = { Text(stringResource(Res.string.subtitle_check_for_updates)) },
						value = Settings.shared.checkForUpdates,
						onSetValue = { Settings.shared.checkForUpdates = it }
					)
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
				Form {
					FormRow(onClick = {
						exceptionConfirmationShown = true
					}) {
						Text(
							text = stringResource(Res.string.action_test_exception_handler),
							color = MaterialTheme.colorScheme.error
						)
					}
				}
			}
		}
	}

	if (exceptionConfirmationShown) {
		FormDialog(
			onDismissRequest = { exceptionConfirmationShown = false },
			title = { Text(stringResource(Res.string.title_confirm)) },
			content = { Text(stringResource(Res.string.info_exception_handler)) },
			buttons = {
				FormButton(
					onClick = {
						exceptionConfirmationShown = false
						throw Error("Testing exception handler")
					},
					color = MaterialTheme.colorScheme.error
				) {
					Text(stringResource(Res.string.action_ok))
				}
				FormButton(
					onClick = {
						exceptionConfirmationShown = false
					}
				) {
					Text(stringResource(Res.string.action_cancel))
				}
			},
		)
	}
}

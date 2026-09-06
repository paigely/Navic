package paige.navic.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_cancel
import navic.composeapp.generated.resources.action_ok
import navic.composeapp.generated.resources.info_app_version
import navic.composeapp.generated.resources.info_update_check
import navic.composeapp.generated.resources.option_check_for_updates
import navic.composeapp.generated.resources.subtitle_check_for_updates
import navic.composeapp.generated.resources.title_about
import navic.composeapp.generated.resources.title_acknowledgements
import navic.composeapp.generated.resources.title_codeberg
import navic.composeapp.generated.resources.title_discord_server
import navic.composeapp.generated.resources.title_github
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import paige.navic.LocalNavStack
import paige.navic.LocalPlatformContext
import paige.navic.domain.manager.PreferenceManager
import paige.navic.icons.Icons
import paige.navic.icons.outlined.ChevronForward
import paige.navic.ui.components.common.Form
import paige.navic.ui.components.common.FormRow
import paige.navic.ui.components.dialogs.LinkConfirmationDialog
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.navigation.Screen
import paige.navic.ui.screens.settings.components.SettingSwitchRow
import paige.navic.util.core.PlatformType

@Composable
fun SettingsAboutScreen() {
	val preferenceManager = koinInject<PreferenceManager>()
	@Suppress("DEPRECATION")
	val clipboard = LocalClipboardManager.current
	val backStack = LocalNavStack.current
	val platformContext = LocalPlatformContext.current
	val hideBack = platformContext.sizeClass.widthSizeClass >= WindowWidthSizeClass.Medium
	var linkToOpen by rememberSaveable { mutableStateOf<String?>(null) }
	var updateDialogIsOpen by rememberSaveable { mutableStateOf(false) }

	Scaffold(
		topBar = {
			NestedTopBar(
				{ Text(stringResource(Res.string.title_about)) },
				hideBack = hideBack
			)
		},
		contentWindowInsets = WindowInsets(0, 0, 0, 0)
	) { innerPadding ->
		Column(
			Modifier
				.fillMaxSize()
				.verticalScroll(rememberScrollState())
				.padding(top = innerPadding.calculateTopPadding())
				.padding(top = 16.dp, end = 16.dp, start = 16.dp)
		) {
			Form {
				SelectionContainer {
					val text = buildString {
						append(platformContext.name + "\n")
						append(
							stringResource(
								Res.string.info_app_version,
								platformContext.appVersion
							)
						)
					}
					FormRow(onClick = {
						clipboard.setText(AnnotatedString(text))
					}) {
						Text(text)
					}
				}
			}

			Form {
				FormRow(onClick = {
					linkToOpen = "https://github.com/ssalggnikool/Navic"
				}) {
					Text(stringResource(Res.string.title_github))
					Icon(Icons.Outlined.ChevronForward, null)
				}
				FormRow(onClick = {
					linkToOpen = "https://codeberg.org/paige/Navic"
				}) {
					Text(stringResource(Res.string.title_codeberg))
					Icon(Icons.Outlined.ChevronForward, null)
				}
				FormRow(onClick = {
					linkToOpen = "https://discord.gg/TBcnNX66PH"
				}) {
					Text(stringResource(Res.string.title_discord_server))
					Icon(Icons.Outlined.ChevronForward, null)
				}
				FormRow(onClick = dropUnlessResumed {
					backStack.add(Screen.Settings.Acknowledgements)
				}) {
					Text(stringResource(Res.string.title_acknowledgements))
					Icon(Icons.Outlined.ChevronForward, null)
				}
			}
			Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
		}
	}

	if (linkToOpen != null) {
		LinkConfirmationDialog(
			linkToOpen = linkToOpen!!,
			onDismissRequest = { linkToOpen = null }
		)
	}

	if (updateDialogIsOpen) {
		AlertDialog(
			text = { Text(stringResource(Res.string.info_update_check)) },
			confirmButton = {
				Button(
					onClick = {
						updateDialogIsOpen = false
						preferenceManager.checkForUpdates = true
					},
					content = { Text(stringResource(Res.string.action_ok)) }
				)
			},
			dismissButton = {
				TextButton(
					onClick = { updateDialogIsOpen = false },
					content = { Text(stringResource(Res.string.action_cancel)) }
				)
			},
			onDismissRequest = {
				updateDialogIsOpen = false
			}
		)
	}
}

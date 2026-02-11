package paige.navic.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_app_version
import navic.composeapp.generated.resources.title_about
import navic.composeapp.generated.resources.title_acknowledgements
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalContentPadding
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.data.models.Screen
import paige.navic.icons.Icons
import paige.navic.icons.outlined.ChevronForward
import paige.navic.ui.components.common.Form
import paige.navic.ui.components.common.FormRow
import paige.navic.ui.components.layouts.NestedTopBar

@Composable
fun SettingsAboutScreen() {
	@Suppress("DEPRECATION")
	val clipboard = LocalClipboardManager.current
	val backStack = LocalNavStack.current
	val ctx = LocalCtx.current
	val hideBack = ctx.sizeClass.widthSizeClass >= WindowWidthSizeClass.Medium
	Scaffold(
		topBar = { NestedTopBar(
			{ Text(stringResource(Res.string.title_about)) },
			hideBack = hideBack
		) },
		contentWindowInsets = WindowInsets.statusBars
	) { innerPadding ->
		Column(
			Modifier
				.padding(innerPadding)
				.verticalScroll(rememberScrollState())
				.padding(top = 12.dp, end = 12.dp, start = 12.dp)
		) {
			Form {
				SelectionContainer {
					val text = buildString {
						append(ctx.name + "\n")
						append(stringResource(Res.string.info_app_version, ctx.appVersion))
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
					backStack.add(Screen.Settings.Acknowledgements)
				}) {
					Text(stringResource(Res.string.title_acknowledgements))
					Icon(Icons.Outlined.ChevronForward, null)
				}
			}
			Spacer(Modifier.height(LocalContentPadding.current.calculateBottomPadding()))
		}
	}
}
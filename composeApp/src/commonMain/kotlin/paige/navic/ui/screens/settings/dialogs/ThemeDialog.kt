package paige.navic.ui.screens.settings.dialogs

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_ok
import navic.composeapp.generated.resources.option_choose_theme
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.data.models.settings.Settings
import paige.navic.data.models.settings.enums.Theme
import paige.navic.data.models.settings.enums.ThemeMode

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ThemeDialog(
	presented: Boolean,
	onDismissRequest: () -> Unit
) {
	if (!presented) return

	val ctx = LocalCtx.current

	AlertDialog(
		title = {
			Text(stringResource(Res.string.option_choose_theme))
		},
		text = {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.verticalScroll(rememberScrollState()),
				verticalArrangement = Arrangement.spacedBy(16.dp)
			) {
				Theme.entries.forEach { theme ->
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.clip(MaterialTheme.shapes.small)
							.clickable {
								ctx.clickSound()
								Settings.shared.theme = theme
								onDismissRequest()
							},
						horizontalArrangement = Arrangement.spacedBy(16.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						RadioButton(
							selected = Settings.shared.theme == theme,
							onClick = null
						)
						Column(Modifier.weight(1f)) {
							Text(
								stringResource(theme.title),
								color = MaterialTheme.colorScheme.onSurface
							)
							Text(
								stringResource(theme.subtitle),
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}
					}
				}
				ThemeModeChooser()
			}
		},
		onDismissRequest = onDismissRequest,
		confirmButton = {
			Button(onClick = {
				ctx.clickSound()
				onDismissRequest()
			}) {
				Text(stringResource(Res.string.action_ok))
			}
		}
	)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ThemeModeChooser() {
	val ctx = LocalCtx.current
	val themes = ThemeMode.entries
	Row(
		modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
		horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
	) {
		themes.forEachIndexed { idx, mode ->
			val checked = Settings.shared.themeMode == mode
			val weight by animateFloatAsState(
				if (checked) 1.25f else 1f
			)
			ToggleButton(
				checked = checked,
				onCheckedChange = { _ ->
					ctx.clickSound()
					Settings.shared.themeMode = mode
				},
				shapes =
					when (idx) {
						0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
						themes.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
						else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
					},
				modifier = Modifier.weight(weight)
			) {
				Text(
					stringResource(mode.title),
					maxLines = 1
				)
			}
		}
	}
}

package paige.navic.ui.components.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_ok
import navic.composeapp.generated.resources.option_choose_theme
import org.jetbrains.compose.resources.stringResource
import paige.navic.data.models.Settings

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ThemeDialog(
	presented: Boolean,
	onDismissRequest: () -> Unit
) {
	if (!presented) return

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
				Settings.Theme.entries.forEach { theme ->
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.clickable(onClick = {
								Settings.shared.theme = theme
								onDismissRequest()
							}),
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
			}
		},
		onDismissRequest = onDismissRequest,
		confirmButton = {
			Button(onClick = onDismissRequest) {
				Text(stringResource(Res.string.action_ok))
			}
		}
	)
}
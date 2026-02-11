package paige.navic.ui.components.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_ok
import navic.composeapp.generated.resources.option_marquee_duration
import org.jetbrains.compose.resources.stringResource
import paige.navic.data.models.Settings

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MarqueeSpeedDialog(
	presented: Boolean,
	onDismissRequest: () -> Unit
) {
	if (!presented) return

	AlertDialog(
		title = {
			Text(stringResource(Res.string.option_marquee_duration))
		},
		text = {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.heightIn(max = 300.dp),
				verticalArrangement = Arrangement.spacedBy(16.dp)
			) {
				Settings.MarqueeSpeed.entries.forEach { speed ->
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.clickable(onClick = {
								Settings.shared.marqueeSpeed = speed
								onDismissRequest()
							}),
						horizontalArrangement = Arrangement.spacedBy(16.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						RadioButton(
							selected = Settings.shared.marqueeSpeed == speed,
							onClick = null
						)
						Text(text = speed.name)
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

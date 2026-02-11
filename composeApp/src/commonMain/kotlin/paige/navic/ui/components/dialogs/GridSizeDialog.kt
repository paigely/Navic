package paige.navic.ui.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kyant.capsule.ContinuousRoundedRectangle
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_ok
import navic.composeapp.generated.resources.option_grid_items_per_row
import org.jetbrains.compose.resources.stringResource
import paige.navic.data.models.Settings

@Composable
fun GridSizeDialog(
	presented: Boolean,
	onDismissRequest: () -> Unit
) {
	if (!presented) return

	AlertDialog(
		title = {
			Text(stringResource(Res.string.option_grid_items_per_row))
		},
		text = {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.heightIn(max = 300.dp),
				verticalArrangement = Arrangement.spacedBy(16.dp)
			) {
				Settings.GridSize.entries.forEach { size ->
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.clickable(onClick = {
								Settings.shared.gridSize = size
								onDismissRequest()
							})
							.padding(8.dp),
						horizontalArrangement = Arrangement.spacedBy(16.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						RadioButton(
							selected = Settings.shared.gridSize == size,
							onClick = null
						)
						GridSizePreview(size = size.value)
						Text(text = size.label, style = MaterialTheme.typography.bodyLarge)
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

@Composable
fun GridSizePreview(
	size: Int
) {
	Column(
		modifier = Modifier.size(48.dp),
		verticalArrangement = Arrangement.spacedBy(2.dp)
	) {
		repeat(size) {
			Row(
				modifier = Modifier.weight(1f),
				horizontalArrangement = Arrangement.spacedBy(2.dp)
			) {
				repeat(size) {
					val shape = ContinuousRoundedRectangle(when (size) {
						2 -> 6.dp
						else -> 4.dp
					})
					Box(
						modifier = Modifier
							.weight(1f)
							.fillMaxHeight()
							.background(MaterialTheme.colorScheme.onPrimary, shape)
							.border(2.dp, MaterialTheme.colorScheme.primary, shape)
					)
				}
			}
		}
	}
}
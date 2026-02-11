package paige.navic.ui.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
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
import com.kyant.capsule.ContinuousRoundedRectangle
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_ok
import navic.composeapp.generated.resources.option_artwork_shape
import org.jetbrains.compose.resources.stringResource
import paige.navic.data.models.Settings

val Shapes = arrayOf(
	"Square" to 0f,
	"Soft" to 16f,
	"Curved" to 32f,
	"Circle" to 200f
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ArtworkShapeDialog(
	presented: Boolean,
	onDismissRequest: () -> Unit
) {
	if (!presented) return

	AlertDialog(
		title = {
			Text(stringResource(Res.string.option_artwork_shape))
		},
		text = {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.heightIn(max = 300.dp),
				verticalArrangement = Arrangement.spacedBy(16.dp)
			) {
				Shapes.forEach { (name, radius) ->
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.clickable(onClick = {
								Settings.shared.artGridRounding = radius
								onDismissRequest()
							}),
						horizontalArrangement = Arrangement.spacedBy(16.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						RadioButton(
							selected = Settings.shared.artGridRounding == radius,
							onClick = null
						)
						val shape = ContinuousRoundedRectangle(radius.dp / 2)
						Box(modifier = Modifier
							.size(48.dp)
							.background(MaterialTheme.colorScheme.onPrimary, shape)
							.border(2.dp, MaterialTheme.colorScheme.primary, shape)
						)
						Text(text = name)
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

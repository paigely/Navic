package paige.navic.ui.components.dialogs

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_cancel
import org.jetbrains.compose.resources.stringResource
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Download
import paige.navic.ui.components.common.FormButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkDownloadDialog(
	artistName: String,
	showDialog: Boolean,
	onDismissRequest: () -> Unit,
	onConfirm: () -> Unit
) {
	if (showDialog) {
		FormDialog(
			onDismissRequest = onDismissRequest,
			icon = { Icon(Icons.Outlined.Download, contentDescription = null) },
			title = { Text("Download All Albums") },
			buttons = {
				FormButton(
					onClick = {
						onConfirm()
						onDismissRequest()
					},
					color = MaterialTheme.colorScheme.primary
				) {
					Text("Download")
				}
				FormButton(onClick = onDismissRequest) {
					Text(stringResource(Res.string.action_cancel))
				}
			},
			content = {
				Text(
					text = buildString {
						append("Are you sure you want to download every album by $artistName? \n")
						append("This may require a significant amount of storage and data.")
					}
				)

			}
		)
	}
}
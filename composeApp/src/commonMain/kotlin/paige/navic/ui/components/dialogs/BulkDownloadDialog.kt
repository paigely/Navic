package paige.navic.ui.components.dialogs

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_cancel
import navic.composeapp.generated.resources.action_download
import navic.composeapp.generated.resources.info_bulk_download_warning
import navic.composeapp.generated.resources.title_bulk_download
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
			title = { Text(stringResource(Res.string.title_bulk_download)) },
			buttons = {
				FormButton(
					onClick = {
						onConfirm()
						onDismissRequest()
					},
					color = MaterialTheme.colorScheme.primary
				) {
					Text(stringResource(Res.string.action_download))
				}
				FormButton(onClick = onDismissRequest) {
					Text(stringResource(Res.string.action_cancel))
				}
			},
			content = {
				Text(
					text = stringResource(
						Res.string.info_bulk_download_warning,
						artistName
					)
				)
			}
		)
	}
}
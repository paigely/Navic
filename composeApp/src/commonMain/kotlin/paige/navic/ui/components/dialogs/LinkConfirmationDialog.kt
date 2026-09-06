package paige.navic.ui.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_cancel
import navic.composeapp.generated.resources.action_visit_site
import navic.composeapp.generated.resources.notice_link_confirmation
import navic.composeapp.generated.resources.title_link_confirmation
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import paige.navic.domain.manager.LinkManager
import paige.navic.ui.components.common.FormButton

@Composable
fun LinkConfirmationDialog(
	linkToOpen: String,
	onDismissRequest: () -> Unit
) {
	val linkManager = koinInject<LinkManager>()

	FormDialog(
		onDismissRequest = onDismissRequest,
		title = { Text(stringResource(Res.string.title_link_confirmation)) },
		content = {
			Text(stringResource(Res.string.notice_link_confirmation))
			Spacer(Modifier.height(8.dp))
			Surface(
				modifier = Modifier.fillMaxWidth(),
				shape = MaterialTheme.shapes.large,
				color = MaterialTheme.colorScheme.surfaceContainer,
				contentColor = MaterialTheme.colorScheme.onSurfaceVariant
			) {
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(12.dp),
					horizontalArrangement = Arrangement.Center
				) {
					Text(
						text = linkToOpen,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis
					)
				}
			}
		},
		buttons = {
			FormButton(
				onClick = {
					linkManager.openLink(linkToOpen)
					onDismissRequest()
				}
			) {
				Text(stringResource(Res.string.action_visit_site))
			}
			FormButton(
				onClick = onDismissRequest
			) {
				Text(stringResource(Res.string.action_cancel))
			}
		}
	)
}

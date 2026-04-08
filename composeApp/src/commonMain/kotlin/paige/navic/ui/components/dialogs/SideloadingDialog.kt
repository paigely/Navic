package paige.navic.ui.components.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_ok
import navic.composeapp.generated.resources.sideloading_warning_description
import navic.composeapp.generated.resources.sideloading_warning_link_mask
import navic.composeapp.generated.resources.sideloading_warning_subtitle
import navic.composeapp.generated.resources.sideloading_warning_title
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.data.models.settings.Settings

@Composable
fun SideloadingDialog() {
	val ctx = LocalCtx.current
	AlertDialog(
		title = { Text(stringResource(Res.string.sideloading_warning_title)) },
		text = {
			Column {
				Text(
					stringResource(Res.string.sideloading_warning_subtitle),
					fontWeight = FontWeight(600),
					color = MaterialTheme.colorScheme.onSurface
				)
				Text(buildAnnotatedString {
					append(stringResource(Res.string.sideloading_warning_description))
					append(" ")
					withLink(LinkAnnotation.Url("https://keepandroidopen.org/")) {
						append(stringResource(Res.string.sideloading_warning_link_mask))
					}
				})
			}
		},
		onDismissRequest = {},
		confirmButton = {
			Button(onClick = {
				ctx.clickSound()
				Settings.shared.showedSideloadingWarning = true
			}) {
				Text(stringResource(Res.string.action_ok))
			}
		}
	)
}

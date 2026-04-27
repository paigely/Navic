package paige.navic.ui.screens.login.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.notice_login_suggestion
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx

@Composable
fun LoginScreenSuggestionChips(
	instanceState: TextFieldState,
	showSuggestions: Boolean = instanceState.text.isNotEmpty() &&
		!instanceState.text.startsWith("http://") &&
		!instanceState.text.startsWith("https://") &&
		(instanceState.text.contains(".") || instanceState.text.contains(":"))
) {
	val ctx = LocalCtx.current
	AnimatedVisibility(
		modifier = Modifier.fillMaxWidth(),
		visible = showSuggestions,
		enter = expandVertically() + fadeIn(),
		exit = shrinkVertically() + fadeOut()
	) {
		Column(
			modifier = Modifier.fillMaxWidth()
		) {
			Spacer(Modifier.height(4.dp))
			Text(
				text = stringResource(Res.string.notice_login_suggestion),
				style = MaterialTheme.typography.labelMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				modifier = Modifier.padding(horizontal = 16.dp)
			)
			Spacer(Modifier.height(4.dp))
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.horizontalScroll(rememberScrollState()),
				horizontalArrangement = Arrangement.spacedBy(8.dp)
			) {
				val url = instanceState.text.toString()
				Spacer(Modifier.width(8.dp))
				SuggestionChip(
					onClick = {
						ctx.clickSound()
						instanceState.edit {
							replace(0, length, "https://$url")
						}
					},
					label = {
						Text(
							text = "https://${url.replace("https://", "").replace("http://", "")}",
							style = MaterialTheme.typography.labelSmall,
							maxLines = 1
						)
					}
				)
				SuggestionChip(
					onClick = {
						ctx.clickSound()
						instanceState.edit {
							replace(0, length, "http://$url")
						}
					},
					label = {
						Text(
							text = "http://${url.replace("https://", "").replace("http://", "")}",
							style = MaterialTheme.typography.labelSmall,
							maxLines = 1
						)
					}
				)
				Spacer(Modifier.width(8.dp))
			}
		}
	}
}

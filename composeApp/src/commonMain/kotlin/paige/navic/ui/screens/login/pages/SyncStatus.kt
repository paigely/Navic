package paige.navic.ui.screens.login.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_syncing
import org.jetbrains.compose.resources.stringResource
import paige.navic.data.models.User
import paige.navic.utils.LoginState

@Composable
fun LoginScreenSyncStatus(
	loginState: LoginState<User?>
) {
	AnimatedVisibility(
		modifier = Modifier.fillMaxWidth(),
		visible = loginState is LoginState.Syncing,
		enter = expandVertically() + fadeIn(),
		exit = shrinkVertically() + fadeOut()
	) {
		val syncState = loginState as? LoginState.Syncing
		Text(
			text = stringResource(syncState?.message ?: Res.string.info_syncing),
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.primary,
			textAlign = TextAlign.Center,
			modifier = Modifier.fillMaxWidth(),
			maxLines = 1,
			overflow = TextOverflow.Ellipsis
		)
	}
}

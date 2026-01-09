package paige.navic.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.account_circle
import navic.composeapp.generated.resources.action_log_in
import navic.composeapp.generated.resources.action_log_out
import navic.composeapp.generated.resources.title_settings
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.Settings
import paige.navic.data.model.User
import paige.navic.ui.viewmodel.TopBarViewModel
import paige.navic.util.LoginState

@Composable
fun LoginButton(
	userState: LoginState<User?>,
	setShowLoginDialog: (Boolean) -> Unit,
	viewModel: TopBarViewModel,
) {
	val ctx = LocalCtx.current
	val user = (userState as? LoginState.Success)?.data
	if (user != null) {
		Box {
			var expanded by remember { mutableStateOf(false) }
			IconButton(onClick = {
				ctx.clickSound()
				expanded = true
			}) {
				AsyncImage(
					model = user.avatarUrl,
					contentDescription = null,
					contentScale = ContentScale.Crop,
					modifier = Modifier
						.size(36.dp)
						.clip(CircleShape)
						.background(MaterialTheme.colorScheme.surfaceContainer)
				)
			}
			DropdownMenu(
				expanded = expanded,
				onDismissRequest = { expanded = false }
			) {
				DropdownMenuItem(
					text = { Text(stringResource(Res.string.action_log_out)) },
					onClick = {
						ctx.clickSound()
						setShowLoginDialog(false)
						viewModel.logout()
					}
				)
			}
		}
	} else {
		IconButton(onClick = {
			ctx.clickSound()
			setShowLoginDialog(true)
		}) {
			Icon(
				vectorResource(Res.drawable.account_circle),
				contentDescription = stringResource(Res.string.action_log_in)
			)
		}
	}
}

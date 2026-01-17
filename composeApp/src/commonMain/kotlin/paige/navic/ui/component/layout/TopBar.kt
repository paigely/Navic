package paige.navic.ui.component.layout

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.account_circle
import navic.composeapp.generated.resources.action_log_in
import navic.composeapp.generated.resources.action_log_out
import navic.composeapp.generated.resources.action_navigate_back
import navic.composeapp.generated.resources.arrow_back
import navic.composeapp.generated.resources.logout
import navic.composeapp.generated.resources.search
import navic.composeapp.generated.resources.settings
import navic.composeapp.generated.resources.title_appearance
import navic.composeapp.generated.resources.title_behaviour
import navic.composeapp.generated.resources.title_library
import navic.composeapp.generated.resources.title_playlists
import navic.composeapp.generated.resources.title_settings
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import paige.navic.Library
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.Playlists
import paige.navic.Search
import paige.navic.Settings
import paige.navic.SettingsAppearance
import paige.navic.SettingsBehaviour
import paige.navic.data.model.User
import paige.navic.shared.Ctx
import paige.navic.ui.component.common.Dropdown
import paige.navic.ui.component.common.DropdownItem
import paige.navic.ui.component.dialog.LoginDialog
import paige.navic.ui.viewmodel.LoginViewModel
import paige.navic.util.LoginState

private class TopBarScope(
	val ctx: Ctx,
	val backStack: SnapshotStateList<Any>,
	val loginState: LoginState<User?>
)

@OptIn(
	ExperimentalMaterial3Api::class,
	ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun TopBar(viewModel: LoginViewModel = viewModel { LoginViewModel() }) {
	val ctx = LocalCtx.current
	val backStack = LocalNavStack.current
	val loginState by viewModel.loginState.collectAsState()

	var showLogin by remember { mutableStateOf(false) }

	val title = when (backStack.last()) {
		Library -> Res.string.title_library
		Playlists -> Res.string.title_playlists
		Settings -> Res.string.title_settings
		SettingsAppearance -> Res.string.title_appearance
		SettingsBehaviour -> Res.string.title_behaviour
		else -> null
	}

	val expandedHeight by animateDpAsState(
		if (backStack.last() != Search)
			TopAppBarDefaults.TopAppBarExpandedHeight
		else 0.dp
	)

	with(
		TopBarScope(
			ctx = ctx,
			backStack = backStack,
			loginState = loginState
		)
	) {
		TopAppBar(
			title = {
				AnimatedContent(
					targetState = title,
					label = "TitleAnimation"
				) { animatedTitle ->
					animatedTitle?.let {
						Text(
							text = stringResource(it),
							style = MaterialTheme.typography.headlineSmall
						)
					}
				}
			},
			navigationIcon = { NavigationIcon() },
			actions = {
				Actions(
					onLogOut = { viewModel.logout() },
					onSetShowLogin = { showLogin = it }
				)
			},
			colors = TopAppBarDefaults.topAppBarColors(
				scrolledContainerColor = MaterialTheme.colorScheme.surface
			),
			expandedHeight = expandedHeight
		)
		if (showLogin && loginState !is LoginState.Success) {
			LoginDialog(
				viewModel = viewModel,
				onDismissRequest = { showLogin = false }
			)
		}
	}
}

@Composable
private fun TopBarScope.NavigationIcon() {
	AnimatedContent(
		backStack.size > 1 && backStack.last() != Search
	) {
		if (it) {
			IconButton(
				modifier = Modifier.padding(
					horizontal = 12.dp
				),
				colors = IconButtonDefaults.iconButtonVibrantColors(
					containerColor = MaterialTheme.colorScheme.surfaceContainer
				),
				onClick = {
					ctx.clickSound()
					backStack.removeLast()
				}
			) {
				Icon(
					imageVector = vectorResource(Res.drawable.arrow_back),
					contentDescription = stringResource(Res.string.action_navigate_back)
				)
			}
		}
	}
}

@Composable
private fun TopBarScope.Actions(
	onLogOut: () -> Unit,
	onSetShowLogin: (shown: Boolean) -> Unit
) {
	val user = (loginState as? LoginState.Success)?.data
	if (backStack.count() > 1) return

	IconButton(
		onClick = {
			ctx.clickSound()
			backStack.add(Search)
		},
		enabled = user != null
	) {
		Icon(
			vectorResource(Res.drawable.search),
			contentDescription = null
		)
	}

	IconButton(onClick = {
		ctx.clickSound()
		backStack.add(Settings)
	}) {
		Icon(
			vectorResource(Res.drawable.settings),
			contentDescription = null
		)
	}

	if (loginState is LoginState.Loading) {
		CircularProgressIndicator(
			modifier = Modifier
				.padding(13.9.dp)
				.size(20.dp)
		)
	} else {
		if (user != null) {
			Box {
				var expanded by remember { mutableStateOf(false) }
				IconButton(
					onClick = {
						ctx.clickSound()
						expanded = true
					}
				) {
					AsyncImage(
						model = user.avatarUrl,
						contentDescription = user.name,
						contentScale = ContentScale.Crop,
						modifier = Modifier
							.size(36.dp)
							.clip(CircleShape)
							.background(MaterialTheme.colorScheme.surfaceContainer)
					)
				}
				Dropdown(
					expanded = expanded,
					onDismissRequest = { expanded = false }
				) {
					DropdownItem(
						text = Res.string.action_log_out,
						onClick = {
							ctx.clickSound()
							onLogOut()
							onSetShowLogin(false)
						},
						leadingIcon = Res.drawable.logout
					)
				}
			}
		} else {
			IconButton(onClick = {
				ctx.clickSound()
				onSetShowLogin(true)
			}) {
				Icon(
					vectorResource(Res.drawable.account_circle),
					contentDescription = stringResource(Res.string.action_log_in)
				)
			}
		}
	}
}

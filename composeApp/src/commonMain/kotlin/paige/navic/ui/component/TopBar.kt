package paige.navic.ui.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_navigate_back
import navic.composeapp.generated.resources.arrow_back
import navic.composeapp.generated.resources.search
import navic.composeapp.generated.resources.settings
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
import paige.navic.ui.viewmodel.TopBarViewModel
import paige.navic.util.LoginState

@OptIn(
	ExperimentalMaterial3Api::class,
	ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun TopBar(
	viewModel: TopBarViewModel = viewModel { TopBarViewModel() }
) {
	val backStack = LocalNavStack.current
	val ctx = LocalCtx.current

	val title = when (backStack.last()) {
		Library -> Res.string.title_library
		Playlists -> Res.string.title_playlists
		Settings -> Res.string.title_settings
		else -> null
	}

	val userState by viewModel.userState.collectAsState()
	var showLoginDialog by remember { mutableStateOf(false) }

	val expandedHeight by animateDpAsState(
		if (backStack.last() != Search)
			TopAppBarDefaults.TopAppBarExpandedHeight
		else 0.dp
	)

	TopAppBar(
		title = {
			title?.let {
				Text(stringResource(title), style = MaterialTheme.typography.headlineMedium)
			}
		},
		navigationIcon = {
			if (backStack.size > 1 && backStack.last() != Search) {
				IconButton(
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
		},
		actions = {
			if (backStack.count() == 1) {
				if ((userState as? LoginState.Success)?.data != null) {
					IconButton(
						onClick = {
							ctx.clickSound()
							backStack.add(Search)
						}
					) {
						Icon(
							vectorResource(Res.drawable.search),
							contentDescription = null
						)
					}
				}
				IconButton(
					onClick = {
						ctx.clickSound()
						backStack.add(Settings)
					}
				) {
					Icon(
						vectorResource(Res.drawable.settings),
						contentDescription = null
					)
				}

				when (userState) {
					is LoginState.Loading -> CircularProgressIndicator(
						modifier = Modifier
							.padding(13.9.dp)
							.size(20.dp)
					)

					is LoginState.Error,
					is LoginState.LoggedOut,
					is LoginState.Success -> LoginButton(
						userState = userState,
						setShowLoginDialog = { showLoginDialog = it },
						viewModel = viewModel
					)
				}
			}
		},
		colors = TopAppBarDefaults.topAppBarColors(
			scrolledContainerColor = MaterialTheme.colorScheme.surface
		),
		expandedHeight = expandedHeight
	)

	LoginDialog(
		userState = userState,
		viewModel = viewModel,
		visible = (showLoginDialog && userState !is LoginState.Success),
		setVisible = { showLoginDialog = it }
	)
}

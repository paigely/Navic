package paige.navic.ui.component.layout

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
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.account_circle
import navic.composeapp.generated.resources.action_log_in
import navic.composeapp.generated.resources.action_log_out
import navic.composeapp.generated.resources.logout
import navic.composeapp.generated.resources.search
import navic.composeapp.generated.resources.settings
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.data.model.Screen
import paige.navic.data.model.User
import paige.navic.ui.component.common.Dropdown
import paige.navic.ui.component.common.DropdownItem
import paige.navic.ui.component.dialog.LoginDialog
import paige.navic.ui.viewmodel.LoginViewModel
import paige.navic.util.LoginState

@OptIn(
	ExperimentalMaterial3Api::class,
	ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun RootTopBar(
	title: @Composable () -> Unit,
	scrollBehavior: TopAppBarScrollBehavior,
	viewModel: LoginViewModel = viewModel { LoginViewModel() },
) {
	val searchBarState = rememberSearchBarState()
	val scope = rememberCoroutineScope()
	val loginState by viewModel.loginState.collectAsState()
	var showLogin by remember { mutableStateOf(false) }
	var canExpandSearch by remember { mutableStateOf(false) }

	MediumFlexibleTopAppBar(
		title = {
			CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.headlineSmall) {
				title()
			}
		},
		actions = {
			Actions(
				loginState = loginState,
				onSearch = {
					canExpandSearch = true
					scope.launch { searchBarState.animateToExpanded()}
						   },
				onLogOut = { viewModel.logout() },
				onSetShowLogin = { showLogin = it }
			)
		},
		scrollBehavior = scrollBehavior,
		colors = TopAppBarDefaults.topAppBarColors(
			scrolledContainerColor = MaterialTheme.colorScheme.surface
		),
	)
	if (canExpandSearch) {
		SearchBar(
			searchBarState = searchBarState,
			enabled = canExpandSearch
		)
	}
	if (showLogin && loginState !is LoginState.Success) {
		LoginDialog(
			viewModel = viewModel,
			onDismissRequest = { showLogin = false }
		)
	}
}

@Composable
private fun Actions(
	loginState: LoginState<User?>,
	onSearch: () -> Unit,
	onLogOut: () -> Unit,
	onSetShowLogin: (shown: Boolean) -> Unit
) {
	val ctx = LocalCtx.current
	val backStack = LocalNavStack.current
	val user = (loginState as? LoginState.Success)?.data

	IconButton(
		onClick = {
			ctx.clickSound()
			onSearch()
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
		backStack.add(Screen.Settings.Root)
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
					modifier = Modifier.padding(end = 12.dp),
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

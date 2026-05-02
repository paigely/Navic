package paige.navic.ui.components.layouts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.dropUnlessResumed
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_log_out
import navic.composeapp.generated.resources.action_sleep_timer
import navic.composeapp.generated.resources.action_sleep_timer_enabled
import navic.composeapp.generated.resources.action_view_shares
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.data.models.NavbarConfig
import paige.navic.data.models.NavbarTab
import paige.navic.data.models.Screen
import paige.navic.icons.Icons
import paige.navic.icons.filled.Settings
import paige.navic.icons.outlined.AccountCircle
import paige.navic.icons.outlined.Bedtime
import paige.navic.icons.outlined.Logout
import paige.navic.icons.outlined.Search
import paige.navic.icons.outlined.Share
import paige.navic.managers.SleepTimerManager
import paige.navic.ui.components.common.Dropdown
import paige.navic.ui.components.common.DropdownItem
import paige.navic.ui.components.sheets.SleepTimerSheet
import paige.navic.ui.screens.login.viewmodels.LoginViewModel
import paige.navic.ui.screens.settings.viewmodels.NavtabsViewModel
import paige.navic.ui.theme.positive
import paige.navic.utils.UiState
import paige.navic.utils.label

@OptIn(
	ExperimentalMaterial3Api::class,
	ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun RootTopBar(
	title: @Composable () -> Unit,
	scrollBehavior: TopAppBarScrollBehavior,
	actions: @Composable RowScope.() -> Unit = {},
) {
	val backStack = LocalNavStack.current
	val navViewModel = koinViewModel<NavtabsViewModel>()
	val viewModel = koinViewModel<LoginViewModel>()

	val navState by navViewModel.state.collectAsState()
	val config = (navState as? UiState.Success)?.data

	MediumFlexibleTopAppBar(
		title = {
			CompositionLocalProvider(
				LocalTextStyle provides when (LocalTextStyle.current) {
					MaterialTheme.typography.headlineMedium -> MaterialTheme.typography.headlineSmall
					else -> MaterialTheme.typography.titleLarge
				}
			) {
				title()
			}
		},
		actions = {
			actions()
			Actions(
				onLogOut = {
					viewModel.logout()
					backStack.clear()
					backStack.add(Screen.Login)
				},
				config = config,
			)
		},
		scrollBehavior = scrollBehavior,
		colors = TopAppBarDefaults.topAppBarColors(
			scrolledContainerColor = MaterialTheme.colorScheme.surface
		),
	)
}

@Composable
private fun Actions(
	onLogOut: () -> Unit,
	config: NavbarConfig?,
) {
	val ctx = LocalCtx.current
	val backStack = LocalNavStack.current

	val isSearchEnabled = config?.tabs?.any {
		it.id == NavbarTab.Id.SEARCH && it.visible
	} == true

	if (!isSearchEnabled) {
		IconButton(
			onClick = dropUnlessResumed {
				ctx.clickSound()
				backStack.add(Screen.Search(nested = true))
			}
		) {
			Icon(
				Icons.Outlined.Search,
				contentDescription = null
			)
		}
	}

	IconButton(onClick = dropUnlessResumed {
		ctx.clickSound()
		backStack.add(Screen.Settings.Root)
	}) {
		Icon(
			Icons.Filled.Settings,
			contentDescription = null
		)
	}

	var expanded by remember { mutableStateOf(false) }
	var sleepTimerSheetOpen by remember { mutableStateOf(false) }
	val sleepTimerManager = koinInject<SleepTimerManager>()
	val sleepTimerLeft = sleepTimerManager.timeLeft

	Box {
		IconButton(onClick = {
			ctx.clickSound()
			expanded = true
		}) {
			Icon(
				Icons.Outlined.AccountCircle,
				contentDescription = null
			)
		}
		Dropdown(
			expanded = expanded,
			onDismissRequest = { expanded = false }
		) {
			DropdownItem(
				text = { Text(stringResource(Res.string.action_view_shares)) },
				onClick = dropUnlessResumed {
					expanded = false
					backStack.add(Screen.ShareList)
				},
				leadingIcon = { Icon(Icons.Outlined.Share, null) }
			)

			if (sleepTimerLeft != null) {
				DropdownItem(
					text = { Text(stringResource(Res.string.action_sleep_timer_enabled, sleepTimerLeft.label()), color = MaterialTheme.colorScheme.positive) },
					onClick = {
						expanded = false
						sleepTimerSheetOpen = true
					},
					leadingIcon = { Icon(Icons.Outlined.Bedtime, null, tint = MaterialTheme.colorScheme.positive) }
				)
			} else {
				DropdownItem(
					text = { Text(stringResource(Res.string.action_sleep_timer)) },
					onClick = {
						expanded = false
						sleepTimerSheetOpen = true
					},
					leadingIcon = { Icon(Icons.Outlined.Bedtime, null) }
				)
			}

			DropdownItem(
				text = { Text(stringResource(Res.string.action_log_out)) },
				onClick = {
					expanded = false
					onLogOut()
				},
				leadingIcon = { Icon(Icons.Outlined.Logout, null) }
			)
		}
	}

	if (sleepTimerSheetOpen) {
		SleepTimerSheet(onDismissRequest = { sleepTimerSheetOpen = false })
	}
}

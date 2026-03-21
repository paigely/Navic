package paige.navic.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyant.capsule.ContinuousRoundedRectangle
import dev.zt64.subsonic.api.model.Share
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_delete
import navic.composeapp.generated.resources.action_share
import navic.composeapp.generated.resources.info_error
import navic.composeapp.generated.resources.info_no_shares
import navic.composeapp.generated.resources.info_share_expired
import navic.composeapp.generated.resources.info_share_expires_in
import navic.composeapp.generated.resources.info_shared_by
import navic.composeapp.generated.resources.title_shares
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.LocalShareManager
import paige.navic.LocalSnackbarState
import paige.navic.data.models.settings.Settings
import paige.navic.data.models.settings.enums.BottomBarVisibilityMode
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Delete
import paige.navic.icons.outlined.Share
import paige.navic.ui.components.common.CoverArt
import paige.navic.ui.components.common.Dropdown
import paige.navic.ui.components.common.DropdownItem
import paige.navic.ui.components.dialogs.DeletionDialog
import paige.navic.ui.components.dialogs.DeletionEndpoint
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.components.layouts.RootBottomBar
import paige.navic.ui.components.layouts.artGridError
import paige.navic.ui.viewmodels.SharesViewModel
import paige.navic.utils.UiState
import paige.navic.utils.toHoursMinutesSeconds
import paige.navic.utils.withoutTop
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharesScreen(
	viewModel: SharesViewModel = viewModel { SharesViewModel() }
) {
	val sharesState by viewModel.sharesState.collectAsState()
	val isRefreshing by viewModel.isRefreshing.collectAsState()
	val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
	var deletionId by remember { mutableStateOf<String?>(null) }

	Scaffold(
		topBar = { NestedTopBar({ Text(stringResource(Res.string.title_shares)) }) },
		bottomBar = {
			if (Settings.shared.bottomBarVisibilityMode == BottomBarVisibilityMode.AllScreens) {
				RootBottomBar(scrolled = viewModel.gridState.lastScrolledForward)
			}
		}
	) { contentPadding ->
		PullToRefreshBox(
			modifier = Modifier
				.padding(top = contentPadding.calculateTopPadding())
				.background(MaterialTheme.colorScheme.surface),
			isRefreshing = isRefreshing || sharesState is UiState.Loading,
			onRefresh = { viewModel.refreshShares() }
		) {
			Crossfade(sharesState) { state ->
				LazyVerticalGrid(
					modifier = Modifier
						.fillMaxSize()
						.nestedScroll(scrollBehavior.nestedScrollConnection),
					columns = GridCells.Fixed(1),
					contentPadding = contentPadding.withoutTop(),
					state = viewModel.gridState
				) {
					when (state) {
						is UiState.Loading -> { return@LazyVerticalGrid }
						is UiState.Error -> artGridError(state)
						is UiState.Success -> {
							items(state.data, { it.id }) { share ->
								SharesScreenItem(
									modifier = Modifier.animateItem(fadeInSpec = null),
									share = share,
									onSetDeletionId = { newDeletionId ->
										deletionId = newDeletionId
									}
								)
							}
							if (state.data.isEmpty()) {
								item(span = { GridItemSpan(maxLineSpan) }) {
									Column(
										modifier = Modifier.fillMaxSize(),
										horizontalAlignment = Alignment.CenterHorizontally,
										verticalArrangement = Arrangement.Center
									) {
										Text(
											stringResource(Res.string.info_no_shares),
											style = MaterialTheme.typography.headlineMedium,
											textAlign = TextAlign.Center,
											modifier = Modifier.alpha(.5f)
										)
									}
								}
							}
						}
					}
				}
			}
		}
	}

	@Suppress("AssignedValueIsNeverRead")
	DeletionDialog(
		endpoint = DeletionEndpoint.SHARE,
		id = deletionId,
		onIdClear = { deletionId = null }
	)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SharesScreenItem(
	modifier: Modifier = Modifier,
	share: Share,
	onSetDeletionId: (newDeletionId: String) -> Unit
) {
	val ctx = LocalCtx.current
	val shareManager = LocalShareManager.current
	val snackbarState = LocalSnackbarState.current
	var expanded by remember { mutableStateOf(false) }
	var currentTime by remember { mutableStateOf(Clock.System.now()) }
	val scope = rememberCoroutineScope()
	val dismissState = rememberSwipeToDismissBoxState()

	LaunchedEffect(dismissState.currentValue) {
		if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
			onSetDeletionId(share.id)
			dismissState.snapTo(SwipeToDismissBoxValue.Settled)
		}
	}

	LaunchedEffect(share.expiresAt) {
		while (true) {
			delay(1.seconds)
			currentTime = Clock.System.now()
		}
	}

	SwipeToDismissBox(
		state = dismissState,
		enableDismissFromStartToEnd = false,
		enableDismissFromEndToStart = true,
		backgroundContent = {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.background(MaterialTheme.colorScheme.errorContainer)
					.padding(horizontal = 16.dp),
				contentAlignment = Alignment.CenterEnd
			) {
				Icon(Icons.Outlined.Delete, null)
			}
		}
	) {
		Surface {
			Box {
				ListItem(
					modifier = modifier,
					leadingContent = {
						CoverArt(
							coverArtId = share.items.firstOrNull()?.coverArtId,
							modifier = Modifier.size(60.dp),
							shape = ContinuousRoundedRectangle((Settings.shared.artGridRounding / 1.5f).dp)
						)
					},
					content = {
						Text(share.description)
					},
					supportingContent = {
						Text(stringResource(Res.string.info_shared_by, share.username))
					},
					overlineContent = {
						val expires = share.expiresAt
						val remaining = expires - currentTime
						if (remaining.isPositive()) {
							Text(
								stringResource(
									Res.string.info_share_expires_in,
									remaining.toHoursMinutesSeconds()
								)
							)
						} else {
							Text(stringResource(Res.string.info_share_expired))
						}
					},
					onClick = {
						ctx.clickSound()
						expanded = true
					},
					onLongClick = {
						expanded = true
					}
				)
				Dropdown(
					expanded = expanded,
					onDismissRequest = { expanded = false }
				) {
					DropdownItem(
						onClick = {
							expanded = false
							scope.launch {
								try {
									shareManager.shareString(share.url)
								} catch (e: Exception) {
									snackbarState.showSnackbar(
										e.message ?: getString(Res.string.info_error)
									)
								}
							}
						},
						leadingIcon = { Icon(Icons.Outlined.Share, null) },
						text = { Text(stringResource(Res.string.action_share)) }
					)
					DropdownItem(
						onClick = {
							expanded = false
							onSetDeletionId(share.id)
						},
						leadingIcon = { Icon(Icons.Outlined.Delete, null) },
						text = { Text(stringResource(Res.string.action_delete)) }
					)
				}
			}
		}
	}
}

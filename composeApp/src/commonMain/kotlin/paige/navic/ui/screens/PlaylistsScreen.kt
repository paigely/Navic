package paige.navic.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyant.capsule.ContinuousRoundedRectangle
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_delete
import navic.composeapp.generated.resources.action_new
import navic.composeapp.generated.resources.action_share
import navic.composeapp.generated.resources.count_songs
import navic.composeapp.generated.resources.title_create_playlist
import navic.composeapp.generated.resources.title_playlists
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalContentPadding
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.data.models.Screen
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Add
import paige.navic.icons.outlined.PlaylistRemove
import paige.navic.icons.outlined.Share
import paige.navic.ui.components.common.Dropdown
import paige.navic.ui.components.common.DropdownItem
import paige.navic.ui.components.common.RefreshBox
import paige.navic.ui.components.dialogs.DeletionDialog
import paige.navic.ui.components.dialogs.DeletionEndpoint
import paige.navic.ui.components.dialogs.ShareDialog
import paige.navic.ui.components.layouts.ArtGrid
import paige.navic.ui.components.layouts.ArtGridItem
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.components.layouts.RootTopBar
import paige.navic.ui.components.layouts.artGridError
import paige.navic.ui.components.layouts.artGridPlaceholder
import paige.navic.ui.viewmodels.PlaylistsViewModel
import paige.navic.utils.UiState
import paige.navic.utils.onRightClick
import paige.subsonic.api.models.Playlist
import kotlin.time.Duration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen(
	nested: Boolean = false,
	viewModel: PlaylistsViewModel = viewModel { PlaylistsViewModel() }
) {
	val ctx = LocalCtx.current
	val backStack = LocalNavStack.current
	val playlistsState by viewModel.playlistsState.collectAsState()
	var shareId by remember { mutableStateOf<String?>(null) }
	var shareExpiry by remember { mutableStateOf<Duration?>(null) }
	var deletionId by remember { mutableStateOf<String?>(null) }
	val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

	Scaffold(
		topBar = {
			if (!nested) {
				RootTopBar({ Text(stringResource(Res.string.title_playlists)) }, scrollBehavior)
			} else {
				NestedTopBar({ Text(stringResource(Res.string.title_playlists)) })
			}
		},
		contentWindowInsets = WindowInsets.statusBars,
		floatingActionButton = {
			ExtendedFloatingActionButton(
				modifier = Modifier.offset(y = -LocalContentPadding.current.calculateBottomPadding()),
				shape = ContinuousRoundedRectangle(14.dp),
				onClick = {
					ctx.clickSound()
					if (backStack.lastOrNull() !is Screen.CreatePlaylist) {
						backStack.add(Screen.CreatePlaylist())
					}
				},
				text = {
					Text(stringResource(Res.string.action_new))
				},
				icon = {
					Icon(
						imageVector = Icons.Outlined.Add,
						contentDescription = stringResource(Res.string.title_create_playlist)
					)
				}
			)
		}
	) { innerPadding ->
		RefreshBox(
			modifier = Modifier
				.padding(innerPadding)
				.background(MaterialTheme.colorScheme.surface),
			isRefreshing = playlistsState is UiState.Loading,
			onRefresh = { viewModel.refreshPlaylists() }
		) { topPadding ->
			AnimatedContent(playlistsState, Modifier.padding(top = topPadding)) {
				ArtGrid(Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)) {
					when (it) {
						is UiState.Loading -> artGridPlaceholder()
						is UiState.Error -> artGridError(it)
						is UiState.Success -> {
							items(it.data, { it.id }) { playlist ->
								PlaylistsScreenItem(
									modifier = Modifier.animateItem(),
									playlist = playlist,
									viewModel = viewModel,
									onSetShareId = { newShareId ->
										shareId = newShareId
									},
									onSetDeletionId = { newDeletionId ->
										deletionId = newDeletionId
									}
								)
							}
						}
					}
				}
			}
		}
	}

	@Suppress("AssignedValueIsNeverRead")
	ShareDialog(
		id = shareId,
		onIdClear = { shareId = null },
		expiry = shareExpiry,
		onExpiryChange = { shareExpiry = it }
	)

	@Suppress("AssignedValueIsNeverRead")
	DeletionDialog(
		endpoint = DeletionEndpoint.PLAYLIST,
		id = deletionId,
		onIdClear = { deletionId = null }
	)
}

@Composable
fun PlaylistsScreenItem(
	modifier: Modifier = Modifier,
	playlist: Playlist,
	viewModel: PlaylistsViewModel,
	onSetShareId: (String) -> Unit,
	onSetDeletionId: (String) -> Unit
) {
	val ctx = LocalCtx.current
	val backStack = LocalNavStack.current
	val selection by viewModel.selectedPlaylist.collectAsState()
	Box(modifier) {
		ArtGridItem(
			imageModifier = Modifier
				.combinedClickable(
					onClick = {
						ctx.clickSound()
						backStack.add(Screen.Tracks(playlist))
					},
					onLongClick = {
						viewModel.selectPlaylist(playlist)
					}
				)
				.onRightClick {
					viewModel.selectPlaylist(playlist)
				},
			imageUrl = playlist.coverArt,
			title = playlist.name,
			subtitle = buildString {
				append(
					pluralStringResource(
						Res.plurals.count_songs,
						playlist.songCount,
						playlist.songCount
					) + "\n"
				)
				playlist.comment?.let {
					append("${playlist.comment}\n")
				}
			}
		)
		Dropdown(
			expanded = selection == playlist,
			onDismissRequest = {
				viewModel.clearSelection()
			}
		) {
			DropdownItem(
				text = { Text(stringResource(Res.string.action_share)) },
				leadingIcon = { Icon(Icons.Outlined.Share, null) },
				onClick = {
					onSetShareId(playlist.id)
					viewModel.clearSelection()
				},
			)
			DropdownItem(
				text = { Text(stringResource(Res.string.action_delete)) },
				leadingIcon = { Icon(Icons.Outlined.PlaylistRemove, null) },
				onClick = {
					onSetDeletionId(playlist.id)
					viewModel.clearSelection()
				}
			)
		}
	}
}

package paige.navic.ui.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.kyant.capsule.ContinuousRoundedRectangle
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_see_all
import navic.composeapp.generated.resources.history
import navic.composeapp.generated.resources.info_needs_log_in
import navic.composeapp.generated.resources.library_add
import navic.composeapp.generated.resources.option_sort_frequent
import navic.composeapp.generated.resources.option_sort_newest
import navic.composeapp.generated.resources.option_sort_random
import navic.composeapp.generated.resources.option_sort_recent
import navic.composeapp.generated.resources.option_sort_starred
import navic.composeapp.generated.resources.shuffle
import navic.composeapp.generated.resources.title_artists
import navic.composeapp.generated.resources.title_library
import navic.composeapp.generated.resources.title_playlists
import navic.composeapp.generated.resources.unstar
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import paige.navic.LocalContentPadding
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.data.model.Screen
import paige.navic.data.session.SessionManager
import paige.navic.shared.systemGesturesExclusion
import paige.navic.ui.component.common.RefreshBox
import paige.navic.ui.component.dialog.DeletionDialog
import paige.navic.ui.component.dialog.DeletionEndpoint
import paige.navic.ui.component.dialog.ShareDialog
import paige.navic.ui.component.layout.ArtGridPlaceholder
import paige.navic.ui.component.layout.RootTopBar
import paige.navic.ui.component.layout.artGridError
import paige.navic.ui.viewmodel.AlbumsViewModel
import paige.navic.ui.viewmodel.ArtistsViewModel
import paige.navic.ui.viewmodel.PlaylistsViewModel
import paige.navic.util.UiState
import paige.subsonic.api.model.Album
import paige.subsonic.api.model.ListType
import paige.subsonic.api.model.Playlist
import kotlin.time.Duration

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
	albumsViewModel: AlbumsViewModel = viewModel(key = "libraryAlbums") { AlbumsViewModel(ListType.RECENT) },
	playlistsViewModel: PlaylistsViewModel = viewModel { PlaylistsViewModel() },
	artistsViewModel: ArtistsViewModel = viewModel { ArtistsViewModel() },
) {
	val recentsState by albumsViewModel.albumsState.collectAsState()
	val playlistsState by playlistsViewModel.playlistsState.collectAsState()
	val artistsState by artistsViewModel.artistsState.collectAsState()
	var shareId by remember { mutableStateOf<String?>(null) }
	var shareExpiry by remember { mutableStateOf<Duration?>(null) }
	var deletionId by remember { mutableStateOf<String?>(null) }
	val isLoggedIn by SessionManager.isLoggedIn.collectAsState()
	val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

	Scaffold(
		topBar = { RootTopBar({ Text(stringResource(Res.string.title_library)) }, scrollBehavior) },
		contentWindowInsets = WindowInsets.statusBars
	) { innerPadding ->
		RefreshBox(
			modifier = Modifier
				.padding(innerPadding)
				.background(MaterialTheme.colorScheme.surface),
			isRefreshing = recentsState is UiState.Loading
				|| artistsState is UiState.Loading,
			onRefresh = {
				if (!isLoggedIn) return@RefreshBox
				albumsViewModel.refreshAlbums()
				playlistsViewModel.refreshPlaylists()
				artistsViewModel.refreshArtists()
			}
		) { topPadding ->
			LazyVerticalGrid(
				modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
				columns = GridCells.Fixed(2),
				contentPadding = PaddingValues(
					top = topPadding + 16.dp,
					bottom = LocalContentPadding.current.calculateBottomPadding(),
				),
				verticalArrangement = Arrangement.spacedBy(6.dp),
				horizontalArrangement = Arrangement.spacedBy(6.dp),
			) {
				overviewButton(
					icon = Res.drawable.library_add,
					label = Res.string.option_sort_newest,
					destination = Screen.Albums(true, ListType.NEWEST),
					start = true
				)
				overviewButton(
					icon = Res.drawable.shuffle,
					label = Res.string.option_sort_random,
					destination = Screen.Albums(true, ListType.RANDOM),
					start = false
				)
				overviewButton(
					icon = Res.drawable.unstar,
					label = Res.string.option_sort_starred,
					destination = Screen.Albums(true, ListType.STARRED),
					start = true
				)
				overviewButton(
					icon = Res.drawable.history,
					label = Res.string.option_sort_frequent,
					destination = Screen.Albums(true, ListType.FREQUENT),
					start = false
				)
				if (!isLoggedIn) {
					item(span = { GridItemSpan(maxLineSpan) }) {
						Text(
							stringResource(Res.string.info_needs_log_in),
							color = MaterialTheme.colorScheme.onSurfaceVariant,
							modifier = Modifier.padding(horizontal = 16.dp)
						)
					}
					return@LazyVerticalGrid
				}
				horizontalAlbums(recentsState, albumsViewModel) { shareId = it }
				horizontalPlaylists(playlistsState, playlistsViewModel, { shareId = it }, { deletionId = it })
				horizontalArtists(artistsState, artistsViewModel)
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun LazyGridScope.header(
	title: StringResource,
	vararg formatArgs: Any,
	destination: NavKey
) {
	item(span = { GridItemSpan(1) }) {
		Text(
			stringResource(title, formatArgs),
			style = MaterialTheme.typography.titleMediumEmphasized,
			fontWeight = FontWeight(600),
			modifier = Modifier.heightIn(min = 32.dp).padding(top = 8.dp, start = 16.dp)
		)
	}
	item(span = { GridItemSpan(1) }) {
		val ctx = LocalCtx.current
		val backStack = LocalNavStack.current
		Text(
			stringResource(Res.string.action_see_all),
			fontSize = 12.sp,
			color = MaterialTheme.colorScheme.primary,
			textAlign = TextAlign.Right,
			modifier = Modifier
				.heightIn(min = 32.dp)
				.padding(top = 8.dp, end = 16.dp)
				.clickable(
					interactionSource = null,
					indication = null
				) {
					ctx.clickSound()
					backStack.add(destination)
				}
		)
	}
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterialApi::class)
private fun LazyGridScope.overviewButton(
	icon: DrawableResource,
	label: StringResource,
	destination: NavKey,
	start: Boolean
) {
	item(span = { GridItemSpan(1) }) {
		val ctx = LocalCtx.current
		val backStack = LocalNavStack.current
		Button(
			modifier = Modifier
				.fillMaxWidth()
				.height(42.dp)
				.padding(
					start = if (start) 16.dp else 0.dp,
					end = if (!start) 16.dp else 0.dp,
				),
			contentPadding = PaddingValues(horizontal = 12.dp),
			elevation = null,
			shapes = ButtonDefaults.shapes(
				shape = ContinuousRoundedRectangle(12.dp),
				pressedShape = ContinuousRoundedRectangle(7.dp)
			),
			colors = ButtonDefaults.buttonColors(
				containerColor = MaterialTheme.colorScheme.surfaceContainer,
				contentColor = MaterialTheme.colorScheme.onSurfaceVariant
			),
			onClick = {
				ctx.clickSound()
				if (backStack.lastOrNull() !is Screen.Albums) {
					backStack.add(destination)
				}
			}
		) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically
			) {
				Icon(
					vectorResource(icon),
					contentDescription = null
				)
				Spacer(Modifier.width(10.dp))
				Text(
					stringResource(label),
					maxLines = 1,
					autoSize = TextAutoSize.StepBased(minFontSize = 1.sp, maxFontSize = 16.sp),
				)
			}
		}
	}
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun LazyGridScope.horizontalAlbums(
	state: UiState<List<Album>>,
	viewModel: AlbumsViewModel,
	onSetShareId: (String) -> Unit
) {
	if (state !is UiState.Success || state.data.isNotEmpty()) {
		header(Res.string.option_sort_recent, destination = Screen.Albums(true, ListType.RECENT))
	}
	when (state) {
		is UiState.Error -> artGridError(state)
		else -> item(
			span = { GridItemSpan(maxLineSpan) }
		) {
			LazyRow(
				modifier = Modifier
					.animateContentSize(
						animationSpec = MaterialTheme.motionScheme.fastSpatialSpec()
					)
					.systemGesturesExclusion(),
				horizontalArrangement = Arrangement.spacedBy(12.dp),
				contentPadding = PaddingValues(horizontal = 16.dp)
			) {
				if (state is UiState.Loading) {
					items(8) {
						ArtGridPlaceholder(
							Modifier.width(150.dp)
						)
					}
				} else if (state is UiState.Success) {
					items(state.data, { it.id }) { album ->
						AlbumsScreenItem(
							modifier = Modifier.animateItem().width(150.dp),
							album = album,
							viewModel = viewModel,
							onSetShareId = onSetShareId
						)
					}
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun LazyGridScope.horizontalPlaylists(
	state: UiState<List<Playlist>>,
	viewModel: PlaylistsViewModel,
	onSetShareId: (String) -> Unit,
	onSetDeletionId: (String) -> Unit
) {
	if (state !is UiState.Success || state.data.isNotEmpty()) {
		header(Res.string.title_playlists, destination = Screen.Playlists(true))
	}
	when (state) {
		is UiState.Error -> artGridError(state)
		else -> item(
			span = { GridItemSpan(maxLineSpan) }
		) {
			LazyRow(
				modifier = Modifier
					.animateContentSize(
						animationSpec = MaterialTheme.motionScheme.fastSpatialSpec()
					)
					.systemGesturesExclusion(),
				horizontalArrangement = Arrangement.spacedBy(12.dp),
				contentPadding = PaddingValues(horizontal = 16.dp)
			) {
				if (state is UiState.Loading) {
					items(8) {
						ArtGridPlaceholder(
							Modifier.width(150.dp)
						)
					}
				} else if (state is UiState.Success) {
					items(state.data, { it.id }) { playlist ->
						PlaylistsScreenItem(
							modifier = Modifier.animateItem().width(150.dp),
							playlist = playlist,
							viewModel = viewModel,
							onSetShareId = onSetShareId,
							onSetDeletionId = onSetDeletionId
						)
					}
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun LazyGridScope.horizontalArtists(
	state: UiState<List<paige.subsonic.api.model.Artists.Index>>,
	viewModel: ArtistsViewModel
) {
	if (state !is UiState.Success || state.data.isNotEmpty()) {
		header(Res.string.title_artists, destination = Screen.Artists(true))
	}
	when (state) {
		is UiState.Error -> artGridError(state)
		else -> item(
			span = { GridItemSpan(maxLineSpan) }
		) {
			LazyRow(
				modifier = Modifier
					.animateContentSize(
						animationSpec = MaterialTheme.motionScheme.fastSpatialSpec()
					)
					.systemGesturesExclusion(),
				horizontalArrangement = Arrangement.spacedBy(12.dp),
				contentPadding = PaddingValues(horizontal = 16.dp)
			) {
				if (state is UiState.Loading) {
					items(8) {
						ArtGridPlaceholder(
							Modifier.width(150.dp)
						)
					}
				} else if (state is UiState.Success) {
					items(state.data.flatMap { it.artist }, { it.id }) { artist ->
						ArtistsScreenItem(
							Modifier.animateItem().width(150.dp),
							artist,
							viewModel
						)
					}
				}
			}
		}
	}
}

package paige.navic.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_remove_star
import navic.composeapp.generated.resources.action_share
import navic.composeapp.generated.resources.action_star
import navic.composeapp.generated.resources.count_albums
import navic.composeapp.generated.resources.info_unknown_album
import navic.composeapp.generated.resources.info_unknown_artist
import navic.composeapp.generated.resources.option_sort_alphabetical_by_artist
import navic.composeapp.generated.resources.option_sort_alphabetical_by_name
import navic.composeapp.generated.resources.option_sort_frequent
import navic.composeapp.generated.resources.option_sort_newest
import navic.composeapp.generated.resources.option_sort_random
import navic.composeapp.generated.resources.option_sort_recent
import navic.composeapp.generated.resources.option_sort_starred
import navic.composeapp.generated.resources.share
import navic.composeapp.generated.resources.sort
import navic.composeapp.generated.resources.switch_on
import navic.composeapp.generated.resources.title_albums
import navic.composeapp.generated.resources.unstar
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.data.model.Screen
import paige.navic.ui.component.common.Dropdown
import paige.navic.ui.component.common.DropdownItem
import paige.navic.ui.component.common.RefreshBox
import paige.navic.ui.component.dialog.ShareDialog
import paige.navic.ui.component.layout.ArtGrid
import paige.navic.ui.component.layout.ArtGridItem
import paige.navic.ui.component.layout.NestedTopBar
import paige.navic.ui.component.layout.RootTopBar
import paige.navic.ui.component.layout.artGridError
import paige.navic.ui.component.layout.artGridPlaceholder
import paige.navic.ui.viewmodel.AlbumsViewModel
import paige.navic.util.UiState
import paige.subsonic.api.model.Album
import paige.subsonic.api.model.ListType
import kotlin.time.Duration

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AlbumsScreen(
	nested: Boolean = false,
	listType: ListType,
	viewModel: AlbumsViewModel = viewModel(key = listType.value) {
		AlbumsViewModel(listType)
	}
) {
	val albumsState by viewModel.albumsState.collectAsState()
	var shareId by remember { mutableStateOf<String?>(null) }
	var shareExpiry by remember { mutableStateOf<Duration?>(null) }
	val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

	Scaffold(
		topBar = {
			if (!nested) {
				RootTopBar({ Text(stringResource(Res.string.title_albums)) }, scrollBehavior)
			} else {
				NestedTopBar({ Text(stringResource(Res.string.title_albums)) })
			}
		}
	) { innerPadding ->
		RefreshBox(
			modifier = Modifier
				.padding(innerPadding)
				.background(MaterialTheme.colorScheme.surface),
			isRefreshing = albumsState is UiState.Loading,
			onRefresh = { viewModel.refreshAlbums() }
		) { topPadding ->
			AnimatedContent(albumsState, Modifier.padding(top = topPadding)) {
				ArtGrid(Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)) {
					when (it) {
						is UiState.Loading -> artGridPlaceholder()
						is UiState.Error -> artGridError(it)
						is UiState.Success -> {
							albumsScreenHeader(it.data, viewModel)
							items(it.data, { it.id }) { album ->
								AlbumsScreenItem(
									modifier = Modifier.animateItem(),
									album = album,
									viewModel = viewModel,
									onSetShareId = { newShareId ->
										shareId = newShareId
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
}

fun LazyGridScope.albumsScreenHeader(
	data: List<Album>,
	viewModel: AlbumsViewModel
) {
	stickyHeader { _ ->
		val currentListType by viewModel.listType.collectAsState()
		Row(
			Modifier
				.background(MaterialTheme.colorScheme.surface)
				.padding(bottom = 8.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				pluralStringResource(
					Res.plurals.count_albums,
					data.count(),
					data.count()
				),
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
			Spacer(Modifier.weight(1f))
			Box {
				var expanded by remember { mutableStateOf(false) }
				IconButton(onClick = {
					expanded = true
				}) {
					Icon(
						vectorResource(Res.drawable.sort),
						contentDescription = null,
						tint = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
				Dropdown(
					expanded = expanded,
					onDismissRequest = { expanded = false }
				) {
					mapOf(
						Res.string.option_sort_random to ListType.RANDOM,
						Res.string.option_sort_newest to ListType.NEWEST,
						Res.string.option_sort_frequent to ListType.FREQUENT,
						Res.string.option_sort_recent to ListType.RECENT,
						Res.string.option_sort_alphabetical_by_name to ListType.ALPHABETICAL_BY_NAME,
						Res.string.option_sort_alphabetical_by_artist to ListType.ALPHABETICAL_BY_ARTIST,
						Res.string.option_sort_starred to ListType.STARRED
					).forEach { (stringRes, listType) ->
						DropdownItem(
							text = stringRes,
							containerColor = if (currentListType == listType)
								MaterialTheme.colorScheme.primaryContainer
							else MaterialTheme.colorScheme.surfaceContainerHigh,
							leadingIcon = if (currentListType == listType)
								Res.drawable.switch_on
							else null,
							onClick = {
								expanded = false
								viewModel.setListType(listType)
								viewModel.refreshAlbums()
							},
							rounding = if (currentListType == listType)
								100.dp
							else 4.dp
						)
					}
				}
			}
		}
	}
}

@Composable
fun AlbumsScreenItem(
	modifier: Modifier = Modifier,
	album: Album,
	viewModel: AlbumsViewModel,
	onSetShareId: (String) -> Unit
) {
	val ctx = LocalCtx.current
	val backStack = LocalNavStack.current
	val selection by viewModel.selectedAlbum.collectAsState()
	val starredState by viewModel.starredState.collectAsState()
	Box(modifier) {
		ArtGridItem(
			imageModifier = Modifier.combinedClickable(
				onClick = {
					ctx.clickSound()
					backStack.add(Screen.Tracks(album))
				},
				onLongClick = {
					viewModel.selectAlbum(album)
				}
			),
			imageUrl = album.coverArt,
			title = album.name
				?: stringResource(Res.string.info_unknown_album),
			subtitle = (album.artist
				?: stringResource(Res.string.info_unknown_artist)) + "\n",
		)
		Dropdown(
			expanded = selection == album,
			onDismissRequest = {
				viewModel.selectAlbum(null)
			}
		) {
			DropdownItem(
				text = Res.string.action_share,
				leadingIcon = Res.drawable.share,
				onClick = {
					viewModel.selectAlbum(null)
					onSetShareId(album.id)
				},
			)
			val starred =
				(starredState as? UiState.Success)?.data
			DropdownItem(
				text = if (starred == true)
					Res.string.action_remove_star
				else Res.string.action_star,
				leadingIcon = Res.drawable.unstar,
				onClick = {
					viewModel.starAlbum(starred != true)
					viewModel.selectAlbum(null)
				},
				enabled = starred != null
			)
		}
	}
}

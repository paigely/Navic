package paige.navic.ui.components.layouts

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationItemIconPosition
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.title_albums
import navic.composeapp.generated.resources.title_artists
import navic.composeapp.generated.resources.title_genres
import navic.composeapp.generated.resources.title_library
import navic.composeapp.generated.resources.title_playlists
import navic.composeapp.generated.resources.title_search
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.data.models.NavbarConfig
import paige.navic.data.models.NavbarTab
import paige.navic.data.models.Screen
import paige.navic.data.models.settings.Settings
import paige.navic.data.models.settings.enums.NavigationBarStyle
import paige.navic.icons.Icons
import paige.navic.icons.filled.Album
import paige.navic.icons.filled.Artist
import paige.navic.icons.filled.LibraryMusic
import paige.navic.icons.outlined.Album
import paige.navic.icons.outlined.Artist
import paige.navic.icons.outlined.Genre
import paige.navic.icons.outlined.LibraryMusic
import paige.navic.icons.outlined.PlaylistPlay
import paige.navic.icons.outlined.Search
import paige.navic.ui.components.common.animatedTabIconPainter
import paige.navic.ui.screens.settings.viewmodels.NavtabsViewModel
import paige.navic.utils.UiState

private enum class NavItem(
	val destination: Screen,
	val icon: ImageVector,
	val iconUnselected: ImageVector = icon,
	val label: StringResource
) {
	LIBRARY(
		destination = Screen.Library(),
		icon = Icons.Filled.LibraryMusic,
		iconUnselected = Icons.Outlined.LibraryMusic,
		label = Res.string.title_library
	),
	ALBUMS(
		destination = Screen.AlbumList(),
		icon = Icons.Filled.Album,
		iconUnselected = Icons.Outlined.Album,
		label = Res.string.title_albums
	),
	PLAYLISTS(
		destination = Screen.PlaylistList(),
		icon = Icons.Outlined.PlaylistPlay,
		label = Res.string.title_playlists
	),
	ARTISTS(
		destination = Screen.ArtistList(),
		icon = Icons.Filled.Artist,
		iconUnselected = Icons.Outlined.Artist,
		label = Res.string.title_artists
	),
	SEARCH(
		destination = Screen.Search(),
		icon = Icons.Outlined.Search,
		iconUnselected = Icons.Outlined.Search,
		label = Res.string.title_search
	),
	GENRES(
		destination = Screen.GenreList(),
		icon = Icons.Outlined.Genre,
		iconUnselected = Icons.Outlined.Genre,
		label = Res.string.title_genres
	)
}

@Composable
fun BottomBar(
	modifier: Modifier = Modifier,
	containerColor: Color = NavigationBarDefaults.containerColor,
	windowInsets: WindowInsets = NavigationBarDefaults.windowInsets,
	enabled: Boolean = true
) {
	val viewModel = koinViewModel<NavtabsViewModel>()
	val backStack = LocalNavStack.current
	val ctx = LocalCtx.current
	val state by viewModel.state.collectAsState()
	val containerColor by animateColorAsState(containerColor)

	AnimatedContent(
		Settings.shared.navigationBarStyle != NavigationBarStyle.Short
			&& ctx.sizeClass.widthSizeClass <= WindowWidthSizeClass.Compact
	) {
		val tabs = ((state as? UiState.Success)?.data ?: NavbarConfig.default)
			.tabs.filter { tab -> tab.visible }
		if (it) {
			NavigationBar(
				modifier = modifier,
				containerColor = containerColor,
				windowInsets = windowInsets
			) {
				tabs.forEach { tab ->
					val item = when (tab.id) {
						NavbarTab.Id.LIBRARY -> NavItem.LIBRARY
						NavbarTab.Id.ALBUMS -> NavItem.ALBUMS
						NavbarTab.Id.PLAYLISTS -> NavItem.PLAYLISTS
						NavbarTab.Id.ARTISTS -> NavItem.ARTISTS
						NavbarTab.Id.SEARCH -> NavItem.SEARCH
						NavbarTab.Id.GENRES -> NavItem.GENRES
					}
					val selected = backStack.lastOrNull() == item.destination

					NavigationBarItem(
						selected = selected,
						enabled = enabled,
						onClick = {
							ctx.clickSound()
							backStack.apply {
								clear()
								add(item.destination)
							}
						},
						icon = {
							if (selected) {
								val painter = animatedTabIconPainter(item.destination)
								if (painter != null) {
									Icon(painter = painter, null)
								} else {
									Icon(item.icon, null)
								}
							} else {
								Icon(item.iconUnselected, null)
							}
						},
						label = {
							Text(
								stringResource(item.label),
								maxLines = 1,
								autoSize = TextAutoSize.StepBased(
									minFontSize = 1.sp,
									maxFontSize = MaterialTheme.typography.labelMedium.fontSize
								)
							)
						}
					)
				}
			}
		} else {
			ShortNavigationBar(
				modifier = modifier,
				containerColor = containerColor
			) {
				tabs.forEach { tab ->
					val item = when (tab.id) {
						NavbarTab.Id.LIBRARY -> NavItem.LIBRARY
						NavbarTab.Id.ALBUMS -> NavItem.ALBUMS
						NavbarTab.Id.PLAYLISTS -> NavItem.PLAYLISTS
						NavbarTab.Id.ARTISTS -> NavItem.ARTISTS
						NavbarTab.Id.SEARCH -> NavItem.SEARCH
						NavbarTab.Id.GENRES -> NavItem.GENRES
					}
					val selected = backStack.last() == item.destination

					ShortNavigationBarItem(
						iconPosition = if (ctx.sizeClass.widthSizeClass > WindowWidthSizeClass.Compact)
							NavigationItemIconPosition.Start
						else NavigationItemIconPosition.Top,
						selected = backStack.last() == item.destination,
						enabled = enabled,
						onClick = {
							ctx.clickSound()
							backStack.apply {
								clear()
								add(item.destination)
							}
						},
						icon = {
							if (selected) {
								val painter = animatedTabIconPainter(item.destination)
								if (painter != null) {
									Icon(painter = painter, null)
								} else {
									Icon(item.icon, null)
								}
							} else {
								Icon(item.iconUnselected, null)
							}
						},
						label = {
							Text(stringResource(item.label))
						}
					)
				}
			}
		}
	}
}

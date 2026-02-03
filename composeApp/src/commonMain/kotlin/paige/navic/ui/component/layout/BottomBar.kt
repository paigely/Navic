package paige.navic.ui.component.layout

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationItemIconPosition
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.json.Json
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.album
import navic.composeapp.generated.resources.album_unselected
import navic.composeapp.generated.resources.artist
import navic.composeapp.generated.resources.artist_unselected
import navic.composeapp.generated.resources.library_music
import navic.composeapp.generated.resources.library_music_unselected
import navic.composeapp.generated.resources.playlist_play
import navic.composeapp.generated.resources.title_albums
import navic.composeapp.generated.resources.title_artists
import navic.composeapp.generated.resources.title_library
import navic.composeapp.generated.resources.title_playlists
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.data.model.NavbarConfig
import paige.navic.data.model.NavbarTab
import paige.navic.data.model.Screen
import paige.navic.data.model.Settings
import paige.navic.ui.component.dialog.NavtabsViewModel
import paige.navic.util.UiState

private enum class NavItem(
	val destination: NavKey,
	val icon: DrawableResource,
	val iconUnselected: DrawableResource = icon,
	val label: StringResource
) {
	LIBRARY(
		destination = Screen.Library(),
		icon = Res.drawable.library_music,
		iconUnselected = Res.drawable.library_music_unselected,
		label = Res.string.title_library
	),
	ALBUMS(
		destination = Screen.Albums(),
		icon = Res.drawable.album,
		iconUnselected = Res.drawable.album_unselected,
		label = Res.string.title_albums
	),
	PLAYLISTS(
		destination = Screen.Playlists(),
		icon = Res.drawable.playlist_play,
		label = Res.string.title_playlists
	),
	ARTISTS(
		destination = Screen.Artists(),
		icon = Res.drawable.artist,
		iconUnselected = Res.drawable.artist_unselected,
		label = Res.string.title_artists
	)
}

@Composable
fun BottomBar(
	collapseSheet: () -> Unit,
	viewModel: NavtabsViewModel = viewModel { NavtabsViewModel(com.russhwolf.settings.Settings(), Json) }
) {
	val backStack = LocalNavStack.current
	val ctx = LocalCtx.current
	val state by viewModel.state.collectAsState()

	AnimatedContent(
		!Settings.shared.useShortNavbar
			&& ctx.sizeClass.widthSizeClass <= WindowWidthSizeClass.Compact
	) {
		val tabs = ((state as? UiState.Success)?.data ?: NavbarConfig.default)
			.tabs.filter { tab -> tab.visible }
		if (it) {
			NavigationBar {
				tabs.forEach { tab ->
					val item = when (tab.id) {
						NavbarTab.Id.LIBRARY -> NavItem.LIBRARY
						NavbarTab.Id.ALBUMS -> NavItem.ALBUMS
						NavbarTab.Id.PLAYLISTS -> NavItem.PLAYLISTS
						NavbarTab.Id.ARTISTS -> NavItem.ARTISTS
					}
					val selected = backStack.last() == item.destination

					NavigationBarItem(
						selected = selected,
						onClick = {
							collapseSheet()
							ctx.clickSound()
							backStack.clear()
							backStack.add(item.destination)
						},
						icon = {
							Crossfade(
								selected,
								animationSpec = tween(3000)
							) { selected ->
								if (selected) {
									Icon(vectorResource(item.icon), null)
								} else {
									Icon(vectorResource(item.iconUnselected), null)
								}
							}
						},
						label = {
							Text(stringResource(item.label))
						}
					)
				}
			}
		} else {
			ShortNavigationBar {
				tabs.forEach { tab ->
					val item = when (tab.id) {
						NavbarTab.Id.LIBRARY -> NavItem.LIBRARY
						NavbarTab.Id.ALBUMS -> NavItem.ALBUMS
						NavbarTab.Id.PLAYLISTS -> NavItem.PLAYLISTS
						NavbarTab.Id.ARTISTS -> NavItem.ARTISTS
					}
					val selected = backStack.last() == item.destination

					ShortNavigationBarItem(
						iconPosition = if (ctx.sizeClass.widthSizeClass > WindowWidthSizeClass.Compact)
							NavigationItemIconPosition.Start
						else NavigationItemIconPosition.Top,
						selected = backStack.last() == item.destination,
						onClick = {
							collapseSheet()
							ctx.clickSound()
							backStack.clear()
							backStack.add(item.destination)
						},
						icon = {
							Crossfade(
								selected
							) { selected ->
								if (selected) {
									Icon(vectorResource(item.icon), null)
								} else {
									Icon(vectorResource(item.iconUnselected), null)
								}
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

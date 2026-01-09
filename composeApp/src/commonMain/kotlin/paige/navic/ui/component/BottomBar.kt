package paige.navic.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationItemIconPosition
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.russhwolf.settings.Settings
import dev.burnoo.compose.remembersetting.rememberBooleanSetting
import kotlinx.serialization.json.Json
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.account_circle
import navic.composeapp.generated.resources.library_music
import navic.composeapp.generated.resources.playlist_play
import navic.composeapp.generated.resources.title_artists
import navic.composeapp.generated.resources.title_library
import navic.composeapp.generated.resources.title_playlists
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import paige.navic.Artists
import paige.navic.Library
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.Playlists
import paige.navic.data.model.NavTabId
import paige.navic.ui.component.dialog.NavbarTabsViewModel

private enum class NavItem(
	val destination: Any,
	val icon: DrawableResource,
	val label: StringResource
) {
	LIBRARY(Library, Res.drawable.library_music, Res.string.title_library),
	PLAYLISTS(Playlists, Res.drawable.playlist_play, Res.string.title_playlists),
	ARTISTS(Artists, Res.drawable.account_circle, Res.string.title_artists)
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BottomBar(
	viewModel: NavbarTabsViewModel = viewModel { NavbarTabsViewModel(Settings(), Json) }
) {
	val backStack = LocalNavStack.current
	val ctx = LocalCtx.current
	var useShortNavbar by rememberBooleanSetting("useShortNavbar", false)
	val config = viewModel.config

	AnimatedContent(targetState = useShortNavbar) { short ->
		if (!short && ctx.sizeClass.widthSizeClass <= WindowWidthSizeClass.Compact) {
			NavigationBar {
				config.tabs
					.filter { it.visible }
					.forEach { tab ->
						val item = when (tab.id) {
							NavTabId.LIBRARY -> NavItem.LIBRARY
							NavTabId.PLAYLISTS -> NavItem.PLAYLISTS
							NavTabId.ARTISTS -> NavItem.ARTISTS
						}

						NavigationBarItem(
							selected = backStack.last() == item.destination,
							onClick = {
								ctx.clickSound()
								backStack.clear()
								backStack.add(item.destination)
							},
							icon = {
								Icon(vectorResource(item.icon), null)
							},
							label = {
								Text(stringResource(item.label))
							}
						)
					}
			}
		} else {
			ShortNavigationBar {
				config.tabs
					.filter { it.visible }
					.forEach { tab ->
						val item = when (tab.id) {
							NavTabId.LIBRARY -> NavItem.LIBRARY
							NavTabId.PLAYLISTS -> NavItem.PLAYLISTS
							NavTabId.ARTISTS -> NavItem.ARTISTS
						}

						ShortNavigationBarItem(
							iconPosition = if (ctx.sizeClass.widthSizeClass > WindowWidthSizeClass.Compact)
								NavigationItemIconPosition.Start
							else NavigationItemIconPosition.Top,
							selected = backStack.last() == item.destination,
							onClick = {
								ctx.clickSound()
								backStack.clear()
								backStack.add(item.destination)
							},
							icon = {
								Icon(vectorResource(item.icon), null)
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


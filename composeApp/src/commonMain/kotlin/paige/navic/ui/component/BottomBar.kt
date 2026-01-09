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
import dev.burnoo.compose.remembersetting.rememberBooleanSetting
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.library_music
import navic.composeapp.generated.resources.playlist_play
import navic.composeapp.generated.resources.title_library
import navic.composeapp.generated.resources.title_playlists
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import paige.navic.Library
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.Playlists

private enum class NavItem(
	val destination: Any,
	val icon: DrawableResource,
	val label: StringResource
) {
	LIBRARY(Library, Res.drawable.library_music, Res.string.title_library),
	PLAYLISTS(Playlists, Res.drawable.playlist_play, Res.string.title_playlists)
}

@Composable
private fun NavItems(
	item: @Composable (
		Boolean,
		() -> Unit,
		@Composable () -> Unit,
		@Composable () -> Unit
	) -> Unit
) {
	val backStack = LocalNavStack.current
	val ctx = LocalCtx.current
	NavItem.entries.forEach { navItem ->
		item(
			backStack.last() == navItem.destination,
			{
				ctx.clickSound()
				backStack.clear()
				backStack.add(navItem.destination)
			},
			{
				Icon(
					vectorResource(navItem.icon),
					contentDescription = null
				)
			},
			{ Text(stringResource(navItem.label)) }
		)
	}
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BottomBar() {
	val ctx = LocalCtx.current
	var useShortNavbar by rememberBooleanSetting("useShortNavbar", false)

	AnimatedContent(targetState = useShortNavbar) { short ->
		if (!short && ctx.sizeClass.widthSizeClass <= WindowWidthSizeClass.Compact) {
			NavigationBar {
				NavItems { selected, onClick, icon, label ->
					NavigationBarItem(
						selected = selected,
						onClick = onClick,
						icon = icon,
						label = label
					)
				}
			}
		} else {
			ShortNavigationBar {
				NavItems { selected, onClick, icon, label ->
					ShortNavigationBarItem(
						iconPosition = if (ctx.sizeClass.widthSizeClass > WindowWidthSizeClass.Compact)
							NavigationItemIconPosition.Start
						else NavigationItemIconPosition.Top,
						selected = selected,
						onClick = onClick,
						icon = icon,
						label = label
					)
				}
			}
		}
	}
}


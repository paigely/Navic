package paige.navic.ui.component

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.library_music
import navic.composeapp.generated.resources.playlist_play
import org.jetbrains.compose.resources.vectorResource
import paige.navic.Library
import paige.navic.LocalCtx
import paige.navic.Playlists

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SideBar(
	backStack: SnapshotStateList<Any>
) {
	val ctx = LocalCtx.current
	NavigationRail {
		NavigationRailItem(
			selected = backStack.last() == Library,
			onClick = {
				ctx.clickSound()
				backStack.clear()
				backStack.add(Library)
			},
			icon = {
				Icon(
					vectorResource(Res.drawable.library_music),
					contentDescription = null
				)
			},
			label = { Text("Library") },
		)
		NavigationRailItem(
			selected = backStack.last() == Playlists,
			onClick = {
				ctx.clickSound()
				backStack.clear()
				backStack.add(Playlists)
			},
			icon = {
				Icon(
					vectorResource(Res.drawable.playlist_play),
					contentDescription = null
				)
			},
			label = { Text("Playlists") },
		)
	}
}

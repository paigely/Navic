package paige.navic

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.NavDisplay.popTransitionSpec
import androidx.navigation3.ui.NavDisplay.predictivePopTransitionSpec
import androidx.navigation3.ui.NavDisplay.transitionSpec
import paige.navic.ui.component.BottomBar
import paige.navic.ui.component.MainScaffold
import paige.navic.ui.component.MediaBar
import paige.navic.ui.component.SideBar
import paige.navic.ui.component.TopBar
import paige.navic.ui.screen.LibraryScreen
import paige.navic.ui.screen.PlaylistsScreen
import paige.navic.ui.screen.SettingsScreen
import paige.navic.ui.screen.TracksScreen
import paige.navic.ui.theme.NavicTheme
import paige.subsonic.api.model.AnyTracks

data object Library
data object Playlists
data object Settings
data class Tracks(val tracks: AnyTracks)

val LocalCtx = staticCompositionLocalOf<Ctx> {
	error("no ctx")
}

val LocalNavStack = staticCompositionLocalOf<SnapshotStateList<Any>> {
	error("no backstack")
}

@Composable
fun App() {
	val ctx = rememberCtx()
	val backStack = remember { mutableStateListOf<Any>(Library) }

	CompositionLocalProvider(
		LocalCtx provides ctx,
		LocalNavStack provides backStack
	) {
		NavicTheme {
			Row {
				if (ctx.sizeClass.widthSizeClass > WindowWidthSizeClass.Compact) {
					SideBar(backStack)
				}
				MainScaffold(
					backStack = backStack,
					topBar = {
						TopBar(backStack = it)
					}
				) {
					Box(modifier = Modifier.fillMaxSize()) {
						val metadata = transitionSpec {
							ContentTransform(fadeIn(), fadeOut())
						} + popTransitionSpec {
							ContentTransform(fadeIn(), fadeOut())
						} + predictivePopTransitionSpec {
							ContentTransform(fadeIn(), fadeOut())
						}
						NavDisplay(
							modifier = Modifier.padding(it),
							backStack = backStack,
							onBack = { backStack.removeLastOrNull() },
							entryProvider = entryProvider {
								entry<Library>(metadata = metadata) {
									LibraryScreen()
								}
								entry<Playlists>(metadata = metadata) {
									PlaylistsScreen(backStack)
								}
								entry<Settings> {
									SettingsScreen()
								}
								entry<Tracks> { key ->
									TracksScreen(key.tracks)
								}
							},
							transitionSpec = {
								slideInHorizontally(initialOffsetX = { it }) togetherWith
									slideOutHorizontally(targetOffsetX = { -it })
							},
							popTransitionSpec = {
								slideInHorizontally(initialOffsetX = { -it }) togetherWith
									slideOutHorizontally(targetOffsetX = { it })
							},
							predictivePopTransitionSpec = {
								slideInHorizontally(initialOffsetX = { -it }) togetherWith
									slideOutHorizontally(targetOffsetX = { it })
							}
						)

						// this@Row needed because of a bug in kotlin (lol)
						// https://stackoverflow.com/a/68742173
						this@Row.AnimatedVisibility(
							modifier = Modifier.align(Alignment.BottomCenter),
							visible = ctx.sizeClass.widthSizeClass <= WindowWidthSizeClass.Compact && backStack.count() == 1,
							enter = slideInHorizontally { -it },
							exit = slideOutHorizontally { -it }
						) {
							Column {
								MediaBar()
								BottomBar(backStack)
							}
						}
					}
				}
			}
		}
	}
}
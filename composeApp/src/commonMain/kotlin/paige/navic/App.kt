package paige.navic

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.NavDisplay.popTransitionSpec
import androidx.navigation3.ui.NavDisplay.predictivePopTransitionSpec
import androidx.navigation3.ui.NavDisplay.transitionSpec
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import paige.navic.ui.component.BottomBar
import paige.navic.ui.component.MainScaffold
import paige.navic.ui.component.TopBar
import paige.navic.ui.screen.LibraryScreen
import paige.navic.ui.screen.PlaylistsScreen
import paige.navic.ui.screen.SettingsScreen
import paige.navic.ui.screen.TracksScreen
import paige.navic.ui.theme.NavicTheme

data object Library
data object Playlists
data object Settings
data class Tracks(val partialTracks: Any)

val LocalCtx = staticCompositionLocalOf<Ctx> {
	error("no ctx")
}

val LocalMediaPlayer = staticCompositionLocalOf<MediaPlayer> {
	error("no media player")
}

val LocalNavStack = staticCompositionLocalOf<SnapshotStateList<Any>> {
	error("no backstack")
}

val LocalImageBuilder = staticCompositionLocalOf<ImageRequest.Builder> {
	error("no image builder")
}

val LocalSnackbarState = staticCompositionLocalOf<SnackbarHostState> {
	error("no snackbar state")
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun App() {
	val ctx = rememberCtx()
	val platformContext = LocalPlatformContext.current
	val mediaPlayer = rememberMediaPlayer()
	val backStack = remember { mutableStateListOf<Any>(Library) }
	val sceneStrategy = rememberListDetailSceneStrategy<Any>()
	val imageBuilder = ImageRequest.Builder(platformContext)
		.crossfade(true)
	val snackbarState = remember { SnackbarHostState() }

	CompositionLocalProvider(
		LocalCtx provides ctx,
		LocalMediaPlayer provides mediaPlayer,
		LocalNavStack provides backStack,
		LocalImageBuilder provides imageBuilder,
		LocalSnackbarState provides snackbarState
	) {
		NavicTheme {
			Row {
				MainScaffold(
					snackbarState = snackbarState,
					topBar = { TopBar() },
					bottomBar = { BottomBar() }
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
							backStack = backStack,
							sceneStrategy = sceneStrategy,
							onBack = { backStack.removeLastOrNull() },
							entryProvider = entryProvider {
								entry<Library>(metadata = metadata + ListDetailSceneStrategy.listPane()) {
									LibraryScreen()
								}
								entry<Playlists>(metadata = metadata) {
									PlaylistsScreen(backStack)
								}
								entry<Settings> {
									SettingsScreen()
								}
								entry<Tracks>(metadata = ListDetailSceneStrategy.detailPane()) { key ->
									TracksScreen(key.partialTracks)
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
					}
				}
			}
		}
	}
}
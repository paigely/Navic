package paige.navic

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy.Companion.detailPane
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy.Companion.listPane
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.NavDisplay.popTransitionSpec
import androidx.navigation3.ui.NavDisplay.predictivePopTransitionSpec
import androidx.navigation3.ui.NavDisplay.transitionSpec
import androidx.savedstate.serialization.SavedStateConfiguration
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.kmpalette.loader.rememberNetworkLoader
import com.kmpalette.rememberDominantColorState
import com.kyant.capsule.ContinuousRoundedRectangle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.http.Url
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import paige.navic.data.model.BottomSheetSceneStrategy
import paige.navic.data.model.Screen
import paige.navic.data.model.Settings
import paige.navic.data.session.SessionManager
import paige.navic.shared.Ctx
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.shared.rememberCtx
import paige.navic.shared.rememberMediaPlayer
import paige.navic.ui.component.layout.BottomBar
import paige.navic.ui.component.layout.PlayerBar
import paige.navic.ui.screen.AlbumsScreen
import paige.navic.ui.screen.ArtistScreen
import paige.navic.ui.screen.ArtistsScreen
import paige.navic.ui.screen.LibraryScreen
import paige.navic.ui.screen.LyricsScreen
import paige.navic.ui.screen.PlayerScreen
import paige.navic.ui.screen.PlaylistsScreen
import paige.navic.ui.screen.SearchScreen
import paige.navic.ui.screen.SettingsAboutScreen
import paige.navic.ui.screen.SettingsAcknowledgementsScreen
import paige.navic.ui.screen.SettingsAppearanceScreen
import paige.navic.ui.screen.SettingsBehaviourScreen
import paige.navic.ui.screen.SettingsScreen
import paige.navic.ui.screen.TracksScreen
import paige.navic.ui.theme.NavicTheme
import paige.navic.util.easedVerticalGradient

// rememberNavBackStack on android will automatically
// make this, but since other platforms don't have
// reflection this needs to be made in kmp manually
private val config = SavedStateConfiguration {
	serializersModule = SerializersModule {
		polymorphic(NavKey::class) {
			// tabs
			subclass(Screen.Library::class, Screen.Library.serializer())
			subclass(Screen.Albums::class, Screen.Albums.serializer())
			subclass(Screen.Playlists::class, Screen.Playlists.serializer())
			subclass(Screen.Artists::class, Screen.Artists.serializer())

			// misc
			subclass(Screen.Player::class, Screen.Player.serializer())
			subclass(Screen.Search::class, Screen.Search.serializer())
			subclass(Screen.Tracks::class, Screen.Tracks.serializer())
			subclass(Screen.Artist::class, Screen.Artist.serializer())

			// settings
			subclass(Screen.Settings.Root::class, Screen.Settings.Root.serializer())
			subclass(Screen.Settings.Appearance::class, Screen.Settings.Appearance.serializer())
			subclass(Screen.Settings.Behaviour::class, Screen.Settings.Behaviour.serializer())
			subclass(Screen.Settings.About::class, Screen.Settings.About.serializer())
			subclass(Screen.Settings.Acknowledgements::class, Screen.Settings.Acknowledgements.serializer())
		}
	}
}

val LocalCtx = staticCompositionLocalOf<Ctx> { error("no ctx") }
val LocalMediaPlayer = staticCompositionLocalOf<MediaPlayerViewModel> { error("no media player") }
val LocalNavStack = staticCompositionLocalOf<NavBackStack<NavKey>> { error("no backstack") }
val LocalImageBuilder = staticCompositionLocalOf<ImageRequest.Builder> { error("no image builder") }
val LocalSnackbarState = staticCompositionLocalOf<SnackbarHostState> { error("no snackbar state") }
val LocalContentPadding = staticCompositionLocalOf { PaddingValues() }

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun App() {
	val platformContext = LocalPlatformContext.current
	val ctx = rememberCtx()
	val mediaPlayer = rememberMediaPlayer()
	val backStack = rememberNavBackStack(config, Screen.Library())
	val imageBuilder = remember { ImageRequest.Builder(platformContext).crossfade(true) }
	val snackbarState = remember { SnackbarHostState() }

	CompositionLocalProvider(
		LocalCtx provides ctx,
		LocalMediaPlayer provides mediaPlayer,
		LocalNavStack provides backStack,
		LocalImageBuilder provides imageBuilder,
		LocalSnackbarState provides snackbarState
	) {
		NavicTheme {
			val contentScale by animateFloatAsState(
				if (backStack.contains(Screen.Player))
					.9f
				else 1f, MaterialTheme.motionScheme.defaultSpatialSpec()
			)
			val contentRounding by animateDpAsState(
				if (backStack.contains(Screen.Player))
					60.dp
				else 0.dp, MaterialTheme.motionScheme.slowEffectsSpec()
			)
			Scaffold(
				modifier = Modifier
					.background(Color.Black)
					.scale(contentScale)
					.clip(ContinuousRoundedRectangle(contentRounding)),
				snackbarHost = {
					SnackbarHost(hostState = snackbarState)
				},
				bottomBar = {
					Column(
						modifier = if (Settings.shared.detachedBar)
							Modifier.background(
								Brush.easedVerticalGradient(color = MaterialTheme.colorScheme.surface)
							)
						else Modifier
					) {
						PlayerBar()
						BottomBar(
							containerColor = if (Settings.shared.detachedBar)
								NavigationBarDefaults.containerColor.copy(alpha = 0f)
							else NavigationBarDefaults.containerColor
						)
					}
				}
			) { contentPadding ->
				CompositionLocalProvider(
					LocalContentPadding provides contentPadding
				) {
					NavDisplay(
						modifier = Modifier
							.padding(
								start = contentPadding.calculateStartPadding(LocalLayoutDirection.current),
								end = contentPadding.calculateEndPadding(LocalLayoutDirection.current)
							)
							.fillMaxSize()
							.background(MaterialTheme.colorScheme.surface),
						backStack = backStack,
						sceneStrategy = rememberListDetailSceneStrategy<NavKey>()
							then remember { BottomSheetSceneStrategy() },
						onBack = { backStack.removeLastOrNull() },
						entryProvider = entryProvider(backStack),
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

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
private fun entryProvider(
	backStack: NavBackStack<NavKey>
): (NavKey) -> (NavEntry<NavKey>) {
	val navtabMetadata = if (backStack.size == 1)
		listPane("root") + transitionSpec {
			ContentTransform(fadeIn(), fadeOut())
		} + popTransitionSpec {
			ContentTransform(fadeIn(), fadeOut())
		} + predictivePopTransitionSpec {
			ContentTransform(fadeIn(), fadeOut())
		}
	else listPane("root")
	return androidx.navigation3.runtime.entryProvider {
		// tabs
		entry<Screen.Library>(metadata = navtabMetadata) {
			LibraryScreen()
		}
		entry<Screen.Albums>(metadata = navtabMetadata) { key ->
			AlbumsScreen(key.nested, key.listType)
		}
		entry<Screen.Playlists>(metadata = navtabMetadata) { key ->
			PlaylistsScreen(key.nested)
		}
		entry<Screen.Artists>(metadata = navtabMetadata) { key ->
			ArtistsScreen(key.nested)
		}

		// misc
		entry<Screen.Player>(metadata = BottomSheetSceneStrategy.bottomSheet()) {
			PlayerScreen()
		}
		entry<Screen.Lyrics>(metadata = BottomSheetSceneStrategy.bottomSheet()) {
			val player = LocalMediaPlayer.current
			val playerState by player.uiState.collectAsState()
			val track = playerState.currentTrack
			LyricsScreen(track)
		}
		entry<Screen.Tracks>(metadata = detailPane("root")) { key ->
			TracksScreen(key.partialCollection)
		}
		entry<Screen.Search> {
			SearchScreen()
		}
		entry<Screen.Artist>(metadata = detailPane("root")) { key ->
			ArtistScreen(key.artist)
		}

		// settings
		entry<Screen.Settings.Root>(metadata = listPane("settings")) {
			SettingsScreen()
		}
		entry<Screen.Settings.Appearance>(metadata = detailPane("settings")) {
			SettingsAppearanceScreen()
		}
		entry<Screen.Settings.Behaviour>(metadata = detailPane("settings")) {
			SettingsBehaviourScreen()
		}
		entry<Screen.Settings.About>(metadata = detailPane("settings")) {
			SettingsAboutScreen()
		}
		entry<Screen.Settings.Acknowledgements>(metadata = detailPane("settings")) {
			SettingsAcknowledgementsScreen()
		}
	}
}

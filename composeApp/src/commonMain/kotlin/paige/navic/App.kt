package paige.navic

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.NavDisplay.popTransitionSpec
import androidx.navigation3.ui.NavDisplay.predictivePopTransitionSpec
import androidx.navigation3.ui.NavDisplay.transitionSpec
import androidx.savedstate.serialization.SavedStateConfiguration
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import paige.navic.data.models.BottomSheetSceneStrategy
import paige.navic.data.models.Screen
import paige.navic.data.models.Settings
import paige.navic.shared.Ctx
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.shared.rememberCtx
import paige.navic.shared.rememberMediaPlayer
import paige.navic.ui.components.layouts.BottomBar
import paige.navic.ui.components.layouts.PlayerBar
import paige.navic.ui.screens.AddToPlaylistScreen
import paige.navic.ui.screens.AlbumsScreen
import paige.navic.ui.screens.ArtistScreen
import paige.navic.ui.screens.ArtistsScreen
import paige.navic.ui.screens.CreatePlaylistScreen
import paige.navic.ui.screens.LibraryScreen
import paige.navic.ui.screens.LyricsScreen
import paige.navic.ui.screens.PlayerScreen
import paige.navic.ui.screens.PlaylistsScreen
import paige.navic.ui.screens.SearchScreen
import paige.navic.ui.screens.TrackInfoScreen
import paige.navic.ui.screens.TracksScreen
import paige.navic.ui.screens.settings.BottomBarScreen
import paige.navic.ui.screens.settings.NowPlayingScreen
import paige.navic.ui.screens.settings.ScrobblingScreen
import paige.navic.ui.screens.settings.SettingsAboutScreen
import paige.navic.ui.screens.settings.SettingsAcknowledgementsScreen
import paige.navic.ui.screens.settings.SettingsAppearanceScreen
import paige.navic.ui.screens.settings.SettingsBehaviourScreen
import paige.navic.ui.screens.settings.SettingsScreen
import paige.navic.ui.theme.NavicTheme
import paige.navic.utils.easedVerticalGradient

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
			subclass(Screen.Lyrics::class, Screen.Lyrics.serializer())
			subclass(Screen.Search::class, Screen.Search.serializer())
			subclass(Screen.Tracks::class, Screen.Tracks.serializer())
			subclass(Screen.TrackInfo::class, Screen.TrackInfo.serializer())
			subclass(Screen.Artist::class, Screen.Artist.serializer())
			subclass(Screen.AddToPlaylist::class, Screen.AddToPlaylist.serializer())
			subclass(Screen.CreatePlaylist::class, Screen.CreatePlaylist.serializer())

			// settings
			subclass(Screen.Settings.Root::class, Screen.Settings.Root.serializer())
			subclass(Screen.Settings.Appearance::class, Screen.Settings.Appearance.serializer())
			subclass(Screen.Settings.Behaviour::class, Screen.Settings.Behaviour.serializer())
			subclass(Screen.Settings.BottomAppBar::class, Screen.Settings.BottomAppBar.serializer())
			subclass(Screen.Settings.NowPlaying::class, Screen.Settings.NowPlaying.serializer())
			subclass(Screen.Settings.Scrobbling::class, Screen.Settings.Scrobbling.serializer())
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
			Scaffold(
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
						sceneStrategy = remember { BottomSheetSceneStrategy<NavKey>() }
							then remember { DialogSceneStrategy() }
							then rememberListDetailSceneStrategy(),
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
		entry<Screen.Player>(metadata = BottomSheetSceneStrategy.bottomSheet(maxWidth = Dp.Unspecified)) {
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
		entry<Screen.TrackInfo> { key ->
			TrackInfoScreen(key.track)
		}
		entry<Screen.Search> {
			SearchScreen()
		}
		entry<Screen.Artist>(metadata = detailPane("root")) { key ->
			ArtistScreen(key.artist)
		}
		entry<Screen.AddToPlaylist>(metadata = DialogSceneStrategy.dialog()) { key ->
			AddToPlaylistScreen(key.tracks, key.playlistToExclude)
		}
		entry<Screen.CreatePlaylist>(metadata = DialogSceneStrategy.dialog()) { key ->
			CreatePlaylistScreen(key.tracks)
		}

		// settings
		entry<Screen.Settings.Root>(metadata = listPane("settings")) {
			SettingsScreen()
		}
		entry<Screen.Settings.Appearance>(metadata = detailPane("settings")) {
			SettingsAppearanceScreen()
		}
		entry<Screen.Settings.BottomAppBar>(metadata = detailPane("settings")) {
			BottomBarScreen()
		}
		entry<Screen.Settings.NowPlaying>(metadata = detailPane("settings")) {
			NowPlayingScreen()
		}
		entry<Screen.Settings.Behaviour>(metadata = detailPane("settings")) {
			SettingsBehaviourScreen()
		}
		entry<Screen.Settings.Scrobbling>(metadata = detailPane("settings")) {
			ScrobblingScreen()
		}
		entry<Screen.Settings.About>(metadata = detailPane("settings")) {
			SettingsAboutScreen()
		}
		entry<Screen.Settings.Acknowledgements>(metadata = detailPane("settings")) {
			SettingsAcknowledgementsScreen()
		}
	}
}

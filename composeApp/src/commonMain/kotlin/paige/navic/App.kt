package paige.navic

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
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
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_update_app
import navic.composeapp.generated.resources.info_update_app
import org.jetbrains.compose.resources.getString
import paige.navic.data.models.Screen
import paige.navic.data.models.settings.Settings
import paige.navic.shared.Ctx
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.shared.ShareManager
import paige.navic.shared.rememberCtx
import paige.navic.shared.rememberMediaPlayer
import paige.navic.shared.rememberShareManager
import paige.navic.ui.components.dialogs.SideloadingDialog
import paige.navic.ui.navigation.Material3Transitions
import paige.navic.ui.scenes.BottomSheetSceneStrategy
import paige.navic.ui.screens.AddToPlaylistScreen
import paige.navic.ui.screens.AlbumsScreen
import paige.navic.ui.screens.ArtistScreen
import paige.navic.ui.screens.ArtistsScreen
import paige.navic.ui.screens.CreatePlaylistScreen
import paige.navic.ui.screens.LibraryScreen
import paige.navic.ui.screens.LyricsScreen
import paige.navic.ui.screens.PlayerScreen
import paige.navic.ui.screens.PlaylistsScreen
import paige.navic.ui.screens.QueueScreen
import paige.navic.ui.screens.SearchScreen
import paige.navic.ui.screens.SharesScreen
import paige.navic.ui.screens.genres.GenresScreen
import paige.navic.ui.screens.settings.BottomBarScreen
import paige.navic.ui.screens.settings.FontsScreen
import paige.navic.ui.screens.settings.SettingsAboutScreen
import paige.navic.ui.screens.settings.SettingsAcknowledgementsScreen
import paige.navic.ui.screens.settings.SettingsAppearanceScreen
import paige.navic.ui.screens.settings.SettingsDeveloperScreen
import paige.navic.ui.screens.settings.SettingsNowPlayingScreen
import paige.navic.ui.screens.settings.SettingsPlaybackScreen
import paige.navic.ui.screens.settings.SettingsScreen
import paige.navic.ui.screens.tracks.TrackInfoScreen
import paige.navic.ui.screens.tracks.TracksScreen
import paige.navic.ui.theme.NavicTheme
import paige.navic.utils.checkForUpdate

@OptIn(ExperimentalSerializationApi::class)
private val config = SavedStateConfiguration {
	serializersModule = SerializersModule {
		polymorphic(NavKey::class) {
			subclassesOfSealed<Screen>()
		}
	}
}

val LocalCtx = staticCompositionLocalOf<Ctx> { error("no ctx") }
val LocalMediaPlayer = staticCompositionLocalOf<MediaPlayerViewModel> { error("no media player") }
val LocalNavStack = staticCompositionLocalOf<NavBackStack<NavKey>> { error("no backstack") }
val LocalImageBuilder = staticCompositionLocalOf<ImageRequest.Builder> { error("no image builder") }
val LocalSnackbarState = staticCompositionLocalOf<SnackbarHostState> { error("no snackbar state") }
val LocalShareManager = staticCompositionLocalOf<ShareManager> { error("no share manager") }
val LocalSharedTransitionScope =
	staticCompositionLocalOf<SharedTransitionScope> { error("no shared transition scope") }

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun App() {
	val shareManager = rememberShareManager()
	val platformContext = LocalPlatformContext.current
	val uriHandler = LocalUriHandler.current
	val ctx = rememberCtx()
	val mediaPlayer = rememberMediaPlayer()
	val backStack = rememberNavBackStack(config, Screen.Library())
	val imageBuilder = remember { ImageRequest.Builder(platformContext).crossfade(true) }
	val snackbarState = remember { SnackbarHostState() }
	val density = LocalDensity.current

	// todo: this should survive config changes but im lazy ykyk
	LaunchedEffect(Unit) {
		checkForUpdate(ctx.appVersion)?.let { newRelease ->
			val result = snackbarState.showSnackbar(
				message = getString(Res.string.info_update_app),
				actionLabel = getString(Res.string.action_update_app),
				withDismissAction = true,
				duration = SnackbarDuration.Indefinite
			)
			if (result == SnackbarResult.ActionPerformed) {
				uriHandler.openUri(newRelease.url)
			}
		}
	}

	SharedTransitionLayout {
		CompositionLocalProvider(
			LocalCtx provides ctx,
			LocalMediaPlayer provides mediaPlayer,
			LocalNavStack provides backStack,
			LocalImageBuilder provides imageBuilder,
			LocalSnackbarState provides snackbarState,
			LocalShareManager provides shareManager,
			LocalSharedTransitionScope provides this@SharedTransitionLayout
		) {
			NavicTheme {
				Scaffold(
					snackbarHost = {
						SnackbarHost(hostState = snackbarState) { snackbarData ->
							Snackbar(
								snackbarData = snackbarData,
								shape = MaterialTheme.shapes.large
							)
						}
					}
				) { contentPadding ->
					NavDisplay(
						modifier = Modifier
							.padding(
								start = contentPadding.calculateStartPadding(
									LocalLayoutDirection.current
								),
								end = contentPadding.calculateEndPadding(
									LocalLayoutDirection.current
								)
							)
							.fillMaxSize()
							.background(MaterialTheme.colorScheme.surface),
						backStack = backStack,
						sceneStrategies = listOf(
							remember { BottomSheetSceneStrategy() },
							remember { DialogSceneStrategy() },
							rememberListDetailSceneStrategy()
						),
						onBack = { backStack.removeLastOrNull() },
						entryProvider = entryProvider(backStack),
						transitionSpec = {
							Material3Transitions.SharedXAxisEnterTransition(density) togetherWith Material3Transitions.SharedXAxisExitTransition(
								density
							)
						},
						popTransitionSpec = {
							Material3Transitions.SharedXAxisPopEnterTransition(density) togetherWith Material3Transitions.SharedXAxisPopExitTransition(
								density
							)
						},
						predictivePopTransitionSpec = {
							Material3Transitions.SharedZAxisEnterTransition togetherWith Material3Transitions.SharedZAxisExitTransition
						}
					)
				}
			}
			if (!Settings.shared.showedSideloadingWarning
				&& ctx.name.lowercase().contains("android")
			) {
				SideloadingDialog()
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
		entry<Screen.Genres>(metadata = navtabMetadata) { key ->
			GenresScreen(key.nested)
		}

		// misc
		entry<Screen.Player>(
			metadata = BottomSheetSceneStrategy.bottomSheet(
				maxWidth = Dp.Unspecified,
				screenType = "player"
			)
		) {
			PlayerScreen()
		}
		entry<Screen.Lyrics>(metadata = BottomSheetSceneStrategy.bottomSheet(isTransparent = true)) {
			val player = LocalMediaPlayer.current
			val playerState by player.uiState.collectAsState()
			val track = playerState.currentTrack
			LyricsScreen(track)
		}
		entry<Screen.Queue>(metadata = BottomSheetSceneStrategy.bottomSheet(isTransparent = true)) {
			QueueScreen()
		}
		entry<Screen.Tracks>(metadata = detailPane("root")) { key ->
			TracksScreen(key.partialCollection, key.tab)
		}
		entry<Screen.TrackInfo>(metadata = detailPane("root")) { key ->
			TrackInfoScreen(key.track)
		}
		entry<Screen.Search>(metadata = navtabMetadata) { key ->
			SearchScreen(key.nested)
		}
		entry<Screen.Shares> {
			SharesScreen()
		}
		entry<Screen.Artist> { key ->
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
			SettingsNowPlayingScreen()
		}
		entry<Screen.Settings.Playback>(metadata = detailPane("settings")) {
			SettingsPlaybackScreen()
		}
		entry<Screen.Settings.Developer>(metadata = detailPane("settings")) {
			SettingsDeveloperScreen()
		}
		entry<Screen.Settings.About>(metadata = detailPane("settings")) {
			SettingsAboutScreen()
		}
		entry<Screen.Settings.Acknowledgements>(metadata = detailPane("settings")) {
			SettingsAcknowledgementsScreen()
		}
		entry<Screen.Settings.Fonts> {
			FontsScreen()
		}
	}
}

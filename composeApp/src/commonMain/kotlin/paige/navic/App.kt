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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
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
import coil3.compose.setSingletonImageLoaderFactory
import coil3.request.ImageRequest
import coil3.request.crossfade
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.data.images.initializeSingletonImageLoader
import paige.navic.data.models.Screen
import paige.navic.data.models.settings.Settings
import paige.navic.data.session.SessionManager
import paige.navic.shared.Ctx
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.shared.rememberCtx
import paige.navic.ui.components.dialogs.SideloadingDialog
import paige.navic.ui.components.sheets.ChangelogSheet
import paige.navic.ui.scenes.BottomSheetSceneStrategy
import paige.navic.ui.scenes.NowPlayingSceneStrategy
import paige.navic.ui.screens.album.AlbumListScreen
import paige.navic.ui.screens.artist.ArtistDetailScreen
import paige.navic.ui.screens.artist.ArtistListScreen
import paige.navic.ui.screens.collection.CollectionDetailScreen
import paige.navic.ui.screens.genre.GenreListScreen
import paige.navic.ui.screens.library.LibraryScreen
import paige.navic.ui.screens.login.LoginScreen
import paige.navic.ui.screens.lyrics.LyricsScreen
import paige.navic.ui.screens.nowPlaying.NowPlayingScreen
import paige.navic.ui.screens.playlist.PlaylistListScreen
import paige.navic.ui.screens.queue.QueueScreen
import paige.navic.ui.screens.radio.RadioListScreen
import paige.navic.ui.screens.search.SearchScreen
import paige.navic.ui.screens.settings.BottomBarScreen
import paige.navic.ui.screens.settings.FontsScreen
import paige.navic.ui.screens.settings.SettingsAboutScreen
import paige.navic.ui.screens.settings.SettingsAcknowledgementsScreen
import paige.navic.ui.screens.settings.SettingsAppearanceScreen
import paige.navic.ui.screens.settings.SettingsCustomHeadersScreen
import paige.navic.ui.screens.settings.SettingsDataStorageScreen
import paige.navic.ui.screens.settings.SettingsDeveloperScreen
import paige.navic.ui.screens.settings.SettingsNowPlayingScreen
import paige.navic.ui.screens.settings.SettingsPlaybackScreen
import paige.navic.ui.screens.settings.SettingsScreen
import paige.navic.ui.screens.settings.SettingsStreamingQualityScreen
import paige.navic.ui.screens.share.ShareListScreen
import paige.navic.ui.screens.song.SongDetailScreen
import paige.navic.ui.screens.song.SongListScreen
import paige.navic.ui.theme.NavicTheme
import paige.navic.utils.BottomBarScrollManager
import paige.navic.utils.LocalBottomBarScrollManager
import paige.navic.utils.Material3Transitions

@OptIn(ExperimentalSerializationApi::class)
private val config = SavedStateConfiguration {
	serializersModule = SerializersModule {
		polymorphic(NavKey::class) {
			subclassesOfSealed<Screen>()
		}
	}
}

val LocalCtx = staticCompositionLocalOf<Ctx> { error("no ctx") }
val LocalNavStack = staticCompositionLocalOf<NavBackStack<NavKey>> { error("no backstack") }
val LocalImageBuilder = staticCompositionLocalOf<ImageRequest.Builder> { error("no image builder") }
val LocalSnackbarState = staticCompositionLocalOf<SnackbarHostState> { error("no snackbar state") }
val LocalSharedTransitionScope =
	staticCompositionLocalOf<SharedTransitionScope> { error("no shared transition scope") }

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun App() {
	val platformContext = LocalPlatformContext.current

	setSingletonImageLoaderFactory { platformContext ->
		initializeSingletonImageLoader(platformContext)
	}

	val ctx = rememberCtx()
	val backStack = rememberNavBackStack(
		config, if (SessionManager.currentUser != null) {
			Screen.Library()
		} else {
			Screen.Login
		}
	)
	val imageBuilder = remember { ImageRequest.Builder(platformContext).crossfade(true) }
	val snackbarState = remember { SnackbarHostState() }
	val density = LocalDensity.current
	val scrollManager = remember {
		BottomBarScrollManager(with(density) { 50.dp.toPx() })
	}

	SharedTransitionLayout {
		CompositionLocalProvider(
			LocalCtx provides ctx,
			LocalNavStack provides backStack,
			LocalImageBuilder provides imageBuilder,
			LocalSnackbarState provides snackbarState,
			LocalSharedTransitionScope provides this@SharedTransitionLayout,
			LocalBottomBarScrollManager provides scrollManager
		) {
			NavicTheme {
				Scaffold(
					modifier = Modifier.nestedScroll(scrollManager.connection),
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
							remember { NowPlayingSceneStrategy() },
							remember { BottomSheetSceneStrategy() },
							rememberListDetailSceneStrategy()
						),
						onBack = {
							if (backStack.size > 1) {
								backStack.removeLastOrNull()
							}
						},
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
				if (!Settings.shared.showedSideloadingWarning
					&& ctx.name.lowercase().contains("android")
				) {
					SideloadingDialog()
				}
				if (Settings.shared.checkForUpdates) {
					ChangelogSheet()
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
		entry<Screen.AlbumList>(metadata = navtabMetadata) { key ->
			AlbumListScreen(key.nested, key.listType)
		}
		entry<Screen.PlaylistList>(metadata = navtabMetadata) { key ->
			PlaylistListScreen(key.nested)
		}
		entry<Screen.ArtistList>(metadata = navtabMetadata) { key ->
			ArtistListScreen(key.nested)
		}
		entry<Screen.GenreList>(metadata = navtabMetadata) { key ->
			GenreListScreen(key.nested)
		}
		entry<Screen.SongList>(metadata = navtabMetadata) { key ->
			SongListScreen(key.nested, key.artistId, key.artistName)
		}

		entry<Screen.RadioList>(metadata = navtabMetadata) { key ->
			RadioListScreen(key.nested)
		}

		// misc
		entry<Screen.Login> {
			LoginScreen()
		}
		entry<Screen.NowPlaying>(
			metadata = NowPlayingSceneStrategy.bottomSheet(
				maxWidth = Dp.Unspecified,
				screenType = "player"
			)
		) {
			NowPlayingScreen()
		}
		entry<Screen.Lyrics>(metadata = NowPlayingSceneStrategy.bottomSheet(isTransparent = true)) {
			val player = koinViewModel<MediaPlayerViewModel>()
			val playerState by player.uiState.collectAsState()
			val song = playerState.currentSong
			LyricsScreen(song)
		}
		entry<Screen.Queue>(metadata = BottomSheetSceneStrategy.bottomSheet()) {
			QueueScreen()
		}
		entry<Screen.CollectionDetail>(metadata = detailPane("root")) { key ->
			CollectionDetailScreen(key.collectionId, key.tab)
		}
		entry<Screen.SongDetail>(metadata = detailPane("root")) { key ->
			SongDetailScreen(key.songId)
		}
		entry<Screen.Search>(metadata = navtabMetadata) { key ->
			SearchScreen(key.nested)
		}
		entry<Screen.ShareList> {
			ShareListScreen()
		}
		entry<Screen.ArtistDetail> { key ->
			ArtistDetailScreen(key.artist)
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
		entry<Screen.Settings.DataStorage>(metadata = detailPane("settings")) {
			SettingsDataStorageScreen()
		}
		entry<Screen.Settings.Fonts> {
			FontsScreen()
		}
		entry<Screen.Settings.CustomHeaders> {
			SettingsCustomHeadersScreen()
		}
		entry<Screen.Settings.StreamingQuality> {
			SettingsStreamingQualityScreen()
		}
	}
}

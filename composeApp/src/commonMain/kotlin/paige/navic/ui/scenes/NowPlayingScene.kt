package paige.navic.ui.scenes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import com.kmpalette.loader.rememberNetworkLoader
import com.kmpalette.rememberDominantColorState
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.http.Url
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.data.session.SessionManager
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.ui.components.sheets.ModalBottomSheet
import paige.navic.ui.scenes.NowPlayingSceneStrategy.Companion.bottomSheet
import paige.navic.ui.theme.NavicTheme

/** An [OverlayScene] that renders an [entry] within a [ModalBottomSheet]. */
@OptIn(ExperimentalMaterial3Api::class)
internal class NowPlayingScene<T : Any>(
	override val key: T,
	override val previousEntries: List<NavEntry<T>>,
	override val overlaidEntries: List<NavEntry<T>>,
	private val entry: NavEntry<T>,
	private val modalBottomSheetProperties: ModalBottomSheetProperties,
	private val sheetMaxWidth: Dp,
	private val onBack: () -> Unit,
	private val screenType: String?,
	private val isTransparent: Boolean
) : OverlayScene<T> {

	override val entries: List<NavEntry<T>> = listOf(entry)

	override val content: @Composable (() -> Unit) = {
		NavicTheme(colorSchemeForCurrentSong()) {
			val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

			ModalBottomSheet(
				containerColor = if (isTransparent) {
					Color.Transparent
				} else {
					MaterialTheme.colorScheme.surface
				},
				onDismissRequest = onBack,
				properties = modalBottomSheetProperties,
				sheetState = sheetState,
				sheetMaxWidth = sheetMaxWidth,
				contentWindowInsets = { WindowInsets() },
				dragHandle = null,
				shape = if (sheetState.targetValue == SheetValue.Expanded)
					RectangleShape
				else BottomSheetDefaults.ExpandedShape
			) {
				Box(Modifier.fillMaxSize()) {
					entry.Content()
				}
			}
		}
	}
}

/**
 * A [SceneStrategy] that displays entries that have added [bottomSheet] to their [NavEntry.metadata]
 * within a [ModalBottomSheet] instance.
 *
 * This strategy should always be added before any non-overlay scene strategies.
 */
@OptIn(ExperimentalMaterial3Api::class)
class NowPlayingSceneStrategy<T : Any> : SceneStrategy<T> {

	override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
		val lastEntry = entries.lastOrNull()
		val bottomSheetProperties =
			lastEntry?.metadata?.get(PROPERTIES_KEY) as? ModalBottomSheetProperties
		val sheetMaxWidth = lastEntry?.metadata?.get(MAX_WIDTH_KEY) as? Dp
		val screenType = lastEntry?.metadata?.get(SCREEN_TYPE_KEY) as? String
		val isTransparent = lastEntry?.metadata?.get(IS_TRANSPARENT_KEY) as? Boolean ?: false
		return bottomSheetProperties?.let { properties ->
			@Suppress("UNCHECKED_CAST")
			NowPlayingScene(
				key = lastEntry.contentKey as T,
				previousEntries = entries.dropLast(1),
				overlaidEntries = entries.dropLast(1),
				entry = lastEntry,
				modalBottomSheetProperties = properties,
				sheetMaxWidth = sheetMaxWidth ?: BottomSheetDefaults.SheetMaxWidth,
				onBack = onBack,
				screenType = screenType,
				isTransparent = isTransparent
			)
		}
	}

	companion object {
		/**
		 * Function to be called on the [NavEntry.metadata] to mark this entry as something that
		 * should be displayed within a [ModalBottomSheet].
		 *
		 * @param modalBottomSheetProperties properties that should be passed to the containing
		 * [ModalBottomSheet].
		 */
		@OptIn(ExperimentalMaterial3Api::class)
		fun bottomSheet(
			modalBottomSheetProperties: ModalBottomSheetProperties = ModalBottomSheetProperties(),
			maxWidth: Dp = BottomSheetDefaults.SheetMaxWidth,
			screenType: String = "",
			isTransparent: Boolean = false
		): Map<String, Any> = mapOf(
			PROPERTIES_KEY to modalBottomSheetProperties,
			MAX_WIDTH_KEY to maxWidth,
			SCREEN_TYPE_KEY to screenType,
			IS_TRANSPARENT_KEY to isTransparent
		)

		internal const val PROPERTIES_KEY = "properties"
		internal const val MAX_WIDTH_KEY = "max_width"
		internal const val SCREEN_TYPE_KEY = "screen_type"
		internal const val IS_TRANSPARENT_KEY = "is_transparent"
	}
}

@Composable
private fun colorSchemeForCurrentSong(): ColorScheme {
	val player = koinViewModel<MediaPlayerViewModel>()
	val playerState by player.uiState.collectAsState()
	val song = playerState.currentSong
	val coverUri = remember(song?.coverArtId) {
		song?.coverArtId?.let { SessionManager.api.getCoverArtUrl(it) }
	}
	val networkLoader = rememberNetworkLoader(HttpClient().config {
		install(HttpTimeout) {
			requestTimeoutMillis = 60_000
			connectTimeoutMillis = 60_000
			socketTimeoutMillis = 60_000
		}
	})
	val dominantColorState = rememberDominantColorState(loader = networkLoader)
	val scheme = rememberDynamicColorScheme(
		seedColor = dominantColorState.color,
		isDark = true,
		style = if (coverUri != null) PaletteStyle.Content else PaletteStyle.Monochrome,
		specVersion = ColorSpec.SpecVersion.SPEC_2021,
	)

	LaunchedEffect(coverUri) {
		coverUri?.let {
			dominantColorState.updateFrom(Url("$it&size=128"))
		}
	}

	return scheme
}

package paige.navic.data.models

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import kotlinx.coroutines.launch
import paige.navic.LocalMediaPlayer
import paige.navic.data.models.BottomSheetSceneStrategy.Companion.bottomSheet
import paige.navic.data.session.SessionManager
import paige.navic.ui.theme.NavicTheme

/** An [OverlayScene] that renders an [entry] within a [ModalBottomSheet]. */
@OptIn(ExperimentalMaterial3Api::class)
internal class BottomSheetScene<T : Any>(
	override val key: T,
	override val previousEntries: List<NavEntry<T>>,
	override val overlaidEntries: List<NavEntry<T>>,
	private val entry: NavEntry<T>,
	private val modalBottomSheetProperties: ModalBottomSheetProperties,
	private val sheetMaxWidth: Dp,
	private val onBack: () -> Unit,
) : OverlayScene<T> {

	override val entries: List<NavEntry<T>> = listOf(entry)

	override val content: @Composable (() -> Unit) = {
		NavicTheme(colorSchemeForTrack()) {
			val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
			val scope = rememberCoroutineScope()
			ModalBottomSheet(
				onDismissRequest = onBack,
				properties = modalBottomSheetProperties,
				sheetState = sheetState,
				sheetMaxWidth = sheetMaxWidth,
				contentWindowInsets = { WindowInsets() },
				dragHandle = null
			) {
				Box(Modifier.fillMaxSize()) {
					entry.Content()

					BottomSheetDefaults.DragHandle(
						modifier = Modifier
							.align(Alignment.TopCenter)
							.padding(
								top = WindowInsets.statusBars
									.asPaddingValues().calculateTopPadding()
							)
							.clickable {
								scope
									.launch { sheetState.hide() }
									.invokeOnCompletion {
										if (!sheetState.isVisible) {
											onBack()
										}
									}
							}
					)
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
class BottomSheetSceneStrategy<T : Any> : SceneStrategy<T> {

	override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
		val lastEntry = entries.lastOrNull()
		val bottomSheetProperties = lastEntry?.metadata?.get(PROPERTIES_KEY) as? ModalBottomSheetProperties
		val sheetMaxWidth = lastEntry?.metadata?.get(MAX_WIDTH_KEY) as? Dp
		return bottomSheetProperties?.let { properties ->
			@Suppress("UNCHECKED_CAST")
			BottomSheetScene(
				key = lastEntry.contentKey as T,
				previousEntries = entries.dropLast(1),
				overlaidEntries = entries.dropLast(1),
				entry = lastEntry,
				modalBottomSheetProperties = properties,
				sheetMaxWidth = sheetMaxWidth ?: BottomSheetDefaults.SheetMaxWidth,
				onBack = onBack
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
			maxWidth: Dp = BottomSheetDefaults.SheetMaxWidth
		): Map<String, Any> = mapOf(
			PROPERTIES_KEY to modalBottomSheetProperties,
			MAX_WIDTH_KEY to maxWidth
		)

		internal const val PROPERTIES_KEY = "properties"
		internal const val MAX_WIDTH_KEY = "max_width"
	}
}

@Composable
private fun colorSchemeForTrack(): ColorScheme? {
	val player = LocalMediaPlayer.current
	val playerState by player.uiState.collectAsState()
	val track = playerState.currentTrack
	val coverUri = remember(track?.coverArt) {
		SessionManager.api.getCoverArtUrl(
			track?.coverArt,
			auth = true
		)
	}
	val networkLoader = rememberNetworkLoader(HttpClient().config {
		install(HttpTimeout) {
			requestTimeoutMillis = 60_000
			connectTimeoutMillis = 60_000
			socketTimeoutMillis = 60_000
		}
	})
	val dominantColorState = rememberDominantColorState(loader = networkLoader)
	val scheme = if (coverUri != null) rememberDynamicColorScheme(
		seedColor = dominantColorState.color,
		// TODO: this is just forced because light mode has unreadable contrast
		isDark = true,
		style = PaletteStyle.Content,
		specVersion = ColorSpec.SpecVersion.SPEC_2021,
	) else null

	LaunchedEffect(coverUri) {
		coverUri?.let {
			dominantColorState.updateFrom(Url("$it&size=128"))
		}
	}

	return scheme
}

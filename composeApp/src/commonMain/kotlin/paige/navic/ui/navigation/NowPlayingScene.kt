package paige.navic.ui.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.get
import androidx.navigation3.runtime.metadata
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import androidx.navigation3.ui.NavDisplay
import org.koin.compose.koinInject
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.ui.components.sheets.ModalBottomSheet
import paige.navic.ui.theme.NavicTheme
import paige.navic.util.color.rememberCoverColorScheme

@OptIn(ExperimentalMaterial3Api::class)
internal class NowPlayingScene<T : Any>(
	override val key: T,
	override val previousEntries: List<NavEntry<T>>,
	override val overlaidEntries: List<NavEntry<T>>,
	private val entry: NavEntry<T>,
	private val modalBottomSheetProperties: ModalBottomSheetProperties,
	private val sheetMaxWidth: Dp,
	private val onBack: () -> Unit,
	private val isTransparent: Boolean
) : OverlayScene<T> {

	override val entries: List<NavEntry<T>> = listOf(entry)

	override val content: @Composable (() -> Unit) = {
		// Use a unique key to preserve state without clashing with Nav3 internal state keys
		key("overlay_content_${entry.contentKey}") {
			val player = koinInject<MediaPlayerViewModel>()
			val playerState by player.uiState.collectAsState()
			val song = playerState.currentSong
			val colorScheme = rememberCoverColorScheme(song?.coverArtId)

			NavicTheme(colorScheme) {
				val sheetState = rememberBottomSheetState(
					initialValue = SheetValue.Hidden,
					enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
				)

				ModalBottomSheet(
					containerColor = if (isTransparent) {
						Color.Transparent
					} else {
						MaterialTheme.colorScheme.surface
					},
					scrimColor = if (isTransparent) {
						Color.Transparent
					} else {
						BottomSheetDefaults.ScrimColor
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

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is NowPlayingScene<*>) return false
		if (key != other.key) return false
		if (isTransparent != other.isTransparent) return false
		if (sheetMaxWidth != other.sheetMaxWidth) return false
		
		if (entries.size != other.entries.size) return false
		for (i in entries.indices) {
			if (entries[i].contentKey != other.entries[i].contentKey) return false
		}
		
		return true
	}

	override fun hashCode(): Int {
		var result = key.hashCode()
		result = 31 * result + entries.map { it.contentKey }.hashCode()
		result = 31 * result + isTransparent.hashCode()
		return result
	}
}

@OptIn(ExperimentalMaterial3Api::class)
class NowPlayingSceneStrategy<T : Any> : SceneStrategy<T> {

	override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
		val lastEntry = entries.lastOrNull() ?: return null
		val properties = lastEntry.metadata[PropertiesKey] ?: return null
		val sheetMaxWidth = lastEntry.metadata[MaxWidthKey] ?: BottomSheetDefaults.SheetMaxWidth
		val isTransparent = lastEntry.metadata[IsTransparentKey] ?: false

		@Suppress("UNCHECKED_CAST")
		return NowPlayingScene(
			key = lastEntry.contentKey as T,
			previousEntries = entries.dropLast(1),
			overlaidEntries = entries.dropLast(1),
			entry = lastEntry,
			modalBottomSheetProperties = properties,
			sheetMaxWidth = sheetMaxWidth,
			onBack = onBack,
			isTransparent = isTransparent
		)
	}

	companion object {
		@OptIn(ExperimentalMaterial3Api::class)
		fun bottomSheet(
			modalBottomSheetProperties: ModalBottomSheetProperties = ModalBottomSheetProperties(),
			maxWidth: Dp = BottomSheetDefaults.SheetMaxWidth,
			isTransparent: Boolean = false
		) = metadata {
			put(PropertiesKey, modalBottomSheetProperties)
			put(MaxWidthKey, maxWidth)
			put(IsTransparentKey, isTransparent)
			// Disable standard nav transitions to let the sheet handle its own animation
			put(NavDisplay.TransitionKey) { ContentTransform(EnterTransition.None, ExitTransition.None) }
			put(NavDisplay.PopTransitionKey) { ContentTransform(EnterTransition.None, ExitTransition.None) }
		}

		object PropertiesKey : NavMetadataKey<ModalBottomSheetProperties>
		object MaxWidthKey : NavMetadataKey<Dp>
		object IsTransparentKey : NavMetadataKey<Boolean>
	}
}

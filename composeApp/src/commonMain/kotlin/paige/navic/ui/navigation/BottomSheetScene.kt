package paige.navic.ui.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.rememberLifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.get
import androidx.navigation3.runtime.metadata
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import androidx.navigation3.ui.NavDisplay
import com.kyant.capsule.ContinuousCapsule
import org.koin.compose.koinInject
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.ui.components.sheets.ModalBottomSheet
import paige.navic.ui.theme.NavicTheme
import paige.navic.util.color.rememberCoverColorScheme

@OptIn(ExperimentalMaterial3Api::class)
internal class BottomSheetScene<T : Any>(
	override val key: T,
	override val previousEntries: List<NavEntry<T>>,
	override val overlaidEntries: List<NavEntry<T>>,
	private val entry: NavEntry<T>,
	private val modalBottomSheetProperties: ModalBottomSheetProperties,
	private val onBack: () -> Unit,
) : OverlayScene<T> {

	override val entries: List<NavEntry<T>> = listOf(entry)

	override val content: @Composable (() -> Unit) = {
		key("overlay_bottom_${entry.contentKey}") {
			val lifecycleOwner = rememberLifecycleOwner()
			val player = koinInject<MediaPlayerViewModel>()
			val playerState by player.uiState.collectAsState()
			val song = playerState.currentSong
			val colorScheme = rememberCoverColorScheme(song?.coverArtId)

			NavicTheme(colorScheme) {
				ModalBottomSheet(
					onDismissRequest = onBack,
					properties = modalBottomSheetProperties,
					dragHandle = {
						Surface(
							modifier = Modifier.padding(vertical = 5.dp),
							color = MaterialTheme.colorScheme.onSurfaceVariant,
							shape = ContinuousCapsule,
						) {
							Box(Modifier.size(width = 32.dp, height = 4.dp))
						}
					}
				) {
					CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
						entry.Content()
					}
				}
			}
		}
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is BottomSheetScene<*>) return false
		if (key != other.key) return false
		
		if (entries.size != other.entries.size) return false
		for (i in entries.indices) {
			if (entries[i].contentKey != other.entries[i].contentKey) return false
		}
		
		return true
	}

	override fun hashCode(): Int {
		var result = key.hashCode()
		result = 31 * result + entries.map { it.contentKey }.hashCode()
		return result
	}
}

@OptIn(ExperimentalMaterial3Api::class)
class BottomSheetSceneStrategy<T : Any> : SceneStrategy<T> {

	override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
		val lastEntry = entries.lastOrNull() ?: return null
		val bottomSheetProperties = lastEntry.metadata[BottomSheetKey] ?: return null

		@Suppress("UNCHECKED_CAST")
		return BottomSheetScene(
			key = lastEntry.contentKey as T,
			previousEntries = entries.dropLast(1),
			overlaidEntries = entries.dropLast(1),
			entry = lastEntry,
			modalBottomSheetProperties = bottomSheetProperties,
			onBack = onBack
		)
	}

	companion object {
		fun bottomSheet(
			modalBottomSheetProperties: ModalBottomSheetProperties = ModalBottomSheetProperties()
		) = metadata {
			put(BottomSheetKey, modalBottomSheetProperties)
			put(NavDisplay.TransitionKey) { ContentTransform(EnterTransition.None, ExitTransition.None) }
			put(NavDisplay.PopTransitionKey) { ContentTransform(EnterTransition.None, ExitTransition.None) }
		}

		object BottomSheetKey : NavMetadataKey<ModalBottomSheetProperties>
	}
}

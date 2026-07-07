@file:OptIn(ExperimentalMaterial3Api::class)
@file:Suppress("UNCHECKED_CAST")

package paige.navic.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.rememberLifecycleOwner
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.get
import androidx.navigation3.runtime.metadata
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import com.kyant.capsule.ContinuousCapsule
import paige.navic.LocalNavStack
import paige.navic.ui.components.sheets.ModalBottomSheet
import paige.navic.ui.theme.NavicTheme
import paige.navic.util.ui.LocalSheetState
import paige.navic.util.ui.rememberColorSchemeForCurrentSong
import paige.navic.util.ui.rememberColorSchemeFromCoverArt

private object PropertiesKey : NavMetadataKey<ModalBottomSheetProperties>
private object CoverArtIdKey : NavMetadataKey<String>

class BottomSheetScene<T : Any>(
	override val key: Any,
	private val entry: NavEntry<T>,
	override val previousEntries: List<NavEntry<T>>,
	override val overlaidEntries: List<NavEntry<T>>,
	private val properties: ModalBottomSheetProperties,
	private val onBack: () -> Unit
) : OverlayScene<T> {
	override val entries = listOf(entry)

	lateinit var sheetState: SheetState


	override val content = @Composable {
		sheetState = rememberBottomSheetState(SheetValue.Hidden)
		val lifecycleOwner = rememberLifecycleOwner()
		val backStack = LocalNavStack.current
		val coverArtId = entry.metadata[CoverArtIdKey]
		val isPlayerOpen = backStack.any { it is Screen.NowPlaying }
		val colorScheme = when {
			coverArtId != null -> rememberColorSchemeFromCoverArt(coverArtId, forceDark = false)
			isPlayerOpen -> rememberColorSchemeForCurrentSong(forceDark = false)
			else -> null
		}

		NavicTheme(colorScheme) {
			val currentColorScheme = MaterialTheme.colorScheme
			ModalBottomSheet(
				onDismissRequest = onBack,
				sheetState = sheetState,
				properties = properties,
				containerColor = currentColorScheme.surface,
				contentWindowInsets = { WindowInsets.systemBars.only(WindowInsetsSides.Top) },
				dragHandle = {
					Surface(
						modifier = Modifier.padding(vertical = 5.dp),
						color = currentColorScheme.primary,
						shape = ContinuousCapsule,
					) {
						Box(Modifier.size(width = 32.dp, height = 4.dp))
					}
				}
			) {
				CompositionLocalProvider(
					LocalLifecycleOwner provides lifecycleOwner,
					LocalSheetState provides sheetState
				) {
					entry.Content()
				}
			}
		}
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other == null || this::class != other::class) return false

		other as BottomSheetScene<*>

		return key == other.key
			&& entry == other.entry
			&& properties == other.properties
	}

	override fun hashCode() = key.hashCode() * 31 +
		entry.hashCode() * 31 +
		properties.hashCode() * 31

	// onRemove is intentionally not used, see the comment
	// on LocalSheetState
}

class BottomSheetSceneStrategy<T : Any> : SceneStrategy<T> {

	override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
		val entry = entries.lastOrNull() ?: return null
		val properties = entry.metadata[PropertiesKey] ?: return null

		return BottomSheetScene(
			key = entry.contentKey as T,
			entry = entry,
			previousEntries = entries.dropLast(1),
			overlaidEntries = entries.dropLast(1),
			properties = properties,
			onBack = onBack
		)
	}

	companion object {
		fun bottomSheet(
			sheetProperties: ModalBottomSheetProperties = ModalBottomSheetProperties(),
			coverArtId: String? = null,
		) = metadata {
			put(PropertiesKey, sheetProperties)
			coverArtId?.let { put(CoverArtIdKey, it) }
		}
	}
}

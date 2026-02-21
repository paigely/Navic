package paige.navic.ui.scenes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_lyrics
import navic.composeapp.generated.resources.action_queue
import navic.composeapp.generated.resources.title_now_playing
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.LocalMediaPlayer
import paige.navic.LocalNavStack
import paige.navic.data.models.Screen
import paige.navic.data.session.SessionManager
import paige.navic.icons.Icons
import paige.navic.icons.outlined.KeyboardArrowDown
import paige.navic.icons.outlined.Lyrics
import paige.navic.icons.outlined.Queue
import paige.navic.ui.components.layouts.TopBarButton
import paige.navic.ui.scenes.BottomSheetSceneStrategy.Companion.bottomSheet
import paige.navic.ui.theme.NavicTheme
import paige.navic.ui.theme.defaultFont

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
	private val screenType: String?
) : OverlayScene<T> {

	override val entries: List<NavEntry<T>> = listOf(entry)

	override val content: @Composable (() -> Unit) = {
		NavicTheme(colorSchemeForTrack()) {
			val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
			val scope = rememberCoroutineScope()
			val ctx = LocalCtx.current
			val backStack = LocalNavStack.current
			ModalBottomSheet(
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
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.padding(
								top = WindowInsets.statusBars
									.asPaddingValues().calculateTopPadding() + 18.dp,
								start = 20.dp,
								end = 24.dp
							),
						horizontalArrangement = Arrangement.spacedBy(12.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						TopBarButton(
							onClick = {
								ctx.clickSound()
								scope
									.launch { sheetState.hide() }
									.invokeOnCompletion {
										if (!sheetState.isVisible) {
											onBack()
										}
									}
							}
						) {
							Icon(Icons.Outlined.KeyboardArrowDown, null)
						}
						if (screenType == "player") {
							Text(
								stringResource(Res.string.title_now_playing),
								fontFamily = defaultFont(round = 100f),
								style = MaterialTheme.typography.bodyMedium
									.copy(
										shadow = Shadow(
											color = MaterialTheme.colorScheme.inverseOnSurface,
											offset = Offset(0f, 4f),
											blurRadius = 10f
										)
									),
							)
							Spacer(Modifier.weight(1f))
							Row(
								modifier = Modifier
									.clip(MaterialTheme.shapes.medium),
								horizontalArrangement = Arrangement.spacedBy(4.dp)
							) {
								SheetTopButton(
									icon = Icons.Outlined.Lyrics,
									contentDescription = stringResource(Res.string.action_lyrics)
								) {
									ctx.clickSound()
									if (!backStack.contains(Screen.Lyrics)) backStack.add(Screen.Lyrics)
								}
								SheetTopButton(
									icon = Icons.Outlined.Queue,
									contentDescription = stringResource(Res.string.action_queue)
								) {
									ctx.clickSound()
									if (!backStack.contains(Screen.Queue)) backStack.add(Screen.Queue)
								}
							}
						}
					}
				}
			}
		}
	}
}

@Composable
private fun SheetTopButton(
	icon: ImageVector,
	contentDescription: String,
	onClick: () -> Unit
) {
	Surface(
		onClick = onClick,
		shape = MaterialTheme.shapes.extraSmall,
		color = MaterialTheme.colorScheme.surfaceContainer,
		contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
		modifier = Modifier.size(45.dp, 40.dp)
	) {
		Box(contentAlignment = Alignment.Center) {
			Icon(
				imageVector = icon,
				contentDescription = contentDescription,
				modifier = Modifier.size(20.dp)
			)
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
		val bottomSheetProperties =
			lastEntry?.metadata?.get(PROPERTIES_KEY) as? ModalBottomSheetProperties
		val sheetMaxWidth = lastEntry?.metadata?.get(MAX_WIDTH_KEY) as? Dp
		val screenType = lastEntry?.metadata?.get(SCREEN_TYPE_KEY) as? String
		return bottomSheetProperties?.let { properties ->
			@Suppress("UNCHECKED_CAST")
			BottomSheetScene(
				key = lastEntry.contentKey as T,
				previousEntries = entries.dropLast(1),
				overlaidEntries = entries.dropLast(1),
				entry = lastEntry,
				modalBottomSheetProperties = properties,
				sheetMaxWidth = sheetMaxWidth ?: BottomSheetDefaults.SheetMaxWidth,
				onBack = onBack,
				screenType = screenType
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
			screenType: String = ""
		): Map<String, Any> = mapOf(
			PROPERTIES_KEY to modalBottomSheetProperties,
			MAX_WIDTH_KEY to maxWidth,
			SCREEN_TYPE_KEY to screenType
		)

		internal const val PROPERTIES_KEY = "properties"
		internal const val MAX_WIDTH_KEY = "max_width"
		internal const val SCREEN_TYPE_KEY = "screen_type"
	}
}

@Composable
private fun colorSchemeForTrack(): ColorScheme {
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

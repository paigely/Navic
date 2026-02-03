package paige.navic.ui.component.layout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.minus
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kmpalette.loader.rememberNetworkLoader
import com.kmpalette.rememberDominantColorState
import com.kyant.capsule.ContinuousRoundedRectangle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.http.Url
import kotlinx.coroutines.launch
import paige.navic.LocalMediaPlayer
import paige.navic.data.model.Settings
import paige.navic.ui.theme.NavicTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
	snackbarState: SnackbarHostState,
	bottomBar: @Composable () -> Unit,
	content: @Composable () -> Unit,
) {
	val player = LocalMediaPlayer.current
	val playerState by player.uiState.collectAsStateWithLifecycle()
	val focusManager = LocalFocusManager.current
	val scope = rememberCoroutineScope()
	val scaffoldState = rememberBottomSheetScaffoldState()
	val expanded = scaffoldState.bottomSheetState.isExpanded
	val progress = scaffoldState.bottomSheetState.progress(
		BottomSheetValue.Collapsed,
		BottomSheetValue.Expanded
	)
	val networkLoader = rememberNetworkLoader(HttpClient().config {
		install(HttpTimeout) {
			requestTimeoutMillis = 60_000
			connectTimeoutMillis = 60_000
			socketTimeoutMillis = 60_000
		}
	})
	val dominantColorState = rememberDominantColorState(loader = networkLoader)
	val coverArt = playerState.tracks?.coverArt
	val musicScheme = if (coverArt != null) rememberDynamicColorScheme(
		seedColor = dominantColorState.color,
		isDark = isSystemInDarkTheme(),
		specVersion = ColorSpec.SpecVersion.SPEC_2025,
	) else null
	val appScheme = MaterialTheme.colorScheme
	val dynamicScheme = if (musicScheme != null) {
		appScheme.lerp(musicScheme, progress)
	} else {
		appScheme
	}
	val alwaysShowSeekbar = Settings.shared.alwaysShowSeekbar

	LaunchedEffect(coverArt) {
		coverArt?.let {
			dominantColorState.updateFrom(Url("$it&size=128"))
		}
	}

	Scaffold(
		snackbarHost = {
			NavicTheme {
				SnackbarHost(
					hostState = snackbarState,
					modifier = Modifier.padding(bottom = if (alwaysShowSeekbar)
						MediaBarDefaults.height
					else MediaBarDefaults.heightNoSeekbar)
				)
			}
		},
		bottomBar = {
			NavicTheme(dynamicScheme, forceColorScheme = expanded) {
				bottomBar()
			}
		}
	) { innerPadding ->
		BottomSheetScaffold(
			modifier = Modifier
				.padding(innerPadding - PaddingValues(top = innerPadding.calculateTopPadding()))
				.clickable(
					indication = null,
					interactionSource = remember { MutableInteractionSource() }
				) {
					focusManager.clearFocus()
				},
			scaffoldState = scaffoldState,
			sheetPeekHeight = if (alwaysShowSeekbar)
				MediaBarDefaults.height
			else MediaBarDefaults.heightNoSeekbar,
			sheetBackgroundColor = Color.Transparent,
			sheetShape = ContinuousRoundedRectangle(24.dp, 24.dp, 0.dp, 0.dp),
			sheetContent = {
				NavicTheme(dynamicScheme, forceColorScheme = expanded) {
					Box(
						modifier = Modifier
							.background(MaterialTheme.colorScheme.surfaceContainer)
							.fillMaxWidth()
					) {
						MediaBar(progress)
						this@BottomSheetScaffold.AnimatedVisibility(
							(progress > 0.5f),
							enter = slideInVertically { -it } + fadeIn() + expandIn(),
							exit = slideOutVertically { -it } + fadeOut() + shrinkOut(),
							modifier = Modifier
								.statusBarsPadding()
								.padding(horizontal = 10.dp)
								.align(Alignment.TopCenter)
						) {
							BottomSheetDefaults.DragHandle(
								modifier = Modifier.clickable {
									scope.launch {
										scaffoldState.bottomSheetState.collapse()
									}
								},
								color = MaterialTheme.colorScheme.onSurface
							)
						}
					}
				}
			}
		) {
			content()
		}
	}
}

fun ColorScheme.lerp(target: ColorScheme, fraction: Float): ColorScheme {
	return this.copy(
		primary = lerp(this.primary, target.primary, fraction),
		onPrimary = lerp(this.onPrimary, target.onPrimary, fraction),
		primaryContainer = lerp(this.primaryContainer, target.primaryContainer, fraction),
		onPrimaryContainer = lerp(this.onPrimaryContainer, target.onPrimaryContainer, fraction),
		inversePrimary = lerp(this.inversePrimary, target.inversePrimary, fraction),
		secondary = lerp(this.secondary, target.secondary, fraction),
		onSecondary = lerp(this.onSecondary, target.onSecondary, fraction),
		secondaryContainer = lerp(this.secondaryContainer, target.secondaryContainer, fraction),
		onSecondaryContainer = lerp(this.onSecondaryContainer, target.onSecondaryContainer, fraction),
		tertiary = lerp(this.tertiary, target.tertiary, fraction),
		onTertiary = lerp(this.onTertiary, target.onTertiary, fraction),
		tertiaryContainer = lerp(this.tertiaryContainer, target.tertiaryContainer, fraction),
		onTertiaryContainer = lerp(this.onTertiaryContainer, target.onTertiaryContainer, fraction),
		background = lerp(this.background, target.background, fraction),
		onBackground = lerp(this.onBackground, target.onBackground, fraction),
		surface = lerp(this.surface, target.surface, fraction),
		onSurface = lerp(this.onSurface, target.onSurface, fraction),
		surfaceVariant = lerp(this.surfaceVariant, target.surfaceVariant, fraction),
		onSurfaceVariant = lerp(this.onSurfaceVariant, target.onSurfaceVariant, fraction),
		outline = lerp(this.outline, target.outline, fraction),
		outlineVariant = lerp(this.outlineVariant, target.outlineVariant, fraction),
		surfaceContainer = lerp(this.surfaceContainer, target.surfaceContainer, fraction),
	)
}
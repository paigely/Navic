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
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Dp
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
	val expanded = scaffoldState.bottomSheetState.targetValue == SheetValue.Expanded
	val networkLoader = rememberNetworkLoader(HttpClient().config {
		install(HttpTimeout) {
			requestTimeoutMillis = 60_000
			connectTimeoutMillis = 60_000
			socketTimeoutMillis = 60_000
		}
	})
	val dominantColorState = rememberDominantColorState(loader = networkLoader)
	val coverArt = playerState.tracks?.coverArt
	val scheme = if (coverArt != null && expanded) rememberDynamicColorScheme(
		seedColor = dominantColorState.color,
		isDark = isSystemInDarkTheme(),
		specVersion = ColorSpec.SpecVersion.SPEC_2025,
	) else null
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
			NavicTheme(scheme, forceColorScheme = expanded) {
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
			sheetDragHandle = {},
			scaffoldState = scaffoldState,
			sheetPeekHeight = if (alwaysShowSeekbar)
				MediaBarDefaults.height
			else MediaBarDefaults.heightNoSeekbar,
			sheetMaxWidth = Dp.Unspecified,
			sheetShape = ContinuousRoundedRectangle(24.dp, 24.dp, 0.dp, 0.dp),
			sheetContent = {
				NavicTheme(scheme, forceColorScheme = expanded) {
					Box(
						modifier = Modifier
							.background(MaterialTheme.colorScheme.surfaceContainer)
							.fillMaxWidth()
					) {
						MediaBar()
						this@BottomSheetScaffold.AnimatedVisibility(
							expanded,
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
										scaffoldState.bottomSheetState.partialExpand()
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

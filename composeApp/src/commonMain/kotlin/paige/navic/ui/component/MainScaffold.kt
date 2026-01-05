package paige.navic.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kmpalette.loader.rememberNetworkLoader
import com.kmpalette.rememberDominantColorState
import com.kyant.capsule.ContinuousRoundedRectangle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.http.Url
import paige.navic.LocalMediaPlayer
import paige.navic.ui.theme.NavicTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
	snackbarState: SnackbarHostState,
	topBar: @Composable () -> Unit,
	bottomBar: @Composable () -> Unit,
	content: @Composable () -> Unit,
) {
	val player = LocalMediaPlayer.current
	val focusManager = LocalFocusManager.current
	val scaffoldState = rememberBottomSheetScaffoldState()
	val localDensity = LocalDensity.current
	var sheetHeightDp by remember { mutableStateOf(0.dp) }
	val expanded = sheetHeightDp > 350.dp
	val networkLoader = rememberNetworkLoader(HttpClient().config {
		install(HttpTimeout) {
			requestTimeoutMillis = 60_000
			connectTimeoutMillis = 60_000
			socketTimeoutMillis = 60_000
		}
	})
	val dominantColorState = rememberDominantColorState(loader = networkLoader)
	val coverArt = player.tracks?.coverArt
	val scheme = if (coverArt != null && expanded) rememberDynamicColorScheme(
		seedColor = dominantColorState.color,
		isDark = isSystemInDarkTheme(),
		specVersion = ColorSpec.SpecVersion.SPEC_2025,
	) else null

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
					modifier = Modifier.padding(bottom = 117.9.dp)
				)
			}
		},
		topBar = topBar,
		bottomBar = {
			NavicTheme(scheme) {
				bottomBar()
			}
		}
	) { innerPadding ->
		BottomSheetScaffold(
			modifier = Modifier
				.padding(innerPadding)
				.clickable(
					indication = null,
					interactionSource = remember { MutableInteractionSource() }
				) {
					focusManager.clearFocus()
				},
			sheetDragHandle = {},
			scaffoldState = scaffoldState,
			sheetPeekHeight = 117.9.dp,
			sheetMaxWidth = Dp.Unspecified,
			sheetShape = ContinuousRoundedRectangle(24.dp, 24.dp, 0.dp, 0.dp),
			sheetContent = {
				NavicTheme(scheme) {
					Box(
						modifier = Modifier
							.background(MaterialTheme.colorScheme.surfaceContainer)
							.fillMaxWidth()
							.onGloballyPositioned {
								sheetHeightDp =
									with(localDensity) { it.boundsInWindow().height.toDp() }
							}
					) {
						MediaBar(expanded)
					}
				}
			}
		) {
			content()
		}
	}
}

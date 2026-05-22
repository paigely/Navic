package paige.navic.ui.screens.nowPlaying.components.controls

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.data.models.settings.Settings
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.ui.screens.nowPlaying.components.NowPlayingArtwork

@Composable
fun NowPlayingArtworkPager(
	modifier: Modifier = Modifier,
	isLandscape: Boolean
) {
	val player = koinViewModel<MediaPlayerViewModel>()
	val playerState by player.uiState.collectAsState()

	val pagerState = rememberPagerState(
		initialPage = playerState.currentIndex.coerceAtLeast(0),
		pageCount = { playerState.queue.size }
	)

	var visible by rememberSaveable { mutableStateOf(false) }
	val scale by animateFloatAsState(if (visible) 1f else 0f)
	val offset by animateDpAsState(if (visible) 0.dp else 200.dp)
	LaunchedEffect(Unit) {
		delay(50)
		visible = true
	}

	LaunchedEffect(playerState.currentIndex) {
		if (playerState.currentIndex != -1 && playerState.currentIndex != pagerState.currentPage) {
			pagerState.animateScrollToPage(playerState.currentIndex)
		}
	}

	LaunchedEffect(pagerState) {
		snapshotFlow { pagerState.settledPage }.collect { page ->
			if (page == playerState.currentIndex) return@collect
			val wasPaused = playerState.isPaused
			player.playAt(page)
			if (wasPaused) {
				player.pause()
			}
		}
	}

	HorizontalPager(
		modifier = modifier.scale(scale).offset {
			IntOffset(x = 0, y = offset.roundToPx())
		},
		state = pagerState,
		contentPadding = PaddingValues(horizontal = if (isLandscape) 0.dp else 8.dp),
		userScrollEnabled = Settings.shared.swipeToSkip,
		overscrollEffect = null
	) { page ->
		val song = playerState.queue[page]
		Box(
			modifier = Modifier.fillMaxSize(),
			contentAlignment = Alignment.Center
		) {
			NowPlayingArtwork(
				song = song,
				isLandscape = isLandscape
			)
		}
	}
}

package paige.navic.ui.screens.nowPlaying.components.controls

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import paige.navic.LocalMediaPlayer
import paige.navic.data.models.settings.Settings
import paige.navic.ui.screens.nowPlaying.components.NowPlayingArtwork

@Composable
fun NowPlayingArtworkPager(
	modifier: Modifier = Modifier,
	isLandscape: Boolean
) {
	val player = LocalMediaPlayer.current
	val playerState by player.uiState.collectAsState()

	val pagerState = rememberPagerState(
		initialPage = playerState.currentIndex.coerceAtLeast(0),
		pageCount = { playerState.queue.size }
	)

	LaunchedEffect(playerState.currentIndex) {
		if (playerState.currentIndex != -1 && playerState.currentIndex != pagerState.currentPage) {
			pagerState.animateScrollToPage(playerState.currentIndex)
		}
	}

	LaunchedEffect(pagerState) {
		snapshotFlow { pagerState.currentPage }.collect { page ->
			if (!pagerState.isScrollInProgress) return@collect
			if (page == playerState.currentIndex) return@collect
			val wasPaused = playerState.isPaused
			player.playAt(page)
			if (wasPaused) {
				player.pause()
			}
		}
	}

	HorizontalPager(
		modifier = modifier,
		state = pagerState,
		contentPadding = PaddingValues(horizontal = if (isLandscape) 0.dp else 8.dp),
		userScrollEnabled = Settings.shared.swipeToSkip,
		overscrollEffect = null
	) { page ->
		val track = playerState.queue[page]
		Box(
			modifier = Modifier.fillMaxSize(),
			contentAlignment = Alignment.Center
		) {
			NowPlayingArtwork(
				track = track,
				isLandscape = isLandscape
			)
		}
	}
}
package paige.navic.ui.screens.nowPlaying.components.rows

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.unit.dp
import paige.navic.LocalMediaPlayer
import paige.navic.utils.toHoursMinutesSeconds
import kotlin.time.Duration.Companion.seconds

@Composable
fun NowPlayingDurationsRow() {
	val player = LocalMediaPlayer.current
	val playerState by player.uiState.collectAsState()
	val duration = playerState.currentTrack?.duration
	val style = MaterialTheme.typography.bodyMedium
		.copy(
			shadow = Shadow(
				color = MaterialTheme.colorScheme.inverseOnSurface,
				offset = Offset(0f, 4f),
				blurRadius = 10f
			)
		)
	val color = MaterialTheme.colorScheme.onSurfaceVariant
	Row(Modifier.padding(horizontal = 16.dp)) {
		if (duration != null) {
			Text(
				text = ((duration.inWholeSeconds * playerState.progress).toDouble().seconds).toHoursMinutesSeconds(),
				color = color, style = style
			)
		} else {
			Text("--:--", color = color, style = style)
		}
		Spacer(Modifier.weight(1f))
		if (duration != null) {
			Text(duration.toHoursMinutesSeconds(), color = color, style = style)
		} else {
			Text("--:--", color = color, style = style)
		}
	}
}
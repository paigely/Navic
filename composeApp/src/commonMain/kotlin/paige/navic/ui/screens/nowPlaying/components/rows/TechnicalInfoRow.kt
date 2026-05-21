package paige.navic.ui.screens.nowPlaying.components.rows

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.shared.MediaPlayerViewModel

@Composable
fun NowPlayingTechnicalInfoRow() {
	val player = koinViewModel<MediaPlayerViewModel>()
	val playerState by player.uiState.collectAsState()
	val song = playerState.currentSong

	val style = MaterialTheme.typography.bodySmall
	val color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp),
		horizontalArrangement = Arrangement.Center
	) {
		Box(contentAlignment = Alignment.Center) {

			Box(
				modifier = Modifier
					.matchParentSize()
					.clip(CircleShape)
					.blur(8.dp)
					.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
			)

			Row(
				modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				val sampleRateFormatted = song?.sampleRate?.let {
					if (it >= 1000) "${it / 1000.0} kHz" else "$it Hz"
				} ?: "-- kHz"

				val bitrateFormatted = song?.bitRate?.let { "$it kbps" } ?: "-- kbps"

				val format = song?.fileExtension?.uppercase() ?: "--"

				Text(
					text = "$format • $sampleRateFormatted • $bitrateFormatted",
					style = style,
					color = color
				)
			}
		}
	}
}

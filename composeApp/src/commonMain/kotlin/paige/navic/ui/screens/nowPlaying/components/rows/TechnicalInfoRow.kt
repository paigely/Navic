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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.koinInject
import paige.navic.data.models.settings.Settings
import paige.navic.managers.ConnectivityManager
import paige.navic.shared.MediaPlayerViewModel

@Composable
fun NowPlayingTechnicalInfoRow() {
	val connectivityManager = koinInject<ConnectivityManager>()
	val player = koinInject<MediaPlayerViewModel>()
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
				val sampleRateFormatted = (playerState.playbackSampleRate ?: song?.sampleRate)?.let {
					if (it >= 1000) "${it / 1000.0} kHz" else "$it Hz"
				} ?: "-- kHz"

				val isCellular by connectivityManager.isCellular.collectAsStateWithLifecycle()
				val requestedBitrate = if (Settings.shared.isAdvancedTranscodingActive) {
					if (isCellular) Settings.shared.customMaxBitrateCellular else Settings.shared.customMaxBitrateWifi
				} else {
					if (isCellular) Settings.shared.streamingQualityCellular.bitrateAndroid else Settings.shared.streamingQualityWifi.bitrateAndroid
				}

				val bitrateFormatted = playerState.playbackBitrate?.let { "${it / 1000} kbps" }
					?: if (playerState.playbackMimeType?.contains("opus") == true) {
						"${if (requestedBitrate > 0) requestedBitrate else "--"} kbps"
					} else {
						song?.bitRate?.let { "$it kbps" }
					} ?: "-- kbps"

				val format = playerState.playbackMimeType?.split("/")?.lastOrNull()?.replace("mpeg", "mp3")?.uppercase()
					?: song?.fileExtension?.uppercase()
					?: "--"

				Text(
					text = "$format • $sampleRateFormatted • $bitrateFormatted",
					style = style,
					color = color
				)
			}
		}
	}
}

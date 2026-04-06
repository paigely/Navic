package paige.navic.ui.screens.track

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_album_replay_gain
import navic.composeapp.generated.resources.info_track_album
import navic.composeapp.generated.resources.info_track_artist
import navic.composeapp.generated.resources.info_track_bit_depth
import navic.composeapp.generated.resources.info_track_bitrate
import navic.composeapp.generated.resources.info_track_channel_count
import navic.composeapp.generated.resources.info_track_disc_number
import navic.composeapp.generated.resources.info_track_duration
import navic.composeapp.generated.resources.info_track_file_size
import navic.composeapp.generated.resources.info_track_format
import navic.composeapp.generated.resources.info_track_genre
import navic.composeapp.generated.resources.info_track_name
import navic.composeapp.generated.resources.info_track_number
import navic.composeapp.generated.resources.info_track_path
import navic.composeapp.generated.resources.info_track_replay_gain
import navic.composeapp.generated.resources.info_track_replay_gain_effective
import navic.composeapp.generated.resources.info_track_sampling_rate
import navic.composeapp.generated.resources.info_track_year
import navic.composeapp.generated.resources.info_unknown
import org.jetbrains.compose.resources.stringResource
import paige.navic.domain.models.DomainSong
import paige.navic.ui.components.common.Form
import paige.navic.ui.components.common.FormRow
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.utils.effectiveGain
import paige.navic.utils.fadeFromTop
import paige.navic.utils.toFileSize
import paige.navic.utils.toHoursMinutesSeconds

@Composable
fun TrackDetailScreen(track: DomainSong) {
	Scaffold(
		topBar = { NestedTopBar({ Text(track.title) }) },
		contentWindowInsets = WindowInsets(0, 0 , 0 ,0)

	) { contentPadding ->
		Column(
			Modifier
				.verticalScroll(rememberScrollState())
				.padding(top = contentPadding.calculateTopPadding() + 12.dp, start = 12.dp, end = 12.dp)
				.fadeFromTop()
		) {
			Form {
				mapOf(
					Res.string.info_track_name to track.title,
					Res.string.info_track_artist to track.artistName,
					Res.string.info_track_album to track.albumTitle,

					Res.string.info_track_number to track.trackNumber,
					Res.string.info_track_disc_number to track.discNumber,
					Res.string.info_track_year to track.year,
					Res.string.info_track_genre to track.genre,

					Res.string.info_track_duration to track.duration.toHoursMinutesSeconds(),
					Res.string.info_track_format to track.mimeType,
					Res.string.info_track_bitrate to "${track.bitRate} kbps",
					Res.string.info_track_bit_depth to track.bitDepth,
					Res.string.info_track_sampling_rate to "${track.sampleRate} Hz",
					Res.string.info_track_channel_count to track.audioChannelCount,

					Res.string.info_track_file_size to track.fileSize.toFileSize(),
					Res.string.info_track_path to track.filePath,

					Res.string.info_track_replay_gain to track.replayGain?.trackGain?.let { "$it dB" },
					Res.string.info_album_replay_gain to track.replayGain?.albumGain?.let { "$it dB" },
					Res.string.info_track_replay_gain_effective to track.replayGain?.effectiveGain()
				).forEach { (key, value) ->
					FormRow {
						Column(Modifier.padding(vertical = 4.dp)) {
							Text(
								text = stringResource(key),
								style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
								color = androidx.compose.material3.MaterialTheme.colorScheme.primary
							)
							SelectionContainer {
								Text(
									text = "${value ?: stringResource(Res.string.info_unknown)}",
									style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
								)
							}
						}
					}
				}
			}
		}
	}
}

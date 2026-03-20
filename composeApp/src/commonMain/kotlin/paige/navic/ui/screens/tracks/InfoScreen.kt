package paige.navic.ui.screens.tracks

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.zt64.subsonic.api.model.Song
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_track_artist
import navic.composeapp.generated.resources.info_track_artist_id
import navic.composeapp.generated.resources.info_track_bit_depth
import navic.composeapp.generated.resources.info_track_bitrate
import navic.composeapp.generated.resources.info_track_channel_count
import navic.composeapp.generated.resources.info_track_disc_number
import navic.composeapp.generated.resources.info_track_duration
import navic.composeapp.generated.resources.info_track_file_size
import navic.composeapp.generated.resources.info_track_format
import navic.composeapp.generated.resources.info_track_genre
import navic.composeapp.generated.resources.info_track_id
import navic.composeapp.generated.resources.info_track_name
import navic.composeapp.generated.resources.info_track_path
import navic.composeapp.generated.resources.info_track_replay_gain
import navic.composeapp.generated.resources.info_track_replay_gain_effective
import navic.composeapp.generated.resources.info_track_sampling_rate
import navic.composeapp.generated.resources.info_unknown
import org.jetbrains.compose.resources.stringResource
import paige.navic.ui.components.common.Form
import paige.navic.ui.components.common.FormRow
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.utils.effectiveGain
import paige.navic.utils.fadeFromTop
import paige.navic.utils.toFileSize
import paige.navic.utils.toHoursMinutesSeconds

@Composable
fun TrackInfoScreen(track: Song) {
	Scaffold(
		topBar = { NestedTopBar({ Text(track.title) }) }
	) { contentPadding ->
		Column(
			Modifier
				.padding(contentPadding)
				.verticalScroll(rememberScrollState())
				.padding(top = 12.dp, end = 12.dp, start = 12.dp)
				.fadeFromTop()
		) {
			Form {
				mapOf(
					Res.string.info_track_name to track.title,
					Res.string.info_track_artist to track.artistId,
					Res.string.info_track_artist_id to track.artistId,
					Res.string.info_track_bitrate to track.bitRate,
					Res.string.info_track_bit_depth to track.bitDepth,
					Res.string.info_track_file_size to track.fileSize.toFileSize(),
					Res.string.info_track_format to track.mimeType,
					Res.string.info_track_sampling_rate to track.sampleRate,
					Res.string.info_track_channel_count to track.audioChannelCount,
					Res.string.info_track_disc_number to track.discNumber,
					Res.string.info_track_genre to track.genre,
					Res.string.info_track_duration to track.duration.toHoursMinutesSeconds(),
					Res.string.info_track_id to track.id,
					Res.string.info_track_path to track.filePath,
					Res.string.info_track_replay_gain to track.replayGain?.toString(),
					Res.string.info_track_replay_gain_effective to track.replayGain?.effectiveGain()
				).forEach { (key, value) ->
					FormRow {
						Text(stringResource(key))
						SelectionContainer {
							Text("${value ?: stringResource(Res.string.info_unknown)}")
						}
					}
				}
			}
		}
	}
}

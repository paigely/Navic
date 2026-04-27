package paige.navic.ui.screens.song

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentMapOf
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
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import paige.navic.ui.components.common.Form
import paige.navic.ui.components.common.FormRow
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.screens.song.viewmodels.SongDetailViewModel
import paige.navic.utils.effectiveGain
import paige.navic.utils.toFileSize
import paige.navic.utils.toHoursMinutesSeconds

@Composable
fun SongDetailScreen(songId: String) {
	val viewModel = koinViewModel<SongDetailViewModel>(
		key = songId,
		parameters = { parametersOf(songId) }
	)

	val songState by viewModel.songState.collectAsStateWithLifecycle()
	val song = songState.data

	val info = remember(song) {
		song?.let {
			persistentMapOf(
				Res.string.info_track_name to song.title,
				Res.string.info_track_artist to song.artistName,
				Res.string.info_track_album to song.albumTitle,

				Res.string.info_track_number to song.trackNumber,
				Res.string.info_track_disc_number to song.discNumber,
				Res.string.info_track_year to song.year,
				Res.string.info_track_genre to song.genre,

				Res.string.info_track_duration to song.duration.toHoursMinutesSeconds(),
				Res.string.info_track_format to song.mimeType,
				Res.string.info_track_bitrate to song.bitRate?.let { "$it kbps" },
				Res.string.info_track_bit_depth to song.bitDepth,
				Res.string.info_track_sampling_rate to song.sampleRate?.let { "$it Hz" },
				Res.string.info_track_channel_count to song.audioChannelCount,

				Res.string.info_track_file_size to song.fileSize.toFileSize(),
				Res.string.info_track_path to song.filePath,

				Res.string.info_track_replay_gain to song.replayGain?.trackGain?.let { "$it dB" },
				Res.string.info_album_replay_gain to song.replayGain?.albumGain?.let { "$it dB" },
				Res.string.info_track_replay_gain_effective to song.replayGain?.effectiveGain()
			)
		}.orEmpty()
	}

	Scaffold(
		topBar = { NestedTopBar({ Text(song?.title.orEmpty()) }) }
	) { contentPadding ->
		Column(
			Modifier
				.verticalScroll(rememberScrollState())
				.padding(
					top = contentPadding.calculateTopPadding() + 12.dp,
					start = 12.dp,
					end = 12.dp
				)
		) {
			Form {
				info.forEach { (key, value) ->
					FormRow {
						Column(Modifier.padding(vertical = 4.dp)) {
							Text(
								text = stringResource(key),
								style = MaterialTheme.typography.labelMedium,
								color = MaterialTheme.colorScheme.primary
							)
							SelectionContainer {
								Text(
									text = "${value ?: stringResource(Res.string.info_unknown)}",
									style = MaterialTheme.typography.bodyLarge
								)
							}
						}
					}
				}
			}
		}
	}
}

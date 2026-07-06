package paige.navic.ui.components.sheets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
import navic.composeapp.generated.resources.track_info_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import paige.navic.domain.manager.PreferenceManager
import paige.navic.ui.components.common.Form
import paige.navic.ui.components.common.FormRow
import paige.navic.ui.screens.song.viewmodels.SongDetailViewModel
import paige.navic.util.core.effectiveGain
import paige.navic.util.core.toFileSize
import paige.navic.util.core.toHoursMinutesSeconds
import paige.navic.util.ui.rememberColorSchemeFromCoverArt
import paige.navic.ui.theme.NavicTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SongDetailSheet(
	songId: String,
	initialCoverArtId: String? = null
) {
	val viewModel = koinViewModel<SongDetailViewModel>(
		key = songId,
		parameters = { parametersOf(songId) }
	)

	val songState by viewModel.songState.collectAsStateWithLifecycle()
	val song = songState.data

	val preferenceManager = koinInject<PreferenceManager>()
	val activeCoverArtId = if (initialCoverArtId.isNullOrEmpty()) song?.coverArtId else initialCoverArtId
	val colorScheme = rememberColorSchemeFromCoverArt(activeCoverArtId)

	val info = remember(song) {
		song?.let {
			persistentMapOf(
				Res.string.info_track_name to song.title,
				Res.string.info_track_artist to song.artistName,
				Res.string.info_track_album to song.albumTitle,

				Res.string.info_track_number to song.trackNumber.toString(),
				Res.string.info_track_disc_number to song.discNumber.toString(),
				Res.string.info_track_year to song.year.toString(),
				Res.string.info_track_genre to song.genre,

				Res.string.info_track_duration to song.duration.toHoursMinutesSeconds(),
				Res.string.info_track_format to song.mimeType,
				Res.string.info_track_bitrate to song.bitRate?.let { "$it kbps" },
				Res.string.info_track_bit_depth to song.bitDepth?.toString(),
				Res.string.info_track_sampling_rate to song.sampleRate?.let { "$it Hz" },
				Res.string.info_track_channel_count to song.audioChannelCount?.toString(),

				Res.string.info_track_file_size to song.fileSize.toFileSize(),
				Res.string.info_track_path to song.filePath,

				Res.string.info_track_replay_gain to song.replayGain?.trackGain?.let { "$it dB" },
				Res.string.info_album_replay_gain to song.replayGain?.albumGain?.let { "$it dB" },
				Res.string.info_track_replay_gain_effective to song.replayGain?.effectiveGain(
					preferenceManager.replayGainMode
				)?.toString()
			)
		}.orEmpty()
	}

	NavicTheme(colorScheme) {
		Surface(
			modifier = Modifier.fillMaxSize(),
			color = MaterialTheme.colorScheme.surface
		) {
			Column(
				modifier = Modifier
					.fillMaxSize()
					.verticalScroll(rememberScrollState())
					.padding(horizontal = 16.dp)
			) {
				Text(
					text = stringResource(Res.string.track_info_title),
					style = MaterialTheme.typography.titleLarge,
					fontWeight = FontWeight.SemiBold,
					color = MaterialTheme.colorScheme.onSurface,
					modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)
				)

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
										text = value ?: stringResource(Res.string.info_unknown),
										style = MaterialTheme.typography.bodyLarge,
										color = MaterialTheme.colorScheme.onSurface
									)
								}
							}
						}
					}
				}

				Spacer(
					Modifier.padding(
						WindowInsets.systemBars
							.only(WindowInsetsSides.Bottom)
							.asPaddingValues()
					)
				)
				Spacer(Modifier.height(16.dp))
			}
		}
	}
}

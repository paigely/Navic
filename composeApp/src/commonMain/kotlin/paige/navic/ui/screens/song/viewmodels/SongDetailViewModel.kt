package paige.navic.ui.screens.song.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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
import org.jetbrains.compose.resources.StringResource
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.models.DomainSong
import paige.navic.domain.repositories.CollectionRepository
import paige.navic.ui.core.UiState
import paige.navic.util.core.effectiveGain
import paige.navic.util.core.toFileSize
import paige.navic.util.core.toHoursMinutesSeconds

class SongDetailViewModel(
	songId: String,
	private val repository: CollectionRepository,
	private val preferenceManager: PreferenceManager
) : ViewModel() {
	val songState: StateFlow<UiState<DomainSong>>
		field = MutableStateFlow<UiState<DomainSong>>(UiState.Loading())

	val info: StateFlow<Map<StringResource, String?>>
		field = MutableStateFlow(emptyMap())

	init {
		viewModelScope.launch {
			val song = repository.getSongById(songId)
			if (song != null) {
				songState.value = UiState.Success(song)
				info.value = getInfo(song)
			} else {
				songState.value = UiState.Error(Exception("Unknown song"))
			}
		}
	}

	fun getInfo(song: DomainSong): Map<StringResource, String?> =
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
}

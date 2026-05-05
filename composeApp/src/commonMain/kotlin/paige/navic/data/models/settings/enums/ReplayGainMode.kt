package paige.navic.data.models.settings.enums

import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_album_replay_gain
import navic.composeapp.generated.resources.info_track_replay_gain
import navic.composeapp.generated.resources.option_off
import org.jetbrains.compose.resources.StringResource

enum class ReplayGainMode(val displayName: StringResource) {
	Off(Res.string.option_off),
	Track(Res.string.info_track_replay_gain),
	Album(Res.string.info_album_replay_gain)
}

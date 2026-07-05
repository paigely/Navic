package paige.navic.domain.models.settings

import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.option_explicit_playback_allowed
import navic.composeapp.generated.resources.option_explicit_playback_skip
import navic.composeapp.generated.resources.option_explicit_playback_skip_session
import org.jetbrains.compose.resources.StringResource

enum class ExplicitContentPlayback(val displayName: StringResource) {
	Allowed(Res.string.option_explicit_playback_allowed),
	Skip(Res.string.option_explicit_playback_skip),
	SkipForThisSession(Res.string.option_explicit_playback_skip_session)
}

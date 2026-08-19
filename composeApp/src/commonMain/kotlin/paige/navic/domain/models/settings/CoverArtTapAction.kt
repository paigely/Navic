package paige.navic.domain.models.settings

import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.option_cover_art_action_disabled
import navic.composeapp.generated.resources.option_cover_art_action_show_lyrics
import org.jetbrains.compose.resources.StringResource

enum class CoverArtTapAction(val displayName: StringResource) {
	Disabled(Res.string.option_cover_art_action_disabled),
	ShowLyrics(Res.string.option_cover_art_action_show_lyrics)
}

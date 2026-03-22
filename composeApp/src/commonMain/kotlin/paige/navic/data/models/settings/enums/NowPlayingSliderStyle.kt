package paige.navic.data.models.settings.enums

import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.option_now_playing_slider_style_flat
import navic.composeapp.generated.resources.option_now_playing_slider_style_squiggly
import org.jetbrains.compose.resources.StringResource

enum class NowPlayingSliderStyle(val displayName: StringResource) {
	Flat(Res.string.option_now_playing_slider_style_flat),
	Squiggly(Res.string.option_now_playing_slider_style_squiggly)
}
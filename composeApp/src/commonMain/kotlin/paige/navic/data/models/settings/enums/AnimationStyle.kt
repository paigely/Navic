package paige.navic.data.models.settings.enums

import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.option_animation_style_expressive
import navic.composeapp.generated.resources.option_animation_style_standard
import org.jetbrains.compose.resources.StringResource

enum class AnimationStyle(val displayName: StringResource) {
	Expressive(Res.string.option_animation_style_expressive),
	Standard(Res.string.option_animation_style_standard)
}

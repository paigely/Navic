package paige.navic.data.models.settings.enums

import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.option_position_bottom
import navic.composeapp.generated.resources.option_position_top
import org.jetbrains.compose.resources.StringResource

enum class ToolbarPosition(val displayName: StringResource) {
	Top(Res.string.option_position_top),
	Bottom(Res.string.option_position_bottom)
}

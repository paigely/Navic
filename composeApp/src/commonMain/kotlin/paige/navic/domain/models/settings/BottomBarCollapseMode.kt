package paige.navic.domain.models.settings

import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.option_bottom_bar_collapse_mode_never
import navic.composeapp.generated.resources.option_bottom_bar_collapse_mode_on_scroll
import org.jetbrains.compose.resources.StringResource

enum class BottomBarCollapseMode(val displayName: StringResource) {
	Never(Res.string.option_bottom_bar_collapse_mode_never),
	OnScroll(Res.string.option_bottom_bar_collapse_mode_on_scroll)
}

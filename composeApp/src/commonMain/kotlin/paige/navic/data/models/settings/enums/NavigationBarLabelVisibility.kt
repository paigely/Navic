package paige.navic.data.models.settings.enums

import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.option_navigation_bar_label_visibility_always
import navic.composeapp.generated.resources.option_navigation_bar_label_visibility_only_selected
import org.jetbrains.compose.resources.StringResource

enum class NavigationBarLabelVisibility(val displayName: StringResource) {
	Always(Res.string.option_navigation_bar_label_visibility_always),
	OnlySelected(Res.string.option_navigation_bar_label_visibility_only_selected)
}

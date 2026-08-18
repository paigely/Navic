package paige.navic.domain.models.settings

import androidx.compose.ui.graphics.vector.ImageVector
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.option_list_view_mode_grid
import navic.composeapp.generated.resources.option_list_view_mode_list
import org.jetbrains.compose.resources.StringResource
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Grid
import paige.navic.icons.outlined.List

enum class ListViewMode(val displayName: StringResource, val icon: ImageVector) {
	Grid(Res.string.option_list_view_mode_grid, Icons.Outlined.Grid),
	List(Res.string.option_list_view_mode_list, Icons.Outlined.List)
}

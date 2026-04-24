package paige.navic.data.models.settings.enums

import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.option_offline_mode_auto
import navic.composeapp.generated.resources.option_offline_mode_forced
import navic.composeapp.generated.resources.option_offline_mode_no_wifi
import org.jetbrains.compose.resources.StringResource

enum class OfflineMode(val displayName: StringResource) {
	Auto(Res.string.option_offline_mode_auto),
	Forced(Res.string.option_offline_mode_forced),
	NoWiFi(Res.string.option_offline_mode_no_wifi),
}

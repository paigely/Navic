package paige.navic.domain.models.settings

import kotlinx.serialization.Serializable
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.option_equaliser_mode_builtin
import navic.composeapp.generated.resources.option_equaliser_mode_disabled
import navic.composeapp.generated.resources.option_equaliser_mode_external
import org.jetbrains.compose.resources.StringResource

// Which equaliser processes Navic's audio session. Builtin/External need to be mutually exclusive, 
// otherwise they will fight for effect control and cause audio issues
enum class EqualiserMode(val displayName: StringResource) {
	Disabled(Res.string.option_equaliser_mode_disabled),
	BuiltIn(Res.string.option_equaliser_mode_builtin),
	External(Res.string.option_equaliser_mode_external)
}

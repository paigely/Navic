package paige.navic.domain.models.snackbars

import org.jetbrains.compose.resources.StringResource

data class PlayerEvent(
	val resource: StringResource,
	val args: List<Any> = emptyList()
)

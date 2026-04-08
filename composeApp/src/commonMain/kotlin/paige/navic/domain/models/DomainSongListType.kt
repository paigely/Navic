package paige.navic.domain.models

import androidx.compose.runtime.Immutable
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.option_sort_frequent
import navic.composeapp.generated.resources.option_sort_random
import navic.composeapp.generated.resources.option_sort_starred
import org.jetbrains.compose.resources.StringResource

@Immutable
enum class DomainSongListType(val displayName: StringResource) {
	FrequentlyPlayed(Res.string.option_sort_frequent),
	Starred(Res.string.option_sort_starred),
	Random(Res.string.option_sort_random)
}

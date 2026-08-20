package paige.navic.domain.models

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Serializable
@Immutable
enum class DomainFilter(val bit: Int) {
	Starred(1 shl 0),
	Downloaded(1 shl 1)
}

fun Set<DomainFilter>.toBitmask(): Int {
	var mask = 0
	forEach { mask = mask or it.bit }
	return mask
}

fun Int.toDomainFilters(): Set<DomainFilter> {
	return DomainFilter.entries.filter { (this and it.bit) != 0 }.toSet()
}

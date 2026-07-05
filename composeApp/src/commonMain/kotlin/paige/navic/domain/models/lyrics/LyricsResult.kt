package paige.navic.domain.models.lyrics

import androidx.compose.runtime.Immutable

@Immutable
data class LyricsResult(
	val lines: List<LyricsLine>,
	val provider: LyricsProvider,
	val rawContent: String? = null
) {
	val isSynced: Boolean = lines.any { it.time != null }
}

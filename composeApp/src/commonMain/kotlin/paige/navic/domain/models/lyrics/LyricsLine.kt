package paige.navic.domain.models.lyrics

import androidx.compose.runtime.Immutable
import kotlin.time.Duration

@Immutable
data class LyricsLine(
	val time: Duration? = null,
	val text: String,
	val words: List<LyricsWord>? = null
)

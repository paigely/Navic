package paige.navic.domain.models.lyrics

data class LyricsResult(
	val lines: List<LyricsLine>,
	val provider: LyricsProvider,
	val rawContent: String? = null
) {
	val isSynced: Boolean = lines.any { it.time != null }
}

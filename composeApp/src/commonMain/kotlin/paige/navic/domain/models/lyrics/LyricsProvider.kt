package paige.navic.domain.models.lyrics

import kotlinx.serialization.Serializable

@Serializable
data class LyricsProvider(
	val id: Id,
	val enabled: Boolean
) {
	@Serializable
	enum class Id {
		SUBSONIC,
		LYRICS_PLUS,
		LRCLIB
	}
}

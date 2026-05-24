package paige.navic.domain.models.lyrics

import kotlinx.serialization.Serializable

@Serializable
enum class LyricsProvider(
	val displayName: String
) {
	LYRICS_PLUS("YouLy+"),
	SUBSONIC("Subsonic"),
	LRCLIB("Lrclib")
}

package paige.navic.domain.models.lyrics

import kotlinx.serialization.Serializable

@Serializable
data class LyricsConfig(
	val priority: List<LyricsProvider> = listOf(
		LyricsProvider.LYRICS_PLUS,
		LyricsProvider.SUBSONIC,
		LyricsProvider.LRCLIB
	),
	val lyricsPlusMirrors: List<String> = listOf(
		"https://lyricsplus.atomix.one",
		"https://lyricsplus-seven.vercel.app",
		"https://lyricsplus.prjktla.workers.dev"
	),
	val lrcLibBaseUrl: String = "https://lrclib.net/api/get"
) {
	companion object {
		const val KEY = "lyrics_config_prefs"
	}
}

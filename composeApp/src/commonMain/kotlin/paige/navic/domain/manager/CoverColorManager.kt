package paige.navic.domain.manager

import androidx.compose.ui.graphics.Color

class CoverColorManager {
	private val colorCache = mutableMapOf<String, Color>()

	fun getColor(coverArtId: String): Color? = colorCache[coverArtId]
	fun putColor(coverArtId: String, color: Color) {
		colorCache[coverArtId] = color
	}
}

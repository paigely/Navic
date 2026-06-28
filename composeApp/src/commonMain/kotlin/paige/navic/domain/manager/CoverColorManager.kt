package paige.navic.domain.manager

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import paige.navic.data.database.dao.CoverColorDao
import paige.navic.data.database.entities.CoverColorEntity

class CoverColorManager(
	private val coverColorDao: CoverColorDao
) {
	private val colorCache = mutableMapOf<String, Color?>()

	suspend fun getColor(coverArtId: String): Color? {
		if (colorCache.containsKey(coverArtId)) return colorCache[coverArtId]

		val color = coverColorDao.getColor(coverArtId)?.let { Color(it.color) }
		colorCache[coverArtId] = color
		return color
	}

	suspend fun putColor(coverArtId: String, color: Color) {
		colorCache[coverArtId] = color
		coverColorDao.insertColor(CoverColorEntity(coverArtId, color.toArgb()))
	}
}

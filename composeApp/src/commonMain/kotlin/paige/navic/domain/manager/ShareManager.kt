package paige.navic.domain.manager

import androidx.compose.ui.graphics.ImageBitmap

expect class ShareManager {
	suspend fun shareImage(bitmap: ImageBitmap, fileName: String)
	suspend fun shareString(string: String)
}

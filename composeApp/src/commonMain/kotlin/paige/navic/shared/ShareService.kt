package paige.navic.shared

import androidx.compose.ui.graphics.ImageBitmap

interface ShareService {
	fun shareImage(bitmap: ImageBitmap, fileName: String)
}

object ShareServiceProvider {
	private var _service: ShareService? = null
	
	val service: ShareService
		get() = _service ?: error("ShareService has not been initialized!")
	
	fun initialize(service: ShareService) {
		_service = service
	}

}

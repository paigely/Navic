package paige.navic.util.color

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import coil3.Image
import coil3.PlatformContext
import coil3.toBitmap

actual fun Image.toComposeImageBitmap(context: PlatformContext): ImageBitmap {
	return this.toBitmap().asComposeImageBitmap()
}

package paige.navic.util.color

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import coil3.Image
import coil3.PlatformContext
import coil3.asDrawable

actual fun Image.toComposeImageBitmap(context: PlatformContext): ImageBitmap {
	return this.asDrawable(context.resources).toBitmap().asImageBitmap()
}

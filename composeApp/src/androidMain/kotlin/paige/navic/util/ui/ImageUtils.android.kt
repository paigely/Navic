package paige.navic.util.ui

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import coil3.Image
import coil3.toBitmap

actual fun Image.toImageBitmap(): ImageBitmap?
	= runCatching { toBitmap().asImageBitmap() }.getOrNull()

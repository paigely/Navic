package paige.navic.util.ui

import androidx.compose.ui.graphics.ImageBitmap
import coil3.Image

expect fun Image.toImageBitmap(): ImageBitmap?

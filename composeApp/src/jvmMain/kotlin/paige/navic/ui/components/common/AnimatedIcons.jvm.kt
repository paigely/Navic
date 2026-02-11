package paige.navic.ui.components.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.navigation3.runtime.NavKey

@Composable
actual fun animatedTabIconPainter(destination: NavKey): Painter? = null

@Composable
actual fun playPauseIconPainter(reversed: Boolean): Painter? = null

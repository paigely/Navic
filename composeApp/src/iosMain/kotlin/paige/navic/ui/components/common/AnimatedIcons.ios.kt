package paige.navic.ui.components.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import paige.navic.data.models.Screen

@Composable
actual fun animatedTabIconPainter(destination: Screen): Painter? = null

@Composable
actual fun playPauseIconPainter(reversed: Boolean): Painter? = null

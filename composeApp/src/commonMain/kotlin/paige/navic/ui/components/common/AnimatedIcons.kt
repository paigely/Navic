package paige.navic.ui.components.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.navigation3.runtime.NavKey

@Composable
expect fun animatedTabIconPainter(destination: NavKey): Painter?

@Composable
expect fun playPauseIconPainter(reversed: Boolean): Painter?

package paige.navic.ui.components.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import paige.navic.data.models.Screen

@Composable
expect fun animatedTabIconPainter(destination: Screen): Painter?

@Composable
expect fun playPauseIconPainter(reversed: Boolean): Painter?

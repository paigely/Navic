package paige.navic.util.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
expect fun rememberScreenCornerRadius(defaultRadius: Dp = 16.dp): Dp

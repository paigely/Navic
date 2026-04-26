package paige.navic.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val ColorScheme.positive: Color
	@Composable
	get() = if (isSystemInDarkTheme()) Color(0xFF50C660) else Color(0xFF238636)

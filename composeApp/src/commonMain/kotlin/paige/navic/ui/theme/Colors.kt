package paige.navic.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val ColorScheme.positive: Color
	@Composable
	get() = if (isSystemInDarkTheme()) Color(0xFF50C660) else Color(0xFF238636)

val ColorScheme.warning: Color
	@Composable
	get() = if (isSystemInDarkTheme()) Color(0xFFEFB55F) else Color(0xFFAC8132)

val ColorScheme.red: Color
	@Composable
	get() = if (isSystemInDarkTheme()) Color(0xFFEF5F5F) else Color(0xFFAC3232)

val ColorScheme.purple: Color
	@Composable
	get() = if (isSystemInDarkTheme()) Color(0xFF955FEF) else Color(0xFF674699)

val ColorScheme.blue: Color
	@Composable
	get() = if (isSystemInDarkTheme()) Color(0xFF5F99EF) else Color(0xFF445FAA)

val ColorScheme.pink: Color
	@Composable
	get() = if (isSystemInDarkTheme()) Color(0xFFEF5F91) else Color(0xFFAA4474)

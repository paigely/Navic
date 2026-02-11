package paige.navic.ui.components.common

import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import androidx.navigation3.runtime.NavKey
import paige.navic.R
import paige.navic.data.models.Screen

@Composable
actual fun animatedTabIconPainter(destination: NavKey): Painter? {
	val res = when (destination) {
		is Screen.Library -> R.drawable.anim_library
		is Screen.Playlists -> R.drawable.anim_playlist
		is Screen.Artists -> R.drawable.anim_artist
		else -> return null
	}

	val image = AnimatedImageVector.animatedVectorResource(res)
	val atEnd = remember { mutableStateOf(false) }

	LaunchedEffect(Unit) {
		atEnd.value = true
	}

	return rememberAnimatedVectorPainter(image, atEnd.value)
}

@Composable
actual fun playPauseIconPainter(reversed: Boolean): Painter? {
	val image = AnimatedImageVector.animatedVectorResource(R.drawable.anim_pause)
	return rememberAnimatedVectorPainter(image, reversed)
}

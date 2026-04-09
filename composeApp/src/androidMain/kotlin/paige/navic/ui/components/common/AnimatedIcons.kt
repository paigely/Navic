package paige.navic.ui.components.common

import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import paige.navic.data.models.Screen

@Composable
actual fun animatedTabIconPainter(destination: Screen): Painter? {
	val context = LocalContext.current
	val resources = LocalResources.current
	val res = resources.getIdentifier(
		when (destination) {
			is Screen.Library -> "anim_library"
			is Screen.PlaylistList -> "anim_playlist"
			is Screen.ArtistList -> "anim_artist"
			else -> return null
		},
		"drawable",
		context.packageName
	)

	val image = AnimatedImageVector.animatedVectorResource(res)
	val atEnd = remember { mutableStateOf(false) }

	LaunchedEffect(Unit) {
		atEnd.value = true
	}

	return rememberAnimatedVectorPainter(image, atEnd.value)
}

@Composable
actual fun playPauseIconPainter(reversed: Boolean): Painter? {
	val context = LocalContext.current
	val resources = LocalResources.current
	val image = AnimatedImageVector.animatedVectorResource(resources.getIdentifier(
		"anim_pause",
		"drawable",
		context.packageName
	))
	return rememberAnimatedVectorPainter(image, reversed)
}

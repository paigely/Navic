package paige.navic.ui.components.common

import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import org.koin.compose.koinInject
import paige.navic.data.models.Screen
import paige.navic.shared.ResourceProvider

@Composable
actual fun animatedTabIconPainter(destination: Screen): Painter? {
	val resourceProvider = koinInject<ResourceProvider>()
	val res = when (destination) {
		is Screen.Library -> resourceProvider.animLibrary
		is Screen.PlaylistList -> resourceProvider.animPlaylist
		is Screen.ArtistList -> resourceProvider.animArtist
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
	val resourceProvider = koinInject<ResourceProvider>()
	val image = AnimatedImageVector.animatedVectorResource(resourceProvider.animPause)
	return rememberAnimatedVectorPainter(image, reversed)
}

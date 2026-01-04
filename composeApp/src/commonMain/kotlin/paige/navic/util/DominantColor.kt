package paige.navic.util

import androidx.compose.runtime.*
import com.kmpalette.DominantColorState
import com.kmpalette.loader.rememberNetworkLoader
import com.kmpalette.rememberDominantColorState
import io.ktor.http.Url

@Composable
fun rememberDominantColor(url: String): DominantColorState<Url> {
	val networkLoader = rememberNetworkLoader()
	val dominantColorState = rememberDominantColorState(loader = networkLoader)
	LaunchedEffect(url) {
		dominantColorState.updateFrom(Url(url))
	}
	return dominantColorState
}

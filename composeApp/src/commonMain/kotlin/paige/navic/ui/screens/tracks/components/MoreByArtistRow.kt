package paige.navic.ui.screens.tracks.components

import androidx.compose.foundation.lazy.LazyListScope
import dev.zt64.subsonic.api.model.Artist
import dev.zt64.subsonic.api.model.SongCollection
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.title_more_by_artist
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalNavStack
import paige.navic.data.models.Screen
import paige.navic.ui.components.layouts.ArtCarousel
import paige.navic.ui.components.layouts.ArtCarouselItem
import paige.navic.utils.UiState

fun LazyListScope.tracksScreenMoreByArtistRow(
	tracks: SongCollection,
	artistState: UiState<Artist>,
	tab: String
) {
	(artistState as? UiState.Success)?.data?.takeIf {
		it.album.any { album -> album.id != tracks.id }
	}?.let { artist ->
		item {
			val backStack = LocalNavStack.current
			ArtCarousel(
				title = stringResource(Res.string.title_more_by_artist, artist.name),
				items = artist.album
					.filter { it.id != tracks.id }.sortedByDescending { it.playCount }
			) { album ->
				ArtCarouselItem(
					coverArtId = album.coverArtId,
					title = album.name,
					contentDescription = album.name,
					onClick = {
						backStack.add(Screen.Tracks(album, tab))
					}
				)
			}
		}
	}
}

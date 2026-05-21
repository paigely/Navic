package paige.navic.ui.screens.collection.components

import androidx.compose.foundation.lazy.LazyListScope
import androidx.lifecycle.compose.dropUnlessResumed
import kotlinx.collections.immutable.toImmutableList
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.title_more_by_artist
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalNavStack
import paige.navic.data.models.Screen
import paige.navic.domain.models.DomainAlbum
import paige.navic.ui.components.layouts.ArtCarousel
import paige.navic.ui.components.layouts.ArtCarouselItem

fun LazyListScope.collectionDetailScreenMoreByArtistRow(
	artistName: String,
	artistAlbums: List<DomainAlbum>,
	tab: String,
) {
	item {
		val backStack = LocalNavStack.current
		ArtCarousel(
			title = stringResource(Res.string.title_more_by_artist, artistName),
			items = artistAlbums.sortedByDescending { it.playCount }.toImmutableList()
		) { album ->
			ArtCarouselItem(
				coverArtId = album.coverArtId,
				title = album.name,
				contentDescription = album.name,
				onClick = dropUnlessResumed {
					backStack.add(Screen.CollectionDetail(album.id, tab))
				}
			)
		}
	}
}

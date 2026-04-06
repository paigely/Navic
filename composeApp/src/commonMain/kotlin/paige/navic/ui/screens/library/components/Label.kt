package paige.navic.ui.screens.library.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.option_sort_alphabetical_by_artist
import navic.composeapp.generated.resources.option_sort_alphabetical_by_name
import navic.composeapp.generated.resources.option_sort_frequent
import navic.composeapp.generated.resources.option_sort_newest
import navic.composeapp.generated.resources.option_sort_random
import navic.composeapp.generated.resources.option_sort_recent
import navic.composeapp.generated.resources.option_sort_starred
import paige.navic.domain.models.DomainAlbumListType

@Composable
fun DomainAlbumListType.label() =
	when (this) {
		DomainAlbumListType.Random -> stringResource(Res.string.option_sort_random)
		DomainAlbumListType.Newest -> stringResource(Res.string.option_sort_newest)
		DomainAlbumListType.Frequent -> stringResource(Res.string.option_sort_frequent)
		DomainAlbumListType.Recent -> stringResource(Res.string.option_sort_recent)
		DomainAlbumListType.AlphabeticalByName -> stringResource(Res.string.option_sort_alphabetical_by_name)
		DomainAlbumListType.AlphabeticalByArtist -> stringResource(Res.string.option_sort_alphabetical_by_artist)
		DomainAlbumListType.Starred -> stringResource(Res.string.option_sort_starred)
		else -> "$this"
	}

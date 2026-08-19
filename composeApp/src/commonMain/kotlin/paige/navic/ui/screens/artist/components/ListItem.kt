package paige.navic.ui.screens.artist.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.count_albums
import org.jetbrains.compose.resources.pluralStringResource
import org.koin.compose.koinInject
import paige.navic.LocalNavStack
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.models.DomainAlbum
import paige.navic.domain.models.DomainArtist
import paige.navic.ui.components.common.CoverArt
import paige.navic.ui.components.common.MarqueeText
import paige.navic.ui.components.sheets.ArtistSheet
import paige.navic.ui.navigation.Screen
import paige.navic.ui.screens.playlist.dialogs.PlaylistUpdateDialog

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ArtistListScreenListItem(
	modifier: Modifier = Modifier,
	artist: DomainArtist,
	selected: Boolean,
	selectedArtistAlbums: ImmutableList<DomainAlbum>?,
	starred: Boolean,
	onSelect: () -> Unit,
	onDeselect: () -> Unit,
	onPlayNext: () -> Unit,
	onAddToQueue: () -> Unit,
	onSetStarred: (starred: Boolean) -> Unit
) {
	val preferenceManager = koinInject<PreferenceManager>()

	val scope = rememberCoroutineScope()

	val backStack = LocalNavStack.current

	var playlistDialogShown by rememberSaveable { mutableStateOf(false) }

	Box(modifier) {
		ListItem(
			leadingContent = {
				CoverArt(
					coverArtId = artist.coverArtId,
					modifier = Modifier.size(50.dp),
					shape = preferenceManager.coverArtShape.decreasedShape
				)
			},
			content = { MarqueeText(artist.name) },
			supportingContent = {
				MarqueeText(
					buildString {
						append(pluralStringResource(
							Res.plurals.count_albums,
							artist.albumCount,
							artist.albumCount
						))
					}
				)
			},
			onClick = dropUnlessResumed {
				scope.launch {
					backStack.add(Screen.ArtistDetail(artist.id))
				}
			},
			onLongClick = onSelect
		)
		if (selected) {
			ArtistSheet(
				onDismissRequest = onDeselect,
				artist = artist,
				onPlayNext = onPlayNext,
				onAddToQueue = onAddToQueue,
				onAddAllToPlaylist = { playlistDialogShown = true },
				starred = starred,
				onSetStarred = { onSetStarred(!starred) }
			)
		}
		if (playlistDialogShown) {
			PlaylistUpdateDialog(
				songs = selectedArtistAlbums?.flatMap { it.songs }.orEmpty().toPersistentList(),
				onDismissRequest = { playlistDialogShown = false }
			)
		}
	}
}

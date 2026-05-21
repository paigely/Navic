package paige.navic.ui.screens.nowPlaying.components.rows

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_not_playing
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.LocalNavStack
import paige.navic.data.models.Screen
import paige.navic.domain.models.DomainExplicitStatus
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.ui.components.common.MarqueeText
import paige.navic.ui.screens.nowPlaying.components.controls.NowPlayingMoreButton
import paige.navic.ui.screens.nowPlaying.components.controls.NowPlayingStarButton
import paige.navic.utils.InlineExplicitIconLarge

@Composable
fun NowPlayingInfoRow(
	songIsStarred: Boolean,
	onSetSongIsStarred: (Boolean) -> Unit,
	songRating: Int,
	onSetSongRating: (Int) -> Unit
) {
	val backStack = LocalNavStack.current
	val player = koinViewModel<MediaPlayerViewModel>()
	val playerState by player.uiState.collectAsState()
	val song = playerState.currentSong
	Row(
		modifier = Modifier
			.padding(horizontal = 16.dp)
			.padding(bottom = 6.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(8.dp)
	) {
		Column(Modifier.weight(1f)) {
			song?.let { song ->
				MarqueeText(
					text = buildAnnotatedString {
						append(song.title)
						if (song.explicitStatus == DomainExplicitStatus.Explicit) {
							append(" ")
							appendInlineContent("InlineExplicitIcon")
						}
					},
					inlineContent = InlineExplicitIconLarge,
					modifier = Modifier.clickable(onClick = dropUnlessResumed {
						song.albumId?.let {
							backStack.removeLastOrNull()

							val lastScreen = backStack.lastOrNull()

							val isSameAlbum = if (lastScreen is Screen.CollectionDetail) {
								lastScreen.collectionId == song.albumId
							} else {
								false
							}

							if (!isSameAlbum)
								backStack.add(
									Screen.CollectionDetail(
										playerState.currentCollection?.id ?: return@dropUnlessResumed,
										""
									)
								)
						}
					}),
					style = MaterialTheme.typography.bodyLarge
						.copy(
							fontSize = MaterialTheme.typography.bodyLarge.fontSize * 1.1
						),
				)
			}
			MarqueeText(
				modifier = Modifier.clickable(
					song != null,
					onClick = dropUnlessResumed {
						song?.artistId?.let { id ->
							backStack.remove(Screen.NowPlaying)
							backStack.add(Screen.ArtistDetail(id))
						}
					}
				),
				style = MaterialTheme.typography.bodyMedium
					.copy(
						color = MaterialTheme.colorScheme.onSurfaceVariant,
						fontSize = MaterialTheme.typography.bodyMedium.fontSize * 1.1
					),
				text = song?.artistName ?: stringResource(Res.string.info_not_playing)
			)
		}
		Row(
			horizontalArrangement = Arrangement.spacedBy(10.dp)
		) {
			NowPlayingStarButton(
				songIsStarred = songIsStarred,
				onSetSongIsStarred = onSetSongIsStarred
			)
			NowPlayingMoreButton(
				songRating = songRating,
				onSetSongRating = onSetSongRating
			)
		}
	}
}

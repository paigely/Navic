package paige.navic.ui.screens.nowPlaying.components.rows

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_not_playing
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalMediaPlayer
import paige.navic.LocalNavStack
import paige.navic.data.models.Screen
import paige.navic.ui.components.common.MarqueeText
import paige.navic.ui.screens.nowPlaying.components.controls.NowPlayingMoreButton
import paige.navic.ui.screens.nowPlaying.components.controls.NowPlayingStarButton

@Composable
fun NowPlayingInfoRow() {
	val backStack = LocalNavStack.current
	val player = LocalMediaPlayer.current
	val playerState by player.uiState.collectAsState()
	val track = playerState.currentTrack
	Row(
		modifier = Modifier
			.padding(horizontal = 16.dp)
			.padding(bottom = 6.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(8.dp)
	) {
		Column(Modifier.weight(1f)) {
			track?.title?.let { title ->
				MarqueeText(
					title,
					modifier = Modifier.clickable {
						track.albumId?.let {
							backStack.removeLastOrNull()

							val lastScreen = backStack.lastOrNull()

							val isSameAlbum = if (lastScreen is Screen.Tracks) {
								lastScreen.partialCollection.id == track.albumId
							} else {
								false
							}

							if (!isSameAlbum)
								backStack.add(
									Screen.Tracks(
										playerState.currentCollection ?: return@clickable,
										""
									)
								)
						}
					},
					style = MaterialTheme.typography.bodyLarge
						.copy(
							fontSize = MaterialTheme.typography.bodyLarge.fontSize * 1.1
						),
				)
			}
			MarqueeText(
				modifier = Modifier.clickable(track != null) {
					track?.artistId?.let { id ->
						backStack.remove(Screen.NowPlaying)
						backStack.add(Screen.Artist(id))
					}
				},
				style = MaterialTheme.typography.bodyMedium
					.copy(
						color = MaterialTheme.colorScheme.onSurfaceVariant,
						fontSize = MaterialTheme.typography.bodyMedium.fontSize * 1.1
					),
				text = track?.artistName ?: stringResource(Res.string.info_not_playing)
			)
		}
		Row(
			horizontalArrangement = Arrangement.spacedBy(10.dp)
		) {
			NowPlayingStarButton()
			NowPlayingMoreButton()
		}
	}
}
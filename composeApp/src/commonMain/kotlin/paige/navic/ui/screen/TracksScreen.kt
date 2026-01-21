package paige.navic.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.kyant.capsule.ContinuousCapsule
import com.kyant.capsule.ContinuousRoundedRectangle
import dev.burnoo.compose.remembersetting.rememberFloatSetting
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_play
import navic.composeapp.generated.resources.action_remove_star
import navic.composeapp.generated.resources.action_share
import navic.composeapp.generated.resources.action_shuffle
import navic.composeapp.generated.resources.action_star
import navic.composeapp.generated.resources.info_unknown_album
import navic.composeapp.generated.resources.share
import navic.composeapp.generated.resources.unstar
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalMediaPlayer
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.ui.component.common.Dropdown
import paige.navic.ui.component.common.DropdownItem
import paige.navic.ui.component.common.ErrorBox
import paige.navic.ui.component.common.Form
import paige.navic.ui.component.common.FormRow
import paige.navic.ui.component.dialog.ShareDialog
import paige.navic.ui.theme.mapleMono
import paige.navic.ui.viewmodel.TracksViewModel
import paige.navic.util.UiState
import paige.navic.util.shimmerLoading
import paige.navic.util.toHHMMSS
import paige.subsonic.api.model.Album
import paige.subsonic.api.model.Playlist
import paige.subsonic.api.model.Track
import paige.subsonic.api.model.TrackCollection
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private class TracksScreenScope(
	val player: MediaPlayerViewModel,
	val tracks: TrackCollection
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TracksScreen(
	partialTracks: TrackCollection,
	viewModel: TracksViewModel = viewModel(key = partialTracks.toString()) {
		TracksViewModel(partialTracks)
	}
) {
	val player = LocalMediaPlayer.current
	val scrollState = rememberScrollState()

	val tracks by viewModel.tracksState.collectAsState()
	val selection by viewModel.selectedTrack.collectAsState()

	var shareId by remember { mutableStateOf<String?>(null) }
	var shareExpiry by remember { mutableStateOf<Duration?>(null) }

	val starredState by viewModel.starredState.collectAsState()

	AnimatedContent(tracks) {
		when (it) {
			is UiState.Loading -> TracksScreenPlaceholder()
			is UiState.Error -> ErrorBox(it)
			is UiState.Success -> {
				val tracks = it.data
				TracksScreenScope(
					player,
					tracks
				).apply {
					Column(
						modifier = Modifier
							.background(MaterialTheme.colorScheme.surface)
							.verticalScroll(scrollState)
							.padding(12.dp)
							.padding(bottom = 200.dp),
						horizontalAlignment = Alignment.CenterHorizontally,
						verticalArrangement = Arrangement.spacedBy(10.dp)
					) {
						Metadata()
						Form {
							tracks.tracks.onEachIndexed { index, track ->
								Box {
									TrackRow(
										track = track,
										onClick = {
											player.play(tracks, index)
										},
										onLongClick = {
											viewModel.selectTrack(track)
										}
									)
									Dropdown(
										expanded = selection == track,
										onDismissRequest = {
											viewModel.clearSelection()
										}
									) {
										DropdownItem(
											containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
											text = Res.string.action_share,
											leadingIcon = Res.drawable.share,
											onClick = {
												shareId = track.id
												viewModel.clearSelection()
											},
										)
										val starred =
											(starredState as? UiState.Success)?.data
										DropdownItem(
											containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
											text = if (starred == true)
												Res.string.action_remove_star
											else Res.string.action_star,
											leadingIcon = Res.drawable.unstar,
											onClick = {
												if (starred == true)
													viewModel.unstarSelectedTrack()
												else viewModel.starSelectedTrack()
												viewModel.clearSelection()
											},
											enabled = starred != null
										)
									}
								}
							}
						}
					}
				}
			}
		}
	}

	@Suppress("AssignedValueIsNeverRead")
	ShareDialog(
		id = shareId,
		onIdClear = { shareId = null; viewModel.clearSelection() },
		expiry = shareExpiry,
		onExpiryChange = { shareExpiry = it }
	)
}

@Composable
private fun TracksScreenScope.Metadata() {
	var artGridRounding by rememberFloatSetting("artGridRounding", 16f)
	AsyncImage(
		model = tracks.coverArt,
		contentDescription = tracks.title,
		contentScale = ContentScale.Crop,
		modifier = Modifier
			.widthIn(0.dp, 420.dp)
			.padding(
				top = 10.dp,
				start = 64.dp,
				end = 64.dp
			)
			.aspectRatio(1f)
			.clip(
				ContinuousRoundedRectangle(artGridRounding.dp)
			)
			.background(MaterialTheme.colorScheme.surfaceContainer)
	)
	Text(
		tracks.title ?: stringResource(Res.string.info_unknown_album),
		style = MaterialTheme.typography.headlineMedium,
		textAlign = TextAlign.Center
	)
	tracks.subtitle?.let {
		Text(
			it,
			style = MaterialTheme.typography.bodySmall,
			textAlign = TextAlign.Center
		)
	}
	Row(
		horizontalArrangement = Arrangement.spacedBy(
			10.dp,
			alignment = Alignment.CenterHorizontally
		)
	) {
		Button(
			modifier = Modifier.width(120.dp),
			onClick = { player.play(tracks, 0) },
			colors = ButtonDefaults.buttonColors(
				containerColor = MaterialTheme.colorScheme.surfaceContainer,
				contentColor = MaterialTheme.colorScheme.onSurface
			),
			shape = ContinuousCapsule
		) { Text(stringResource(Res.string.action_play)) }
		Button(
			modifier = Modifier.width(120.dp),
			onClick = {
				when (tracks) {
					is Album -> player.play(tracks.copy(
						song = tracks.song?.shuffled()
					), 0)
					is Playlist -> player.play(tracks.copy(
						entry = tracks.entry?.shuffled()
					), 0)
				}
			},
			colors = ButtonDefaults.buttonColors(
				containerColor = MaterialTheme.colorScheme.surfaceContainer,
				contentColor = MaterialTheme.colorScheme.onSurface
			),
			shape = ContinuousCapsule
		) { Text(stringResource(Res.string.action_shuffle)) }
	}
}

@Composable
private fun TracksScreenScope.TrackRow(
	track: Track,
	onClick: (() -> Unit)? = null,
	onLongClick: (() -> Unit)? = null
) {
	FormRow(
		onClick = onClick,
		onLongClick = onLongClick,
		horizontalArrangement = Arrangement.spacedBy(12.dp),
		modifier = Modifier.fillMaxWidth()
	) {
		Text(
			"${tracks.tracks.indexOf(track) + 1}",
			fontFamily = mapleMono(),
			fontWeight = FontWeight(400),
			fontSize = 13.sp,
			color = MaterialTheme.colorScheme.onSurface.copy(alpha = .5f),
			modifier = Modifier.width(25.dp),
			textAlign = TextAlign.Center
		)

		Row(
			modifier = Modifier.weight(1f),
			horizontalArrangement = Arrangement.spacedBy(12.dp)
		) {
			Column {
				Text(track.title, maxLines = 1)
				Text(
					track.artist.orEmpty(),
					style = MaterialTheme.typography.bodySmall,
					maxLines = 1
				)
			}
		}

		track.duration?.seconds?.toHHMMSS()?.let {
			Text(
				it,
				fontFamily = mapleMono(),
				fontWeight = FontWeight(400),
				fontSize = 13.sp,
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = .5f),
				maxLines = 1
			)
		}
	}
}

@Composable
private fun TracksScreenPlaceholder(
	modifier: Modifier = Modifier,
	rowCount: Int = 10
) {
	Column(
		modifier = modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.surface)
			.verticalScroll(rememberScrollState())
			.padding(12.dp)
			.padding(bottom = 200.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(10.dp)
	) {
		Box(
			modifier = Modifier
				.widthIn(0.dp, 420.dp)
				.padding(
					top = 10.dp,
					start = 64.dp,
					end = 64.dp
				)
				.aspectRatio(1f)
				.clip(ContinuousRoundedRectangle(16.dp))
				.background(MaterialTheme.colorScheme.surfaceContainer)
				.shimmerLoading()
		)

		Box(
			modifier = Modifier
				.padding(top = 8.dp)
				.fillMaxWidth(0.6f)
				.height(24.dp)
				.clip(ContinuousCapsule)
				.shimmerLoading()
		)

		Box(
			modifier = Modifier
				.padding(top = 4.dp)
				.fillMaxWidth(0.4f)
				.height(16.dp)
				.clip(ContinuousCapsule)
				.shimmerLoading()
		)

		Row(
			horizontalArrangement = Arrangement.spacedBy(10.dp),
			modifier = Modifier.padding(top = 8.dp)
		) {
			Box(
				modifier = Modifier
					.width(120.dp)
					.height(36.dp)
					.clip(ContinuousCapsule)
					.shimmerLoading()
			)
			Box(
				modifier = Modifier
					.width(120.dp)
					.height(36.dp)
					.clip(ContinuousCapsule)
					.shimmerLoading()
			)
		}

		Column(
			modifier = Modifier.fillMaxWidth(),
			verticalArrangement = Arrangement.spacedBy(8.dp)
		) {
			repeat(rowCount) {
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(vertical = 6.dp),
					horizontalArrangement = Arrangement.spacedBy(12.dp)
				) {
					Box(
						modifier = Modifier
							.width(25.dp)
							.height(14.dp)
							.clip(ContinuousCapsule)
							.shimmerLoading()
					)

					Column(
						modifier = Modifier
							.weight(1f),
						verticalArrangement = Arrangement.spacedBy(4.dp)
					) {
						Box(
							modifier = Modifier
								.fillMaxWidth(0.7f)
								.height(16.dp)
								.clip(ContinuousCapsule)
								.shimmerLoading()
						)
						Box(
							modifier = Modifier
								.fillMaxWidth(0.5f)
								.height(14.dp)
								.clip(ContinuousCapsule)
								.shimmerLoading()
						)
					}

					Box(
						modifier = Modifier
							.width(36.dp)
							.height(14.dp)
							.clip(ContinuousCapsule)
							.shimmerLoading()
					)
				}
			}
		}
	}
}


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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import dev.burnoo.compose.remembersetting.rememberBooleanSetting
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.ios_share
import org.jetbrains.compose.resources.vectorResource
import paige.navic.LocalMediaPlayer
import paige.navic.MediaPlayer
import paige.navic.ui.component.ErrorBox
import paige.navic.ui.component.Form
import paige.navic.ui.component.FormRow
import paige.navic.ui.component.dialog.ShareDialog
import paige.navic.ui.theme.mapleMono
import paige.navic.ui.viewmodel.TracksViewModel
import paige.navic.util.UiState
import paige.navic.util.shimmerLoading
import paige.subsonic.api.model.AnyTrack
import paige.subsonic.api.model.AnyTracks
import kotlin.time.Duration

private class TracksScreenScope(
	val player: MediaPlayer,
	val tracks: AnyTracks
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TracksScreen(
	partialTracks: Any,
	viewModel: TracksViewModel = viewModel(key = partialTracks.toString()) {
		TracksViewModel(partialTracks)
	}
) {
	val player = LocalMediaPlayer.current
	val scrollState = rememberScrollState()

	val tracks by viewModel.tracksState.collectAsState()
	val selectedTrack by viewModel.selectedTrack.collectAsState()

	var shareId by remember { mutableStateOf<String?>(null) }
	var shareExpiry by remember { mutableStateOf<Duration?>(null) }

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
								TrackRow(
									track = track,
									onClick = {
										player.play(tracks, index)
									},
									onLongClick = {
										viewModel.selectTrack(track)
									}
								)
							}
						}
					}
				}
			}
		}
	}

	selectedTrack?.let {
		ModalBottomSheet(
			onDismissRequest = { viewModel.clearSelection() }
		) {
			Form(modifier = Modifier.padding(14.dp)) {
				FormRow(
					horizontalArrangement = Arrangement.spacedBy(8.dp),
					onClick = { shareId = selectedTrack?.id },
				) {
					Icon(
						vectorResource(Res.drawable.ios_share),
						contentDescription = null
					)
					Text("Share")
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
	var roundCoverArt by rememberBooleanSetting("roundCoverArt", true)
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
				ContinuousRoundedRectangle(
					if (roundCoverArt) 16.dp else 0.dp
				)
			)
			.background(MaterialTheme.colorScheme.surfaceContainer)
	)
	Text(
		tracks.title ?: "???",
		style = MaterialTheme.typography.headlineMedium,
		textAlign = TextAlign.Center
	)
	Text(
		tracks.subtitle ?: "???",
		style = MaterialTheme.typography.bodySmall,
		textAlign = TextAlign.Center
	)
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
		) { Text("Play") }
		Button(
			modifier = Modifier.width(120.dp),
			onClick = {},
			colors = ButtonDefaults.buttonColors(
				containerColor = MaterialTheme.colorScheme.surfaceContainer,
				contentColor = MaterialTheme.colorScheme.onSurface
			),
			shape = ContinuousCapsule
		) { Text("Shuffle") }
	}
}

@Composable
private fun TracksScreenScope.TrackRow(
	track: AnyTrack,
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

		track.duration?.let {
			val minutes = it / 60
			val seconds = it % 60
			val formatted = minutes.toString().padStart(2, '0') + ":" + seconds.toString().padStart(2, '0')
			Text(
				formatted,
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


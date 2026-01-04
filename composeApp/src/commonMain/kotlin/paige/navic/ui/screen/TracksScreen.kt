package paige.navic.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
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
import paige.navic.ui.component.Form
import paige.navic.ui.component.FormRow
import paige.navic.ui.theme.mapleMono
import paige.navic.ui.viewmodel.TracksViewModel
import paige.subsonic.api.model.AnyTrack
import paige.subsonic.api.model.AnyTracks

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TracksScreen(
	viewModel: TracksViewModel = viewModel { TracksViewModel() },
	tracks: AnyTracks
) {
	val player = LocalMediaPlayer.current
	val clipboard = LocalClipboardManager.current
	val scrollState = rememberScrollState()
	var roundCoverArt by rememberBooleanSetting("roundCoverArt", true)
	val selectedTrack by viewModel.selectedtrack.collectAsState()
	val error by viewModel.error.collectAsState()
	Column(
		modifier = Modifier
			.background(MaterialTheme.colorScheme.surface)
			.verticalScroll(scrollState)
			.padding(12.dp)
			.padding(bottom = 200.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(10.dp)
	) {
		AsyncImage(
			model = tracks.coverArt,
			contentDescription = tracks.title,
			contentScale = ContentScale.Crop,
			modifier = Modifier
				.fillMaxWidth()
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
			for ((label, onClick) in mapOf("Play" to {
				player.play(tracks, 0)
			}, "Shuffle" to {})) {
				Button(
					modifier = Modifier.width(120.dp),
					onClick = onClick,
					colors = ButtonDefaults.buttonColors(
						containerColor = MaterialTheme.colorScheme.surfaceContainer,
						contentColor = MaterialTheme.colorScheme.onSurface
					),
					shape = ContinuousCapsule,
					content = { Text(label) }
				)
			}
		}
		Form {
			tracks.tracks.onEachIndexed { index, track ->
				TrackRow(index, tracks, track, viewModel)
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
					onClick = { viewModel.shareSelectedTrack(clipboard) },
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

	error?.let {
		AlertDialog(
			onDismissRequest = { viewModel.clearError() },
			title = { Text("Error") },
			text = { Text("$error") },
			confirmButton = {
				Button(
					shape = ContinuousCapsule,
					onClick = { viewModel.clearError() }
				) {
					Text("OK")
				}
			}
		)
	}
}

@Composable
fun TrackRow(
	index: Int,
	tracks: AnyTracks,
	track: AnyTrack,
	viewModel: TracksViewModel
) {
	val player = LocalMediaPlayer.current
	FormRow(
		onClick = {
			player.play(tracks, index)
		},
		onLongClick = {
			viewModel.selectTrack(track)
		},
		horizontalArrangement = Arrangement.spacedBy(12.dp),
		modifier = Modifier.fillMaxWidth()
	) {
		Text(
			"${index + 1}",
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

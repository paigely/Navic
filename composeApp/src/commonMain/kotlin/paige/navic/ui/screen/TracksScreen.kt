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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.kyant.capsule.ContinuousCapsule
import com.kyant.capsule.ContinuousRoundedRectangle
import dev.burnoo.compose.remembersetting.rememberBooleanSetting
import paige.navic.LocalMediaPlayer
import paige.navic.ui.component.Form
import paige.navic.ui.component.FormRow
import paige.navic.ui.theme.mapleMono
import paige.subsonic.api.model.AnyTrack
import paige.subsonic.api.model.AnyTracks

@Composable
fun TracksScreen(
	tracks: AnyTracks
) {
	val player = LocalMediaPlayer.current
	val scrollState = rememberScrollState()
	var roundCoverArt by rememberBooleanSetting("roundCoverArt", true)
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
		Text(tracks.title ?: "???", style = MaterialTheme.typography.headlineMedium)
		Text(tracks.subtitle ?: "???", style = MaterialTheme.typography.bodySmall)
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
				TrackRow(index, tracks, track)
			}
		}
	}
}

@Composable
fun TrackRow(
	index: Int,
	tracks: AnyTracks,
	track: AnyTrack
) {
	val player = LocalMediaPlayer.current
	FormRow(
		onClick = {
			player.play(tracks, index)
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

package paige.navic.ui.screens.track.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.capsule.ContinuousCapsule
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_play
import navic.composeapp.generated.resources.action_shuffle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.domain.models.DomainSongCollection
import paige.navic.icons.Icons
import paige.navic.icons.filled.Play
import paige.navic.icons.outlined.Shuffle
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.ui.theme.defaultFont

@Composable
fun TracksScreenHeadingRowButtons(
	tracks: DomainSongCollection
) {
	val player = koinViewModel<MediaPlayerViewModel>()
	Row(
		modifier = Modifier.padding(horizontal = 31.dp, vertical = 10.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(
			10.dp,
			alignment = Alignment.CenterHorizontally
		)
	) {
		val shape = ContinuousCapsule
		Button(
			modifier = Modifier.weight(1f),
			onClick = {
				player.clearQueue()
				player.addToQueue(tracks)
				player.playAt(0)
			},
			shape = shape
		) {
			Icon(
				Icons.Filled.Play,
				null,
				modifier = Modifier.size(24.dp).padding(end = 4.dp)
			)
			Text(
				stringResource(Res.string.action_play),
				maxLines = 1,
				autoSize = TextAutoSize.StepBased(
					minFontSize = 1.sp,
					maxFontSize = MaterialTheme.typography.labelLarge.fontSize
				),
				fontFamily = defaultFont(grade = 100)
			)
		}
		OutlinedButton(
			modifier = Modifier.weight(1f),
			onClick = {
				player.shufflePlay(tracks)
			},
			shape = shape
		) {
			Icon(
				Icons.Outlined.Shuffle,
				null,
				modifier = Modifier.size(22.dp).padding(end = 6.dp)
			)
			Text(
				stringResource(Res.string.action_shuffle),
				maxLines = 1,
				autoSize = TextAutoSize.StepBased(
					minFontSize = 1.sp,
					maxFontSize = MaterialTheme.typography.labelLarge.fontSize
				),
				fontFamily = defaultFont(grade = 100)
			)
		}
	}
}

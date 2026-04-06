package paige.navic.ui.screens.track.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.count_songs
import org.jetbrains.compose.resources.pluralStringResource
import paige.navic.domain.models.DomainSongCollection
import paige.navic.ui.theme.defaultFont

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TracksScreenFooterRow(
	tracks: DomainSongCollection
) {
	Text(
		buildString {
			append(pluralStringResource(
				Res.plurals.count_songs,
				tracks.songCount,
				tracks.songCount
			))
			append(" • ")
			append(tracks.duration.toString())
		},
		style = MaterialTheme.typography.titleSmall,
		fontFamily = defaultFont(round = 100f),
		color = MaterialTheme.colorScheme.onSurfaceVariant,
		modifier = Modifier.fillMaxWidth().padding(
			horizontal = 16.dp,
			vertical = 8.dp
		)
	)
}
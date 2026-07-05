package paige.navic.ui.screens.lyrics.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_refresh
import navic.composeapp.generated.resources.info_no_lyrics
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalPlatformContext
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Lyrics
import paige.navic.ui.components.common.ContentUnavailable

@Composable
fun LyricsScreenPlaceholder(
	onRefresh: () -> Unit
) {
	val platformContext = LocalPlatformContext.current

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
	) {
		ContentUnavailable(
			modifier = Modifier,
			icon = Icons.Outlined.Lyrics,
			label = stringResource(Res.string.info_no_lyrics)
		)

		TextButton(onClick = dropUnlessResumed {
			platformContext.clickSound()
			onRefresh()
		}) {
			Text(stringResource(Res.string.action_refresh))
		}
	}
}

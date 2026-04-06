package paige.navic.ui.screens.library.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.data.models.Screen
import paige.navic.domain.models.DomainPlaylist
import paige.navic.ui.components.common.CoverArt
import paige.navic.ui.theme.defaultFont

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun LazyGridScope.libraryScreenHomePlaylistButton(
	playlist: DomainPlaylist,
	start: Boolean,
	fullWidth: Boolean = false
) {
	item(span = { GridItemSpan(if (fullWidth) 2 else 1) }) {
		val ctx = LocalCtx.current
		val backStack = LocalNavStack.current
		Button(
			modifier = Modifier
				.fillMaxWidth()
				.height(42.dp)
				.padding(
					start = if (start || fullWidth) 16.dp else 0.dp,
					end = if (!start || fullWidth) 16.dp else 0.dp,
				),
			contentPadding = PaddingValues(horizontal = 12.dp),
			elevation = null,
			shapes = ButtonDefaults.shapes(
				shape = MaterialTheme.shapes.small,
				pressedShape = MaterialTheme.shapes.extraSmall
			),
			colors = ButtonDefaults.buttonColors(
				containerColor = MaterialTheme.colorScheme.surfaceContainer,
				contentColor = MaterialTheme.colorScheme.onSurfaceVariant
			),
			onClick = {
				ctx.clickSound()
				backStack.add(Screen.TrackList(playlist, "library"))
			}
		) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically
			) {
				CoverArt(
					coverArtId = playlist.coverArtId,
					modifier = Modifier.size(24.dp),
					shape = MaterialTheme.shapes.extraSmall
				)
				Spacer(Modifier.width(10.dp))
				Text(
					playlist.name,
					maxLines = 1,
					fontFamily = defaultFont(100, round = 100f),
					autoSize = TextAutoSize.StepBased(minFontSize = 1.sp, maxFontSize = 14.sp),
				)
			}
		}
	}
}

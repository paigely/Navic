package paige.navic.ui.screens.artist.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_more
import org.jetbrains.compose.resources.stringResource
import paige.navic.ui.components.common.CoverArt
import paige.navic.ui.components.common.MarqueeText
import paige.navic.ui.screens.artist.truncateText

@Composable
fun ArtistDetailScreenHeading(
	artistName: String,
	coverArtId: String?,
	subtitle: String?,
	lastfm: String?,
	innerPadding: PaddingValues,
	scrolled: Boolean
) {
	val layoutDirection = LocalLayoutDirection.current
	val progress by animateFloatAsState(if (scrolled) 0f else 1f)
	BoxWithConstraints(
		modifier = Modifier.fillMaxWidth()
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.height((400.dp / (maxWidth / 300.dp)) + innerPadding.calculateTopPadding())
				.background(MaterialTheme.colorScheme.surfaceContainer)
		) {
			CoverArt(
				coverArtId = coverArtId,
				modifier = Modifier.fillMaxSize(),
				shape = RectangleShape,
				square = false
			)
			Box(
				modifier = Modifier
					.fillMaxSize()
					.background(
						Brush.linearGradient(
							0.025f to MaterialTheme.colorScheme.background,
							1.0f to Color.Transparent,
							start = Offset(0f, Float.POSITIVE_INFINITY),
							end = Offset(0f, 0f)
						)
					)
			)

			Column(
				modifier = Modifier
					.align(Alignment.BottomStart)
					.padding(horizontal = 20.dp)
					.padding(start = innerPadding.calculateStartPadding(layoutDirection))
					.padding(end = innerPadding.calculateEndPadding(layoutDirection)),
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				subtitle?.let { subtitle ->
					Text(
						text = buildAnnotatedString {
							append(truncateText(subtitle, 200))
							if (subtitle.length > 200 && lastfm != null) {
								append(" ")
								withLink(LinkAnnotation.Url(lastfm)) {
									append(stringResource(Res.string.action_more))
								}
							}
						},
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurface,
						modifier = Modifier.widthIn(max = 500.dp)
					)
				}
				MarqueeText(
					text = artistName,
					style = MaterialTheme.typography.displaySmall.copy(
						fontWeight = FontWeight.Bold,
					),
					modifier = Modifier
						.fillMaxWidth()
						.alpha(progress)
						.scale(progress)
				)
			}
		}
	}
}

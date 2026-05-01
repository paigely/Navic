package paige.navic.ui.screens.collection.components

import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_unknown_genre
import navic.composeapp.generated.resources.info_unknown_year
import navic.composeapp.generated.resources.subtitle_playlist
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.LocalSharedTransitionScope
import paige.navic.data.models.Screen
import paige.navic.domain.models.DomainAlbum
import paige.navic.domain.models.DomainPlaylist
import paige.navic.domain.models.DomainSongCollection
import paige.navic.ui.components.common.CoverArt
import paige.navic.ui.theme.defaultFont
import paige.navic.utils.EmphasizedDecelerateEasing

@Composable
fun CollectionDetailScreenHeadingRow(
	collection: DomainSongCollection,
	tab: String,
	titleAlpha: Float
) {
	val ctx = LocalCtx.current
	val backStack = LocalNavStack.current
	with(LocalSharedTransitionScope.current) {
		CoverArt(
			coverArtId = collection.coverArtId,
			contentDescription = collection.name,
			modifier = Modifier
				.widthIn(0.dp, 420.dp)
				.padding(horizontal = 64.dp)
				.aspectRatio(1f)
				.sharedElement(
					sharedContentState = this@with.rememberSharedContentState("${tab}-${collection.id}-cover"),
					boundsTransform = BoundsTransform { _, _ ->
						tween(
							durationMillis = 500,
							easing = EmphasizedDecelerateEasing
						)
					},
					animatedVisibilityScope = LocalNavAnimatedContentScope.current
				)
				.alpha(titleAlpha),
			crossfadeMs = 0
		)
		Spacer(Modifier.height(10.dp))
		Column(
			modifier = Modifier
				.padding(horizontal = 31.dp)
				.alpha(titleAlpha),
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			Text(
				collection.name,
				style = MaterialTheme.typography.headlineSmall,
				textAlign = TextAlign.Center,
				modifier = Modifier
			)
			val subtitle = when (collection) {
				is DomainAlbum -> collection.artistName
				is DomainPlaylist -> collection.comment
			}
			subtitle?.let { subtitle ->
				Text(
					subtitle,
					color = MaterialTheme.colorScheme.primary,
					modifier = Modifier.clickable(collection is DomainAlbum, onClick = dropUnlessResumed {
						ctx.clickSound()
						(collection as? DomainAlbum)?.artistId?.let { id ->
							backStack.add(Screen.ArtistDetail(id))
						}
					}),
					style = MaterialTheme.typography.bodyMedium,
					fontFamily = defaultFont(grade = 100, round = 100f)
				)
			}
			Text(
				if (collection is DomainAlbum)
					"${collection.genre ?: stringResource(Res.string.info_unknown_genre)} • ${
						collection.year ?: stringResource(
							Res.string.info_unknown_year
						)
					}"
				else stringResource(Res.string.subtitle_playlist),
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				style = MaterialTheme.typography.bodySmall,
				fontFamily = defaultFont(grade = 100, round = 100f)
			)
		}
	}
}

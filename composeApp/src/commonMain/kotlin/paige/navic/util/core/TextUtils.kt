package paige.navic.util.core

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.launch
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_explicit
import navic.composeapp.generated.resources.info_external_song
import navic.composeapp.generated.resources.info_external_song_description
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalSnackBarState
import paige.navic.domain.models.DomainSong
import paige.navic.domain.models.DomainSongArtist
import paige.navic.icons.Icons
import paige.navic.icons.filled.Explicit
import paige.navic.ui.theme.warning

val InlineExplicitIcon = persistentMapOf(
	"InlineExplicitIcon" to InlineTextContent(
		Placeholder(
			width = 16.sp,
			height = 16.sp,
			placeholderVerticalAlign = PlaceholderVerticalAlign.Center
		)
	) {
		Icon(
			imageVector = Icons.Filled.Explicit,
			contentDescription = stringResource(Res.string.info_explicit)
		)
	}
)

val InlineExplicitIconLarge = persistentMapOf(
	"InlineExplicitIcon" to InlineTextContent(
		Placeholder(
			width = 20.sp,
			height = 20.sp,
			placeholderVerticalAlign = PlaceholderVerticalAlign.Center
		)
	) {
		Icon(
			imageVector = Icons.Filled.Explicit,
			contentDescription = stringResource(Res.string.info_explicit)
		)
	}
)

fun AnnotatedString.Builder.appendBulletPoint()
	= append(" • ")

fun AnnotatedString.Builder.appendArtists(
	artists: List<DomainSongArtist>,
	onClick: (artistId: String) -> Unit
) {
	val listener = LinkInteractionListener { annotation ->
		val artistId = (annotation as LinkAnnotation.Clickable).tag
		onClick(artistId)
	}
	artists.forEachIndexed { index, artist ->
		withLink(
			link = LinkAnnotation.Clickable(
				linkInteractionListener = listener,
				tag = artist.id,
				styles = TextLinkStyles()
			)
		) {
			append(artist.name)
		}
		if (index != artists.lastIndex) {
			append(", ")
		}
	}
}

@Composable
fun buildSongInfoString(
	song: DomainSong,
	onClickArtist: (artistId: String) -> Unit,
	showExternal: Boolean = true,
	showAlbum: Boolean = true,
	showYear: Boolean = true
): AnnotatedString {
	val snackBarState = LocalSnackBarState.current
	val extSnackBarText = stringResource(Res.string.info_external_song_description)
	val scope = rememberCoroutineScope()

	return buildAnnotatedString {
		if ((song.isExternal || song.id.startsWith("ext-")) && showExternal) {
			withLink(
				link = LinkAnnotation.Clickable(
					linkInteractionListener = LinkInteractionListener {
						scope.launch {
							snackBarState.currentSnackbarData?.dismiss()
							snackBarState.showSnackbar(extSnackBarText)
						}
					},
					tag = "external",
					styles = TextLinkStyles(
						style = SpanStyle(
							color = MaterialTheme.colorScheme.warning,
							textDecoration = TextDecoration.Underline
						)
					)
				)
			) {
				append(stringResource(Res.string.info_external_song))
			}
			appendBulletPoint()
		}

		if (showAlbum) {
			append("${song.albumTitle}")
			appendBulletPoint()
		}

		if (showYear && song.year != null) {
			append("${song.year}")
			appendBulletPoint()
		}

		appendArtists(
			artists = song.artists,
			onClick = onClickArtist
		)
	}
}

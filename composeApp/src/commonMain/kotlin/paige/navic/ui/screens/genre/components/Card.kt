package paige.navic.ui.screens.genre.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.materialkolor.rememberDynamicColorScheme
import dev.zt64.compose.pipette.HsvColor
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.count_albums
import org.jetbrains.compose.resources.pluralStringResource
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.data.models.Screen
import paige.navic.data.models.settings.Settings
import paige.navic.data.models.settings.enums.ThemeMode
import paige.navic.domain.models.DomainAlbumListType
import paige.navic.domain.models.DomainGenre
import paige.navic.ui.components.common.CoverArt
import paige.navic.ui.theme.defaultFont
import kotlin.math.abs

@Composable
fun GenreListScreenCard(
	modifier: Modifier = Modifier,
	genre: DomainGenre
) {
	val ctx = LocalCtx.current
	val backStack = LocalNavStack.current
	val inDarkTheme = isSystemInDarkTheme()
	val isDark = remember(Settings.shared.themeMode) {
		when (Settings.shared.themeMode) {
			ThemeMode.System -> inDarkTheme
			ThemeMode.Dark -> true
			ThemeMode.Light -> false
		}
	}
	val seedColor = remember(genre.name) {
		HsvColor(
			hue = abs(genre.name.hashCode() % 360).toFloat(),
			saturation = 0.6f,
			value = 0.5f
		).toColor()
	}
	val colorScheme = rememberDynamicColorScheme(
		seedColor = seedColor,
		isDark = isDark
	)
	val firstAlbumCoverArt = genre.albums.firstOrNull()?.coverArtId
	val secondAlbumCoverArt = genre.albums.getOrNull(1)?.coverArtId
	Surface(
		modifier = modifier,
		color = colorScheme.primary,
		contentColor = colorScheme.onPrimary,
		shape = MaterialTheme.shapes.medium,
		shadowElevation = 2.dp,
		onClick = dropUnlessResumed {
			ctx.clickSound()
			backStack.add(
				Screen.AlbumList(
					nested = true,
					listType = DomainAlbumListType.ByGenre(genre.name)
				)
			)
		}
	) {
		Box {
			Box(Modifier.align(Alignment.CenterEnd)) {
				if (firstAlbumCoverArt != null) {
					CoverArt(
						coverArtId = firstAlbumCoverArt,
						modifier = Modifier
							.padding(top = 10.dp)
							.rotate(10f)
							.size(90.dp)
							.offset(x = 5.dp, y = 5.dp),
						shape = MaterialTheme.shapes.medium,
						shadowElevation = 3.dp
					)
					if (secondAlbumCoverArt != null) {
						CoverArt(
							coverArtId = secondAlbumCoverArt,
							modifier = Modifier
								.padding(top = 13.dp)
								.rotate(25f)
								.size(90.dp)
								.offset(x = 25.dp, y = 15.dp),
							shape = MaterialTheme.shapes.medium,
							shadowElevation = 10.dp
						)
					}
				}
			}
			Column(
				modifier = Modifier.fillMaxWidth().padding(
					start = 8.dp,
					end = 85.dp,
					top = 10.dp,
					bottom = 10.dp
				).align(Alignment.TopStart)
			) {
				Text(
					genre.name,
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight(600),
					fontFamily = defaultFont(round = 100f),
					overflow = TextOverflow.Ellipsis,
					maxLines = 2
				)
				Text(
					pluralStringResource(
						Res.plurals.count_albums,
						genre.albums.count(),
						genre.albums.count()
					),
					style = MaterialTheme.typography.titleSmall,
				)
			}
		}
	}
}

package paige.navic.ui.screens.genre.components

import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.Modifier
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_no_genres
import org.jetbrains.compose.resources.stringResource
import paige.navic.domain.models.DomainGenre
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Genre
import paige.navic.ui.components.common.ContentUnavailable
import paige.navic.utils.UiState

fun LazyGridScope.genreListScreenContent(
	state: UiState<List<DomainGenre>>
) {
	val data = state.data.orEmpty()
	if (data.isNotEmpty()) {
		items(data, { it.name }) { genre ->
			GenreListScreenCard(
				modifier = Modifier.animateItem(),
				genre = genre
			)
		}
	} else {
		when (state) {
			is UiState.Loading -> items(10) {
				GenreListScreenCardPlaceholder()
			}

			else -> {
				item(span = { GridItemSpan(maxLineSpan) }) {
					ContentUnavailable(
						icon = Icons.Outlined.Genre,
						label = stringResource(Res.string.info_no_genres)
					)
				}
			}
		}
	}
}
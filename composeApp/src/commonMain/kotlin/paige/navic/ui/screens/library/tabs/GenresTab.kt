package paige.navic.ui.screens.library.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import kotlinx.collections.immutable.ImmutableList
import paige.navic.domain.models.DomainGenre
import paige.navic.ui.components.layouts.ArtGrid
import paige.navic.ui.screens.genre.components.genreListScreenContent
import paige.navic.utils.UiState
import paige.navic.utils.withoutTop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreenGenresTab(
	scrollBehavior: TopAppBarScrollBehavior,
	innerPadding: PaddingValues,
	genresState: UiState<ImmutableList<DomainGenre>>
) {
	Column(
		modifier = Modifier
			.nestedScroll(scrollBehavior.nestedScrollConnection)
			.fillMaxSize()
	) {
		ArtGrid(
			modifier = Modifier.weight(1f),
			contentPadding = innerPadding.withoutTop()
		) {
			genreListScreenContent(state = genresState)
		}
	}
}

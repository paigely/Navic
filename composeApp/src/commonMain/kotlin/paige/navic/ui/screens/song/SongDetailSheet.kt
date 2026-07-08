package paige.navic.ui.screens.song

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.title_track_info
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import paige.navic.ui.components.common.Form
import paige.navic.ui.screens.song.components.SongDetailScreenInfoRow
import paige.navic.ui.screens.song.viewmodels.SongDetailViewModel
import paige.navic.ui.theme.NavicTheme
import paige.navic.util.ui.rememberColorSchemeFromCoverArt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SongDetailSheet(
	songId: String,
	initialCoverArtId: String? = null,
	useSongTheme: Boolean = true
) {
	val viewModel = koinViewModel<SongDetailViewModel>(
		key = songId,
		parameters = { parametersOf(songId) }
	)

	val songState by viewModel.songState.collectAsStateWithLifecycle()
	val song = songState.data
	val info by viewModel.info.collectAsStateWithLifecycle()

	val activeCoverArtId = if (initialCoverArtId.isNullOrEmpty()) song?.coverArtId else initialCoverArtId
	val colorScheme = if (useSongTheme) rememberColorSchemeFromCoverArt(activeCoverArtId) else null

	NavicTheme(colorScheme) {
		Surface(
			modifier = Modifier.fillMaxSize(),
			color = MaterialTheme.colorScheme.surface
		) {
			Column(
				modifier = Modifier
					.fillMaxSize()
					.verticalScroll(rememberScrollState())
					.padding(horizontal = 16.dp)
			) {
				Text(
					text = stringResource(Res.string.title_track_info),
					style = MaterialTheme.typography.titleLarge,
					fontWeight = FontWeight.SemiBold,
					color = MaterialTheme.colorScheme.onSurface,
					modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)
				)

				Form(bottomPadding = 0.dp) {
					info.forEach { (key, value) ->
						SongDetailScreenInfoRow(key, value)
					}
				}

				Spacer(
					Modifier.padding(
						WindowInsets.systemBars
							.only(WindowInsetsSides.Bottom)
							.asPaddingValues()
					)
				)
			}
		}
	}
}

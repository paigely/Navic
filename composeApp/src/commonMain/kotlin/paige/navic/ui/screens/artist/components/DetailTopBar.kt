package paige.navic.ui.screens.artist.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalUriHandler
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_more
import navic.composeapp.generated.resources.action_view_on_lastfm
import navic.composeapp.generated.resources.action_view_on_musicbrainz
import org.jetbrains.compose.resources.stringResource
import paige.navic.icons.Icons
import paige.navic.icons.brand.Lastfm
import paige.navic.icons.brand.Musicbrainz
import paige.navic.icons.outlined.MoreVert
import paige.navic.ui.components.common.Dropdown
import paige.navic.ui.components.common.DropdownItem
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.components.layouts.TopBarButton
import paige.navic.ui.screens.artist.viewmodels.ArtistState
import paige.navic.utils.UiState

@Composable
fun ArtistDetailScreenTopBar(
	scrolled: Boolean,
	artistState: UiState<ArtistState>
) {
	val uriHandler = LocalUriHandler.current
	val state = (artistState as? UiState.Success)?.data
	val alpha by animateFloatAsState(
		if (scrolled) 1f else 0f
	)
	if (state != null) {
		NestedTopBar(
			colors = TopAppBarDefaults.topAppBarColors(
				containerColor = MaterialTheme.colorScheme.surface.copy(alpha = alpha)
			),
			title = {
				AnimatedVisibility(
					scrolled,
					enter = scaleIn() + fadeIn(),
					exit = scaleOut() + fadeOut()
				) {
					Text(state.artist.name)
				}
			},
			actions = {
				Box {
					var expanded by remember { mutableStateOf(false) }
					TopBarButton({
						expanded = true
					}) {
						Icon(
							Icons.Outlined.MoreVert,
							stringResource(Res.string.action_more)
						)
					}
					Dropdown(
						expanded = expanded,
						onDismissRequest = { expanded = false }
					) {
						DropdownItem(
							text = { Text(stringResource(Res.string.action_view_on_lastfm)) },
							leadingIcon = { Icon(Icons.Brand.Lastfm, null) },
							enabled = state.artist.lastFmUrl != null,
							onClick = {
								expanded = false
								state.artist.lastFmUrl?.let { url ->
									uriHandler.openUri(url)
								}
							}
						)
						DropdownItem(
							text = { Text(stringResource(Res.string.action_view_on_musicbrainz)) },
							leadingIcon = { Icon(Icons.Brand.Musicbrainz, null) },
							enabled = state.artist.musicBrainzId != null,
							onClick = {
								expanded = false
								state.artist.musicBrainzId?.let { id ->
									uriHandler.openUri(
										"https://musicbrainz.org/artist/$id"
									)
								}
							}
						)
					}
				}
			}
		)
	} else {
		NestedTopBar({})
	}
}

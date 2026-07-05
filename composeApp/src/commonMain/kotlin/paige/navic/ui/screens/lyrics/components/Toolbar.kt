package paige.navic.ui.screens.lyrics.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.collections.immutable.ImmutableList
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_navigate_back
import navic.composeapp.generated.resources.action_share_lyrics
import navic.composeapp.generated.resources.count_lines
import navic.composeapp.generated.resources.title_select_lyrics
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import paige.navic.icons.Icons
import paige.navic.icons.outlined.ArrowBack
import paige.navic.icons.outlined.Check
import paige.navic.icons.outlined.KeyboardArrowDown
import paige.navic.icons.outlined.Share
import paige.navic.ui.components.layouts.TopBarButton
import paige.navic.ui.components.toolbars.SheetToolbar

@Composable
fun LyricsScreenToolbar(
	onDismissRequest: () -> Unit,
	onShare: () -> Unit,
	isSelecting: Boolean,
	toggleIsSelecting: () -> Unit,
	windowInsets: WindowInsets,
	selectedIndices: ImmutableList<Int>
) {
	SheetToolbar(
		windowInsets = windowInsets,
		navigationIcon = {
			TopBarButton(
				onClick = dropUnlessResumed {
					if (!isSelecting) {
						onDismissRequest()
					} else {
						toggleIsSelecting()
					}
				},
				content = {
					Icon(
						imageVector = if (!isSelecting)
							Icons.Outlined.KeyboardArrowDown
						else Icons.Outlined.ArrowBack,
						contentDescription = stringResource(Res.string.action_navigate_back)
					)
				}
			)
			NavigationBackHandler(
				state = rememberNavigationEventState(NavigationEventInfo.None),
				isBackEnabled = isSelecting,
				onBackCompleted = toggleIsSelecting
			)
		},
		actions = {
			TopBarButton(
				enabled = !isSelecting || selectedIndices.isNotEmpty(),
				onClick = {
					if (isSelecting) {
						onShare()
					} else {
						toggleIsSelecting()
					}
				}
			) {
				Icon(
					imageVector = if (!isSelecting)
						Icons.Outlined.Share
					else Icons.Outlined.Check,
					contentDescription = stringResource(Res.string.action_share_lyrics),
					modifier = Modifier.size(26.dp)
				)
			}
		},
		title = {
			if (isSelecting) {
				Column {
					Text(
						stringResource(Res.string.title_select_lyrics),
						fontWeight = FontWeight.SemiBold
					)
					Text(
						pluralStringResource(
							Res.plurals.count_lines,
							selectedIndices.count(),
							selectedIndices.count()
						)
					)
				}
			}
		}
	)
}

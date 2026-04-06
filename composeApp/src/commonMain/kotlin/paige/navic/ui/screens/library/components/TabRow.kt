package paige.navic.ui.screens.library.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import paige.navic.ui.screens.library.LibraryScreenTab

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LibraryScreenTabRow(
	selectedTab: LibraryScreenTab,
	onSelectTab: (LibraryScreenTab) -> Unit
) {
	PrimaryScrollableTabRow(
		selectedTabIndex = selectedTab.ordinal,
		divider = {},
		edgePadding = 0.dp
	) {
		LibraryScreenTab.entries.forEach { tab ->
			Tab(
				selected = selectedTab == tab,
				onClick = { onSelectTab(tab) },
				content = {
					Text(
						stringResource(tab.title),
						modifier = Modifier.padding(8.dp),
						color = if (selectedTab == tab)
							MaterialTheme.colorScheme.primary
						else MaterialTheme.colorScheme.onSurfaceVariant,
						style = MaterialTheme.typography.labelLargeEmphasized,
						fontWeight = FontWeight(if (selectedTab == tab) 600 else 500)
					)
				}
			)
		}
	}
}

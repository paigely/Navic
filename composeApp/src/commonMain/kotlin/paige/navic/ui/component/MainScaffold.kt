package paige.navic.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
	backStack: SnapshotStateList<Any>,
	topBar: @Composable (SnapshotStateList<Any>) -> Unit,
	content: @Composable (PaddingValues) -> Unit,
) {
	val focusManager = LocalFocusManager.current

	Scaffold(
		modifier = Modifier
			.clickable(
				indication = null,
				interactionSource = remember { MutableInteractionSource() }
			) {
				focusManager.clearFocus()
			},
		topBar = { topBar(backStack) },
		content = content
	)
}

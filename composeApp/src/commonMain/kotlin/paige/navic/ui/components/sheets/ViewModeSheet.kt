package paige.navic.ui.components.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.option_list_view_mode
import org.jetbrains.compose.resources.stringResource
import paige.navic.domain.models.settings.ListViewMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewModeSheet(
	onDismissRequest: () -> Unit,
	selectedViewMode: ListViewMode,
	onSetViewMode: (ListViewMode) -> Unit
) {
	ModalBottomSheet(
		onDismissRequest = onDismissRequest,
		sheetState = rememberBottomSheetState(
			initialValue = SheetValue.Hidden,
			enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
		)
	) {
		Column(
			modifier = Modifier.verticalScroll(rememberScrollState()),
			verticalArrangement = Arrangement.spacedBy(8.dp)
		) {
			Text(
				text = stringResource(Res.string.option_list_view_mode),
				style = MaterialTheme.typography.titleMedium,
				modifier = Modifier.padding(horizontal = 16.dp)
			)

			SingleChoiceSegmentedButtonRow(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 16.dp)
			) {
				ListViewMode.entries.forEachIndexed { index, viewMode ->
					SegmentedButton(
						shape = SegmentedButtonDefaults.itemShape(
							index = index,
							count = ListViewMode.entries.count()
						),
						onClick = { onSetViewMode(viewMode) },
						selected = selectedViewMode == viewMode,
						label = { Text(stringResource(viewMode.displayName)) },
						icon = {
							Icon(
								imageVector = viewMode.icon,
								contentDescription = null
							)
						}
					)
				}
			}
		}
	}
}

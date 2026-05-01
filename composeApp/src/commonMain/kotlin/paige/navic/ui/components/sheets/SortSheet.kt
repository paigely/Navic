package paige.navic.ui.components.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.option_sort_ascending
import navic.composeapp.generated.resources.option_sort_descending
import navic.composeapp.generated.resources.title_direction
import navic.composeapp.generated.resources.title_sort_by
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SortSheet(
	entries: ImmutableList<T>,
	selectedSorting: T,
	selectedReversed: Boolean,
	label: @Composable (T) -> String,
	onSetSorting: (T) -> Unit,
	onSetReversed: (Boolean) -> Unit,
	onDismissRequest: () -> Unit
) {
	val ctx = LocalCtx.current
	ModalBottomSheet(
		onDismissRequest = onDismissRequest,
		sheetState = rememberModalBottomSheetState(true)
	) {
		Column(
			verticalArrangement = Arrangement.spacedBy(8.dp)
		) {
			Text(
				text = stringResource(Res.string.title_sort_by),
				style = MaterialTheme.typography.titleLarge,
				modifier = Modifier.padding(horizontal = 16.dp)
			)

			Column(Modifier.selectableGroup()) {
				entries.forEach { sorting ->
					Row(
						Modifier
							.padding(horizontal = 16.dp)
							.fillMaxWidth()
							.height(56.dp)
							.selectable(
								selected = (sorting == selectedSorting),
								onClick = {
									ctx.clickSound()
									onSetSorting(sorting)
								},
								role = Role.RadioButton
							),
						verticalAlignment = Alignment.CenterVertically
					) {
						RadioButton(
							selected = (sorting == selectedSorting),
							onClick = null
						)
						Text(
							text = label(sorting),
							style = MaterialTheme.typography.bodyLarge,
							modifier = Modifier.padding(start = 16.dp)
						)
					}
				}
			}

			Text(
				text = stringResource(Res.string.title_direction),
				style = MaterialTheme.typography.titleMedium,
				modifier = Modifier.padding(horizontal = 16.dp)
			)

			SingleChoiceSegmentedButtonRow(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 16.dp)
			) {
				SegmentedButton(
					shape = SegmentedButtonDefaults.itemShape(
						index = 0,
						count = 2
					),
					onClick = {
						ctx.clickSound()
						onSetReversed(false)
					},
					selected = !selectedReversed,
					label = { Text(stringResource(Res.string.option_sort_ascending)) }
				)
				SegmentedButton(
					shape = SegmentedButtonDefaults.itemShape(
						index = 1,
						count = 2
					),
					onClick = {
						ctx.clickSound()
						onSetReversed(true)
					},
					selected = selectedReversed,
					label = { Text(stringResource(Res.string.option_sort_descending)) }
				)
			}
		}
	}
}

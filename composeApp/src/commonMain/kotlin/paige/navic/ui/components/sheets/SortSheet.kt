package paige.navic.ui.components.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.option_list_view_mode
import navic.composeapp.generated.resources.option_sort_ascending
import navic.composeapp.generated.resources.option_sort_descending
import navic.composeapp.generated.resources.option_sort_downloaded
import navic.composeapp.generated.resources.option_sort_starred
import navic.composeapp.generated.resources.title_direction
import navic.composeapp.generated.resources.title_sort_by
import org.jetbrains.compose.resources.stringResource
import paige.navic.domain.models.DomainFilter
import paige.navic.domain.models.settings.ListViewMode
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Download
import paige.navic.icons.outlined.ListArrow
import paige.navic.icons.outlined.Star

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SortSheet(
	entries: ImmutableList<T>,
	label: @Composable (T) -> String,
	selectedSorting: T,
	onSetSorting: (T) -> Unit,
	selectedReversed: Boolean,
	onSetReversed: (Boolean) -> Unit,
	selectedViewMode: ListViewMode? = null,
	onSetViewMode: ((ListViewMode) -> Unit)? = null,
	selectedFilters: Set<DomainFilter> = emptySet(),
	onToggleFilter: ((DomainFilter) -> Unit)? = null,
	onDismissRequest: () -> Unit
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

			if (onToggleFilter != null) {
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 16.dp),
					horizontalArrangement = Arrangement.spacedBy(8.dp)
				) {
					DomainFilter.entries.forEach { filter ->
						FilterChip(
							selected = selectedFilters.contains(filter),
							onClick = { onToggleFilter(filter) },
							label = {
								Text(
									stringResource(
										when (filter) {
											DomainFilter.Starred -> Res.string.option_sort_starred
											DomainFilter.Downloaded -> Res.string.option_sort_downloaded
										}
									)
								)
							},
							leadingIcon = {
								Icon(
									imageVector = when (filter) {
										DomainFilter.Starred -> Icons.Outlined.Star
										DomainFilter.Downloaded -> Icons.Outlined.Download
									},
									contentDescription = null
								)
							}
						)
					}
				}
			}

			if (selectedViewMode != null && onSetViewMode != null) {
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
						onSetReversed(false)
					},
					selected = !selectedReversed,
					label = { Text(stringResource(Res.string.option_sort_ascending)) },
					icon = {
						Icon(
							imageVector = Icons.Outlined.ListArrow,
							contentDescription = null,
							modifier = Modifier.rotate(180f)
						)
					}
				)
				SegmentedButton(
					shape = SegmentedButtonDefaults.itemShape(
						index = 1,
						count = 2
					),
					onClick = {
						onSetReversed(true)
					},
					selected = selectedReversed,
					label = { Text(stringResource(Res.string.option_sort_descending)) },
					icon = {
						Icon(
							imageVector = Icons.Outlined.ListArrow,
							contentDescription = null
						)
					}
				)
			}
		}
	}
}

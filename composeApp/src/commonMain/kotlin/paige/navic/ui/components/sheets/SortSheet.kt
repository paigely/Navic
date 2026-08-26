package paige.navic.ui.components.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
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
import paige.navic.util.ui.segmentedShapes

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
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
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 24.dp)
				.verticalScroll(rememberScrollState())
				.selectableGroup(),
			verticalArrangement = Arrangement.spacedBy(2.dp)
		) {
			Text(
				text = stringResource(Res.string.title_sort_by),
				style = MaterialTheme.typography.headlineMedium,
				fontWeight = FontWeight.Bold,
				modifier = Modifier.padding(start = 2.dp, bottom = 16.dp)
			)

			Column(
				modifier = Modifier.selectableGroup(),
				verticalArrangement = Arrangement.spacedBy(2.dp)
			) {
				entries.forEachIndexed { index, sorting ->
					val isSelected = sorting == selectedSorting
					val containerColor = if (isSelected) {
						MaterialTheme.colorScheme.secondaryContainer
					} else {
						MaterialTheme.colorScheme.surfaceContainerLowest
					}

					SegmentedListItem(
						selected = isSelected,
						onClick = { onSetSorting(sorting) },
						shapes = segmentedShapes(index = index, count = entries.size),
						colors = ListItemDefaults.colors(
							containerColor = containerColor,
							selectedContainerColor = containerColor,
						),
						content = {
							Text(
								text = label(sorting),
								style = MaterialTheme.typography.bodyLarge,
								color = if (isSelected) {
									MaterialTheme.colorScheme.onSecondaryContainer
								} else {
									MaterialTheme.colorScheme.onSurface
								}
							)
						},
						trailingContent = {
							RadioButton(
								selected = isSelected,
								onClick = null
							)
						},
						contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
					)
				}
			}

			if (onToggleFilter != null) {
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(top = 8.dp),
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
					modifier = Modifier.padding(top = 8.dp)
				)

				SingleChoiceSegmentedButtonRow(
					modifier = Modifier.fillMaxWidth()
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
				modifier = Modifier.padding(top = 8.dp)
			)

			SingleChoiceSegmentedButtonRow(
				modifier = Modifier.fillMaxWidth()
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

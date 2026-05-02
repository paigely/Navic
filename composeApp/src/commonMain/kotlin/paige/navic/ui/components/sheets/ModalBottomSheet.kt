package paige.navic.ui.components.sheets

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import paige.navic.utils.SheetHideMotionSpec
import paige.navic.utils.SheetShowMotionSpec

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalBottomSheet(
	onDismissRequest: () -> Unit,
	modifier: Modifier = Modifier,
	sheetState: SheetState = rememberModalBottomSheetState(),
	sheetMaxWidth: Dp = BottomSheetDefaults.SheetMaxWidth,
	sheetGesturesEnabled: Boolean = true,
	shape: Shape = BottomSheetDefaults.ExpandedShape,
	containerColor: Color = BottomSheetDefaults.ContainerColor,
	contentColor: Color = contentColorFor(containerColor),
	tonalElevation: Dp = 0.dp,
	scrimColor: Color = BottomSheetDefaults.ScrimColor,
	dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
	contentWindowInsets: @Composable () -> WindowInsets = { BottomSheetDefaults.modalWindowInsets },
	properties: ModalBottomSheetProperties = ModalBottomSheetProperties(),
	content: @Composable ColumnScope.() -> Unit,
) {
	androidx.compose.material3.ModalBottomSheet(
		onDismissRequest = onDismissRequest,
		modifier = modifier,
		sheetState = sheetState,
		sheetMaxWidth = sheetMaxWidth,
		sheetGesturesEnabled = sheetGesturesEnabled,
		shape = shape,
		containerColor = containerColor,
		contentColor = contentColor,
		tonalElevation = tonalElevation,
		scrimColor = scrimColor,
		dragHandle = dragHandle,
		contentWindowInsets = contentWindowInsets,
		properties = properties,
		content = content,
	)

	@Suppress("INVISIBLE_REFERENCE")
	LaunchedEffect(Unit) {
		sheetState.showMotionSpec = SheetShowMotionSpec
		sheetState.hideMotionSpec = SheetHideMotionSpec
	}
}

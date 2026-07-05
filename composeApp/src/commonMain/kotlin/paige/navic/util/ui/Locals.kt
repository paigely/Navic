package paige.navic.util.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * this exists because if you use `sheetState.hide()` inside
 * of `onRemove` of an `OverlayScene`, then spam tap the sheet's
 * scrim, the app will become COMPLETELY unresponsive FOREVER
 *
 * why? I don't fucking know
 */
@OptIn(ExperimentalMaterial3Api::class)
val LocalSheetState = staticCompositionLocalOf<SheetState> {
	error("LocalSheetState used outside of a sheet")
}

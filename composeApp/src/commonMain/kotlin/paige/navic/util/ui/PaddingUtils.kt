package paige.navic.util.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.minus
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import paige.navic.LocalGlobalBottomBarHeight

fun PaddingValues.withoutTop() = this.minus(PaddingValues(top = this.calculateTopPadding()))

@Composable
fun PaddingValues.withGlobalBottomBar(): PaddingValues {
	val direction = LocalLayoutDirection.current
	return PaddingValues(
		start = this.calculateStartPadding(direction),
		top = this.calculateTopPadding(),
		end = this.calculateEndPadding(direction),
		bottom = this.calculateBottomPadding() + LocalGlobalBottomBarHeight.current
	)
}

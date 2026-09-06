package paige.navic.util.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.minus
import androidx.compose.foundation.layout.plus
import androidx.compose.runtime.Composable

fun PaddingValues.withoutTop() = this.minus(PaddingValues(top = this.calculateTopPadding()))

@Composable
fun PaddingValues.withGlobalBottomBar()
	= this.plus(PaddingValues(bottom = LocalGlobalBottomBarHeight.current))

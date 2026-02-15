package paige.navic.ui.components.common

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.capsule.ContinuousRoundedRectangle
import paige.navic.LocalCtx
import paige.navic.data.models.Settings
import paige.navic.utils.onRightClick

@Composable
fun FormRow(
	modifier: Modifier = Modifier,
	horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
	color: Color? = null,
	onClick: (() -> Unit)? = null,
	onLongClick: (() -> Unit)? = null,
	rounding: Dp = 3.dp,
	contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 18.dp),
	content: @Composable RowScope.() -> Unit
) {
	val ctx = LocalCtx.current
	Surface(
		modifier = modifier
			.fillMaxWidth()
			.then(
				if (onClick != null)
					Modifier
						.combinedClickable(
							onClick = {
								ctx.clickSound()
								onClick()
							},
							onLongClick = onLongClick
						)
						.onRightClick {
							onLongClick?.invoke()
						}
				else Modifier
			),
		color = color ?: MaterialTheme.colorScheme.surfaceContainer,
		shape = ContinuousRoundedRectangle(
			if (Settings.shared.theme.isMaterialLike()) rounding else 0.dp
		)
	) {
		Row(
			horizontalArrangement = horizontalArrangement,
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.padding(contentPadding)
		) {
			content()
		}
	}
}

package paige.navic.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.kyant.capsule.ContinuousRoundedRectangle
import paige.navic.LocalCtx
import paige.navic.data.models.settings.Settings

@Composable
fun FormRow(
	modifier: Modifier = Modifier,
	horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
	color: Color? = null,
	onClick: (() -> Unit)? = null,
	onLongClick: (() -> Unit)? = null,
	rounding: Dp = 5.dp,
	contentPadding: PaddingValues = PaddingValues(14.dp),
	interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
	content: @Composable RowScope.() -> Unit
) {
	val ctx = LocalCtx.current
	Box(
		modifier = modifier
			.then(
				if (onClick != null)
					Modifier
						.combinedClickable(
							onClick = dropUnlessResumed {
								ctx.clickSound()
								onClick()
							},
							onLongClick = onLongClick,
							interactionSource = interactionSource,
							indication = null
						)
				else Modifier
			)
			.clip(
				ContinuousRoundedRectangle(
					if (Settings.shared.theme.isMaterialLike()) rounding else 0.dp
				)
			)
			.background(color ?: MaterialTheme.colorScheme.surfaceContainer)
			.fillMaxWidth()
			.indication(interactionSource, ripple())
	) {
		Row(
			horizontalArrangement = horizontalArrangement,
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.padding(contentPadding).fillMaxWidth()
		) {
			content()
		}
	}
}

package paige.navic.ui.components.toolbars

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.kyant.capsule.ContinuousRoundedRectangle
import paige.navic.LocalCtx
import paige.navic.ui.theme.defaultFont

@Composable
fun SheetToolbar(
	modifier: Modifier = Modifier,
	windowInsets: WindowInsets,
	title: @Composable () -> Unit = {},
	navigationIcon: @Composable () -> Unit,
	actions: @Composable () -> Unit = {}
) {
	val ctx = LocalCtx.current
	val isLandscape = ctx.sizeClass.widthSizeClass > WindowWidthSizeClass.Compact
	Row(
		modifier = modifier
			.fillMaxWidth()
			.padding(
				horizontal = 16.dp,
				vertical = if (isLandscape) 0.dp else 24.dp
			)
			.windowInsetsPadding(windowInsets),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(12.dp)
		) {
			navigationIcon()
			CompositionLocalProvider(
				LocalTextStyle provides MaterialTheme.typography.bodyMedium
					.copy(
						fontFamily = defaultFont(round = 100f),
						shadow = Shadow(
							color = MaterialTheme.colorScheme.inverseOnSurface,
							offset = Offset(0f, 4f),
							blurRadius = 10f
						)
					)
			) {
				title()
			}
		}
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(4.dp)
		) {
			actions()
		}
	}
}

@Composable
fun SheetActionButton(
	icon: ImageVector,
	contentDescription: String,
	isStartRounded: Boolean = false,
	isEndRounded: Boolean = false,
	onClick: () -> Unit,
) {
	val interactionSource = remember { MutableInteractionSource() }
	val isPressed by interactionSource.collectIsPressedAsState()
	val startRadius by animateDpAsState(if (isStartRounded || isPressed) 12.dp else 4.dp)
	val endRadius by animateDpAsState(if (isEndRounded || isPressed) 12.dp else 4.dp)
	Surface(
		onClick = onClick,
		shape = ContinuousRoundedRectangle(
			topStart = startRadius,
			bottomStart = startRadius,
			topEnd = endRadius,
			bottomEnd = endRadius
		),
		color = MaterialTheme.colorScheme.surfaceContainer,
		contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
		modifier = Modifier.size(45.dp, 40.dp),
		interactionSource = interactionSource,
		shadowElevation = 4.dp
	) {
		Box(contentAlignment = Alignment.Center) {
			Icon(
				imageVector = icon,
				contentDescription = contentDescription,
				modifier = Modifier.size(20.dp)
			)
		}
	}
}

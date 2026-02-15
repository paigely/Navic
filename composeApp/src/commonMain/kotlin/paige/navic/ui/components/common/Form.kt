package paige.navic.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.capsule.ContinuousRoundedRectangle
import paige.navic.data.models.Settings

@Composable
fun Form(
	modifier: Modifier = Modifier,
	rounding: Dp = 18.dp,
	spacing: Dp = 3.dp,
	content: @Composable ColumnScope.() -> Unit
) {
	Column(
		modifier = modifier
			.padding(bottom = 24.dp)
			.clip(ContinuousRoundedRectangle(rounding))
			.background(
				if (Settings.shared.theme != Settings.Theme.iOS
					&& Settings.shared.theme != Settings.Theme.Spotify
					&& Settings.shared.theme != Settings.Theme.AppleMusic
				) Color.Unspecified else MaterialTheme.colorScheme.surfaceContainerHighest
			),
		verticalArrangement = Arrangement.spacedBy(
			if (Settings.shared.theme.isMaterialLike()) spacing else 1.dp
		)
	) {
		content()
	}
}

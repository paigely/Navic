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
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.models.settings.Theme

@Composable
fun Form(
	modifier: Modifier = Modifier,
	rounding: Dp = 18.dp,
	spacing: Dp = 3.dp,
	bottomPadding: Dp = 24.dp,
	content: @Composable ColumnScope.() -> Unit
) {
	Column(
		modifier = modifier
			.padding(bottom = bottomPadding)
			.clip(ContinuousRoundedRectangle(rounding))
			.background(
				if (PreferenceManager.shared.theme != Theme.iOS
					&& PreferenceManager.shared.theme != Theme.Spotify
					&& PreferenceManager.shared.theme != Theme.AppleMusic
				) Color.Unspecified else MaterialTheme.colorScheme.surfaceContainerHighest
			),
		verticalArrangement = Arrangement.spacedBy(
			if (PreferenceManager.shared.theme.isMaterialLike()) spacing else 1.dp
		)
	) {
		content()
	}
}

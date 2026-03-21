package paige.navic.ui.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun ContentUnavailable(
	modifier: Modifier = Modifier,
	icon: ImageVector,
	label: String
) {
	Column(
		modifier = modifier.fillMaxWidth().alpha(.6f),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
	) {
		Icon(
			imageVector = icon,
			contentDescription = null,
			modifier = Modifier.size(48.dp)
		)
		Text(
			label,
			style = MaterialTheme.typography.headlineMedium,
			modifier = Modifier.widthIn(max = 400.dp)
		)
	}
}
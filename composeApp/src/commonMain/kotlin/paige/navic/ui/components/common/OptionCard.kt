package paige.navic.ui.components.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun OptionCard(
	label: String,
	icon: ImageVector,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	isActive: Boolean = false
) {
	val containerColor = if (isActive) {
		MaterialTheme.colorScheme.primaryContainer
	} else {
		MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
	}
	val contentColor = if (isActive) {
		MaterialTheme.colorScheme.onPrimaryContainer
	} else {
		MaterialTheme.colorScheme.onSurfaceVariant
	}

	Surface(
		modifier = modifier
			.clip(MaterialTheme.shapes.medium)
			.clickable(onClick = onClick),
		color = containerColor,
		contentColor = contentColor,
		shape = MaterialTheme.shapes.medium
	) {
		Column(
			modifier = Modifier.padding(16.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center
		) {
			Icon(
				imageVector = icon,
				contentDescription = null,
				modifier = Modifier.size(24.dp)
			)
			Spacer(Modifier.height(8.dp))
			Text(
				text = label,
				style = MaterialTheme.typography.labelLarge,
				textAlign = TextAlign.Center,
				maxLines = 1
			)
		}
	}
}

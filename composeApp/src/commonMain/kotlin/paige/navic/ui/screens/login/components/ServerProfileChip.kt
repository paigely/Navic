package paige.navic.ui.screens.login.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import paige.navic.data.database.entities.ServerEntity
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Close

@Composable
fun ServerProfileChip(
	server: ServerEntity,
	isSelected: Boolean,
	onClick: () -> Unit,
	onDelete: () -> Unit
) {
	FilterChip(
		selected = isSelected,
		onClick = onClick,
		shape = RoundedCornerShape(16.dp),
		label = {
			Column(modifier = Modifier.padding(vertical = 6.dp)) {
				Text(
					text = server.name,
					style = MaterialTheme.typography.bodyMedium,
					fontWeight = if (isSelected) FontWeight.Bold else null
				)
				Text(
					text = server.username,
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
			}
		},
		trailingIcon = {
			if (isSelected) {
				IconButton(
					onClick = onDelete,
					modifier = Modifier.size(24.dp)
				) {
					Icon(
						imageVector = Icons.Outlined.Close,
						contentDescription = "Delete Server",
						modifier = Modifier.size(16.dp)
					)
				}
			}
		}
	)
}

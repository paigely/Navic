package paige.navic.ui.screens.login.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import paige.navic.data.database.entities.ServerEntity
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Add
import paige.navic.icons.outlined.DataTable
import paige.navic.icons.outlined.Delete
import paige.navic.icons.outlined.Edit

@Composable
fun ServerCard(
	server: ServerEntity,
	isSelected: Boolean,
	onClick: () -> Unit,
	onEdit: () -> Unit,
	onDelete: () -> Unit
) {
	val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
	val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant

	Card(
		modifier = Modifier
			.size(width = 160.dp, height = 120.dp)
			.clip(RoundedCornerShape(16.dp))
			.clickable(onClick = onClick),
		colors = CardDefaults.cardColors(containerColor = containerColor),
		border = BorderStroke(2.dp, borderColor),
		shape = RoundedCornerShape(16.dp)
	) {
		Column(
			modifier = Modifier.fillMaxSize()
		) {
			Row(
				modifier = Modifier.fillMaxWidth().padding(top = 4.dp, end = 4.dp),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.Top
			) {
				Icon(
					imageVector = Icons.Outlined.DataTable,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onSurfaceVariant,
					modifier = Modifier.padding(start = 12.dp, top = 8.dp).size(24.dp)
				)
				Row {
					IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
						Icon(
							imageVector = Icons.Outlined.Edit,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.onSurfaceVariant,
							modifier = Modifier.size(18.dp)
						)
					}
					IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
						Icon(
							imageVector = Icons.Outlined.Delete,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.error,
							modifier = Modifier.size(18.dp)
						)
					}
				}
			}
			Spacer(modifier = Modifier.weight(1f))
			Text(
				text = server.name.ifBlank { "Server ${server.serverId}" },
				style = MaterialTheme.typography.titleMedium,
				fontWeight = FontWeight.Bold,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
				modifier = Modifier.padding(horizontal = 12.dp)
			)
			Text(
				text = server.username,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
				modifier = Modifier.padding(horizontal = 12.dp)
			)
			Spacer(modifier = Modifier.height(12.dp))
		}
	}
}

@Composable
fun AddServerCard(
	isSelected: Boolean,
	onClick: () -> Unit
) {
	val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
	val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant

	Card(
		modifier = Modifier
			.size(width = 160.dp, height = 120.dp)
			.clip(RoundedCornerShape(16.dp))
			.clickable(onClick = onClick),
		colors = CardDefaults.cardColors(containerColor = containerColor),
		border = BorderStroke(2.dp, borderColor),
		shape = RoundedCornerShape(16.dp)
	) {
		Column(
			modifier = Modifier.fillMaxSize(),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center
		) {
			Icon(
				imageVector = Icons.Outlined.Add,
				contentDescription = null,
				tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
				modifier = Modifier.size(32.dp)
			)
			Spacer(modifier = Modifier.height(8.dp))
			Text(
				text = "Add Server",
				style = MaterialTheme.typography.titleMedium,
				color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
				fontWeight = FontWeight.Medium
			)
		}
	}
}

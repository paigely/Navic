package paige.navic.ui.screens.login.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kyant.capsule.ContinuousRoundedRectangle
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_add_server
import navic.composeapp.generated.resources.info_default_server_name
import org.jetbrains.compose.resources.stringResource
import paige.navic.data.database.entities.ServerEntity
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Add
import paige.navic.icons.outlined.DataTable
import paige.navic.icons.outlined.Delete
import paige.navic.icons.outlined.Edit
import paige.navic.ui.theme.defaultFont

@Composable
fun ServerCard(
	server: ServerEntity,
	isSelected: Boolean,
	isEnabled: Boolean,
	onClick: () -> Unit,
	onEdit: () -> Unit,
	onDelete: () -> Unit
) {
	val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
	val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
	val layoutDirection = LocalLayoutDirection.current
	val isRtl = remember(layoutDirection) {
		layoutDirection == LayoutDirection.Rtl
	}

	Card(
		colors = CardDefaults.cardColors(containerColor = containerColor),
		border = BorderStroke(2.dp, borderColor),
		shape = MaterialTheme.shapes.large,
		onClick = onClick,
		enabled = isEnabled
	) {
		Box(
			modifier = Modifier.sizeIn(
				minWidth = 160.dp,
				minHeight = 120.dp,
				maxHeight = 120.dp
			)
		) {
			Icon(
				imageVector = Icons.Outlined.DataTable,
				contentDescription = null,
				modifier = Modifier
					.size(90.dp)
					.align(if (!isRtl) Alignment.BottomEnd else Alignment.BottomStart)
					.offset(x = 18.dp, y = 18.dp)
					.graphicsLayer(
						alpha = .75f,
						rotationZ = 5f
					)
			)

			Row(
				modifier = Modifier
					.align(if (!isRtl) Alignment.TopEnd else Alignment.TopStart)
					.padding(4.dp)
			) {
				IconButton(
					onClick = onEdit,
					enabled = isEnabled,
					modifier = Modifier.size(36.dp)
				) {
					Icon(
						imageVector = Icons.Outlined.Edit,
						contentDescription = null,
						tint = MaterialTheme.colorScheme.onSurfaceVariant,
						modifier = Modifier.size(18.dp)
					)
				}
				IconButton(
					onClick = onDelete,
					enabled = isEnabled,
					modifier = Modifier.size(36.dp)
				) {
					Icon(
						imageVector = Icons.Outlined.Delete,
						contentDescription = null,
						tint = MaterialTheme.colorScheme.error,
						modifier = Modifier.size(18.dp)
					)
				}
			}

			Column(
				modifier = Modifier
					.align(if (!isRtl) Alignment.BottomStart else Alignment.BottomEnd)
					.padding(10.dp)
			) {
				Text(
					text = server.name.ifBlank { stringResource(Res.string.info_default_server_name, server.serverId) },
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.SemiBold,
					fontFamily = defaultFont(round = 100f),
					maxLines = 1
				)
				Text(
					text = server.username,
					style = MaterialTheme.typography.bodySmall,
					modifier = Modifier.alpha(.75f),
					maxLines = 1
				)
			}
		}
	}
}

@Composable
fun AddServerCard(
	isSelected: Boolean,
	isEnabled: Boolean,
	onClick: () -> Unit
) {
	val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
	val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant

	Card(
		modifier = Modifier
			.size(width = 160.dp, height = 120.dp)
			.clip(ContinuousRoundedRectangle(16.dp))
			.clickable(onClick = onClick, enabled = isEnabled),
		colors = CardDefaults.cardColors(containerColor = containerColor),
		border = BorderStroke(2.dp, borderColor),
		shape = ContinuousRoundedRectangle(16.dp)
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
				text = stringResource(Res.string.action_add_server),
				style = MaterialTheme.typography.titleMedium,
				color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
				fontWeight = FontWeight.Medium
			)
		}
	}
}

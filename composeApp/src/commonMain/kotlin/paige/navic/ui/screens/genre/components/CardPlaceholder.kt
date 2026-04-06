package paige.navic.ui.screens.genre.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import paige.navic.utils.shimmerLoading

@Composable
fun GenreListScreenCardPlaceholder() {
	Box(
		modifier = Modifier
			.size(172.dp, 103.dp)
			.clip(MaterialTheme.shapes.medium)
			.shimmerLoading()
	)
}
package paige.navic.ui.components.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import paige.navic.ui.theme.defaultFont

@Composable
fun Monogram(
	modifier: Modifier = Modifier,
	text: String
) {
	Surface(
		modifier = modifier.size(56.dp),
		color = MaterialTheme.colorScheme.secondary,
		shape = CircleShape
	) {
		Box(
			modifier = Modifier.fillMaxSize(),
			contentAlignment = Alignment.Center
		) {
			Text(
				text = text.take(2).uppercase(),
				fontFamily = defaultFont(grade = 100, round = 100f),
				fontSize = 18.sp,
				modifier = Modifier.semantics { hideFromAccessibility() }
			)
		}
	}
}

package paige.navic.ui.components.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import paige.navic.ui.theme.defaultFont

@Composable
fun FormTitle(text: String) {
	@OptIn(ExperimentalMaterial3ExpressiveApi::class)
	Text(
		text,
		style = MaterialTheme.typography.titleSmallEmphasized,
		fontFamily = defaultFont(grade = 100, round = 100f),
		color = MaterialTheme.colorScheme.primary,
		modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 6.dp)
	)
}

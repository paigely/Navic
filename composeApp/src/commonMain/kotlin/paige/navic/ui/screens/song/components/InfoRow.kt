package paige.navic.ui.screens.song.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_unknown
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import paige.navic.ui.components.common.FormRow

@Composable
fun SongDetailScreenInfoRow(
	key: StringResource,
	value: String?
) {
	FormRow {
		Column(Modifier.padding(vertical = 4.dp)) {
			Text(
				text = stringResource(key),
				style = MaterialTheme.typography.labelMedium,
				color = MaterialTheme.colorScheme.primary
			)
			SelectionContainer {
				Text(
					text = value ?: stringResource(Res.string.info_unknown),
					style = MaterialTheme.typography.bodyLarge
				)
			}
		}
	}
}

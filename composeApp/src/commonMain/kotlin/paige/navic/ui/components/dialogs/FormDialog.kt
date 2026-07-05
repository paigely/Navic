package paige.navic.ui.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import paige.navic.ui.components.common.Form
import paige.navic.ui.theme.defaultFont

@Composable
fun FormDialog(
	width: Dp = 300.dp,
	onDismissRequest: () -> Unit,
	icon: @Composable () -> Unit = {},
	title: @Composable () -> Unit = {},
	action: @Composable () -> Unit = {},
	buttons: @Composable ColumnScope.() -> Unit = {},
	content: @Composable () -> Unit
) {
	@OptIn(
		ExperimentalMaterial3Api::class,
		ExperimentalMaterial3ExpressiveApi::class
	)
	BasicAlertDialog(
		modifier = Modifier.width(width),
		onDismissRequest = onDismissRequest
	) {
		Surface(
			modifier = Modifier
				.fillMaxWidth()
				.wrapContentHeight()
				.verticalScroll(rememberScrollState()),
			shape = MaterialTheme.shapes.extraLarge,
			tonalElevation = AlertDialogDefaults.TonalElevation,
		) {
			Box {
				Box(Modifier.align(Alignment.TopEnd).padding(16.dp)) {
					CompositionLocalProvider(
						LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant
					) {
						action()
					}
				}
				Column(
					modifier = Modifier
						.padding(16.dp)
						.fillMaxWidth(),
					horizontalAlignment = Alignment.CenterHorizontally,
					verticalArrangement = Arrangement.spacedBy(12.dp)
				) {
					Spacer(Modifier.height(12.dp))
					CompositionLocalProvider(
						LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
						content = icon
					)
					CompositionLocalProvider(
						LocalTextStyle provides MaterialTheme.typography.headlineSmall
							.copy(fontFamily = defaultFont(round = 100f)),
						content = title
					)
					CompositionLocalProvider(
						LocalTextStyle provides MaterialTheme.typography.bodyMedium,
						LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant
					) {
						Column(
							modifier = Modifier
								.heightIn(max = 400.dp)
								.verticalScroll(rememberScrollState()),
							horizontalAlignment = Alignment.CenterHorizontally
						) {
							content()
						}
					}
					Spacer(Modifier.height(12.dp))
					Form(
						bottomPadding = 0.dp,
						spacing = 4.dp
					) {
						buttons()
					}
				}
			}
		}
	}
}

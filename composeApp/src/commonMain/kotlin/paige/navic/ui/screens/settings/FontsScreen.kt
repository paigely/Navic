package paige.navic.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.title_choose_font
import navic.composeapp.generated.resources.title_fonts_external
import navic.composeapp.generated.resources.title_fonts_inbuilt
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.data.models.settings.Settings
import paige.navic.data.models.settings.enums.FontOption
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Check
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.theme.googleSans

@Composable
fun FontsScreen() {
	Scaffold(
		topBar = { NestedTopBar({ Text(stringResource(Res.string.title_choose_font)) }) }
	) { contentPadding ->
		CompositionLocalProvider(
			LocalMinimumInteractiveComponentSize provides 0.dp
		) {
			LazyColumn(
				verticalArrangement = Arrangement.spacedBy(3.dp),
				contentPadding = contentPadding + PaddingValues(
					top = 16.dp, end = 16.dp, start = 16.dp
				)
			) {
				inbuiltFonts()
				externalFonts()
			}
		}
	}
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun LazyListScope.heading(resource: StringResource) {
	item {
		Text(
			stringResource(resource),
			style = MaterialTheme.typography.titleSmallEmphasized,
			modifier = Modifier.padding(horizontal = 12.dp)
		)
	}
}

private fun LazyListScope.inbuiltFonts() {
	heading(Res.string.title_fonts_inbuilt)
	item {
		FontRow(
			fontName = "System",
			fontFamily = FontFamily.Default,
			index = 0,
			count = 2,
			onClick = {
				Settings.shared.font = FontOption.System
			},
			selected = Settings.shared.font == FontOption.System
		)
	}
	item {
		FontRow(
			fontName = "Google Sans",
			fontFamily = googleSans(),
			index = 1,
			count = 2,
			onClick = {
				Settings.shared.font = FontOption.GoogleSans
			},
			selected = Settings.shared.font == FontOption.GoogleSans
		)
		Spacer(Modifier.height(10.dp))
	}
}

private fun LazyListScope.externalFonts() {
	heading(Res.string.title_fonts_external)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun FontRow(
	fontName: String,
	fontFamily: FontFamily?,
	selected: Boolean,
	index: Int,
	@Suppress("SameParameterValue")
	count: Int,
	onClick: () -> Unit
) {
	val ctx = LocalCtx.current
	val color = MaterialTheme.colorScheme.surfaceContainer
	SegmentedListItem(
		onClick = {
			ctx.clickSound()
			onClick()
		},
		selected = selected,
		colors = ListItemDefaults.colors(
			containerColor = color,
		),
		shapes = ListItemDefaults.segmentedShapes(
			index = index,
			count = count
		),
		content = {
			Text(fontName)
		},
		supportingContent = {
			Text(
				"The quick brown fox jumps over the lazy dog",
				fontFamily = fontFamily
			)
		},
		trailingContent = {
			if (selected) {
				Box(Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)) {
					Icon(
						Icons.Outlined.Check,
						contentDescription = null,
						modifier = Modifier.size(16.dp),
						tint = MaterialTheme.colorScheme.onPrimary
					)
				}
			}
		}
	)
}

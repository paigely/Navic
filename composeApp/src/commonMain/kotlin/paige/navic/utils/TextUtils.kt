package paige.navic.utils

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.Icon
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.persistentMapOf
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_explicit
import org.jetbrains.compose.resources.stringResource
import paige.navic.icons.Icons
import paige.navic.icons.filled.Explicit

val InlineExplicitIcon = persistentMapOf(
	"InlineExplicitIcon" to InlineTextContent(
		Placeholder(
			width = 16.sp,
			height = 16.sp,
			placeholderVerticalAlign = PlaceholderVerticalAlign.Center
		)
	) {
		Icon(
			imageVector = Icons.Filled.Explicit,
			contentDescription = stringResource(Res.string.info_explicit)
		)
	}
)

val InlineExplicitIconLarge = persistentMapOf(
	"InlineExplicitIcon" to InlineTextContent(
		Placeholder(
			width = 20.sp,
			height = 20.sp,
			placeholderVerticalAlign = PlaceholderVerticalAlign.Center
		)
	) {
		Icon(
			imageVector = Icons.Filled.Explicit,
			contentDescription = stringResource(Res.string.info_explicit)
		)
	}
)

package paige.navic.ui.components.sheets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kyant.capsule.ContinuousCapsule
import com.mikepenz.aboutlibraries.entity.Developer
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults
import com.mikepenz.aboutlibraries.ui.compose.m3.style.m3VariantColors
import com.mikepenz.aboutlibraries.ui.compose.m3.style.m3VariantTextStyles
import com.mikepenz.aboutlibraries.ui.compose.style.DefaultLibraryActionBadges
import com.mikepenz.aboutlibraries.ui.compose.style.librariesStyle
import com.mikepenz.aboutlibraries.ui.compose.variant.LibraryActionKind
import com.mikepenz.aboutlibraries.ui.compose.variant.LibraryActionMode
import com.mikepenz.aboutlibraries.ui.compose.variant.LibrarySheetDetail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibrarySheet(
	library: Library,
	onDismissRequest: () -> Unit,
	onOpenLink: (String?) -> Unit
) {
	ModalBottomSheet(
		onDismissRequest = onDismissRequest,
		dragHandle = {
			Surface(
				modifier = Modifier.padding(vertical = 6.dp),
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				shape = ContinuousCapsule,
			) {
				Box(Modifier.size(width = 32.dp, height = 4.dp))
			}
		}
	) {
		LibrarySheetDetail(
			library = library.copy(developers = listOf(Developer(name = "rniii", ""))),
			style = LibraryDefaults.librariesStyle(
				colors = LibraryDefaults.m3VariantColors(),
				textStyles = LibraryDefaults.m3VariantTextStyles()
			),
			actionMode = LibraryActionMode.Icons,
			actionLabels = DefaultLibraryActionBadges,
			onActionClick = { library, actionKind ->
				val link = when (actionKind) {
					LibraryActionKind.Source -> library.scm?.url
					LibraryActionKind.Website -> library.website
					LibraryActionKind.Sponsor -> library.funding.firstOrNull()?.url
					LibraryActionKind.License -> library.licenses.firstOrNull()?.url
				}
				onOpenLink(link)
				// consume
				return@LibrarySheetDetail true
			},
		)
	}
}

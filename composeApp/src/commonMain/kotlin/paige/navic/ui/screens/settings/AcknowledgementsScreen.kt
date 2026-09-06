package paige.navic.ui.screens.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.produceLibraries
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.title_acknowledgements
import org.jetbrains.compose.resources.stringResource
import paige.navic.ui.components.dialogs.LinkConfirmationDialog
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.components.sheets.LibrarySheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAcknowledgementsScreen() {
	val libraries by produceLibraries {
		Res.readBytes("files/acknowledgements.json").decodeToString()
	}
	val direction = LocalLayoutDirection.current
	val density = LocalDensity.current
	Scaffold(
		topBar = { NestedTopBar({ Text(stringResource(Res.string.title_acknowledgements)) }) },
		contentWindowInsets = WindowInsets(0, 0, 0, 0)
	) { innerPadding ->
		LibrariesContainer(
			libraries,
			modifier = Modifier
				.fillMaxSize(),
			contentPadding = PaddingValues(
				start = innerPadding.calculateStartPadding(direction),
				top = innerPadding.calculateTopPadding(),
				end = innerPadding.calculateEndPadding(direction),
				bottom = with(density) { WindowInsets.navigationBars.getBottom(this).toDp() }
			)
		)
	}
}

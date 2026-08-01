package paige.navic.ui.screens.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.produceLibraries
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.title_acknowledgements
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalGlobalBottomBarHeight
import paige.navic.ui.components.layouts.NestedTopBar

@Composable
fun SettingsAcknowledgementsScreen() {
	val libraries by produceLibraries {
		Res.readBytes("files/acknowledgements.json").decodeToString()
	}
	val direction = LocalLayoutDirection.current
	Scaffold(
		topBar = { NestedTopBar({ Text(stringResource(Res.string.title_acknowledgements)) }) }
	) { innerPadding ->
		LibrariesContainer(
			libraries,
			modifier = Modifier
				.fillMaxSize(),
			contentPadding = PaddingValues(
				start = innerPadding.calculateStartPadding(direction),
				top = innerPadding.calculateTopPadding(),
				end = innerPadding.calculateEndPadding(direction),
				bottom = innerPadding.calculateBottomPadding() + LocalGlobalBottomBarHeight.current
			)
		)
	}
}

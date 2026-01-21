package paige.navic.ui.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.produceLibraries
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.title_acknowledgements
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.ui.component.layout.NestedTopBar

@Composable
fun SettingsAcknowledgementsScreen() {
	val libraries by produceLibraries {
		Res.readBytes("files/acknowledgements.json").decodeToString()
	}
	val ctx = LocalCtx.current
	val hideBack = ctx.sizeClass.widthSizeClass >= WindowWidthSizeClass.Medium
	Scaffold(
		topBar = { NestedTopBar(
			{ Text(stringResource(Res.string.title_acknowledgements)) },
			hideBack = hideBack
		) }
	) { innerPadding ->
		LibrariesContainer(
			libraries,
			modifier = Modifier.padding(innerPadding).fillMaxSize(),
			contentPadding = PaddingValues(bottom = 117.9.dp)
		)
	}
}
package paige.navic.ui.screens.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.plus
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
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.utils.fadeFromTop
import paige.navic.utils.withoutTop

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
			modifier = Modifier
				.fillMaxSize()
				.fadeFromTop(),
			contentPadding = innerPadding.withoutTop() + PaddingValues(
				top = 16.dp, end = 16.dp, start = 16.dp
			)
		)
	}
}
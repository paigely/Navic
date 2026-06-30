package paige.navic.ui.components.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.kyant.capsule.ContinuousCapsule
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_dont_show_again
import navic.composeapp.generated.resources.action_update_app
import navic.composeapp.generated.resources.info_update
import navic.composeapp.generated.resources.title_update
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import paige.navic.LocalPlatformContext
import paige.navic.domain.manager.PreferenceManager
import paige.navic.ui.components.common.Markdown
import paige.navic.ui.theme.defaultFont
import paige.navic.util.core.Logger
import paige.navic.util.core.PlatformContext

@Serializable
data class GitHubRelease(
	@SerialName("tag_name") val tag: String,
	@SerialName("html_url") val url: String,
	@SerialName("body") val body: String
)

class ChangelogViewModel(
	platformContext: PlatformContext
) : ViewModel() {
	val release: StateFlow<GitHubRelease?>
		field = MutableStateFlow(null)

	private val updateClient = HttpClient {
		install(ContentNegotiation) {
			json(Json { ignoreUnknownKeys = true })
		}
	}

	init {
		checkForUpdates(platformContext.appVersion)
	}

	fun checkForUpdates(currentVersion: String) {
		viewModelScope.launch {
			release.value = try {
				val release: GitHubRelease =
					updateClient.get("https://api.github.com/repos/ssalggnikool/Navic/releases/latest")
						.body()
				val remoteVersion = release.tag
					.filter { it.isDigit() }
					.toIntOrNull() ?: return@launch
				val localVersion = currentVersion
					.filter { it.isDigit() }
					.toIntOrNull() ?: return@launch
				if (remoteVersion > localVersion || "$remoteVersion".length != "$localVersion".length)
					release
				else null
			} catch (e: Exception) {
				Logger.e("ChangelogViewModel", "couldn't check for updates", e)
				null
			}
		}
	}

	fun clearRelease() {
		release.value = null
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangelogSheet() {
	val preferenceManager = koinInject<PreferenceManager>()
	val platformContext = LocalPlatformContext.current
	val uriHandler = LocalUriHandler.current
	val viewModel = koinViewModel<ChangelogViewModel>(
		parameters = { parametersOf(platformContext) }
	)
	val release by viewModel.release.collectAsStateWithLifecycle()

	release?.let { release ->
		ModalBottomSheet(
			onDismissRequest = { viewModel.clearRelease() },
			sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
		) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.verticalScroll(rememberScrollState())
					.padding(horizontal = 16.dp)
			) {
				Column(
					modifier = Modifier.fillMaxWidth(),
					horizontalAlignment = Alignment.CenterHorizontally
				) {
					Text(
						text = stringResource(Res.string.title_update),
						style = MaterialTheme.typography.titleLarge,
						fontFamily = defaultFont(round = 100f)
					)
					Text(
						stringResource(Res.string.info_update, release.tag),
						style = MaterialTheme.typography.bodyMedium
					)
				}

				Spacer(Modifier.height(8.dp))

				Markdown(
					text = release.body,
					modifier = Modifier
						.heightIn(max = 400.dp)
						.fillMaxWidth()
						.clip(MaterialTheme.shapes.large)
						.background(MaterialTheme.colorScheme.surfaceContainerHigh)
						.verticalScroll(rememberScrollState())
						.padding(10.dp)
				)

				Spacer(Modifier.height(8.dp))
				Spacer(Modifier.weight(1f))

				Button(
					onClick = {
						viewModel.clearRelease()
						uriHandler.openUri(release.url)
					},
					modifier = Modifier.fillMaxWidth(),
					shape = ContinuousCapsule
				) {
					Text(
						text = stringResource(Res.string.action_update_app),
						fontFamily = defaultFont(100)
					)
				}

				OutlinedButton(
					onClick = {
						viewModel.clearRelease()
						preferenceManager.checkForUpdates = false
					},
					modifier = Modifier.fillMaxWidth(),
					shape = ContinuousCapsule
				) {
					Text(
						text = stringResource(Res.string.action_dont_show_again),
						fontFamily = defaultFont(100)
					)
				}
			}
		}
	}
}

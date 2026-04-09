package paige.navic.ui.screens.login.pages

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import paige.navic.data.models.User
import paige.navic.ui.components.common.ErrorBox
import paige.navic.utils.LoginState
import paige.navic.utils.UiState

@Composable
fun LoginScreenError(
	loginState: LoginState<User?>
) {
	val spatialSpec = MaterialTheme.motionScheme.slowSpatialSpec<Float>()
	val effectSpec = MaterialTheme.motionScheme.slowEffectsSpec<Float>()
	AnimatedContent(
		(loginState as? LoginState.Error),
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp),
		transitionSpec = {
			(fadeIn(
				animationSpec = effectSpec
			) + scaleIn(
				initialScale = 0.8f,
				animationSpec = spatialSpec
			)) togetherWith (fadeOut(
				animationSpec = effectSpec
			) + scaleOut(
				animationSpec = spatialSpec
			))
		}
	) {
		if (it != null) {
			ErrorBox(
				UiState.Error(it.error, null),
				padding = PaddingValues(0.dp),
				modifier = Modifier.fillMaxWidth(),
				bottomPadding = 8.dp
			)
		}
	}
}

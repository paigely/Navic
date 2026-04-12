package paige.navic.ui.screens.login.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import paige.navic.data.models.User
import paige.navic.utils.LoginState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoginScreenProgress(
	modifier: Modifier = Modifier,
	isBusy: Boolean,
	loginState: LoginState<User?>
) {
	val smoothedProgress by animateFloatAsState(
		((loginState as? LoginState.Syncing)?.progress ?: 1f).coerceIn(0f..1f),
		animationSpec = tween(
			durationMillis = 250,
			easing = EaseOut
		)
	)
	AnimatedVisibility(
		modifier = modifier.fillMaxWidth(),
		visible = isBusy,
		enter = expandVertically() + fadeIn(),
		exit = shrinkVertically() + fadeOut()
	) {
		if (loginState is LoginState.Syncing) {
			LinearWavyProgressIndicator(
				modifier = Modifier.fillMaxWidth(),
				progress = { smoothedProgress }
			)
		} else {
			LinearWavyProgressIndicator(
				modifier = Modifier.fillMaxWidth()
			)
		}
	}
}
